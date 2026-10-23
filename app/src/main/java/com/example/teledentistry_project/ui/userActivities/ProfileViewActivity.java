package com.example.teledentistry_project.ui.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.MainActivity;
import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.ActiveChat;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.UserProfile;
import com.example.teledentistry_project.data.model.practitioner.MedicalNote;
import com.example.teledentistry_project.data.model.practitioner.MedicalNoteRequest;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.utils.SessionManager;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "profileUserId";

    private SessionManager session;
    private String profileUserId;          // whose profile is on screen
    private boolean viewerIsPractitioner;  // is the logged-in user a practitioner?
    private String existingNoteId;         // null until this practitioner has saved a note

    private TextView tvProfileName, tvPractice, tvLocation, tvEmail, tvPhone, tvCost;
    private View rowPractice, dividerPractice, rowLocation, dividerLocation,
            rowCost, dividerCost, cardNotes;
    private EditText etNotes;
    private View btnSaveNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        viewerIsPractitioner = "practitioner".equalsIgnoreCase(session.getRole());

        // No extra passed = show my own profile
        profileUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (profileUserId == null || profileUserId.isEmpty()) {
            profileUserId = session.getUserId();
        }

        bindViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSaveNotes.setOnClickListener(v -> saveNotes());

        // Hide everything optional until we know whose profile this is
        setVisible(false, rowPractice, dividerPractice, rowLocation, dividerLocation,
                rowCost, dividerCost, cardNotes);

        loadProfile();
    }

    private void bindViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvPractice = findViewById(R.id.tvPractice);
        tvLocation = findViewById(R.id.tvLocation);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvCost = findViewById(R.id.tvCost);
        rowPractice = findViewById(R.id.rowPractice);
        dividerPractice = findViewById(R.id.dividerPractice);
        rowLocation = findViewById(R.id.rowLocation);
        dividerLocation = findViewById(R.id.dividerLocation);
        rowCost = findViewById(R.id.rowCost);
        dividerCost = findViewById(R.id.dividerCost);
        cardNotes = findViewById(R.id.cardNotes);
        etNotes = findViewById(R.id.etNotes);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
    }

    // ---------------------------------------------------- 1. the person's record

    private void loadProfile() {
        android.util.Log.d("PROFILE_VIEW", "loading user id = '" + profileUserId + "'");

        PocketBaseClient.api
                .getUserProfile(session.getAuthToken(), profileUserId)
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        android.util.Log.d("PROFILE_VIEW", "users response code = " + response.code());

                        if (response.code() == 401) {
                            session.clearSession();
                            goToLogin();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            try {
                                if (response.errorBody() != null) {
                                    android.util.Log.e("PROFILE_VIEW",
                                            "users error body = " + response.errorBody().string());
                                }
                            } catch (java.io.IOException ignored) { }

                            Toast.makeText(ProfileViewActivity.this,
                                    "Could not load profile (" + response.code() + ")",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        onUserLoaded(response.body());
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {
                        android.util.Log.e("PROFILE_VIEW", "users request failed", t);
                        Toast.makeText(ProfileViewActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onUserLoaded(UserProfile user) {
        boolean profileIsPractitioner = "practitioner".equalsIgnoreCase(user.getRole());

        // Fields every profile shows
        String name = (safe(user.getName()) + " " + safe(user.getSurname())).trim();
        tvProfileName.setText(profileIsPractitioner ? "Dr. " + name : name);
        tvEmail.setText(safe(user.getEmail()));
        tvPhone.setText(safe(user.getPhone()));


        if (profileIsPractitioner) {
            // Practitioner: everything except notes
            setVisible(true, rowPractice, dividerPractice, rowLocation, dividerLocation,
                    rowCost, dividerCost);

            // Patients can long-press the practitioner's name to start a chat.
            setupPractitionerLongPress();

            loadPractitionerDetails();
        } else if (viewerIsPractitioner) {
            // Patient, viewed by a practitioner: no practice name, location or cost,
            // but the notes section is available
            setVisible(true, cardNotes);
            loadExistingNote();
        }
        // Patient viewed by anyone else: just the basic fields, no notes
    }

    //  practice details

    private void loadPractitionerDetails() {
        // The practitioners record whose "user" field points at this person
        String filter = "user = \"" + profileUserId + "\"";

        PocketBaseClient.api
                .getPractitioners(session.getAuthToken(), filter)
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {
                    @Override
                    public void onResponse(Call<PocketBaseListResponse<Practitioner>> call,
                                           Response<PocketBaseListResponse<Practitioner>> response) {
                        android.util.Log.d("PROFILE_VIEW", "practitioners response code = " + response.code()
                                + ", items = " + (response.body() == null || response.body().getItems() == null
                                ? "none" : response.body().getItems().size()));
                        List<Practitioner> list = (response.isSuccessful() && response.body() != null)
                                ? response.body().getItems() : null;

                        if (list == null || list.isEmpty()) {
                            Toast.makeText(ProfileViewActivity.this,
                                    "No practice details found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Practitioner p = list.get(0);
                        tvPractice.setText(safe(p.getPracticeName()));
                        tvLocation.setText(safe(p.getPracticeLocation()));

                        double fee = p.getConsultationFee();
                        tvCost.setText(fee > 0
                                ? getString(R.string.session_cost_format, formatAmount(fee))
                                : "-");
                    }

                    @Override
                    public void onFailure(Call<PocketBaseListResponse<Practitioner>> call,
                                          Throwable t) {
                        Toast.makeText(ProfileViewActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ----------------------------- 3. notes (practitioner viewing a patient only)

    private void loadExistingNote() {
        // My note for this patient: my id (logged in) + the patient's id (passed in)
        String filter = "practitioner = \"" + session.getUserId()
                + "\" && patient = \"" + profileUserId + "\"";

        PocketBaseClient.api
                .getMedicalNotes(session.getAuthToken(), filter)
                .enqueue(new Callback<PocketBaseListResponse<MedicalNote>>() {
                    @Override
                    public void onResponse(Call<PocketBaseListResponse<MedicalNote>> call,
                                           Response<PocketBaseListResponse<MedicalNote>> response) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        List<MedicalNote> notes = response.body().getItems();
                        if (notes != null && !notes.isEmpty()) {
                            existingNoteId = notes.get(0).getId();
                            etNotes.setText(safe(notes.get(0).getNotes()));
                        }
                    }

                    @Override
                    public void onFailure(Call<PocketBaseListResponse<MedicalNote>> call,
                                          Throwable t) {
                        Toast.makeText(ProfileViewActivity.this,
                                "Could not load notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNotes() {
        if (!viewerIsPractitioner) return;   // only practitioners write notes

        String text = etNotes.getText().toString().trim();

        // Nothing written and nothing saved yet: nothing to do
        if (text.isEmpty() && existingNoteId == null) return;

        btnSaveNotes.setEnabled(false);   // stops a double tap creating two notes

        Callback<MedicalNote> callback = new Callback<MedicalNote>() {
            @Override
            public void onResponse(Call<MedicalNote> call, Response<MedicalNote> response) {
                btnSaveNotes.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    existingNoteId = response.body().getId();
                    Toast.makeText(ProfileViewActivity.this,
                            "Notes saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileViewActivity.this,
                            "Could not save notes (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MedicalNote> call, Throwable t) {
                btnSaveNotes.setEnabled(true);
                Toast.makeText(ProfileViewActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        };

        if (existingNoteId == null) {
            // First note: practitioner = me, patient = the profile on screen
            PocketBaseClient.api.createMedicalNote(
                    session.getAuthToken(),
                    new MedicalNoteRequest(session.getUserId(), profileUserId, text)
            ).enqueue(callback);
        } else {
            // Later edits: only the text changes
            PocketBaseClient.api.updateMedicalNote(
                    session.getAuthToken(),
                    existingNoteId,
                    new MedicalNoteRequest(null, null, text)
            ).enqueue(callback);
        }
    }

    // --------------------------------------------------------------- helpers

    private void setVisible(boolean visible, View... views) {
        for (View v : views) {
            v.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatAmount(double amount) {
        return (amount == Math.floor(amount))
                ? String.valueOf((long) amount)
                : String.format(Locale.getDefault(), "%.2f", amount);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void setupPractitionerLongPress() {
        // Only patients can initiate a chat from a practitioner's profile.
        if (viewerIsPractitioner) {
            return;
        }

        tvProfileName.setOnLongClickListener(v -> {
            showStartChatDialog();
            return true;
        });
    }

    private void showStartChatDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView title = dialog.findViewById(R.id.tvDialogTitle);
        TextView message = dialog.findViewById(R.id.tvDialogMessage);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        MaterialButton btnStart = dialog.findViewById(R.id.btnDialogStart);

        title.setText("Start a chat?");
        message.setText(
                "Would you like to start a chat with this practitioner?"
        );

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            createActiveChat();
        });

        dialog.show();

        // Apply width after show(), since Dialog may reset window dimensions.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void createActiveChat() {
        if (!session.isLoggedIn()) {
            Toast.makeText(
                    this,
                    "Please log in to start a chat.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // This action is intended for a patient viewing a practitioner.
        if (viewerIsPractitioner) {
            Toast.makeText(
                    this,
                    "Only patients can start a chat from this screen.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String patientId = session.getUserId();
        String practitionerId = profileUserId;
        String token = session.getAuthToken();

        if (patientId == null || practitionerId == null
                || token == null
                || patientId.trim().isEmpty()
                || practitionerId.trim().isEmpty()
                || token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Missing account information. Please log in again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // The logged-in patient and the practitioner whose profile is open.
        Map<String, String> request = new HashMap<>();
        request.put("patient", patientId);
        request.put("practitioner", practitionerId);

        PocketBaseClient.api.createActiveChat(token, request)
                .enqueue(new Callback<ActiveChat>() {
                    @Override
                    public void onResponse(
                            Call<ActiveChat> call,
                            Response<ActiveChat> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(
                                    ProfileViewActivity.this,
                                    "Chat started successfully.",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {
                            String error = "Could not start chat (HTTP "
                                    + response.code() + ").";

                            try {
                                if (response.errorBody() != null) {
                                    android.util.Log.e(
                                            "ACTIVE_CHAT",
                                            response.errorBody().string()
                                    );
                                }
                            } catch (java.io.IOException e) {
                                android.util.Log.e(
                                        "ACTIVE_CHAT",
                                        "Could not read error response",
                                        e
                                );
                            }

                            Toast.makeText(
                                    ProfileViewActivity.this,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ActiveChat> call,
                            Throwable t
                    ) {
                        android.util.Log.e(
                                "ACTIVE_CHAT",
                                "Failed to create active chat",
                                t
                        );

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                ProfileViewActivity.this,
                                "Network error while starting chat.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

}