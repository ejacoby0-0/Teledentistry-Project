package com.example.teledentistry_project.ui.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.MainActivity;
import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.UserProfile;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.repository.AccountRepository;
import com.example.teledentistry_project.ui.patient.PatientPrescriptionsActivity;
import com.example.teledentistry_project.utils.NavbarHelper;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** The logged-in user's own profile (opened from the navbar's Profile tab). */
public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private AccountRepository accountRepository;

    private TextView tvName;
    private TextView tvSurname;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvDob;
    private TextView tvRole;
    private TextView tvDentist;     // practice name
    private TextView tvLocation;
    private TextView tvCost;

    private View rowPracticeName;
    private View rowLocation;
    private View rowCost;

    private MaterialButton btnEditProfile;
    private MaterialButton btnReceipts;
    private MaterialButton btnPrescriptions;
    private MaterialButton btnLogout;
    private MaterialButton btnDeleteAccount;

    private String authToken;
    private String userId;
    private String userRole;

    private AlertDialog deleteAccountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            openLogin();
            return;
        }

        accountRepository = new AccountRepository();

        bindViews();
        readSession();
        configureProfileForRole();
        NavbarHelper.setup(this, NavbarHelper.Tab.PROFILE);
        configureButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isLoggedIn()) {
            loadUserProfile();   // refreshes after returning from Edit profile
        }
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvName);
        tvSurname = findViewById(R.id.tvSurname);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvDob = findViewById(R.id.tvDob);
        tvRole = findViewById(R.id.tvRole);
        tvDentist = findViewById(R.id.tvDentist);
        tvLocation = findViewById(R.id.tvLocation);
        tvCost = findViewById(R.id.tvCost);

        rowPracticeName = findViewById(R.id.rowPracticeName);
        rowLocation = findViewById(R.id.rowLocation);
        rowCost = findViewById(R.id.rowCost);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnReceipts = findViewById(R.id.btnReceipts);
        btnPrescriptions = findViewById(R.id.btnPrescriptions);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void readSession() {
        authToken = session.getAuthToken();
        userId = session.getUserId();
        userRole = session.getRole();

        if (userRole != null) {
            userRole = userRole.trim().toLowerCase(Locale.ROOT);
        }
    }

    private boolean isPatient() {
        return "patient".equals(userRole);
    }

    private boolean isPractitioner() {
        return "practitioner".equals(userRole);
    }

    // The navbar itself is shown, hidden and wired by NavbarHelper
    private void configureProfileForRole() {
        if (isPatient()) {
            setVisibility(rowPracticeName, View.GONE);
            setVisibility(rowLocation, View.GONE);
            setVisibility(rowCost, View.GONE);

            setVisibility(btnPrescriptions, View.VISIBLE);
            setText(tvRole, "Patient");

        } else if (isPractitioner()) {
            setVisibility(rowPracticeName, View.VISIBLE);
            setVisibility(rowLocation, View.VISIBLE);
            setVisibility(rowCost, View.VISIBLE);

            setVisibility(btnPrescriptions, View.GONE);
            setText(tvRole, "Practitioner");

        } else {
            setVisibility(rowPracticeName, View.GONE);
            setVisibility(rowLocation, View.GONE);
            setVisibility(rowCost, View.GONE);
            setVisibility(btnPrescriptions, View.GONE);

            setText(tvRole, "Unknown");
        }
    }

    // ------------------------------------------------ 1. the user's own record

    private void loadUserProfile() {
        if (TextUtils.isEmpty(authToken) || TextUtils.isEmpty(userId)) {
            Toast.makeText(this,
                    "Your session is missing. Please sign in again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        PocketBaseClient.api
                .getUserProfile(authToken, userId)
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call,
                                           Response<UserProfile> response) {
                        if (response.code() == 401) {
                            session.clearSession();
                            openLogin();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(ProfileActivity.this,
                                    "Could not load your profile (HTTP "
                                            + response.code() + ").",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        displayProfile(response.body());
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this,
                                "Unable to load your profile. Check your connection.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void displayProfile(UserProfile profile) {
        setText(tvName, profile.getName());
        setText(tvSurname, profile.getSurname());
        setText(tvEmail, profile.getEmail());
        setText(tvPhone, profile.getPhone());
        setText(tvDob, formatDateOfBirth(profile.getDateOfBirth()));

        // If the session somehow had no role, take it from the record and keep it
        String profileRole = profile.getRole();
        if (TextUtils.isEmpty(userRole) && !TextUtils.isEmpty(profileRole)) {
            userRole = profileRole.trim().toLowerCase(Locale.ROOT);
            session.saveSession(authToken, userId, session.getEmail(), userRole);
            configureProfileForRole();
            NavbarHelper.setup(this, NavbarHelper.Tab.PROFILE);
        }

        if (isPatient()) {
            setText(tvRole, "Patient");
        } else if (isPractitioner()) {
            setText(tvRole, "Practitioner");
            loadPractitionerDetails();
        } else {
            setText(tvRole, "Unknown");
        }
    }

    // ---------------------- 2. practice details (from the practitioners collection)

    private void loadPractitionerDetails() {
        // The practitioners record whose "user" field points at me
        String filter = "user = \"" + userId + "\"";

        PocketBaseClient.api
                .getPractitioners(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {
                    @Override
                    public void onResponse(Call<PocketBaseListResponse<Practitioner>> call,
                                           Response<PocketBaseListResponse<Practitioner>> response) {
                        List<Practitioner> list =
                                (response.isSuccessful() && response.body() != null)
                                        ? response.body().getItems() : null;

                        if (list == null || list.isEmpty()) {
                            Toast.makeText(ProfileActivity.this,
                                    "No practice details found.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Practitioner p = list.get(0);
                        setText(tvDentist, p.getPracticeName());
                        setText(tvLocation, p.getPracticeLocation());
                        setText(tvCost, String.format(
                                Locale.getDefault(), "R %.2f", p.getConsultationFee()));
                    }

                    @Override
                    public void onFailure(Call<PocketBaseListResponse<Practitioner>> call,
                                          Throwable t) {
                        Toast.makeText(ProfileActivity.this,
                                "Could not load practice details.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // PocketBase dates look like "1994-03-14 00:00:00.000Z"; show "14 March 1994"
    private String formatDateOfBirth(String raw) {
        if (TextUtils.isEmpty(raw) || raw.length() < 10) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = in.parse(raw.substring(0, 10));
            if (date == null) return "";
            return new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ buttons

    private void configureButtons() {
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnPrescriptions.setOnClickListener(v -> {
            if (isPatient()) {
                startActivity(new Intent(this, PatientPrescriptionsActivity.class));
            }
        });

        // TODO: open your receipts screen here
        // btnReceipts.setOnClickListener(v ->
        //         startActivity(new Intent(this, ReceiptsActivity.class)));

        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> clearSessionAndOpenLogin())
                .show();
    }

    private void clearSessionAndOpenLogin() {
        session.clearSession();
        openLogin();
    }

    // MainActivity is the login screen
    private void openLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteAccount() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogStart);

        tvDialogTitle.setText("Delete Account");
        tvDialogMessage.setText(
                "Are you sure you want to permanently delete your account? "
                        + "This action cannot be undone.");

        btnDialogCancel.setText("Cancel");
        btnDialogConfirm.setText("Delete account");

        deleteAccountDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnDialogCancel.setOnClickListener(v -> deleteAccountDialog.dismiss());

        btnDialogConfirm.setOnClickListener(v -> {
            deleteAccountDialog.dismiss();
            deletePocketBaseAccount();
        });

        deleteAccountDialog.show();

        if (deleteAccountDialog.getWindow() != null) {
            deleteAccountDialog.getWindow()
                    .setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void deletePocketBaseAccount() {
        if (TextUtils.isEmpty(authToken) || TextUtils.isEmpty(userId)) {
            Toast.makeText(this,
                    "Your session is missing. Please sign in again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnDeleteAccount.setEnabled(false);

        accountRepository.deleteAccount(
                authToken,
                userId,
                new AccountRepository.DeleteAccountCallback() {
                    @Override
                    public void onSuccess() {
                        btnDeleteAccount.setEnabled(true);
                        Toast.makeText(ProfileActivity.this,
                                "Account deleted successfully.",
                                Toast.LENGTH_LONG).show();
                        clearSessionAndOpenLogin();
                    }

                    @Override
                    public void onError(String message) {
                        btnDeleteAccount.setEnabled(true);
                        Toast.makeText(ProfileActivity.this,
                                message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // helpers

    private void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void setText(TextView view, String value) {
        if (view != null) {
            view.setText(value == null ? "" : value);
        }
    }
}