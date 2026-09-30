package com.example.teledentistry_project.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class PatientProfileActivity extends AppCompatActivity {

    private String patientId;
    private SessionManager sessionManager;
    private PatientRepository patientRepository;

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;

    private Button btnSave;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication check
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Authorization check
        if (!"patient".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "You are not authorized to access this page.",
                    Toast.LENGTH_LONG
            ).show();

            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_patient_profile);

        patientRepository = new PatientRepository();

        initializeViews();

        loadPatientProfile();

        btnSave.setOnClickListener(view ->
                saveProfile()
        );

        btnLogout.setOnClickListener(view ->
                logout()
        );
    }

    //Method for saving a user profile
    private void saveProfile() {

        if (patientId == null || patientId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Patient profile has not been loaded.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String fullName =
                etFullName.getText() != null
                        ? etFullName.getText().toString().trim()
                        : "";

        String phoneNumber =
                etPhone.getText() != null
                        ? etPhone.getText().toString().trim()
                        : "";

        if (fullName.isEmpty()) {

            etFullName.setError("Full name is required");
            return;
        }

        patientRepository.updatePatient(
                sessionManager.getAuthToken(),
                patientId,
                fullName,
                phoneNumber,
                new PatientRepository.UpdatePatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        Toast.makeText(
                                PatientProfileActivity.this,
                                "Profile updated successfully.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PatientProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void initializeViews() {

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnSave = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadPatientProfile() {

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        patientRepository.getPatientByUserId(
                token,
                userId,
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        patientId = patient.getId();

                        // Load patient information from PocketBase
                        etFullName.setText(patient.getFullName());
                        etPhone.setText(patient.getPhoneNumber());

                        // Email comes from the authenticated users account
                        etEmail.setText(sessionManager.getEmail());

                        Toast.makeText(
                                PatientProfileActivity.this,
                                "Patient profile loaded.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotFound() {

                        /*
                         * The authenticated user exists,
                         * but they don't have a patients
                         * collection record yet.
                         */
                        etEmail.setText(sessionManager.getEmail());

                        Toast.makeText(
                                PatientProfileActivity.this,
                                "Patient profile has not been created yet.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PatientProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void logout() {

        sessionManager.clearSession();

        Toast.makeText(
                this,
                "Logged out successfully.",
                Toast.LENGTH_SHORT
        ).show();

        redirectToLogin();
    }

    private void redirectToLogin() {

        Intent intent = new Intent(
                PatientProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
