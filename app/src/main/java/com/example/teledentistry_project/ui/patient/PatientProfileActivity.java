package com.example.teledentistry_project.ui.patient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;

public class PatientProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    private String patientId;
    private SessionManager sessionManager;
    private PatientRepository patientRepository;

    private ImageView ivProfilePicture;
    private FloatingActionButton btnUploadProfileImage;
    private TextView tvRole;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;

    private Button btnSave;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        if (!"patient".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access denied.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_patient_profile);

        patientRepository = new PatientRepository();

        initializeViews();
        loadSavedProfileImage();
        loadPatientProfile();

        if (btnSave != null) {
            btnSave.setOnClickListener(view -> saveProfile());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(view -> logout());
        }

        if (btnUploadProfileImage != null) {
            btnUploadProfileImage.setOnClickListener(view -> selectImage());
        }
    }

    private void initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnUploadProfileImage = findViewById(R.id.btnUploadProfileImage);
        tvRole = findViewById(R.id.tvRole);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnSave = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        if (tvRole != null) {
            tvRole.setText("Role: " + sessionManager.getRole().toUpperCase());
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (validateAndSaveImage(imageUri)) {
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageURI(imageUri);
                }
                Toast.makeText(this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateAndSaveImage(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null && !mimeType.startsWith("image/")) {
                Toast.makeText(this, "Invalid file type. Please select an image file (JPEG/PNG/WEBP).", Toast.LENGTH_LONG).show();
                return false;
            }

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                int size = inputStream.available();
                inputStream.close();
                if (size > MAX_FILE_SIZE_BYTES) {
                    Toast.makeText(this, "File size exceeds 5MB limit. Please select a smaller image.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            // Save persistent image URI reference for this user
            SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("profile_image_" + sessionManager.getUserId(), uri.toString()).apply();
            return true;

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load selected image.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void loadSavedProfileImage() {
        SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        String savedUriStr = prefs.getString("profile_image_" + sessionManager.getUserId(), null);
        if (savedUriStr != null && ivProfilePicture != null) {
            try {
                ivProfilePicture.setImageURI(Uri.parse(savedUriStr));
            } catch (Exception ignored) {}
        }
    }

    private void saveProfile() {
        if (patientId == null || patientId.isEmpty()) {
            Toast.makeText(this, "Patient profile is still loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String phoneNumber = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

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
                        Toast.makeText(PatientProfileActivity.this, "Profile saved successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PatientProfileActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void loadPatientProfile() {
        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        patientRepository.ensurePatientRecord(
                token,
                userId,
                new PatientRepository.PatientCallback() {
                    @Override
                    public void onSuccess(Patient patient) {
                        if (patient != null) {
                            patientId = patient.getId();
                            if (etFullName != null) etFullName.setText(patient.getFullName());
                            if (etPhone != null) etPhone.setText(patient.getPhoneNumber());
                        }
                        if (etEmail != null) etEmail.setText(sessionManager.getEmail());
                    }

                    @Override
                    public void onNotFound() {
                        if (etEmail != null) etEmail.setText(sessionManager.getEmail());
                    }

                    @Override
                    public void onError(String message) {
                        if (etEmail != null) etEmail.setText(sessionManager.getEmail());
                    }
                }
        );
    }

    private void logout() {
        sessionManager.clearSession();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(PatientProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}