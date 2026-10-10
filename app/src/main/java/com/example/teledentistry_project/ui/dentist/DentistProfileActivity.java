package com.example.teledentistry_project.ui.dentist;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.practitioner.PractitionerUpdateRequest;
import com.example.teledentistry_project.utils.SessionManager;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.PractitionerRepository;

import android.content.Intent;

import com.example.teledentistry_project.ui.auth.LoginActivity;

public class DentistProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerRepository practitionerRepository;

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etLicense;
    private TextInputEditText etSpecialization;
    private TextInputEditText etAvailability;

    private MaterialButton btnSavePractitionerProfile;

    private String practitionerId;

    private MaterialButton btnPractitionerLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Must be logged in
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Must be a practitioner
        if (!"practitioner".equals(sessionManager.getRole())) {
            finish();
            return;
        }

        setContentView(R.layout.activity_dentist_profile);

        practitionerRepository = new PractitionerRepository();

        etFullName = findViewById(R.id.etPractitionerFullName);
        etEmail = findViewById(R.id.etPractitionerEmail);
        etPhone = findViewById(R.id.etPractitionerPhone);
        etLicense = findViewById(R.id.etPractitionerLicense);
        etSpecialization = findViewById(R.id.etPractitionerSpecialization);
        etAvailability = findViewById(R.id.etPractitionerAvailability);
        btnSavePractitionerProfile = findViewById(R.id.btnSavePractitionerProfile);
        btnSavePractitionerProfile.setOnClickListener(v -> savePractitionerProfile());
        etAvailability.setOnClickListener(v -> showAvailabilityDialog());
        btnPractitionerLogout = findViewById(R.id.btnPractitionerLogout);
        btnPractitionerLogout.setOnClickListener(v -> logoutPractitioner());

        loadPractitionerProfile();
    }

    private void loadPractitionerProfile() {

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        practitionerRepository.getPractitionerByUserId(
                token,
                userId,
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {

                        practitionerId = practitioner.getId();

                        etFullName.setText(
                                practitioner.getFullName() != null
                                        ? practitioner.getFullName()
                                        : ""
                        );

                        etEmail.setText(
                                sessionManager.getEmail() != null
                                        ? sessionManager.getEmail()
                                        : ""
                        );

                        etPhone.setText(
                                practitioner.getPhoneNumber() != null
                                        ? practitioner.getPhoneNumber()
                                        : ""
                        );

                        etLicense.setText(
                                practitioner.getLicenseNumber() != null
                                        ? practitioner.getLicenseNumber()
                                        : ""
                        );

                        etSpecialization.setText(
                                practitioner.getSpecialization() != null
                                        ? practitioner.getSpecialization()
                                        : ""
                        );

                        etAvailability.setText(
                                practitioner.getAvailabilityStatus() != null
                                        ? practitioner.getAvailabilityStatus()
                                        : ""
                        );



                    }

                    @Override
                    public void onNotFound() {

                        Toast.makeText(
                                DentistProfileActivity.this,
                                "Dentist profile not found.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                DentistProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    //Save the changes in PractitonerProfile method
    private void savePractitionerProfile() {

        if (practitionerId == null || practitionerId.isEmpty()) {
            Toast.makeText(
                    this,
                    "Practitioner profile has not loaded yet.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String fullName = etFullName.getText() != null
                ? etFullName.getText().toString().trim()
                : "";

        String phoneNumber = etPhone.getText() != null
                ? etPhone.getText().toString().trim()
                : "";

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            return;
        }

        String availabilityStatus =
                etAvailability.getText() != null
                        ? etAvailability.getText().toString().trim()
                        : "";

        PractitionerUpdateRequest request =
                new PractitionerUpdateRequest(
                        fullName,
                        phoneNumber,
                        availabilityStatus
                );

        btnSavePractitionerProfile.setEnabled(false);

        practitionerRepository.updatePractitioner(
                sessionManager.getAuthToken(),
                practitionerId,
                request,
                new PractitionerRepository.PractitionerUpdateCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {

                        btnSavePractitionerProfile.setEnabled(true);

                        Toast.makeText(
                                DentistProfileActivity.this,
                                "Profile updated successfully.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        btnSavePractitionerProfile.setEnabled(true);

                        Toast.makeText(
                                DentistProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }



        );
    }

    //Availability status method
    private void showAvailabilityDialog() {

        String[] options = {
                "available",
                "unavailable"
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Availability Status");

        builder.setItems(options, (dialog, which) -> {
            etAvailability.setText(options[which]);
        });

        builder.show();
    }

    private void logoutPractitioner() {

        sessionManager.clearSession();

        Toast.makeText(
                this,
                "Logged out successfully.",
                Toast.LENGTH_SHORT
        ).show();

        redirectToLogin();
    }

    //Logout method
    private void redirectToLogin() {

        Intent intent = new Intent(
                DentistProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }


}