package com.example.teledentistry_project.ui.patient;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.data.repository.PrescriptionRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PatientPrescriptionsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private PrescriptionRepository prescriptionRepository;

    private TextView tvPatientPrescriptionInfo;
    private TextView tvPatientPrescriptionStatus;
    private LinearLayout layoutPatientPrescriptions;

    private String patientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Must be logged in
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Patient-only screen
        if (!"patient".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(
                R.layout.activity_patient_prescriptions
        );


        tvPatientPrescriptionInfo =
                findViewById(
                        R.id.tvPatientPrescriptionInfo
                );

        tvPatientPrescriptionStatus =
                findViewById(
                        R.id.tvPatientPrescriptionStatus
                );

        layoutPatientPrescriptions =
                findViewById(
                        R.id.layoutPatientPrescriptions
                );


        patientRepository =
                new PatientRepository();

        prescriptionRepository =
                new PrescriptionRepository();


        loadPatient();
    }


    // ---------------------------------------------------------
    // 1. Find logged-in patient's Patient record
    // ---------------------------------------------------------

    private void loadPatient() {

        tvPatientPrescriptionStatus.setText(
                "Loading patient information..."
        );


        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(
                            Patient patient
                    ) {

                        patientId =
                                patient.getId();


                        String patientName =
                                patient.getFullName();

                        if (patientName == null
                                || patientName.trim().isEmpty()) {

                            patientName =
                                    "Patient";
                        }


                        tvPatientPrescriptionInfo.setText(
                                patientName
                        );


                        loadPrescriptions();
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientPrescriptionStatus.setText(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientPrescriptionStatus.setText(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientPrescriptionsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 2. Load prescriptions belonging to patient
    // ---------------------------------------------------------

    private void loadPrescriptions() {

        tvPatientPrescriptionStatus.setText(
                "Loading prescriptions..."
        );

        layoutPatientPrescriptions.removeAllViews();


        prescriptionRepository.getPrescriptionsByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new PrescriptionRepository.PrescriptionListCallback() {

                    @Override
                    public void onSuccess(
                            List<Prescription> prescriptions
                    ) {

                        layoutPatientPrescriptions.removeAllViews();


                        if (prescriptions == null
                                || prescriptions.isEmpty()) {

                            tvPatientPrescriptionStatus.setText(
                                    "No prescriptions found."
                            );

                            return;
                        }


                        tvPatientPrescriptionStatus.setText(
                                prescriptions.size()
                                        + " prescription(s)"
                        );


                        for (Prescription prescription
                                : prescriptions) {

                            displayPrescription(
                                    prescription
                            );
                        }
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientPrescriptionStatus.setText(
                                "Unable to load prescriptions."
                        );

                        Toast.makeText(
                                PatientPrescriptionsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 3. Display read-only prescription card
    // ---------------------------------------------------------

    private void displayPrescription(
            Prescription prescription
    ) {

        MaterialCardView card =
                new MaterialCardView(this);


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                24
        );

        card.setLayoutParams(
                cardParams
        );

        card.setRadius(16);
        card.setCardElevation(4);


        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                32,
                32,
                32,
                32
        );


        TextView tvTitle =
                new TextView(this);

        tvTitle.setText(
                "Prescription"
        );

        tvTitle.setTextSize(18);

        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(
                tvTitle
        );


        addText(
                content,
                "Medication: "
                        + safeText(
                        prescription.getMedication()
                )
        );


        addText(
                content,
                "Dosage: "
                        + safeText(
                        prescription.getDosage()
                )
        );


        addText(
                content,
                "Issue Date: "
                        + formatDate(
                        prescription.getIssueDate()
                )
        );


        addText(
                content,
                "Expiry Date: "
                        + formatDate(
                        prescription.getExpiryDate()
                )
        );


        card.addView(
                content
        );

        layoutPatientPrescriptions.addView(
                card
        );
    }


    private void addText(
            LinearLayout layout,
            String text
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(15);

        textView.setPadding(
                0,
                10,
                0,
                0
        );

        layout.addView(
                textView
        );
    }


    private String safeText(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not specified";
        }

        return value;
    }


    private String formatDate(
            String date
    ) {

        if (date == null
                || date.trim().isEmpty()) {

            return "Not specified";
        }

        // PocketBase may return a full date/time string.
        // For the UI we only need YYYY-MM-DD.
        if (date.length() >= 10) {
            return date.substring(0, 10);
        }

        return date;
    }
}
