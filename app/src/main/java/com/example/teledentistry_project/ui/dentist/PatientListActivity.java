
package com.example.teledentistry_project.ui.dentist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.ActiveChat;
import com.example.teledentistry_project.ui.adapters.PatientAdapter;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatientListActivity extends AppCompatActivity {

    private static final String POCKETBASE_URL =
            "http://10.0.2.2:8090";

    private static final String CHATS_COLLECTION = "activeChats";

    private RecyclerView rvPatients;
    private TextView tvNoPatients;
    private PatientAdapter adapter;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private String practitionerId;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        rvPatients = findViewById(R.id.rvPatients);
        tvNoPatients = findViewById(R.id.tvNoPatients);

        rvPatients.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatientAdapter(chat -> {
            // Open PrescriptionActivity for the selected patient.
            android.content.Intent intent = new android.content.Intent(
                    PatientListActivity.this,
                    PrescriptionActivity.class
            );

            intent.putExtra("patientId", chat.patient);
            intent.putExtra("patientName", getPatientName(chat));

            startActivity(intent);
        });

        rvPatients.setAdapter(adapter);

        SharedPreferences session =
                getSharedPreferences("session", MODE_PRIVATE);

        authToken = session.getString("authToken", "");
        practitionerId = session.getString("userId", "");

        String role = session.getString("role", "");

        if (authToken.isEmpty() || practitionerId.isEmpty()) {
            showMessage("Please log in to view your patients.");
            return;
        }

        if (!"practitioner".equalsIgnoreCase(role)) {
            showMessage("This screen is only available to practitioners.");
            return;
        }

        loadPatients();
    }

    private void loadPatients() {
        tvNoPatients.setVisibility(View.GONE);

        HttpUrl url = HttpUrl.parse(
                POCKETBASE_URL + "/api/collections/"
                        + CHATS_COLLECTION + "/records"
        );

        if (url == null) {
            showMessage("Invalid PocketBase URL.");
            return;
        }

        url = url.newBuilder()
                .addQueryParameter(
                        "filter",
                        "practitioner = \"" + practitionerId + "\""
                )
                .addQueryParameter("expand", "patient")
                .addQueryParameter("sort", "updated")
                .addQueryParameter("page", "1")
                .addQueryParameter("perPage", "200")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", authToken)
                .build();

        new Thread(() -> {
            List<ActiveChat> chats = new ArrayList<>();
            String errorMessage = null;

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    errorMessage = "Could not load patients (HTTP "
                            + response.code() + ").";
                } else if (response.body() == null) {
                    errorMessage = "PocketBase returned an empty response.";
                } else {
                    ChatListResponse result = gson.fromJson(
                            response.body().string(),
                            ChatListResponse.class
                    );

                    if (result != null && result.items != null) {
                        // Keep one entry per patient, even if there are
                        // multiple chat records for the same patient.
                        Map<String, ActiveChat> uniquePatients =
                                new LinkedHashMap<>();

                        for (ActiveChat chat : result.items) {
                            if (chat.patient != null
                                    && !chat.patient.trim().isEmpty()) {
                                uniquePatients.putIfAbsent(
                                        chat.patient,
                                        chat
                                );
                            }
                        }

                        chats.addAll(uniquePatients.values());
                    }
                }
            } catch (IOException | RuntimeException e) {
                errorMessage = "Unable to connect to PocketBase.";
            }

            final String finalError = errorMessage;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (finalError != null) {
                    showMessage(finalError);
                    return;
                }

                adapter.setChats(chats);

                boolean isEmpty = chats.isEmpty();
                tvNoPatients.setVisibility(
                        isEmpty ? View.VISIBLE : View.GONE
                );
                rvPatients.setVisibility(
                        isEmpty ? View.GONE : View.VISIBLE
                );

                if (isEmpty) {
                    tvNoPatients.setText("No patients with chats yet.");
                }
            });
        }).start();
    }

    private String getPatientName(ActiveChat chat) {
        if (chat.expand == null || chat.expand.patient == null) {
            return "Patient";
        }

        String name = chat.expand.patient.name == null
                ? "" : chat.expand.patient.name.trim();

        String surname = chat.expand.patient.surname == null
                ? "" : chat.expand.patient.surname.trim();

        String fullName = (name + " " + surname).trim();

        return fullName.isEmpty() ? "Patient" : fullName;
    }

    private void showMessage(String message) {
        tvNoPatients.setText(message);
        tvNoPatients.setVisibility(View.VISIBLE);
        rvPatients.setVisibility(View.GONE);
    }

    private static class ChatListResponse {
        @SerializedName("items")
        List<ActiveChat> items;
    }
}
