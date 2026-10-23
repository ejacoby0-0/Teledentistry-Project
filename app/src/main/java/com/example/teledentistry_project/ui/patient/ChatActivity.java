package com.example.teledentistry_project.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.remote.matrix.MatrixAuthResponse;
import com.example.teledentistry_project.data.remote.matrix.MatrixClient;
import com.example.teledentistry_project.data.remote.matrix.MatrixSyncResponse;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** In-app chat for a confirmed appointment, backed by the self-hosted Matrix room of that appointment. */
public class ChatActivity extends AppCompatActivity {

    private static final int HISTORY_LIMIT = 50;

    private SessionManager sessionManager;
    private MatrixClient matrix;

    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnCall;
    private TextView btnBack;
    private TextView tvDentistName;
    private LinearLayout chatMessagesContainer;

    // Chat messages (the local welcome bubble and the error bubble are not part of this list)
    private final List<ChatMessage> messageList = new ArrayList<>();
    private final Map<String, ChatMessage> byEventId = new HashMap<>();
    private final Map<String, ChatMessage> byTxnId = new HashMap<>();
    private final Map<String, String> displayNames = new HashMap<>();

    private String appointmentId = "";
    private String roomId = null;
    private String myMatrixId = "";
    private ChatMessage welcomeMessage;
    private MatrixClient.SyncHandle syncHandle;
    private boolean connecting = false;
    private String connectError = null;
    private boolean destroyed = false;
    private int generation = 0;

    public static class ChatMessage {
        public String id;
        public String senderId;
        public String sender;
        public String text;
        public String timestamp;
        public boolean isSelf; // legacy field; ownership is derived from senderId
        public String status; // "SENDING", "DELIVERED", "FAILED"
        public String txnId;
        public String eventId;
        public long serverTs; // 0 while only local

        public ChatMessage(String id, String senderId, String sender, String text, String timestamp, String status) {
            this.id = id;
            this.senderId = senderId;
            this.sender = sender;
            this.text = text;
            this.timestamp = timestamp;
            this.status = status;
        }
    }

    // Kept for the legacy PocketBase consultation model classes
    public static class ConsultationPayload {
        public String notes = "Consultation notes";
        public List<ChatMessage> chatLog = new ArrayList<>();
        public String overlayState = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        matrix = MatrixClient.getInstance();
        matrix.init(getApplicationContext());

        boolean isPractitioner = "practitioner".equals(sessionManager.getRole());

        appointmentId = getIntent().getStringExtra("appointment_id");
        if (appointmentId == null || appointmentId.isEmpty()) {
            appointmentId = getIntent().getStringExtra("appointmentId");
        }

        if (appointmentId == null || appointmentId.isEmpty()) {
            Toast.makeText(this, "Error: Chat must be opened from a specific Smile Session.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnCall = findViewById(R.id.btnCall);
        btnBack = findViewById(R.id.btnBack);
        tvDentistName = findViewById(R.id.tvDentistName);
        chatMessagesContainer = findViewById(R.id.chatMessagesContainer);

        if (isPractitioner) {
            tvDentistName.setText("Smile Session Chat (Session #" + appointmentId.substring(0, Math.min(appointmentId.length(), 6)) + ")");
        } else {
            tvDentistName.setText("Dr. Emily Watson (Smile Session #" + appointmentId.substring(0, Math.min(appointmentId.length(), 6)) + ")");
        }

        String welcomeText = isPractitioner
                ? "Connected with patient for Smile Session. Secure channel active."
                : "Hello! Dr. Emily Watson here. Ready to assist with your Smile Session consultation.";
        String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        welcomeMessage = new ChatMessage("welcome", "", "Dr. Emily Watson", welcomeText, timeStr, "DELIVERED");

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
                intent.putExtra("appointment_id", appointmentId);
                startActivity(intent);
            });
        }

        if (btnSend != null && etMessage != null && chatMessagesContainer != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }

        refreshChatUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appointmentId == null || appointmentId.isEmpty() || chatMessagesContainer == null) return;
        if (roomId != null) {
            startLive();
        } else {
            connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        stopLive();
    }

    // ------------------------------------------------------------------ Matrix

    private void connect() {
        if (connecting) return;
        connecting = true;
        connectError = null;
        final int gen = ++generation;

        matrix.ensureSession(new MatrixClient.MatrixCallback<MatrixAuthResponse>() {
            @Override
            public void onSuccess(MatrixAuthResponse auth) {
                if (destroyed || gen != generation) return;
                myMatrixId = auth.getUserId();
                matrix.getAppointmentRoom(appointmentId, new MatrixClient.MatrixCallback<String>() {
                    @Override
                    public void onSuccess(String id) {
                        if (destroyed || gen != generation) return;
                        connecting = false;
                        roomId = id;
                        refreshChatUI();
                        startLive();
                    }

                    @Override
                    public void onError(String error) {
                        connectFailed(gen, error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                connectFailed(gen, error);
            }
        });
    }

    private void connectFailed(int gen, String error) {
        if (destroyed || gen != generation) return;
        connecting = false;
        connectError = error;
        Toast.makeText(this, "Chat unavailable: " + error, Toast.LENGTH_LONG).show();
        refreshChatUI();
    }

    private void startLive() {
        if (roomId == null || syncHandle != null) return;
        final String room = roomId;
        syncHandle = matrix.startSync(room, new MatrixClient.SyncListener() {
            @Override
            public void onEvents(String rid, List<MatrixSyncResponse.Event> state,
                                 List<MatrixSyncResponse.Event> timeline, boolean initial) {
                if (destroyed || !room.equals(roomId)) return;
                handleEvents(state);
                handleEvents(timeline);
                refreshChatUI();
            }

            @Override
            public void onSyncError(String error) {
                // The loop retries by itself; nothing to show
            }
        });
        matrix.getRecentMessages(room, HISTORY_LIMIT, Collections.singletonList("m.room.message"), new MatrixClient.MatrixCallback<List<MatrixSyncResponse.Event>>() {
            @Override
            public void onSuccess(List<MatrixSyncResponse.Event> events) {
                if (destroyed || !room.equals(roomId)) return;
                handleEvents(events);
                refreshChatUI();
            }

            @Override
            public void onError(String error) {
                if (destroyed) return;
                Toast.makeText(ChatActivity.this, "Could not load chat history: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopLive() {
        if (syncHandle != null) {
            syncHandle.stop();
            syncHandle = null;
        }
        generation++;
        connecting = false;
    }

    private void handleEvents(List<MatrixSyncResponse.Event> events) {
        if (events == null) return;
        for (MatrixSyncResponse.Event ev : events) {
            if (ev == null || ev.getType() == null) continue;
            if ("m.room.member".equals(ev.getType()) && ev.getStateKey() != null && ev.getContent() != null) {
                JsonObject c = ev.getContent();
                if (c.has("displayname") && c.get("displayname").isJsonPrimitive()) {
                    displayNames.put(ev.getStateKey(), c.get("displayname").getAsString());
                }
            } else if ("m.room.message".equals(ev.getType())) {
                addRemoteMessage(ev);
            }
        }
    }

    private void addRemoteMessage(MatrixSyncResponse.Event ev) {
        JsonObject c = ev.getContent();
        if (c == null || !c.has("body") || !c.get("body").isJsonPrimitive()) return; // redacted/unsupported
        String eventId = ev.getEventId();
        if (eventId != null && byEventId.containsKey(eventId)) {
            // Our own message whose send response arrived before this echo: take the server
            // timestamp now, otherwise it stays sorted below newer messages
            ChatMessage known = byEventId.get(eventId);
            if (known.serverTs == 0) {
                known.serverTs = ev.getOriginServerTs();
                known.timestamp = formatTime(ev.getOriginServerTs());
            }
            return;
        }

        String body = c.get("body").getAsString();
        ChatMessage local = null;
        String txn = ev.getTransactionId();
        if (txn != null) local = byTxnId.get(txn);

        if (local != null) {
            // remote echo of a message we sent from this device
            local.eventId = eventId;
            local.serverTs = ev.getOriginServerTs();
            local.timestamp = formatTime(ev.getOriginServerTs());
            local.status = "DELIVERED";
            if (eventId != null) byEventId.put(eventId, local);
            return;
        }

        ChatMessage msg = new ChatMessage(eventId, ev.getSender(), null, body,
                formatTime(ev.getOriginServerTs()), "DELIVERED");
        msg.eventId = eventId;
        msg.serverTs = ev.getOriginServerTs();
        messageList.add(msg);
        if (eventId != null) byEventId.put(eventId, msg);
    }

    private static String formatTime(long ts) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ts > 0 ? ts : System.currentTimeMillis()));
    }

    private String nameOf(String matrixId) {
        if (matrixId == null) return "Unknown";
        String n = displayNames.get(matrixId);
        if (n != null && !n.isEmpty()) return n;
        String s = matrixId.startsWith("@") ? matrixId.substring(1) : matrixId;
        int colon = s.indexOf(':');
        return colon > 0 ? s.substring(0, colon) : s;
    }

    // ---------------------------------------------------------------- sending

    private void sendMessage() {
        String msgText = etMessage.getText().toString().trim();
        if (msgText.isEmpty()) {
            return;
        }
        if (roomId == null) {
            Toast.makeText(this, connecting ? "Still connecting to chat…" : "Chat not connected. Tap the error message to retry.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String txn = MatrixClient.newTxnId();
        ChatMessage newMsg = new ChatMessage(txn, myMatrixId, null, msgText, formatTime(0), "SENDING");
        newMsg.txnId = txn;
        messageList.add(newMsg);
        byTxnId.put(txn, newMsg);
        refreshChatUI();

        etMessage.setText("");

        pushMessage(newMsg);
    }

    private void pushMessage(ChatMessage msg) {
        final String room = roomId;
        matrix.sendTextMessage(room, msg.txnId, msg.text, new MatrixClient.MatrixCallback<String>() {
            @Override
            public void onSuccess(String eventId) {
                if (destroyed) return;
                ChatMessage existing = byEventId.get(eventId);
                if (existing != null && existing != msg) {
                    // remote echo was added without a transaction id: drop the local duplicate
                    messageList.remove(msg);
                    byTxnId.remove(msg.txnId);
                } else {
                    msg.eventId = eventId;
                    msg.status = "DELIVERED";
                    byEventId.put(eventId, msg);
                }
                refreshChatUI();
            }

            @Override
            public void onError(String error) {
                if (destroyed) return;
                if ("DELIVERED".equals(msg.status)) return; // already seen via sync
                msg.status = "FAILED";
                refreshChatUI();
                Toast.makeText(ChatActivity.this, "Message not sent: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // -------------------------------------------------------------- rendering

    private void refreshChatUI() {
        if (chatMessagesContainer == null) return;
        chatMessagesContainer.removeAllViews();
        renderMessageBubble(welcomeMessage);

        List<ChatMessage> confirmed = new ArrayList<>();
        List<ChatMessage> pending = new ArrayList<>();
        for (ChatMessage m : messageList) {
            if (m.serverTs > 0) confirmed.add(m);
            else pending.add(m);
        }
        java.util.Collections.sort(confirmed, (a, b) -> Long.compare(a.serverTs, b.serverTs)); // stable
        for (ChatMessage m : confirmed) renderMessageBubble(m);
        for (ChatMessage m : pending) renderMessageBubble(m);

        if (connectError != null) renderErrorBubble();

        final android.view.View scroll = findViewById(R.id.chatScroll);
        if (scroll instanceof android.widget.ScrollView) {
            scroll.post(() -> ((android.widget.ScrollView) scroll).fullScroll(android.view.View.FOCUS_DOWN));
        }
    }

    private void renderErrorBubble() {
        TextView tv = new TextView(this);
        tv.setText("⚠ Could not connect to chat: " + connectError + "\nTap to retry.");
        tv.setTextSize(14f);
        tv.setPadding(28, 20, 28, 20);
        tv.setTextColor(0xFFB00020);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 12, 16, 12);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        tv.setLayoutParams(params);
        tv.setOnClickListener(v -> connect());
        chatMessagesContainer.addView(tv);
    }

    private boolean isMine(ChatMessage message) {
        if (message.senderId != null && !message.senderId.isEmpty() && !myMatrixId.isEmpty()) {
            return message.senderId.equals(myMatrixId);
        }
        return false;
    }

    private void renderMessageBubble(ChatMessage message) {
        if (chatMessagesContainer == null) return;

        boolean mine = isMine(message);
        String statusText = "";
        if (mine) {
            if ("SENDING".equals(message.status)) statusText = " • Sending…";
            else if ("FAILED".equals(message.status)) statusText = " • ⚠ Not sent";
            else statusText = " • ✓✓ Delivered";
        }

        String senderName = message.sender != null ? message.sender : nameOf(message.senderId);
        if (mine && message.sender == null && !displayNames.containsKey(message.senderId)) senderName = "Me";

        TextView tv = new TextView(this);
        tv.setText(senderName + " (" + message.timestamp + statusText + "):\n" + message.text);
        tv.setTextSize(15f);
        tv.setPadding(28, 20, 28, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 12, 16, 12);

        if (mine) {
            params.gravity = android.view.Gravity.END;
            tv.setBackgroundResource(R.drawable.bg_message_sent);
            tv.setTextColor(0xFF000000);
        } else {
            params.gravity = android.view.Gravity.START;
            tv.setBackgroundResource(R.drawable.bg_message_received);
            tv.setTextColor(0xFF000000);
        }

        tv.setLayoutParams(params);
        chatMessagesContainer.addView(tv);

        if (mine && "FAILED".equals(message.status)) {
            tv.setOnClickListener(v -> {
                message.status = "SENDING";
                refreshChatUI();
                pushMessage(message);
            });
        }
    }
}
