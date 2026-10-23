package com.example.teledentistry_project.ui.dentist;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.ui.consultation.ConsultationActivity;
import com.example.teledentistry_project.ui.patient.ChatActivity;
import com.example.teledentistry_project.ui.patient.VideoCallActivity;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class PractitionerAppointmentsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PractitionerRepository practitionerRepository;
    private AppointmentRepository appointmentRepository;

    private LinearLayout layoutAppointments;
    private TextView tvNoAppointments;

    private String practitionerId;
    private String currentFilter = "ALL";

    private final List<Appointment> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        if (!"practitioner".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access denied.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_practitioner_appointments);

        practitionerRepository = new PractitionerRepository();
        appointmentRepository = new AppointmentRepository();

        layoutAppointments = findViewById(R.id.layoutAppointments);
        tvNoAppointments = findViewById(R.id.tvNoAppointments);

        setupFilterButtons();
        loadPractitioner();
    }

    private void setupFilterButtons() {
        Button btnAll = findViewById(R.id.btnFilterAll);
        Button btnPending = findViewById(R.id.btnFilterPending);
        Button btnConfirmed = findViewById(R.id.btnFilterConfirmed);
        Button btnCompleted = findViewById(R.id.btnFilterCompleted);
        Button btnCancelled = findViewById(R.id.btnFilterCancelled);

        if (btnAll != null) btnAll.setOnClickListener(v -> applyFilter("ALL"));
        if (btnPending != null) btnPending.setOnClickListener(v -> applyFilter("pending"));
        if (btnConfirmed != null) btnConfirmed.setOnClickListener(v -> applyFilter("confirmed"));
        if (btnCompleted != null) btnCompleted.setOnClickListener(v -> applyFilter("completed"));
        if (btnCancelled != null) btnCancelled.setOnClickListener(v -> applyFilter("cancelled"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        displayAppointments();
    }

    private void loadPractitioner() {
        practitionerRepository.getPractitionerByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {
                        practitionerId = practitioner.getId();
                        loadAppointments();
                    }

                    @Override
                    public void onNotFound() {
                        tvNoAppointments.setText("Practitioner profile not found.");
                        tvNoAppointments.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PractitionerAppointmentsActivity.this, message, Toast.LENGTH_LONG).show();
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
                    public void onSuccess(List<Appointment> appointments) {
                        appointmentList.clear();
                        if (appointments != null) {
                            appointmentList.addAll(appointments);
                        }
                        displayAppointments();
                    }

                    @Override
                    public void onError(String message) {
                        tvNoAppointments.setText("Unable to load appointments.");
                        tvNoAppointments.setVisibility(View.VISIBLE);
                        Toast.makeText(PractitionerAppointmentsActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void displayAppointments() {
        layoutAppointments.removeAllViews();

        List<Appointment> filtered = new ArrayList<>();
        for (Appointment appt : appointmentList) {
            if ("ALL".equalsIgnoreCase(currentFilter)
                    || (appt.getStatus() != null && appt.getStatus().equalsIgnoreCase(currentFilter))) {
                filtered.add(appt);
            }
        }

        if (filtered.isEmpty()) {
            tvNoAppointments.setText("No " + ("ALL".equals(currentFilter) ? "" : currentFilter) + " appointments found.");
            tvNoAppointments.setVisibility(View.VISIBLE);
            return;
        }

        tvNoAppointments.setVisibility(View.GONE);

        for (Appointment appointment : filtered) {
            MaterialCardView cardView = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(16);
            cardView.setCardElevation(4);

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(32, 32, 32, 32);

            TextView tvDate = new TextView(this);
            tvDate.setText(formatDate(appointment.getDateTime()));
            tvDate.setTextSize(18);
            tvDate.setTypeface(null, Typeface.BOLD);
            cardContent.addView(tvDate);

            TextView tvTime = new TextView(this);
            tvTime.setText(appointment.getStartTime() + " - " + appointment.getEndTime());
            tvTime.setTextSize(16);
            cardContent.addView(tvTime);

            TextView tvStatus = new TextView(this);
            String statusStr = appointment.getStatus() != null ? appointment.getStatus().toUpperCase() : "UNKNOWN";
            tvStatus.setText("Status: " + statusStr);
            tvStatus.setTextSize(15);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            statusParams.setMargins(0, 12, 0, 16);
            tvStatus.setLayoutParams(statusParams);
            cardContent.addView(tvStatus);

            if ("pending".equalsIgnoreCase(appointment.getStatus())) {
                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                Button btnConfirm = new Button(this);
                btnConfirm.setText("Approve & Confirm");
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                buttonParams.setMargins(0, 0, 8, 0);
                btnConfirm.setLayoutParams(buttonParams);

                Button btnCancel = new Button(this);
                btnCancel.setText("Reject");
                LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                cancelParams.setMargins(8, 0, 0, 0);
                btnCancel.setLayoutParams(cancelParams);

                btnConfirm.setOnClickListener(v -> updateAppointmentStatus(appointment, "confirmed"));
                btnCancel.setOnClickListener(v -> updateAppointmentStatus(appointment, "cancelled"));

                buttonLayout.addView(btnConfirm);
                buttonLayout.addView(btnCancel);
                cardContent.addView(buttonLayout);

            } else if ("confirmed".equalsIgnoreCase(appointment.getStatus())) {
                LinearLayout callConsultLayout = new LinearLayout(this);
                callConsultLayout.setOrientation(LinearLayout.HORIZONTAL);

                Button btnVideoCall = new Button(this);
                btnVideoCall.setText("Video Call");
                LinearLayout.LayoutParams callParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                callParams.setMargins(0, 0, 6, 0);
                btnVideoCall.setLayoutParams(callParams);
                btnVideoCall.setOnClickListener(v -> {
                    Intent intent = new Intent(PractitionerAppointmentsActivity.this, VideoCallActivity.class);
                    intent.putExtra("appointment_id", appointment.getId());
                    startActivity(intent);
                });

                Button btnConsultation = new Button(this);
                btnConsultation.setText("Clinical Notes");
                LinearLayout.LayoutParams consultParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                consultParams.setMargins(6, 0, 0, 0);
                btnConsultation.setLayoutParams(consultParams);
                btnConsultation.setOnClickListener(v -> {
                    Intent intent = new Intent(PractitionerAppointmentsActivity.this, ConsultationActivity.class);
                    intent.putExtra("appointmentId", appointment.getId());
                    intent.putExtra("appointmentDate", appointment.getDateTime());
                    intent.putExtra("appointmentStartTime", appointment.getStartTime());
                    intent.putExtra("appointmentEndTime", appointment.getEndTime());
                    intent.putExtra("patientId", appointment.getPatient());
                    startActivity(intent);
                });

                callConsultLayout.addView(btnVideoCall);
                callConsultLayout.addView(btnConsultation);
                cardContent.addView(callConsultLayout);

                Button btnChat = new Button(this);
                btnChat.setText("Chat with Patient");
                LinearLayout.LayoutParams chatParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                chatParams.setMargins(0, 8, 0, 8);
                btnChat.setLayoutParams(chatParams);
                btnChat.setOnClickListener(v -> {
                    Intent intent = new Intent(PractitionerAppointmentsActivity.this, ChatActivity.class);
                    intent.putExtra("appointment_id", appointment.getId());
                    startActivity(intent);
                });
                cardContent.addView(btnChat);

                Button btnComplete = new Button(this);
                btnComplete.setText("Complete Appointment");
                LinearLayout.LayoutParams completeParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                completeParams.setMargins(0, 12, 0, 0);
                btnComplete.setLayoutParams(completeParams);
                btnComplete.setOnClickListener(v -> updateAppointmentStatus(appointment, "completed"));

                cardContent.addView(btnComplete);

            } else if ("completed".equalsIgnoreCase(appointment.getStatus())) {
                Button btnViewConsultation = new Button(this);
                btnViewConsultation.setText("View Consultation Record");
                LinearLayout.LayoutParams viewConsultationParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                viewConsultationParams.setMargins(0, 8, 0, 0);
                btnViewConsultation.setLayoutParams(viewConsultationParams);
                btnViewConsultation.setOnClickListener(v -> {
                    Intent intent = new Intent(PractitionerAppointmentsActivity.this, ConsultationActivity.class);
                    intent.putExtra("appointmentId", appointment.getId());
                    intent.putExtra("patientId", appointment.getPatient());
                    intent.putExtra("appointmentDate", appointment.getDateTime());
                    intent.putExtra("appointmentStartTime", appointment.getStartTime());
                    intent.putExtra("appointmentEndTime", appointment.getEndTime());
                    startActivity(intent);
                });

                cardContent.addView(btnViewConsultation);
            }

            cardView.addView(cardContent);
            layoutAppointments.addView(cardView);
        }
    }

    private void updateAppointmentStatus(Appointment appointment, String newStatus) {
        appointmentRepository.updateAppointmentStatus(
                sessionManager.getAuthToken(),
                appointment.getId(),
                newStatus,
                new AppointmentRepository.AppointmentUpdateCallback() {

                    @Override
                    public void onSuccess(Appointment updatedAppointment) {
                        Toast.makeText(PractitionerAppointmentsActivity.this, "Appointment status updated to " + newStatus.toUpperCase(), Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PractitionerAppointmentsActivity.this, message, Toast.LENGTH_LONG).show();
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