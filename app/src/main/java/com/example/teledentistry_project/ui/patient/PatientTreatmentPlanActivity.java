package com.example.teledentistry_project.ui.patient;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlan;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.data.repository.TreatmentPlanRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class PatientTreatmentPlanActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private TreatmentPlanRepository treatmentPlanRepository;

    private TextView tvPatientTreatmentInfo;
    private TextView tvPatientTreatmentStatus;
    private LinearLayout layoutPatientTreatmentPlans;

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
                R.layout.activity_patient_treatment_plan
        );


        tvPatientTreatmentInfo =
                findViewById(
                        R.id.tvPatientTreatmentInfo
                );

        tvPatientTreatmentStatus =
                findViewById(
                        R.id.tvPatientTreatmentStatus
                );

        layoutPatientTreatmentPlans =
                findViewById(
                        R.id.layoutPatientTreatmentPlans
                );


        patientRepository =
                new PatientRepository();

        treatmentPlanRepository =
                new TreatmentPlanRepository();


        loadPatient();
    }


    // ---------------------------------------------------------
    // 1. Find logged-in patient's Patient record
    // ---------------------------------------------------------

    private void loadPatient() {

        tvPatientTreatmentStatus.setText(
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


                        tvPatientTreatmentInfo.setText(
                                patientName
                        );


                        loadTreatmentPlans();
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientTreatmentStatus.setText(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientTreatmentStatus.setText(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientTreatmentPlanActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 2. Load treatment plans belonging to this patient
    // ---------------------------------------------------------

    private void loadTreatmentPlans() {

        tvPatientTreatmentStatus.setText(
                "Loading treatment plans..."
        );

        layoutPatientTreatmentPlans.removeAllViews();


        treatmentPlanRepository.getTreatmentPlansByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new TreatmentPlanRepository.TreatmentPlanListCallback() {

                    @Override
                    public void onSuccess(
                            List<TreatmentPlan> treatmentPlans
                    ) {

                        layoutPatientTreatmentPlans.removeAllViews();


                        if (treatmentPlans == null
                                || treatmentPlans.isEmpty()) {

                            tvPatientTreatmentStatus.setText(
                                    "No treatment plans found."
                            );

                            return;
                        }


                        tvPatientTreatmentStatus.setText(
                                treatmentPlans.size()
                                        + " treatment plan(s)"
                        );


                        for (TreatmentPlan treatmentPlan
                                : treatmentPlans) {

                            displayTreatmentPlan(
                                    treatmentPlan
                            );
                        }
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientTreatmentStatus.setText(
                                "Unable to load treatment plans."
                        );

                        Toast.makeText(
                                PatientTreatmentPlanActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 3. Display treatment plan as a read-only card
    // ---------------------------------------------------------

    private void displayTreatmentPlan(
            TreatmentPlan treatmentPlan
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
                "Treatment Plan"
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
                "Start Date: "
                        + safeText(
                        treatmentPlan.getStartDate()
                )
        );


        addText(
                content,
                "Total Cost: "
                        + formatCurrency(
                        treatmentPlan.getTotalCost()
                )
        );


        addText(
                content,
                "Status: "
                        + formatStatus(
                        treatmentPlan.getStatus()
                )
        );


        card.addView(
                content
        );

        layoutPatientTreatmentPlans.addView(
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


    private String formatStatus(
            String status
    ) {

        if (status == null
                || status.trim().isEmpty()) {

            return "UNKNOWN";
        }

        return status
                .replace("_", " ")
                .toUpperCase(Locale.US);
    }


    private String formatCurrency(
            double amount
    ) {

        return String.format(
                Locale.US,
                "R %.2f",
                amount
        );
    }
}