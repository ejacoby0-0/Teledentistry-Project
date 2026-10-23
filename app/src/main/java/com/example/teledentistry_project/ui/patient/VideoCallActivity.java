package com.example.teledentistry_project.ui.patient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.remote.matrix.MatrixCallSession;
import com.example.teledentistry_project.data.remote.matrix.MatrixClient;
import com.example.teledentistry_project.data.remote.matrix.MatrixSyncResponse;
import com.example.teledentistry_project.data.remote.matrix.TeethOverlayEvent;
import com.example.teledentistry_project.ui.components.TeethOverlayView;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.gson.Gson;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;
import java.util.Map;

/**
 * Real 1:1 video consultation: WebRTC media signalled over Matrix (MatrixCallSession); the
 * practitioner's teeth overlay is a Matrix state event (org.teledentistry.teeth_overlay).
 */
public class VideoCallActivity extends AppCompatActivity implements MatrixCallSession.Listener {

    private static final int PERMISSION_REQUEST_CODE = 3003;
    private static final String OVERLAY_EVENT_TYPE = "org.teledentistry.teeth_overlay";
    private static final String OVERLAY_STATE_KEY = "active_session";
    private static final String PRACTITIONER_LABEL = "Dr. Practitioner";

    private static final int COLOR_PENDING = 0xFFFFC107;
    private static final int COLOR_OK = 0xFF00FF66;
    private static final int COLOR_ERROR = 0xFFFF5252;
    private static final int COLOR_NEUTRAL = 0xFFFFFFFF;

    private SessionManager sessionManager;
    private MatrixClient matrixClient;
    private MatrixClient.SyncHandle syncHandle;
    private MatrixCallSession callSession;
    private Gson gson;

    private TextView tvCallStatus;
    private TextView tvRemotePlaceholder;
    private TeethOverlayView teethOverlayView;
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;
    private Button btnToggleTeethOverlay;
    private Button btnToggleMic;
    private Button btnToggleCamera;
    private Button btnEndCall;

    private boolean isOverlayVisible = false;
    private boolean isMicMuted = false;
    private boolean isCameraOff = false;
    private boolean isPractitioner = false;

    private String appointmentId = "";
    private String roomId = "";
    private boolean permissionsGranted = false;
    private boolean initialSyncDone = false;
    private boolean callStarted = false;

    private long connectedAtMs = 0;
    private boolean callEnded = false;
    private Handler uiHandler;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sessionManager = new SessionManager(this);
        matrixClient = MatrixClient.getInstance();
        matrixClient.init(getApplicationContext());
        gson = new Gson();
        uiHandler = new Handler(Looper.getMainLooper());

        isPractitioner = "practitioner".equals(sessionManager.getRole());

        appointmentId = getIntent().getStringExtra("appointment_id");
        if (appointmentId == null) {
            appointmentId = getIntent().getStringExtra("appointmentId");
        }

        tvCallStatus = findViewById(R.id.tvCallStatus);
        tvRemotePlaceholder = findViewById(R.id.tvRemotePlaceholder);
        teethOverlayView = findViewById(R.id.teethOverlayView);
        localVideoView = findViewById(R.id.localVideoView);
        remoteVideoView = findViewById(R.id.remoteVideoView);
        btnToggleTeethOverlay = findViewById(R.id.btnToggleTeethOverlay);
        btnToggleMic = findViewById(R.id.btnToggleMic);
        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnEndCall = findViewById(R.id.btnEndCall);

        setupRolePermissions();
        setupControlListeners();
        showStatus("Connecting…", COLOR_PENDING);

        if (appointmentId == null || appointmentId.isEmpty()) {
            showStatus("Call failed: no appointment selected", COLOR_ERROR);
            return;
        }

        permissionsGranted = checkAndRequestPermissions();
        openRoom();
    }

    private void openRoom() {
        matrixClient.getAppointmentRoom(appointmentId, new MatrixClient.MatrixCallback<String>() {
            @Override
            public void onSuccess(String id) {
                if (isFinishing() || isDestroyed()) return;
                roomId = id;
                syncHandle = matrixClient.startSync(roomId, new MatrixClient.SyncListener() {
                    @Override
                    public void onEvents(String room, List<MatrixSyncResponse.Event> stateEvents,
                                         List<MatrixSyncResponse.Event> timelineEvents, boolean initial) {
                        handleRoomEvents(stateEvents, timelineEvents, initial);
                    }

                    @Override
                    public void onSyncError(String error) {
                        // the sync loop retries on its own
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                showStatus("Call failed: " + error, COLOR_ERROR);
            }
        });
    }

    private void handleRoomEvents(List<MatrixSyncResponse.Event> stateEvents,
                                  List<MatrixSyncResponse.Event> timelineEvents, boolean initial) {
        MatrixSyncResponse.Event latestOverlay = null;
        for (MatrixSyncResponse.Event ev : stateEvents) {
            if (isOverlayEvent(ev)) latestOverlay = ev;
        }
        for (MatrixSyncResponse.Event ev : timelineEvents) {
            if (isOverlayEvent(ev)) latestOverlay = ev;
        }
        // The practitioner is the only writer, so only restore own state from the initial snapshot
        if (latestOverlay != null && (!isPractitioner || initial)) {
            applyOverlayEvent(latestOverlay);
        }

        if (initial) {
            initialSyncDone = true;
            maybeStartCall();
        }
        if (callSession != null) callSession.onRoomEvents(timelineEvents);
    }

    private static boolean isOverlayEvent(MatrixSyncResponse.Event ev) {
        return OVERLAY_EVENT_TYPE.equals(ev.getType()) && OVERLAY_STATE_KEY.equals(ev.getStateKey());
    }

    private void applyOverlayEvent(MatrixSyncResponse.Event ev) {
        if (ev.getContent() == null) return;
        try {
            TeethOverlayEvent event = gson.fromJson(ev.getContent(), TeethOverlayEvent.class);
            if (event == null) return;
            isOverlayVisible = event.isVisible();
            teethOverlayView.setVisibility(event.isVisible() ? View.VISIBLE : View.GONE);
            if (event.getPractitionerName() != null) {
                teethOverlayView.setPractitionerName(event.getPractitionerName());
            }
            teethOverlayView.updateConditions(event.getToothConditions(), event.getSelectedTooth());
        } catch (Exception ignored) {}
    }

    private boolean checkAndRequestPermissions() {
        int cameraPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (cameraPerm != PackageManager.PERMISSION_GRANTED || audioPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = grantResults.length > 0;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                permissionsGranted = true;
                maybeStartCall();
            } else {
                Toast.makeText(this, "Camera and microphone permissions are required for video consultations.", Toast.LENGTH_LONG).show();
                showStatus("Call failed: camera/microphone permission denied", COLOR_ERROR);
            }
        }
    }

    /** Starts the call once the room is known, its state was synced and permissions are granted. */
    private void maybeStartCall() {
        if (callStarted || !permissionsGranted || !initialSyncDone || roomId.isEmpty()) return;
        callStarted = true;
        callSession = new MatrixCallSession(this, roomId, this);
        callSession.attachRenderers(localVideoView, remoteVideoView);
        callSession.start();
    }

    // ---------------------------------------------------------- call callbacks

    @Override
    public void onStatus(MatrixCallSession.Status status, String detail) {
        if (isFinishing() || isDestroyed()) return;
        switch (status) {
            case PREPARING:
            case CONNECTING:
                showStatus(detail, COLOR_PENDING);
                break;
            case RINGING:
                showStatus("Ringing…", COLOR_PENDING);
                break;
            case CONNECTED:
                if (connectedAtMs == 0) {
                    connectedAtMs = System.currentTimeMillis();
                    startTimer();
                }
                updateTimerText();
                break;
            case ENDED:
            case FAILED:
                stopTimer();
                callEnded = true;
                if (status == MatrixCallSession.Status.ENDED) {
                    showStatus("Call ended", COLOR_NEUTRAL);
                    uiHandler.postDelayed(this::finish, 2000);
                } else {
                    showStatus("Call ended: " + detail, COLOR_ERROR);
                }
                tvRemotePlaceholder.setVisibility(View.VISIBLE);
                tvRemotePlaceholder.setText(detail);
                break;
        }
    }

    @Override
    public void onRemoteVideoAvailable(boolean available) {
        if (callEnded && available) return;
        tvRemotePlaceholder.setVisibility(available ? View.GONE : View.VISIBLE);
    }

    private void showStatus(String text, int color) {
        tvCallStatus.setText(text);
        tvCallStatus.setTextColor(color);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimerText();
                uiHandler.postDelayed(this, 1000);
            }
        };
        uiHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (timerRunnable != null) uiHandler.removeCallbacks(timerRunnable);
        timerRunnable = null;
    }

    private void updateTimerText() {
        if (callEnded) return;
        long secs = (System.currentTimeMillis() - connectedAtMs) / 1000;
        showStatus(String.format("Connected • %02d:%02d", secs / 60, secs % 60), COLOR_OK);
    }

    // ---------------------------------------------------------------- controls

    private void setupRolePermissions() {
        teethOverlayView.setPractitionerMode(isPractitioner);

        if (isPractitioner) {
            teethOverlayView.setPractitionerName(PRACTITIONER_LABEL);
            btnToggleTeethOverlay.setVisibility(View.VISIBLE);
            btnToggleTeethOverlay.setText("Toggle Teeth Overlay");

            teethOverlayView.setToothClickListener((toothNumber, newCondition, allConditions) -> {
                Toast.makeText(VideoCallActivity.this,
                        "Tooth #" + toothNumber + " marked as " + newCondition.toUpperCase(),
                        Toast.LENGTH_SHORT).show();

                broadcastTeethOverlayState(isOverlayVisible, toothNumber, allConditions);
            });
        } else {
            btnToggleTeethOverlay.setText("Doctor Overlay Status");
            btnToggleTeethOverlay.setEnabled(false);
            btnToggleTeethOverlay.setAlpha(0.8f);
        }
    }

    private void setupControlListeners() {
        btnEndCall.setOnClickListener(v -> leaveCallSession());

        if (isPractitioner) {
            btnToggleTeethOverlay.setOnClickListener(v -> {
                isOverlayVisible = !isOverlayVisible;
                teethOverlayView.setVisibility(isOverlayVisible ? View.VISIBLE : View.GONE);

                Toast.makeText(this,
                        isOverlayVisible ? "Dental Overlay Enabled for Patient" : "Dental Overlay Hidden",
                        Toast.LENGTH_SHORT).show();

                broadcastTeethOverlayState(isOverlayVisible,
                        teethOverlayView.getSelectedToothNumber(),
                        teethOverlayView.getToothConditions());
            });
        }

        btnToggleMic.setOnClickListener(v -> {
            isMicMuted = !isMicMuted;
            btnToggleMic.setAlpha(isMicMuted ? 0.4f : 1.0f);
            if (callSession != null) callSession.setMicEnabled(!isMicMuted);
            Toast.makeText(this, isMicMuted ? "Microphone Muted" : "Microphone Unmuted", Toast.LENGTH_SHORT).show();
        });

        btnToggleCamera.setOnClickListener(v -> {
            isCameraOff = !isCameraOff;
            btnToggleCamera.setAlpha(isCameraOff ? 0.4f : 1.0f);
            if (callSession != null) callSession.setCameraEnabled(!isCameraOff);
            Toast.makeText(this, isCameraOff ? "Camera Off" : "Camera On", Toast.LENGTH_SHORT).show();
        });
    }

    private void leaveCallSession() {
        if (callSession != null) callSession.hangup();
        Toast.makeText(this, "Left video consultation session.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void broadcastTeethOverlayState(boolean visible, String selectedTooth, Map<String, String> conditions) {
        if (roomId.isEmpty()) return;
        matrixClient.sendTeethOverlayState(
                roomId,
                visible,
                PRACTITIONER_LABEL,
                conditions,
                selectedTooth,
                new MatrixClient.MatrixCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {}

                    @Override
                    public void onError(String error) {
                        if (!isFinishing()) {
                            Toast.makeText(VideoCallActivity.this, "Could not share overlay: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        uiHandler.removeCallbacksAndMessages(null);
        if (syncHandle != null) syncHandle.stop();
        if (callSession != null) {
            callSession.release();
            callSession = null;
        }
    }
}
