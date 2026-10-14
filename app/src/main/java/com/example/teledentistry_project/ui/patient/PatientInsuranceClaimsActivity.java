package com.example.teledentistry_project.ui.patient;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.billing.Billing;
import com.example.teledentistry_project.data.model.insurance.InsuranceClaim;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.BillingRepository;
import com.example.teledentistry_project.data.repository.InsuranceClaimRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class PatientInsuranceClaimsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private BillingRepository billingRepository;
    private InsuranceClaimRepository insuranceClaimRepository;

    private TextView tvPatientClaimInfo;
    private TextView tvPatientClaimStatus;
    private LinearLayout layoutPatientInsuranceClaims;

    private String patientId;

    private int pendingBillingRequests = 0;
    private int pendingClaimRequests = 0;
    private int claimCount = 0;
    private boolean billingLoadingFinished = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

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
                R.layout.activity_patient_insurance_claims
        );


        tvPatientClaimInfo =
                findViewById(R.id.tvPatientClaimInfo);

        tvPatientClaimStatus =
                findViewById(R.id.tvPatientClaimStatus);

        layoutPatientInsuranceClaims =
                findViewById(R.id.layoutPatientInsuranceClaims);


        patientRepository =
                new PatientRepository();

        appointmentRepository =
                new AppointmentRepository();

        billingRepository =
                new BillingRepository();

        insuranceClaimRepository =
                new InsuranceClaimRepository();


        loadPatient();
    }


    // ---------------------------------------------------------
    // 1. Load logged-in Patient record
    // ---------------------------------------------------------

    private void loadPatient() {

        tvPatientClaimStatus.setText(
                "Loading patient information..."
        );


        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        patientId = patient.getId();

                        String patientName =
                                patient.getFullName();

                        if (patientName == null
                                || patientName.trim().isEmpty()) {

                            patientName = "Patient";
                        }

                        tvPatientClaimInfo.setText(
                                patientName
                        );

                        loadAppointments();
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientClaimStatus.setText(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(String message) {

                        tvPatientClaimStatus.setText(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientInsuranceClaimsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 2. Load patient's appointments
    // ---------------------------------------------------------

    private void loadAppointments() {

        tvPatientClaimStatus.setText(
                "Loading appointments..."
        );

        layoutPatientInsuranceClaims.removeAllViews();


        appointmentRepository.getPatientAppointments(
                sessionManager.getAuthToken(),
                patientId,
                new AppointmentRepository.AppointmentListCallback() {

                    @Override
                    public void onSuccess(
                            List<Appointment> appointments
                    ) {

                        if (appointments == null
                                || appointments.isEmpty()) {

                            tvPatientClaimStatus.setText(
                                    "No appointments found."
                            );

                            return;
                        }

                        loadBillingForAppointments(
                                appointments
                        );
                    }


                    @Override
                    public void onError(String message) {

                        tvPatientClaimStatus.setText(
                                "Unable to load appointments."
                        );

                        Toast.makeText(
                                PatientInsuranceClaimsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 3. Load billing records for every appointment
    // ---------------------------------------------------------

    private void loadBillingForAppointments(
            List<Appointment> appointments
    ) {

        claimCount = 0;
        pendingClaimRequests = 0;
        pendingBillingRequests = appointments.size();
        billingLoadingFinished = false;

        tvPatientClaimStatus.setText(
                "Loading billing records..."
        );


        for (Appointment appointment : appointments) {

            billingRepository.getBillingByAppointment(
                    sessionManager.getAuthToken(),
                    appointment.getId(),
                    new BillingRepository.BillingListCallback() {

                        @Override
                        public void onSuccess(
                                List<Billing> billingRecords
                        ) {

                            if (billingRecords != null) {

                                for (Billing billing
                                        : billingRecords) {

                                    pendingClaimRequests++;

                                    loadClaimsForBilling(
                                            appointment,
                                            billing
                                    );
                                }
                            }

                            billingRequestFinished();
                        }


                        @Override
                        public void onError(String message) {

                            /*
                             * Allow other billing records
                             * to continue loading.
                             */
                            billingRequestFinished();
                        }
                    }
            );
        }
    }


    // ---------------------------------------------------------
    // 4. Load claims belonging to a billing record
    // ---------------------------------------------------------

    private void loadClaimsForBilling(
            Appointment appointment,
            Billing billing
    ) {

        insuranceClaimRepository.getClaimsByBilling(
                sessionManager.getAuthToken(),
                billing.getId(),
                new InsuranceClaimRepository.InsuranceClaimListCallback() {

                    @Override
                    public void onSuccess(
                            List<InsuranceClaim> claims
                    ) {

                        if (claims != null) {

                            for (InsuranceClaim claim : claims) {

                                claimCount++;

                                displayClaim(
                                        appointment,
                                        billing,
                                        claim
                                );
                            }
                        }

                        claimRequestFinished();
                    }


                    @Override
                    public void onError(String message) {

                        /*
                         * One failed claim request should
                         * not stop the remaining claims.
                         */
                        claimRequestFinished();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 5. Track billing requests
    // ---------------------------------------------------------

    private void billingRequestFinished() {

        pendingBillingRequests--;

        if (pendingBillingRequests <= 0) {

            billingLoadingFinished = true;

            updateFinalStatus();
        }
    }


    // ---------------------------------------------------------
    // 6. Track insurance claim requests
    // ---------------------------------------------------------

    private void claimRequestFinished() {

        pendingClaimRequests--;

        updateFinalStatus();
    }


    // ---------------------------------------------------------
    // 7. Update loading / empty state
    // ---------------------------------------------------------

    private void updateFinalStatus() {

        if (!billingLoadingFinished) {
            return;
        }

        if (pendingClaimRequests > 0) {

            tvPatientClaimStatus.setText(
                    "Loading insurance claims..."
            );

            return;
        }


        if (claimCount == 0) {

            tvPatientClaimStatus.setText(
                    "No insurance claims found."
            );

        } else {

            tvPatientClaimStatus.setText(
                    claimCount
                            + " insurance claim(s)"
            );
        }
    }


    // ---------------------------------------------------------
    // 8. Display read-only claim
    // ---------------------------------------------------------

    private void displayClaim(
            Appointment appointment,
            Billing billing,
            InsuranceClaim claim
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

        card.setLayoutParams(cardParams);

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
                "Insurance Claim"
        );

        tvTitle.setTextSize(18);

        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(tvTitle);


        addText(
                content,
                "Appointment Date: "
                        + formatDate(
                        appointment.getDateTime()
                )
        );


        addText(
                content,
                "Billing Amount: "
                        + formatCurrency(
                        billing.getAmount()
                )
        );


        addText(
                content,
                "Insurance Coverage: "
                        + formatCurrency(
                        billing.getInsuranceCoverage()
                )
        );


        addText(
                content,
                "Policy Number: "
                        + safeText(
                        claim.getPolicyNumber()
                )
        );


        addText(
                content,
                "Claim Status: "
                        + formatStatus(
                        claim.getClaimStatus()
                )
        );


        card.addView(content);

        layoutPatientInsuranceClaims.addView(card);
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

        layout.addView(textView);
    }


    private String safeText(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not specified";
        }

        return value;
    }


    private String formatDate(String date) {

        if (date == null
                || date.trim().isEmpty()) {

            return "Not specified";
        }

        if (date.length() >= 10) {
            return date.substring(0, 10);
        }

        return date;
    }


    private String formatCurrency(double amount) {

        return String.format(
                Locale.US,
                "R %.2f",
                amount
        );
    }


    private String formatStatus(String status) {

        if (status == null
                || status.trim().isEmpty()) {

            return "UNKNOWN";
        }

        return status
                .replace("_", " ")
                .toUpperCase(Locale.US);
    }
}
