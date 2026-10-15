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
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.BillingRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class PatientBillingActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private BillingRepository billingRepository;

    private TextView tvPatientBillingInfo;
    private TextView tvPatientBillingStatus;
    private LinearLayout layoutPatientBillingRecords;

    private String patientId;

    private int pendingBillingRequests = 0;
    private int billingRecordCount = 0;


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
                R.layout.activity_patient_billing
        );


        tvPatientBillingInfo =
                findViewById(R.id.tvPatientBillingInfo);

        tvPatientBillingStatus =
                findViewById(R.id.tvPatientBillingStatus);

        layoutPatientBillingRecords =
                findViewById(R.id.layoutPatientBillingRecords);


        patientRepository =
                new PatientRepository();

        appointmentRepository =
                new AppointmentRepository();

        billingRepository =
                new BillingRepository();


        loadPatient();
    }


    // ---------------------------------------------------------
    // 1. Load logged-in patient's Patient record
    // ---------------------------------------------------------

    private void loadPatient() {

        tvPatientBillingStatus.setText(
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

                        tvPatientBillingInfo.setText(
                                patientName
                        );

                        loadAppointments();
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientBillingStatus.setText(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(String message) {

                        tvPatientBillingStatus.setText(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientBillingActivity.this,
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

        tvPatientBillingStatus.setText(
                "Loading appointments..."
        );

        layoutPatientBillingRecords.removeAllViews();


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

                            tvPatientBillingStatus.setText(
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

                        tvPatientBillingStatus.setText(
                                "Unable to load appointments."
                        );

                        Toast.makeText(
                                PatientBillingActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 3. Load billing for each patient appointment
    // ---------------------------------------------------------

    private void loadBillingForAppointments(
            List<Appointment> appointments
    ) {

        layoutPatientBillingRecords.removeAllViews();

        billingRecordCount = 0;
        pendingBillingRequests = appointments.size();

        tvPatientBillingStatus.setText(
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

                                    billingRecordCount++;

                                    displayBillingRecord(
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
                             * One failed appointment billing request
                             * should not prevent the other billing
                             * records from being displayed.
                             */
                            billingRequestFinished();
                        }
                    }
            );
        }
    }


    // ---------------------------------------------------------
    // 4. Track when all billing requests have finished
    // ---------------------------------------------------------

    private void billingRequestFinished() {

        pendingBillingRequests--;


        if (pendingBillingRequests > 0) {
            return;
        }


        if (billingRecordCount == 0) {

            tvPatientBillingStatus.setText(
                    "No billing records found."
            );

        } else {

            tvPatientBillingStatus.setText(
                    billingRecordCount
                            + " billing record(s)"
            );
        }
    }


    // ---------------------------------------------------------
    // 5. Display read-only billing card
    // ---------------------------------------------------------

    private void displayBillingRecord(
            Appointment appointment,
            Billing billing
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
                "Billing Record"
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
                "Appointment Time: "
                        + safeText(
                        appointment.getStartTime()
                )
        );


        addText(
                content,
                "Amount: "
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


        double patientBalance =
                Math.max(
                        billing.getAmount()
                                - billing.getInsuranceCoverage(),
                        0
                );


        addText(
                content,
                "Patient Balance: "
                        + formatCurrency(
                        patientBalance
                )
        );


        addText(
                content,
                "Payment Status: "
                        + formatStatus(
                        billing.getPaymentStatus()
                )
        );


        card.addView(content);

        layoutPatientBillingRecords.addView(card);
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
