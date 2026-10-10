package com.example.teledentistry_project.ui.patient;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
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
import java.util.List;


public class BookAppointmentActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerRepository practitionerRepository;

    private Button btnSelectPractitioner;
    private Button btnBookAppointment;

    private TextView tvSelectPractitionerMessage;
    private TextView tvSelectedAppointment;

    private LinearLayout layoutAvailableSlots;

    private final List<Practitioner> practitionerList =
            new ArrayList<>();

    private Practitioner selectedPractitioner;

    private PractitionerAvailabilityRepository availabilityRepository;

    private PractitionerAvailability selectedAvailability;

    private String selectedAppointmentDate;
    private String selectedAppointmentStartTime;
    private String selectedAppointmentEndTime;

    private AppointmentRepository appointmentRepository;

    private final List<Appointment> practitionerAppointments =
            new ArrayList<>();

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

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_book_appointment);

        practitionerRepository = new PractitionerRepository();

        appointmentRepository = new AppointmentRepository();

        availabilityRepository =
                new PractitionerAvailabilityRepository();

        patientRepository = new PatientRepository();

        btnSelectPractitioner =
                findViewById(R.id.btnSelectPractitioner);

        btnBookAppointment =
                findViewById(R.id.btnBookAppointment);

        tvSelectPractitionerMessage =
                findViewById(R.id.tvSelectPractitionerMessage);

        tvSelectedAppointment =
                findViewById(R.id.tvSelectedAppointment);

        layoutAvailableSlots =
                findViewById(R.id.layoutAvailableSlots);

        btnSelectPractitioner.setOnClickListener(v ->
                showPractitionerDialog()
        );

        btnBookAppointment.setOnClickListener(v -> bookAppointment());

        // Booking will be connected later
        btnBookAppointment.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Select an appointment first.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        loadPractitioners();
        loadPatient();
    }

    private void loadPractitioners() {

        practitionerRepository.getAllPractitioners(
                sessionManager.getAuthToken(),
                new PractitionerRepository.PractitionerListCallback() {

                    @Override
                    public void onSuccess(
                            List<Practitioner> practitioners
                    ) {

                        practitionerList.clear();

                        if (practitioners != null) {
                            practitionerList.addAll(practitioners);
                        }

                        if (practitionerList.isEmpty()) {

                            Toast.makeText(
                                    BookAppointmentActivity.this,
                                    "No practitioners are currently available.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showPractitionerDialog() {

        if (practitionerList.isEmpty()) {

            Toast.makeText(
                    this,
                    "No practitioners available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] practitionerNames =
                new String[practitionerList.size()];

        for (int i = 0; i < practitionerList.size(); i++) {

            Practitioner practitioner =
                    practitionerList.get(i);

            String name = practitioner.getFullName();
            String specialization =
                    practitioner.getSpecialization();

            if (specialization != null
                    && !specialization.isEmpty()) {

                practitionerNames[i] =
                        name + " - " + specialization;

            } else {

                practitionerNames[i] = name;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Practitioner")
                .setItems(
                        practitionerNames,
                        (dialog, which) -> {

                            selectedPractitioner =
                                    practitionerList.get(which);

                            btnSelectPractitioner.setText(
                                    practitionerNames[which]
                            );

                            practitionerSelected();
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void practitionerSelected() {

        layoutAvailableSlots.removeAllViews();

        tvSelectedAppointment.setText(
                "No appointment selected."
        );

        btnBookAppointment.setEnabled(false);

        tvSelectPractitionerMessage.setText(
                "Loading available appointments..."
        );

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
                    public void onSuccess(
                            List<PractitionerAvailability> availabilityList
                    ) {

                        loadExistingAppointments(availabilityList);
                    }

                    @Override
                    public void onError(String message) {

                        tvSelectPractitionerMessage.setText(
                                "Unable to load available appointments."
                        );

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadPatient() {

        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        patientId = patient.getId();
                    }

                    @Override
                    public void onNotFound() {

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                "Patient profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        btnBookAppointment.setEnabled(false);
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        btnBookAppointment.setEnabled(false);
                    }
                }
        );
    }

    private void loadExistingAppointments(
            List<PractitionerAvailability> availabilityList
    ) {

        appointmentRepository.getPractitionerAppointments(
                sessionManager.getAuthToken(),
                selectedPractitioner.getId(),
                new AppointmentRepository.AppointmentListCallback() {

                    @Override
                    public void onSuccess(
                            List<Appointment> appointments
                    ) {

                        practitionerAppointments.clear();

                        if (appointments != null) {
                            practitionerAppointments.addAll(appointments);
                        }

                        displayAvailability(availabilityList);
                    }

                    @Override
                    public void onError(String message) {

                        tvSelectPractitionerMessage.setText(
                                "Unable to load appointments."
                        );

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayAvailability(
            List<PractitionerAvailability> availabilityList
    ) {

        layoutAvailableSlots.removeAllViews();

        if (availabilityList == null || availabilityList.isEmpty()) {

            tvSelectPractitionerMessage.setText(
                    "No available appointments for this practitioner."
            );

            return;
        }

        tvSelectPractitionerMessage.setText(
                "Select an available appointment:"
        );

        boolean slotAdded = false;

        final int SLOT_DURATION = 30;

        for (PractitionerAvailability availability : availabilityList) {

            if (!availability.isAvailable()) {
                continue;
            }

            int startMinutes =
                    timeToMinutes(availability.getStartTime());

            int endMinutes =
                    timeToMinutes(availability.getEndTime());

            while (startMinutes + SLOT_DURATION <= endMinutes) {

                int slotStart = startMinutes;
                int slotEnd = startMinutes + SLOT_DURATION;

                String slotStartTime =
                        minutesToTime(slotStart);

                String slotEndTime =
                        minutesToTime(slotEnd);

                String formattedDate =
                        formatDate(availability.getDate());

                String slotText =
                        formattedDate
                                + " | "
                                + slotStartTime
                                + " - "
                                + slotEndTime;

                Button slotButton = new Button(this);

                slotButton.setText(slotText);

                slotButton.setOnClickListener(v -> {

                    selectedAvailability = availability;

                    selectedAppointmentDate =
                            formattedDate;

                    selectedAppointmentStartTime =
                            slotStartTime;

                    selectedAppointmentEndTime =
                            slotEndTime;

                    tvSelectedAppointment.setText(
                            selectedPractitioner.getFullName()
                                    + "\n"
                                    + slotText
                    );

                    btnBookAppointment.setEnabled(true);
                });

                layoutAvailableSlots.addView(slotButton);

                slotAdded = true;

                startMinutes += SLOT_DURATION;
            }
        }

        if (!slotAdded) {

            tvSelectPractitionerMessage.setText(
                    "No available appointments for this practitioner."
            );
        }
    }

    private void bookAppointment() {

        if (patientId == null || patientId.isEmpty()) {
            Toast.makeText(
                    this,
                    "Patient profile has not loaded yet.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedPractitioner == null) {
            Toast.makeText(
                    this,
                    "Please select a practitioner.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedAppointmentDate == null
                || selectedAppointmentStartTime == null
                || selectedAppointmentEndTime == null) {

            Toast.makeText(
                    this,
                    "Please select an appointment time.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String dateTime =
                selectedAppointmentDate
                        + " "
                        + selectedAppointmentStartTime
                        + ":00";

        AppointmentCreateRequest request =
                new AppointmentCreateRequest(
                        patientId,
                        selectedPractitioner.getId(),
                        dateTime,
                        selectedAppointmentStartTime,
                        selectedAppointmentEndTime,
                        "scheduled"
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
                                "Appointment booked successfully.",
                                Toast.LENGTH_LONG
                        ).show();

                        clearAppointmentSelection();
                    }

                    @Override
                    public void onError(String message) {

                        btnBookAppointment.setEnabled(true);

                        Toast.makeText(
                                BookAppointmentActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void clearAppointmentSelection() {

        selectedAvailability = null;
        selectedAppointmentDate = null;
        selectedAppointmentStartTime = null;
        selectedAppointmentEndTime = null;

        tvSelectedAppointment.setText(
                "No appointment selected"
        );

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

        return String.format(
                java.util.Locale.US,
                "%02d:%02d",
                hours,
                minutes
        );
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
