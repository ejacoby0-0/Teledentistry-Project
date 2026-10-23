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

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlan;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.data.repository.PrescriptionRepository;
import com.example.teledentistry_project.data.repository.TreatmentPlanRepository;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.utils.SessionManager;

import java.util.List;

public class PatientHomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private TreatmentPlanRepository treatmentPlanRepository;
    private PrescriptionRepository prescriptionRepository;

    private ImageView ivDashProfilePic;
    private TextView tvPatientName;
    private TextView tvAppointmentSummary;
    private TextView tvTreatmentPlanSummary;
    private TextView tvPrescriptionsSummary;

    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        if (!"patient".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access denied. Patient portal only.", Toast.LENGTH_LONG).show();
            sessionManager.clearSession();
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_patient_home);

        patientRepository = new PatientRepository();
        appointmentRepository = new AppointmentRepository();
        treatmentPlanRepository = new TreatmentPlanRepository();
        prescriptionRepository = new PrescriptionRepository();

        initializeViews();
        initializeNavigation();
        loadPatientData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadSavedProfileImage();
            loadPatientData();
        }
    }

    private void initializeViews() {
        ivDashProfilePic = findViewById(R.id.ivDashProfilePic);
        tvPatientName = findViewById(R.id.tvPatientName);

        tvAppointmentSummary = findViewById(R.id.tvAppointmentSummary);
        tvTreatmentPlanSummary = findViewById(R.id.tvTreatmentPlanSummary);
        tvPrescriptionsSummary = findViewById(R.id.tvPrescriptionsSummary);

        loadSavedProfileImage();
    }

    private void loadSavedProfileImage() {
        if (ivDashProfilePic == null) return;
        SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        String savedUriStr = prefs.getString("profile_image_" + sessionManager.getUserId(), null);
        if (savedUriStr != null) {
            try {
                ivDashProfilePic.setImageURI(Uri.parse(savedUriStr));
            } catch (Exception ignored) {}
        }
    }

    private void initializeNavigation() {
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnJoinCall = findViewById(R.id.btnJoinCall);
        Button btnBookAppointment = findViewById(R.id.btnBookAppointment);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnDentalRecord = findViewById(R.id.btnDentalRecord);
        Button btnPatientLogout = findViewById(R.id.btnPatientLogout);

        TextView tvViewAllAppointments = findViewById(R.id.tvViewAllAppointments);
        TextView tvViewAllTreatmentPlans = findViewById(R.id.tvViewAllTreatmentPlans);
        TextView tvViewAllPrescriptions = findViewById(R.id.tvViewAllPrescriptions);

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileActivity.class)));
        }

        if (btnJoinCall != null) {
            btnJoinCall.setOnClickListener(v -> startActivity(new Intent(this, VideoCallActivity.class)));
        }

        if (btnBookAppointment != null) {
            btnBookAppointment.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
        }

        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> startActivity(new Intent(this, UploadImageActivity.class)));
        }

        if (btnDentalRecord != null) {
            btnDentalRecord.setOnClickListener(v -> startActivity(new Intent(this, PatientDentalRecordActivity.class)));
        }

        if (btnPatientLogout != null) {
            btnPatientLogout.setOnClickListener(v -> logoutPatient());
        }

        if (tvViewAllAppointments != null) {
            tvViewAllAppointments.setOnClickListener(v -> startActivity(new Intent(this, PatientAppointmentsActivity.class)));
        }

        if (tvViewAllTreatmentPlans != null) {
            tvViewAllTreatmentPlans.setOnClickListener(v -> startActivity(new Intent(this, PatientTreatmentPlanActivity.class)));
        }

        if (tvViewAllPrescriptions != null) {
            tvViewAllPrescriptions.setOnClickListener(v -> startActivity(new Intent(this, PatientPrescriptionsActivity.class)));
        }


    }

    private void loadPatientData() {
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
                            String fullName = patient.getFullName();
                            if (fullName != null && !fullName.trim().isEmpty()) {
                                tvPatientName.setText(fullName);
                            } else {
                                tvPatientName.setText("Patient Account");
                            }

                            // Load dashboard summaries for this patient ONLY
                            loadAppointmentsDashboard();
                            loadTreatmentPlansDashboard();
                            loadPrescriptionsDashboard();
                        }
                    }

                    @Override
                    public void onNotFound() {
                        tvPatientName.setText("Patient Account");
                        showDashboardEmptyStates();
                    }

                    @Override
                    public void onError(String message) {
                        tvPatientName.setText("Patient Account");
                        showDashboardEmptyStates();
                    }
                }
        );
    }

    private void showDashboardEmptyStates() {
        if (tvAppointmentSummary != null) tvAppointmentSummary.setText("No upcoming appointments. Select 'Book Appointment' to schedule a consultation.");
        if (tvTreatmentPlanSummary != null) tvTreatmentPlanSummary.setText("No active treatment plans issued at this time.");
        if (tvPrescriptionsSummary != null) tvPrescriptionsSummary.setText("No active or issued prescriptions available.");
    }

    private void loadAppointmentsDashboard() {
        if (patientId == null) return;
        appointmentRepository.getPatientAppointments(
                sessionManager.getAuthToken(),
                patientId,
                new AppointmentRepository.AppointmentListCallback() {
                    @Override
                    public void onSuccess(List<Appointment> appointments) {
                        if (appointments != null && !appointments.isEmpty()) {
                            Appointment latest = appointments.get(appointments.size() - 1);
                            String date = latest.getDateTime().length() >= 10 ? latest.getDateTime().substring(0, 10) : latest.getDateTime();
                            String status = latest.getStatus() != null ? latest.getStatus().toUpperCase() : "PENDING";
                            tvAppointmentSummary.setText("Next Appointment: " + date + " (" + latest.getStartTime() + " - " + latest.getEndTime() + ")\nStatus: " + status);
                        } else {
                            tvAppointmentSummary.setText("No upcoming appointments. Tap 'Book Appointment' above to pick a date.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        tvAppointmentSummary.setText("No upcoming appointments recorded.");
                    }
                }
        );
    }

    private void loadTreatmentPlansDashboard() {
        if (patientId == null) return;
        treatmentPlanRepository.getTreatmentPlansByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new TreatmentPlanRepository.TreatmentPlanListCallback() {
                    @Override
                    public void onSuccess(List<TreatmentPlan> plans) {
                        if (plans != null && !plans.isEmpty()) {
                            TreatmentPlan plan = plans.get(0);
                            tvTreatmentPlanSummary.setText("Active Plan (" + plans.size() + " total)\nStart Date: " + plan.getStartDate() + " | Status: " + plan.getStatus());
                        } else {
                            tvTreatmentPlanSummary.setText("No active treatment plans assigned to your profile.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        tvTreatmentPlanSummary.setText("No active treatment plans assigned to your profile.");
                    }
                }
        );
    }

    private void loadPrescriptionsDashboard() {
        if (patientId == null) return;
        prescriptionRepository.getPrescriptionsByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new PrescriptionRepository.PrescriptionListCallback() {
                    @Override
                    public void onSuccess(List<Prescription> prescriptions) {
                        if (prescriptions != null && !prescriptions.isEmpty()) {
                            Prescription p = prescriptions.get(0);
                            tvPrescriptionsSummary.setText("Latest Medication: " + p.getMedication() + " (" + p.getDosage() + ")\nIssued Date: " + (p.getIssueDate().length() >= 10 ? p.getIssueDate().substring(0, 10) : p.getIssueDate()));
                        } else {
                            tvPrescriptionsSummary.setText("No issued prescriptions available.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        tvPrescriptionsSummary.setText("No issued prescriptions available.");
                    }
                }
        );
    }



    private void logoutPatient() {
        sessionManager.clearSession();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(PatientHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}