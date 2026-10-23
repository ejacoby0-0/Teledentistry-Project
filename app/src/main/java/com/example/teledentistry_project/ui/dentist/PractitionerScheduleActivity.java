package com.example.teledentistry_project.ui.dentist;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailabilityRequest;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PractitionerAvailabilityRepository;
import com.example.teledentistry_project.ui.adapters.UpcomingAppointmentAdapter;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PractitionerScheduleActivity extends AppCompatActivity {

    // Availability views
    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private ChipGroup chipGroupSlots;
    private TextView tvNoSlots;
    private EditText etStartTime;
    private Button btnAddSlot;

    // Upcoming appointments views
    private RecyclerView rvUpcoming;
    private TextView tvNoUpcoming;
    private UpcomingAppointmentAdapter upcomingAdapter;

    // Repositories and session
    private PractitionerAvailabilityRepository availabilityRepository;
    private AppointmentRepository appointmentRepository;
    private SessionManager session;

    private String practitionerId;
    private String authToken;
    private String selectedDate;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practitioner_schedule);

        session = new SessionManager(this);

        availabilityRepository = new PractitionerAvailabilityRepository();
        appointmentRepository = new AppointmentRepository();

        // Use the getter names provided by your SessionManager.
        practitionerId = session.getUserId();
        authToken = session.getAuthToken();

        // Availability views
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        chipGroupSlots = findViewById(R.id.chipGroupSlots);
        tvNoSlots = findViewById(R.id.tvNoSlots);
        etStartTime = findViewById(R.id.etSlotTime);
        btnAddSlot = findViewById(R.id.btnAddSlot);

        // Upcoming appointments views
        rvUpcoming = findViewById(R.id.rvUpcoming);
        tvNoUpcoming = findViewById(R.id.tvNoUpcoming);

        upcomingAdapter = new UpcomingAppointmentAdapter();
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        rvUpcoming.setAdapter(upcomingAdapter);

        // Initialize selected date
        Calendar today = Calendar.getInstance();
        selectedDate = dateFormat.format(today.getTime());
        tvSelectedDate.setText(selectedDate);

        // Use the time picker instead of the keyboard
        etStartTime.setFocusable(false);
        etStartTime.setClickable(true);
        etStartTime.setOnClickListener(v -> showTimePicker());

        calendarView.setOnDateChangeListener(
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    selectedDate = dateFormat.format(selected.getTime());
                    tvSelectedDate.setText(selectedDate);

                    etStartTime.setText("");

                    loadSlots();
                }
        );

        btnAddSlot.setOnClickListener(v -> createSlot());

        if (practitionerId == null || practitionerId.trim().isEmpty()
                || authToken == null || authToken.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Your session has expired. Please sign in again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        loadSlots();
        loadUpcomingAppointments();
    }

    // Time Picker

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(
                            Locale.US,
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    etStartTime.setText(time);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        ).show();
    }

    // Create Availability slot

    private void createSlot() {
        String startTime = etStartTime.getText().toString().trim();

        if (practitionerId == null || practitionerId.trim().isEmpty()
                || authToken == null || authToken.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Your session has expired. Please sign in again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (startTime.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select a start time.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        PractitionerAvailabilityRequest request =
                new PractitionerAvailabilityRequest(
                        practitionerId,
                        selectedDate,
                        startTime,
                        true
                );

        btnAddSlot.setEnabled(false);

        availabilityRepository.createSlot(
                authToken,
                request,
                new PractitionerAvailabilityRepository.AvailabilityCallback() {

                    @Override
                    public void onSuccess(PractitionerAvailability slot) {
                        runOnUiThread(() -> {
                            btnAddSlot.setEnabled(true);
                            etStartTime.setText("");

                            Toast.makeText(
                                    PractitionerScheduleActivity.this,
                                    "Availability slot created.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSlots();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            btnAddSlot.setEnabled(true);

                            Toast.makeText(
                                    PractitionerScheduleActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // Load Availability slot

    private void loadSlots() {
        if (practitionerId == null || authToken == null) {
            return;
        }

        availabilityRepository.getSlots(
                authToken,
                practitionerId,
                selectedDate,
                new PractitionerAvailabilityRepository.AvailabilityListCallback() {

                    @Override
                    public void onSuccess(
                            List<PractitionerAvailability> slots) {

                        runOnUiThread(() -> {
                            chipGroupSlots.removeAllViews();

                            if (slots != null) {
                                for (PractitionerAvailability slot : slots) {
                                    addSlotChip(slot);
                                }
                            }

                            boolean empty = slots == null || slots.isEmpty();

                            tvNoSlots.setVisibility(
                                    empty ? View.VISIBLE : View.GONE
                            );
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        PractitionerScheduleActivity.this,
                                        "Could not load availability: " + message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // Display availability Chip

    private void addSlotChip(PractitionerAvailability slot) {
        Chip chip = new Chip(this);

        String label = slot.getStartTime();

        if (!slot.isAvailable()) {
            label += " (unavailable)";
        }

        chip.setText(label);
        chip.setTag(slot.getId());
        chip.setCheckable(false);

        if (slot.isAvailable()) {
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> deleteSlot(slot));
        } else {
            chip.setCloseIconVisible(false);
            chip.setEnabled(false);
        }

        chipGroupSlots.addView(chip);
    }

    // Delete availability slot

    private void deleteSlot(PractitionerAvailability slot) {
        if (!slot.isAvailable()) {
            Toast.makeText(
                    this,
                    "This slot is unavailable and cannot be deleted here.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete availability")
                .setMessage(
                        "Delete " + slot.getStartTime()
                                + " on " + slot.getDate() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    availabilityRepository.deleteSlot(
                            authToken,
                            slot.getId(),
                            new PractitionerAvailabilityRepository.DeleteCallback() {

                                @Override
                                public void onSuccess() {
                                    runOnUiThread(() -> {
                                        Toast.makeText(
                                                PractitionerScheduleActivity.this,
                                                "Availability deleted.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        loadSlots();
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    runOnUiThread(() ->
                                            Toast.makeText(
                                                    PractitionerScheduleActivity.this,
                                                    "Could not delete slot: " + message,
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                                }
                            }
                    );
                })
                .show();
    }

    // Load upcoming appointments

    private void loadUpcomingAppointments() {
        if (practitionerId == null || authToken == null) {
            return;
        }

        appointmentRepository.getPractitionerAppointments(
                authToken,
                practitionerId,
                new AppointmentRepository.AppointmentListCallback() {

                    @Override
                    public void onSuccess(List<Appointment> appointments) {
                        runOnUiThread(() -> {
                            List<Appointment> upcoming = new ArrayList<>();

                            String today = new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.US
                            ).format(Calendar.getInstance().getTime());

                            if (appointments != null) {
                                for (Appointment appointment : appointments) {
                                    String date = appointment.getAppointmentDate();
                                    String status = appointment.getStatus();

                                    boolean active =
                                            status == null
                                                    || (!status.equalsIgnoreCase("cancelled")
                                                    && !status.equalsIgnoreCase("completed"));

                                    if (date != null
                                            && !date.isEmpty()
                                            && date.compareTo(today) >= 0
                                            && active) {
                                        upcoming.add(appointment);
                                    }
                                }
                            }

                            Collections.sort(
                                    upcoming,
                                    Comparator.comparing(
                                            appointment ->
                                                    String.valueOf(
                                                            appointment.getAppointmentDate()
                                                    ) + " " +
                                                            String.valueOf(
                                                                    appointment.getAppointmentTime()
                                                            )
                                    )
                            );

                            upcomingAdapter.setAppointments(upcoming);

                            boolean empty = upcoming.isEmpty();

                            tvNoUpcoming.setVisibility(
                                    empty ? View.VISIBLE : View.GONE
                            );

                            rvUpcoming.setVisibility(
                                    empty ? View.GONE : View.VISIBLE
                            );

                            if (empty) {
                                tvNoUpcoming.setText(
                                        "No upcoming appointments."
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            rvUpcoming.setVisibility(View.GONE);
                            tvNoUpcoming.setVisibility(View.VISIBLE);
                            tvNoUpcoming.setText(
                                    "Could not load appointments: " + message
                            );
                        });
                    }
                }
        );
    }
}