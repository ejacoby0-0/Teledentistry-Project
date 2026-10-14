package com.example.teledentistry_project.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.utils.SessionManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.auth.LoginActivity;


public class PatientHomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PatientRepository patientRepository;

    private TextView tvPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // 1. Check login
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // 2. Check role
        if (!"patient".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "You are not authorized to access the Patient Portal.",
                    Toast.LENGTH_LONG
            ).show();

            sessionManager.clearSession();
            redirectToLogin();
            return;
        }

        // 3. Load XML FIRST
        setContentView(R.layout.activity_patient_home);

        // 4. Create repository
        patientRepository = new PatientRepository();

        // 5. Find TextView AFTER setContentView
        tvPatientName = findViewById(R.id.tvPatientName);

        // 6. Navigation
        initializeNavigation();

        // 7. Load PocketBase patient
        loadPatientName();
    }

    //Update the name changes
    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadPatientName();
        }
    }

    private void initializeNavigation() {

        Button btnProfile =
                findViewById(R.id.btnProfile);

        Button btnJoinCall =
                findViewById(R.id.btnJoinCall);

        Button btnBookAppointment =
                findViewById(R.id.btnBookAppointment);

        Button btnAppointments =
                findViewById(R.id.btnAppointments);

        Button btnDentalRecord =
                findViewById(R.id.btnDentalRecord);

        Button btnChatDentist =
                findViewById(R.id.btnChatDentist);

        Button btnUploadImage =
                findViewById(R.id.btnUploadImage);

        Button btnTreatmentPlan =
                findViewById(R.id.btnTreatmentPlan);

        Button btnPrescriptions =
                findViewById(R.id.btnPrescriptions);

        Button btnBilling =
                findViewById(R.id.btnBilling);

        Button btnInsuranceClaims =
                findViewById(R.id.btnInsuranceClaims);


        /*
         * PROFILE
         */
        btnProfile.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientProfileActivity.class
                        )
                )
        );


        /*
         * VIDEO CONSULTATION
         */
        btnJoinCall.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                VideoCallActivity.class
                        )
                )
        );


        /*
         * BOOK APPOINTMENT
         */
        btnBookAppointment.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                BookAppointmentActivity.class
                        )
                )
        );

        /*
         * VIEWING APPOINTMENT
         */
        btnAppointments.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PatientHomeActivity.this,
                    PatientAppointmentsActivity.class
            );

            startActivity(intent);
        });

        /*
         * MY DENTAL RECORD
         */
        btnDentalRecord.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientDentalRecordActivity.class
                        )
                )
        );


        /*
         * CHAT
         */
        btnChatDentist.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                ChatActivity.class
                        )
                )
        );


        /*
         * UPLOAD DENTAL IMAGE
         */
        btnUploadImage.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                UploadImageActivity.class
                        )
                )
        );


        /*
         * MY TREATMENT PLANS
         */
        btnTreatmentPlan.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientTreatmentPlanActivity.class
                        )
                )
        );

        /*
         * MY PRESCRIPTIONS
         */
        btnPrescriptions.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientPrescriptionsActivity.class
                        )
                )
        );



        /*
         * MY BILLING
         */
        btnBilling.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientBillingActivity.class
                        )
                )
        );

        /*
         * MY INSURANCE CLAIMS
         */
        btnInsuranceClaims.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                PatientHomeActivity.this,
                                PatientInsuranceClaimsActivity.class
                        )
                )
        );
    }

    private void redirectToLogin() {

        Intent intent = new Intent(
                PatientHomeActivity.this,
                LoginActivity.class
        );

        /*
         * Clear previous Activities so the user
         * cannot press Back to return to a
         * protected screen.
         */
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void loadPatientName() {

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        patientRepository.getPatientByUserId(
                token,
                userId,
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        String fullName = patient.getFullName();

                        if (fullName != null && !fullName.trim().isEmpty()) {
                            tvPatientName.setText(fullName);
                        } else {
                            tvPatientName.setText("Patient");
                        }
                    }

                    @Override
                    public void onNotFound() {

                        tvPatientName.setText("Patient");
                    }

                    @Override
                    public void onError(String message) {

                        tvPatientName.setText("Patient");

                        Toast.makeText(
                                PatientHomeActivity.this,
                                "Unable to load patient information.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                }
        );
    }


}