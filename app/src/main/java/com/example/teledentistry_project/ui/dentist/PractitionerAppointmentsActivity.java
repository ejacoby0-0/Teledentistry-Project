package com.example.teledentistry_project.ui.dentist;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import android.content.Intent;

import com.example.teledentistry_project.ui.consultation.ConsultationActivity;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;

import com.google.android.material.card.MaterialCardView;

public class PractitionerAppointmentsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PractitionerRepository practitionerRepository;
    private AppointmentRepository appointmentRepository;

    private LinearLayout layoutAppointments;
    private TextView tvNoAppointments;

    private String practitionerId;

    private final List<Appointment> appointmentList =
            new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication check
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Practitioner RBAC check
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(
                R.layout.activity_practitioner_appointments
        );

        practitionerRepository =
                new PractitionerRepository();

        appointmentRepository =
                new AppointmentRepository();

        layoutAppointments =
                findViewById(R.id.layoutAppointments);

        tvNoAppointments =
                findViewById(R.id.tvNoAppointments);

        loadPractitioner();
    }


    private void loadPractitioner() {

        practitionerRepository.getPractitionerByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(
                            Practitioner practitioner
                    ) {

                        practitionerId =
                                practitioner.getId();

                        loadAppointments();
                    }

                    @Override
                    public void onNotFound() {

                        tvNoAppointments.setText(
                                "Practitioner profile not found."
                        );

                        tvNoAppointments.setVisibility(
                                TextView.VISIBLE
                        );
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PractitionerAppointmentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadAppointments() {

        if (practitionerId == null || practitionerId.isEmpty()) {
            return;
        }

        appointmentRepository.getPractitionerAppointments(
                sessionManager.getAuthToken(),
                practitionerId,
                new AppointmentRepository.AppointmentListCallback() {

                    @Override
                    public void onSuccess(
                            List<Appointment> appointments
                    ) {

                        appointmentList.clear();

                        if (appointments != null) {
                            appointmentList.addAll(appointments);
                        }

                        displayAppointments();
                    }

                    @Override
                    public void onError(String message) {

                        tvNoAppointments.setText(
                                "Unable to load appointments."
                        );

                        tvNoAppointments.setVisibility(
                                TextView.VISIBLE
                        );

                        Toast.makeText(
                                PractitionerAppointmentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayAppointments() {

        layoutAppointments.removeAllViews();

        if (appointmentList.isEmpty()) {

            tvNoAppointments.setText("No appointments found.");
            tvNoAppointments.setVisibility(View.VISIBLE);

            return;
        }

        tvNoAppointments.setVisibility(View.GONE);

        for (Appointment appointment : appointmentList) {

            // Card
            MaterialCardView cardView =
                    new MaterialCardView(this);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(0, 0, 0, 24);
            cardView.setLayoutParams(cardParams);

            cardView.setRadius(16);
            cardView.setCardElevation(4);


            // Content inside card
            LinearLayout cardContent =
                    new LinearLayout(this);

            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(32, 32, 32, 32);


            // Date
            TextView tvDate = new TextView(this);

            tvDate.setText(
                    formatDate(appointment.getDateTime())
            );

            tvDate.setTextSize(18);
            tvDate.setTypeface(null, Typeface.BOLD);

            cardContent.addView(tvDate);


            // Time
            TextView tvTime = new TextView(this);

            tvTime.setText(
                    appointment.getStartTime()
                            + " - "
                            + appointment.getEndTime()
            );

            tvTime.setTextSize(16);

            cardContent.addView(tvTime);


            // Status
            TextView tvStatus = new TextView(this);

            tvStatus.setText(
                    "Status: "
                            + appointment.getStatus().toUpperCase()
            );

            tvStatus.setTextSize(15);

            LinearLayout.LayoutParams statusParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            statusParams.setMargins(0, 16, 0, 16);

            tvStatus.setLayoutParams(statusParams);

            cardContent.addView(tvStatus);


            // Only pending appointments can be reviewed
            if ("pending".equalsIgnoreCase(
                    appointment.getStatus()
            )) {

                LinearLayout buttonLayout =
                        new LinearLayout(this);

                buttonLayout.setOrientation(
                        LinearLayout.HORIZONTAL
                );


                Button btnConfirm =
                        new Button(this);

                btnConfirm.setText("Confirm");


                Button btnCancel =
                        new Button(this);

                btnCancel.setText("Cancel");


                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );

                buttonParams.setMargins(
                        0,
                        0,
                        8,
                        0
                );

                btnConfirm.setLayoutParams(
                        buttonParams
                );


                LinearLayout.LayoutParams cancelParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        );

                cancelParams.setMargins(
                        8,
                        0,
                        0,
                        0
                );

                btnCancel.setLayoutParams(
                        cancelParams
                );


                btnConfirm.setOnClickListener(v ->
                        updateAppointmentStatus(
                                appointment,
                                "confirmed"
                        )
                );


                btnCancel.setOnClickListener(v ->
                        updateAppointmentStatus(
                                appointment,
                                "cancelled"
                        )
                );


                buttonLayout.addView(
                        btnConfirm
                );

                buttonLayout.addView(
                        btnCancel
                );

                cardContent.addView(
                        buttonLayout
                );
            }
            // Confirmed appointments can be completed
            if ("confirmed".equalsIgnoreCase(
                    appointment.getStatus()
            )) {

                // Open Consultation button
                Button btnConsultation =
                        new Button(this);

                btnConsultation.setText(
                        "Open Consultation"
                );

                LinearLayout.LayoutParams consultationParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                consultationParams.setMargins(
                        0,
                        8,
                        0,
                        8
                );

                btnConsultation.setLayoutParams(
                        consultationParams
                );

                btnConsultation.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    PractitionerAppointmentsActivity.this,
                                    ConsultationActivity.class
                            );

                    intent.putExtra(
                            "appointmentId",
                            appointment.getId()
                    );

                    intent.putExtra(
                            "appointmentDate",
                            appointment.getDateTime()
                    );

                    intent.putExtra(
                            "appointmentStartTime",
                            appointment.getStartTime()
                    );

                    intent.putExtra(
                            "appointmentEndTime",
                            appointment.getEndTime()
                    );

                    startActivity(intent);
                });

                cardContent.addView(
                        btnConsultation
                );


                // Complete Appointment button
                Button btnComplete =
                        new Button(this);

                btnComplete.setText(
                        "Complete Appointment"
                );

                LinearLayout.LayoutParams completeParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                completeParams.setMargins(
                        0,
                        8,
                        0,
                        0
                );

                btnComplete.setLayoutParams(
                        completeParams
                );

                btnComplete.setOnClickListener(v ->
                        updateAppointmentStatus(
                                appointment,
                                "completed"
                        )
                );

                cardContent.addView(
                        btnComplete
                );
            }


            cardView.addView(
                    cardContent
            );

            layoutAppointments.addView(
                    cardView
            );
        }
    }

    private void updateAppointmentStatus(
            Appointment appointment,
            String newStatus
    ) {

        appointmentRepository.updateAppointmentStatus(
                sessionManager.getAuthToken(),
                appointment.getId(),
                newStatus,
                new AppointmentRepository.AppointmentUpdateCallback() {

                    @Override
                    public void onSuccess(
                            Appointment updatedAppointment
                    ) {

                        Toast.makeText(
                                PractitionerAppointmentsActivity.this,
                                "Appointment "
                                        + newStatus
                                        + ".",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadAppointments();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                PractitionerAppointmentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String formatDate(String dateTime) {

        if (dateTime == null || dateTime.isEmpty()) {
            return "Unknown date";
        }

        if (dateTime.length() >= 10) {
            return dateTime.substring(0, 10);
        }

        return dateTime;
    }
}
