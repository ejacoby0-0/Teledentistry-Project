package com.example.teledentistry_project.ui.patient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.model.prescription.PrescriptionListResponse;
import com.example.teledentistry_project.ui.adapters.PrescriptionAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatientPrescriptionsActivity extends AppCompatActivity {

    private static final String POCKETBASE_URL =
            "http://10.0.2.2:8090/";

    private static final String PRESCRIPTIONS_COLLECTION =
            "prescriptions";

    private static final String PREFS_NAME = "session";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    private RecyclerView rvPrescriptions;
    private TextView tvNoPrescriptions;

    private PrescriptionAdapter prescriptionAdapter;

    private String authToken;
    private String patientId;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescriptions);

        rvPrescriptions = findViewById(R.id.rvPrescriptions);
        tvNoPrescriptions = findViewById(R.id.tvNoPrescriptions);

        prescriptionAdapter = new PrescriptionAdapter();

        rvPrescriptions.setLayoutManager(
                new LinearLayoutManager(this)
        );
        rvPrescriptions.setAdapter(prescriptionAdapter);

        readSession();

        if (!"patient".equals(userRole)) {
            showEmptyState("This screen is available to patients only.");
            return;
        }

        if (TextUtils.isEmpty(authToken)
                || TextUtils.isEmpty(patientId)) {
            showEmptyState("Your session has expired. Please sign in again.");
            return;
        }

        loadPrescriptions();
    }

    private void readSession() {
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        authToken = preferences.getString(KEY_AUTH_TOKEN, "");
        patientId = preferences.getString(KEY_USER_ID, "");
        userRole = preferences.getString(KEY_ROLE, "");

        if (userRole != null) {
            userRole = userRole.trim().toLowerCase(
                    java.util.Locale.ROOT
            );
        }
    }

    private void loadPrescriptions() {
        HttpUrl baseUrl = HttpUrl.parse(
                POCKETBASE_URL + "/api/collections/"
                        + PRESCRIPTIONS_COLLECTION + "/records"
        );

        if (baseUrl == null) {
            showEmptyState("The PocketBase server URL is invalid.");
            return;
        }

        HttpUrl url = baseUrl.newBuilder()
                .addQueryParameter(
                        "filter",
                        "patient = \"" + patientId + "\""
                )
                .addQueryParameter("expand", "practitioner")
                .addQueryParameter("sort", "-created")
                .addQueryParameter("page", "1")
                .addQueryParameter("perPage", "200")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", authToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showEmptyState(
                        "Unable to load prescriptions. Check your connection."
                ));
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                String responseBody;
                int statusCode;
                boolean successful;

                try (Response responseToClose = response) {
                    statusCode = responseToClose.code();
                    successful = responseToClose.isSuccessful();

                    responseBody = responseToClose.body() != null
                            ? responseToClose.body().string()
                            : "";
                }

                if (!successful) {
                    runOnUiThread(() -> showEmptyState(
                            "Could not load prescriptions (HTTP "
                                    + statusCode + "). Check your access rules."
                    ));
                    return;
                }

                final PrescriptionListResponse result;

                try {
                    result = gson.fromJson(
                            responseBody,
                            PrescriptionListResponse.class
                    );
                } catch (JsonSyntaxException e) {
                    runOnUiThread(() -> showEmptyState(
                            "The prescription data could not be read."
                    ));
                    return;
                }

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (result == null || result.items == null
                            || result.items.isEmpty()) {
                        prescriptionAdapter.setPrescriptions(
                                new ArrayList<>()
                        );
                        showEmptyState("You have no prescriptions yet.");
                        return;
                    }

                    prescriptionAdapter.setPrescriptions(result.items);

                    tvNoPrescriptions.setVisibility(View.GONE);
                    rvPrescriptions.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showEmptyState(String message) {
        prescriptionAdapter.setPrescriptions(new ArrayList<>());
        rvPrescriptions.setVisibility(View.GONE);
        tvNoPrescriptions.setVisibility(View.VISIBLE);
        tvNoPrescriptions.setText(message);
    }
}