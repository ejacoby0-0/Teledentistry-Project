package com.example.teledentistry_project.ui.patient;

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
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class PatientAppointmentsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;

    private LinearLayout layoutAppointments;
    private TextView tvNoAppointments;

    private String patientId;

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

        // Patient RBAC check
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
                R.layout.activity_patient_appointments
        );

        patientRepository =
                new PatientRepository();

        appointmentRepository =
                new AppointmentRepository();

        layoutAppointments =
                findViewById(R.id.layoutAppointments);

        tvNoAppointments =
                findViewById(R.id.tvNoAppointments);

        loadPatient();
    }


    private void loadPatient() {

        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        patientId = patient.getId();

                        loadAppointments();
                    }

                    @Override
                    public void onNotFound() {

                        tvNoAppointments.setText(
                                "Patient profile not found."
                        );

                        tvNoAppointments.setVisibility(
                                View.VISIBLE
                        );
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PatientAppointmentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void loadAppointments() {

        if (patientId == null || patientId.isEmpty()) {
            return;
        }

        appointmentRepository.getPatientAppointments(
                sessionManager.getAuthToken(),
                patientId,
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
                                View.VISIBLE
                        );

                        Toast.makeText(
                                PatientAppointmentsActivity.this,
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

            tvNoAppointments.setText(
                    "You have no appointments."
            );

            tvNoAppointments.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        tvNoAppointments.setVisibility(
                View.GONE
        );

        for (Appointment appointment : appointmentList) {

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


            // Date
            TextView tvDate =
                    new TextView(this);

            tvDate.setText(
                    formatDate(
                            appointment.getDateTime()
                    )
            );

            tvDate.setTextSize(18);

            tvDate.setTypeface(
                    null,
                    Typeface.BOLD
            );

            content.addView(tvDate);


            // Time
            TextView tvTime =
                    new TextView(this);

            tvTime.setText(
                    appointment.getStartTime()
                            + " - "
                            + appointment.getEndTime()
            );

            tvTime.setTextSize(16);

            content.addView(tvTime);


            // Status
            TextView tvStatus =
                    new TextView(this);

            String status =
                    appointment.getStatus();

            if (status == null) {
                status = "Unknown";
            }

            tvStatus.setText(
                    "Status: "
                            + status.toUpperCase()
            );

            tvStatus.setTextSize(15);

            LinearLayout.LayoutParams statusParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            statusParams.setMargins(
                    0,
                    16,
                    0,
                    0
            );

            tvStatus.setLayoutParams(
                    statusParams
            );

            content.addView(
                    tvStatus
            );

            // If confirmed/approved, allow patient to Chat and Start Video Call
            if ("confirmed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                LinearLayout callChatLayout = new LinearLayout(this);
                callChatLayout.setOrientation(LinearLayout.HORIZONTAL);
                callChatLayout.setPadding(0, 20, 0, 0);

                Button btnChat = new Button(this);
                btnChat.setText("Chat with Dentist");
                LinearLayout.LayoutParams chatParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                chatParams.setMargins(0, 0, 8, 0);
                btnChat.setLayoutParams(chatParams);
                btnChat.setOnClickListener(v -> {
                    Intent intent = new Intent(PatientAppointmentsActivity.this, ChatActivity.class);
                    intent.putExtra("appointment_id", appointment.getId());
                    startActivity(intent);
                });

                Button btnVideoCall = new Button(this);
                btnVideoCall.setText("Video Call");
                LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                videoParams.setMargins(8, 0, 0, 0);
                btnVideoCall.setLayoutParams(videoParams);
                btnVideoCall.setOnClickListener(v -> {
                    Intent intent = new Intent(PatientAppointmentsActivity.this, VideoCallActivity.class);
                    intent.putExtra("appointment_id", appointment.getId());
                    startActivity(intent);
                });

                callChatLayout.addView(btnChat);
                callChatLayout.addView(btnVideoCall);
                content.addView(callChatLayout);
            }

            card.addView(content);

            layoutAppointments.addView(card);
        }
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
