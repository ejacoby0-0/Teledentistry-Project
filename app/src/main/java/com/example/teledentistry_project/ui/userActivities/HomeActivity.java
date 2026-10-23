package com.example.teledentistry_project.ui.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.MainActivity;
import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.ActiveChat;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.UserRecords;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.ui.adapters.ViewChatAdapter;
import com.example.teledentistry_project.utils.NavbarHelper;
import com.example.teledentistry_project.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private SessionManager session;
    private ViewChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        NavbarHelper.setup(this, NavbarHelper.Tab.HOME);

        // Chat list
        RecyclerView rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ViewChatAdapter(item -> openChat(item));
        rvChats.setAdapter(adapter);

        // Search by name as the user types
        EditText etSearch = findViewById(R.id.etSearchChats);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.isLoggedIn()) {
            loadChats();   // refresh when returning from a chat
        }
    }

    private void loadChats() {
        final String myId = session.getUserId();
        String filter = "patient = \"" + myId + "\" || practitioner = \"" + myId + "\"";

        PocketBaseClient.api
                .getActiveChats(session.getAuthToken(), filter, "-updated", "patient,practitioner", 100)
                .enqueue(new Callback<PocketBaseListResponse<ActiveChat>>() {
                    @Override
                    public void onResponse(Call<PocketBaseListResponse<ActiveChat>> call,
                                           Response<PocketBaseListResponse<ActiveChat>> response) {
                        if (response.code() == 401) {          // token expired or invalid
                            session.clearSession();
                            goToLogin();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getItems() == null) {
                            Toast.makeText(HomeActivity.this,
                                    "Could not load chats", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        adapter.setItems(buildItems(response.body().getItems(), myId));
                    }

                    @Override
                    public void onFailure(Call<PocketBaseListResponse<ActiveChat>> call, Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<ViewChatAdapter.ChatItem> buildItems(List<ActiveChat> chats, String myId) {
        List<ViewChatAdapter.ChatItem> items = new ArrayList<>();

        for (ActiveChat chat : chats) {
            if (chat.expand == null) continue;

            // The "other person"
            UserRecords other = myId.equals(chat.patient)
                    ? chat.expand.practitioner
                    : chat.expand.patient;
            if (other == null) continue;

            items.add(new ViewChatAdapter.ChatItem(
                    chat.id,
                    other.id,
                    buildDisplayName(other),
                    formatTimestamp(chat.updated)
            ));
        }
        return items;
    }

    private String buildDisplayName(UserRecords user) {
        String name = (user.name == null ? "" : user.name)
                + " " + (user.surname == null ? "" : user.surname);
        name = name.trim();
        return "practitioner".equalsIgnoreCase(user.role) ? "Dr. " + name : name;
    }

    // Timestamp
    private String formatTimestamp(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = in.parse(raw);
            if (date == null) return "";

            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            boolean isToday = dayFormat.format(date).equals(dayFormat.format(new Date()));

            SimpleDateFormat out = new SimpleDateFormat(isToday ? "HH:mm" : "dd/MM", Locale.getDefault());
            return out.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void openChat(ViewChatAdapter.ChatItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", item.chatId);
        intent.putExtra("otherUserId", item.otherUserId);
        intent.putExtra("otherName", item.displayName);
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}