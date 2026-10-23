package com.example.teledentistry_project.ui.patient;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.repository.PractitionerAvailabilityRepository;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.PatientRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerRepository practitionerRepository;

    private Button btnSelectPractitioner;
    private Button btnBookAppointment;

    private TextView tvSelectPractitionerMessage;
    private TextView tvSelectedAppointment;

    private LinearLayout layoutAvailableSlots;
    private CalendarView calendarView;

    private final List<Practitioner> practitionerList = new ArrayList<>();
    private Practitioner selectedPractitioner;

    private PractitionerAvailabilityRepository availabilityRepository;
    private PractitionerAvailability selectedAvailability;
    private final List<PractitionerAvailability> currentAvailabilityList = new ArrayList<>();

    private String selectedCalendarDate;
    private String selectedAppointmentStartTime;
    private String selectedAppointmentEndTime;

    private AppointmentRepository appointmentRepository;
    private final List<Appointment> practitionerAppointments = new ArrayList<>();

    private PatientRepository patientRepository;
    private String patientId;

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
            Toast.makeText(this, "Access denied.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_book_appointment);

        practitionerRepository = new PractitionerRepository();
        appointmentRepository = new AppointmentRepository();
        availabilityRepository = new PractitionerAvailabilityRepository();
        patientRepository = new PatientRepository();

        btnSelectPractitioner = findViewById(R.id.btnSelectPractitioner);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        tvSelectPractitionerMessage = findViewById(R.id.tvSelectPractitionerMessage);
        tvSelectedAppointment = findViewById(R.id.tvSelectedAppointment);
        layoutAvailableSlots = findViewById(R.id.layoutAvailableSlots);
        calendarView = findViewById(R.id.calendarView);
        com.google.android.material.textfield.TextInputEditText etReason = findViewById(R.id.etReason);

        // Initialize selectedCalendarDate to today
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        selectedCalendarDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);

        calendarView.setOnDateChangeListener((view, y, m, d) -> {
            selectedCalendarDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            clearAppointmentSelection();
            if (!currentAvailabilityList.isEmpty()) {
                displayAvailability(currentAvailabilityList);
            } else if (selectedPractitioner != null) {
                tvSelectPractitionerMessage.setText("📅 No available appointments on " + selectedCalendarDate);
            }
        });

        btnSelectPractitioner.setOnClickListener(v -> showPractitionerDialog());
        btnBookAppointment.setOnClickListener(v -> bookAppointment());

        loadPractitioners();
        loadPatient();
    }

    private void loadPractitioners() {
        btnSelectPractitioner.setText("Loading Dentists...");
        practitionerRepository.getAllPractitioners(
                sessionManager.getAuthToken(),
                new PractitionerRepository.PractitionerListCallback() {
                    @Override
                    public void onSuccess(List<Practitioner> practitioners) {
                        practitionerList.clear();
                        if (practitioners != null && !practitioners.isEmpty()) {
                            practitionerList.addAll(practitioners);

                            // Auto-select the first practitioner if none is selected yet
                            if (selectedPractitioner == null && !practitionerList.isEmpty()) {
                                selectedPractitioner = practitionerList.get(0);
                                String name = selectedPractitioner.getFullName();
                                String specialization = selectedPractitioner.getSpecialization();
                                if (specialization != null && !specialization.isEmpty()) {
                                    btnSelectPractitioner.setText(name + " - " + specialization);
                                } else {
                                    btnSelectPractitioner.setText(name);
                                }
                                practitionerSelected();
                            }
                        } else {
                            btnSelectPractitioner.setText("No Dentists Available");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        btnSelectPractitioner.setText("Select Practitioner");
                        Toast.makeText(BookAppointmentActivity.this, "Failed to load practitioners: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showPractitionerDialog() {
        if (practitionerList.isEmpty()) {
            Toast.makeText(this, "Loading practitioners from PocketBase...", Toast.LENGTH_SHORT).show();
            practitionerRepository.getAllPractitioners(
                    sessionManager.getAuthToken(),
                    new PractitionerRepository.PractitionerListCallback() {
                        @Override
                        public void onSuccess(List<Practitioner> practitioners) {
                            practitionerList.clear();
                            if (practitioners != null && !practitioners.isEmpty()) {
                                practitionerList.addAll(practitioners);
                                displayPractitionerDialog();
                            } else {
                                Toast.makeText(BookAppointmentActivity.this, "No practitioners found in database.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(String message) {
                            new AlertDialog.Builder(BookAppointmentActivity.this)
                                    .setTitle("Error Loading Practitioners")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
            );
            return;
        }

        displayPractitionerDialog();
    }

    private void displayPractitionerDialog() {
        String[] practitionerNames = new String[practitionerList.size()];
        for (int i = 0; i < practitionerList.size(); i++) {
            Practitioner practitioner = practitionerList.get(i);
            String name = practitioner.getFullName();
            String specialization = practitioner.getSpecialization();
            if (specialization != null && !specialization.isEmpty()) {
                practitionerNames[i] = name + " - " + specialization;
            } else {
                practitionerNames[i] = name;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Practitioner")
                .setItems(
                        practitionerNames,
                        (dialog, which) -> {
                            selectedPractitioner = practitionerList.get(which);
                            btnSelectPractitioner.setText(practitionerNames[which]);
                            practitionerSelected();
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void practitionerSelected() {
        layoutAvailableSlots.removeAllViews();
        tvSelectedAppointment.setText("No appointment selected.");
        btnBookAppointment.setEnabled(false);
        tvSelectPractitionerMessage.setText("Loading available appointments...");
        loadPractitionerAvailability();
    }

    private void loadPractitionerAvailability() {
        if (selectedPractitioner == null) {
            return;
        }

        availabilityRepository.getAvailabilityByPractitioner(
                sessionManager.getAuthToken(),
                selectedPractitioner.getId(),
                new PractitionerAvailabilityRepository.AvailabilityListCallback() {
                    @Override
                    public void onSuccess(List<PractitionerAvailability> availabilityList) {
                        currentAvailabilityList.clear();
                        if (availabilityList != null) {
                            currentAvailabilityList.addAll(availabilityList);
                        }

                        if (currentAvailabilityList.isEmpty()) {
                            // Default 9am-5pm working hours for selected practitioner if no custom slots configured
                            currentAvailabilityList.add(new PractitionerAvailability(
                                    "default_avail",
                                    selectedPractitioner.getId(),
                                    selectedCalendarDate,
                                    "09:00",
                                    "17:00",
                                    true
                            ));
                        }
                        loadExistingAppointments();
                    }

                    @Override
                    public void onError(String message) {
                        tvSelectPractitionerMessage.setText("Error loading availability: " + message);
                        Toast.makeText(BookAppointmentActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void loadPatient() {
        patientRepository.ensurePatientRecord(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {
                    @Override
                    public void onSuccess(Patient patient) {
                        if (patient != null && patient.getId() != null && !patient.getId().isEmpty()) {
                            patientId = patient.getId();
                        }
                    }

                    @Override
                    public void onNotFound() {
                        Toast.makeText(BookAppointmentActivity.this, "Patient profile not found in PocketBase.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(BookAppointmentActivity.this, "Patient profile error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void loadExistingAppointments() {
        appointmentRepository.getPractitionerAppointments(
                sessionManager.getAuthToken(),
                selectedPractitioner.getId(),
                new AppointmentRepository.AppointmentListCallback() {
                    @Override
                    public void onSuccess(List<Appointment> appointments) {
                        practitionerAppointments.clear();
                        if (appointments != null) {
                            practitionerAppointments.addAll(appointments);
                        }
                        displayAvailability(currentAvailabilityList);
                    }

                    @Override
                    public void onError(String message) {
                        displayAvailability(currentAvailabilityList);
                    }
                }
        );
    }

    private void displayAvailability(List<PractitionerAvailability> availabilityList) {
        layoutAvailableSlots.removeAllViews();

        if (availabilityList == null || availabilityList.isEmpty()) {
            tvSelectPractitionerMessage.setText("📅 No available appointments for this practitioner.");
            return;
        }

        tvSelectPractitionerMessage.setText("📅 Available slots for " + selectedCalendarDate + ":");

        boolean slotAdded = false;
        final int SLOT_DURATION = 30;

        for (PractitionerAvailability availability : availabilityList) {
            if (!availability.isAvailable()) {
                continue;
            }

            String availDate = formatDate(availability.getDate());
            if (!availDate.isEmpty() && !availDate.equals("default_avail") && !availDate.contains(selectedCalendarDate) && !selectedCalendarDate.contains(availDate)) {
                continue;
            }

            int startMinutes = timeToMinutes(availability.getStartTime());
            int endMinutes = timeToMinutes(availability.getEndTime());

            while (startMinutes + SLOT_DURATION <= endMinutes) {
                int slotStart = startMinutes;
                int slotEnd = startMinutes + SLOT_DURATION;

                String slotStartTime = minutesToTime(slotStart);
                String slotEndTime = minutesToTime(slotEnd);

                boolean isBookedByOther = false;
                boolean isBookedBySelf = false;

                if (practitionerAppointments != null) {
                    for (Appointment appt : practitionerAppointments) {
                        String apptDate = formatDate(appt.getDateTime());
                        String apptTime = appt.getStartTime();
                        if (apptDate.contains(selectedCalendarDate) && apptTime != null && apptTime.startsWith(slotStartTime)) {
                            if (appt.getPatient() != null && appt.getPatient().equals(patientId)) {
                                isBookedBySelf = true;
                            } else {
                                isBookedByOther = true;
                            }
                        }
                    }
                }

                Button slotButton = new Button(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                slotButton.setLayoutParams(params);
                slotButton.setPadding(24, 24, 24, 24);

                if (isBookedByOther) {
                    slotButton.setText(selectedCalendarDate + " | " + slotStartTime + " - " + slotEndTime + "  [🔒 BUSY]");
                    slotButton.setEnabled(false);
                    slotButton.setBackgroundColor(android.graphics.Color.parseColor("#CFD8DC"));
                    slotButton.setTextColor(android.graphics.Color.parseColor("#78909C"));
                } else if (isBookedBySelf) {
                    slotButton.setText(selectedCalendarDate + " | " + slotStartTime + " - " + slotEndTime + "  [⭐ Your Booking]");
                    slotButton.setEnabled(false);
                    slotButton.setBackgroundColor(android.graphics.Color.parseColor("#81C784"));
                    slotButton.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                } else {
                    slotButton.setText(selectedCalendarDate + " | " + slotStartTime + " - " + slotEndTime + "  [🟢 Available]");
                    slotButton.setEnabled(true);
                    slotButton.setBackgroundColor(android.graphics.Color.parseColor("#00838F"));
                    slotButton.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));

                    slotButton.setOnClickListener(v -> {
                        selectedAvailability = availability;
                        selectedAppointmentStartTime = slotStartTime;
                        selectedAppointmentEndTime = slotEndTime;

                        tvSelectedAppointment.setText(
                                "Selected: " + selectedPractitioner.getFullName()
                                        + "\n" + selectedCalendarDate + " from " + slotStartTime + " to " + slotEndTime
                        );

                        btnBookAppointment.setEnabled(true);
                    });
                }

                layoutAvailableSlots.addView(slotButton);
                slotAdded = true;
                startMinutes += SLOT_DURATION;
            }
        }

        if (!slotAdded) {
            tvSelectPractitionerMessage.setText("📅 No available appointments on " + selectedCalendarDate);
        }
    }

    private void bookAppointment() {
        if (patientId == null || patientId.isEmpty()) {
            Toast.makeText(this, "Patient record loading from PocketBase. Please try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPractitioner == null || selectedPractitioner.getId() == null) {
            Toast.makeText(this, "Please select a practitioner.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCalendarDate == null
                || selectedAppointmentStartTime == null
                || selectedAppointmentEndTime == null) {
            Toast.makeText(this, "Please select an appointment date and time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.android.material.textfield.TextInputEditText etReason = findViewById(R.id.etReason);
        String reason = etReason != null && etReason.getText() != null ? etReason.getText().toString().trim() : "";
        if (reason.isEmpty()) {
            if (etReason != null) etReason.setError("Please enter the reason for your consultation or symptoms.");
            Toast.makeText(this, "Please enter your symptoms or reason for visit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // PocketBase expects ISO 8601 date-time format with T
        String dateTime = selectedCalendarDate + "T" + selectedAppointmentStartTime + ":00.000Z";

        AppointmentCreateRequest request = new AppointmentCreateRequest(
                patientId,
                selectedPractitioner.getId(),
                dateTime,
                selectedAppointmentStartTime,
                selectedAppointmentEndTime,
                "pending"
        );

        btnBookAppointment.setEnabled(false);

        appointmentRepository.createAppointment(
                sessionManager.getAuthToken(),
                request,
                new AppointmentRepository.AppointmentCreateCallback() {
                    @Override
                    public void onSuccess(Appointment appointment) {
                        Toast.makeText(
                                BookAppointmentActivity.this,
                                "Appointment booked successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        clearAppointmentSelection();
                        if (selectedPractitioner != null) {
                            practitionerSelected();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        btnBookAppointment.setEnabled(true);

                        new AlertDialog.Builder(BookAppointmentActivity.this)
                                .setTitle("Booking Failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
        );
    }

    private void clearAppointmentSelection() {
        selectedAvailability = null;
        selectedAppointmentStartTime = null;
        selectedAppointmentEndTime = null;
        tvSelectedAppointment.setText("No appointment selected");
        btnBookAppointment.setEnabled(false);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (hours * 60) + minutes;
    }

    private String minutesToTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format(Locale.US, "%02d:%02d", hours, minutes);
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        if (date.length() >= 10) {
            return date.substring(0, 10);
        }
        return date;
    }
}
