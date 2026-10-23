package com.example.teledentistry_project.ui.dentist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.ui.patient.VideoCallActivity;
import com.example.teledentistry_project.utils.SessionManager;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.PractitionerRepository;

public class DentistHomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerRepository practitionerRepository;

    private TextView tvPractitionerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create SessionManager
        sessionManager = new SessionManager(this);

        /*
         * AUTHENTICATION CHECK
         *
         * User must have an active session.
         */
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        /*
         * AUTHORIZATION / RBAC CHECK
         *
         * Only practitioners may access this Activity.
         */
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "You are not authorized to access the Practitioner Portal.",
                    Toast.LENGTH_LONG
            ).show();

            sessionManager.clearSession();
            redirectToLogin();
            return;
        }

        /*
         * Authentication + authorization passed.
         */
        setContentView(R.layout.activity_dentist_home);

        practitionerRepository = new PractitionerRepository();

        tvPractitionerName = findViewById(R.id.tvPractitionerName);

        loadPractitionerInformation();

        initializeNavigation();
    }

    //Updates practitoner name when changed
    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadPractitionerInformation();
        }
    }

    private void loadPractitionerInformation() {

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        practitionerRepository.getPractitionerByUserId(
                token,
                userId,
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {
                        String status = practitioner.getStatus();
                        if (status != null && ("pending".equalsIgnoreCase(status)
                                || "rejected".equalsIgnoreCase(status)
                                || "suspended".equalsIgnoreCase(status))) {
                            Toast.makeText(
                                    DentistHomeActivity.this,
                                    "Account Status: " + status.toUpperCase() + ". Access restricted. Please contact Administrator.",
                                    Toast.LENGTH_LONG
                            ).show();
                            sessionManager.clearSession();
                            redirectToLogin();
                            return;
                        }

                        String fullName = practitioner.getFullName();

                        if (fullName != null && !fullName.trim().isEmpty()) {
                            tvPractitionerName.setText(fullName);
                        } else {
                            tvPractitionerName.setText("Practitioner");
                        }
                    }

                    @Override
                    public void onNotFound() {

                        tvPractitionerName.setText("Practitioner");

                        Toast.makeText(
                                DentistHomeActivity.this,
                                "Practitioner profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        tvPractitionerName.setText("Practitioner");

                        Toast.makeText(
                                DentistHomeActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }



    private void initializeNavigation() {

        Button btnStartConsultation =
                findViewById(R.id.btnStartConsultation);

        Button btnViewAppointments =
                findViewById(R.id.btnViewAppointments);

        Button btnPatientRecords =
                findViewById(R.id.btnPatientRecords);

        Button btnUploadedImages =
                findViewById(R.id.btnUploadedImages);

        Button btnTreatmentManagement =
                findViewById(R.id.btnTreatmentManagement);

        Button btnProfile =
                findViewById(R.id.btnDentistProfile);

        Button btnManageAvailability =
                findViewById(R.id.btnManageAvailability);

        Button btnDentistLogout =
                findViewById(R.id.btnDentistLogout);


        // VIDEO CONSULTATION
        btnStartConsultation.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                DentistHomeActivity.this,
                                VideoCallActivity.class
                        )
                )
        );


        // APPOINTMENTS
        btnViewAppointments.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                DentistHomeActivity.this,
                                PractitionerAppointmentsActivity.class
                        )
                )
        );


        // PATIENT RECORDS
        btnPatientRecords.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                DentistHomeActivity.this,
                                PatientRecordsActivity.class
                        )
                )
        );


        // UPLOADED IMAGES
        btnUploadedImages.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                DentistHomeActivity.this,
                                UploadedImagesActivity.class
                        )
                )
        );


        // TREATMENT MANAGEMENT
        btnTreatmentManagement.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                DentistHomeActivity.this,
                                TreatmentManagementActivity.class
                        )
                )
        );


        // Profile
        btnProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DentistHomeActivity.this,
                    DentistProfileActivity.class
            );

            startActivity(intent);
        });

        // Manage Availability
        btnManageAvailability.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DentistHomeActivity.this,
                    PractitionerAvailabilityActivity.class
            );

            startActivity(intent);
        });

        // Logout
        btnDentistLogout.setOnClickListener(v ->
                logoutPractitioner()
        );
    }


    private void logoutPractitioner() {

        // Remove saved PocketBase session
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
                DentistHomeActivity.this,
                LoginActivity.class
        );

        /*
         * Clear the Activity stack so pressing Back
         * cannot return to the Practitioner Portal.
         */
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}