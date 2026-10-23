package com.example.teledentistry_project.ui.dentist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailabilityRequest;
import com.example.teledentistry_project.data.repository.PractitionerAvailabilityRepository;

import java.util.Calendar;
import java.util.Locale;

public class PractitionerAvailabilityActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerRepository practitionerRepository;

    private Button btnSelectDate;
    private Button btnSelectStartTime;
    private Button btnSelectEndTime;
    private Button btnSaveAvailability;

    private LinearLayout layoutAvailabilityList;
    private TextView tvNoAvailability;

    private String practitionerId;

    private String selectedDate;
    private String selectedStartTime;
    private String selectedEndTime;

    private PractitionerAvailabilityRepository availabilityRepository;

    private java.util.List<PractitionerAvailability> currentAvailabilityList =
            new java.util.ArrayList<>();


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

        setContentView(R.layout.activity_practitioner_availability);

        practitionerRepository = new PractitionerRepository();

        availabilityRepository =
                new PractitionerAvailabilityRepository();

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        btnSaveAvailability = findViewById(R.id.btnSaveAvailability);

        layoutAvailabilityList =
                findViewById(R.id.layoutAvailabilityList);

        tvNoAvailability =
                findViewById(R.id.tvNoAvailability);

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSelectStartTime.setOnClickListener(
                v -> showStartTimePicker()
        );

        btnSelectEndTime.setOnClickListener(
                v -> showEndTimePicker()
        );

        // Saving will be implemented in the next step
        btnSaveAvailability.setOnClickListener(v ->
                saveAvailability()
        );

        loadPractitioner();
    }

    private void loadPractitioner() {

        String authToken = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();

        practitionerRepository.getPractitionerByUserId(
                authToken,
                userId,
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {

                        practitionerId = practitioner.getId();

                        loadAvailability();
                    }

                    @Override
                    public void onNotFound() {

                        Toast.makeText(
                                PractitionerAvailabilityActivity.this,
                                "Practitioner profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PractitionerAvailabilityActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void loadAvailability() {

        if (practitionerId == null || practitionerId.isEmpty()) {
            return;
        }

        availabilityRepository.getAvailabilityByPractitioner(
                sessionManager.getAuthToken(),
                practitionerId,
                new PractitionerAvailabilityRepository.AvailabilityListCallback() {

                    @Override
                    public void onSuccess(
                            java.util.List<PractitionerAvailability> availabilityList
                    ) {

                        currentAvailabilityList.clear();

                        if (availabilityList != null) {
                            currentAvailabilityList.addAll(availabilityList);
                        }

                        displayAvailability(currentAvailabilityList);
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PractitionerAvailabilityActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void displayAvailability(
            java.util.List<PractitionerAvailability> availabilityList
    ) {

        layoutAvailabilityList.removeAllViews();

        if (availabilityList == null || availabilityList.isEmpty()) {

            tvNoAvailability.setVisibility(TextView.VISIBLE);

            return;
        }

        tvNoAvailability.setVisibility(TextView.GONE);

        for (PractitionerAvailability availability : availabilityList) {

            TextView availabilityView = new TextView(this);

            String formattedDate =
                    formatDate(availability.getDate());

            String displayText =
                    formattedDate
                            + "\n"
                            + availability.getStartTime()
                            + " - "
                            + availability.getEndTime();

            availabilityView.setText(displayText);
            availabilityView.setTextSize(16);
            availabilityView.setPadding(
                    24,
                    24,
                    24,
                    24
            );

            layoutAvailabilityList.addView(availabilityView);
        }
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    selectedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                    );

                    btnSelectDate.setText(selectedDate);
                },
                year,
                month,
                day
        );

        // Prevent selecting dates in the past
        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        dialog.show();
    }

    private void showStartTimePicker() {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {

                    selectedStartTime = String.format(
                            Locale.US,
                            "%02d:%02d",
                            selectedHour,
                            selectedMinute
                    );

                    btnSelectStartTime.setText(selectedStartTime);
                },
                hour,
                minute,
                true
        );

        dialog.show();
    }

    private void showEndTimePicker() {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {

                    selectedEndTime = String.format(
                            Locale.US,
                            "%02d:%02d",
                            selectedHour,
                            selectedMinute
                    );

                    btnSelectEndTime.setText(selectedEndTime);
                },
                hour,
                minute,
                true
        );

        dialog.show();
    }

    private void saveAvailability() {

        if (practitionerId == null || practitionerId.isEmpty()) {
            practitionerId = sessionManager.getUserId();
            if (practitionerId == null || practitionerId.isEmpty()) {
                practitionerId = "default_prac_id";
            }
        }

        if (selectedDate == null) {
            Toast.makeText(
                    this,
                    "Please select a date.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedStartTime == null) {
            Toast.makeText(
                    this,
                    "Please select a start time.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedEndTime == null) {
            Toast.makeText(
                    this,
                    "Please select an end time.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedEndTime.compareTo(selectedStartTime) <= 0) {
            Toast.makeText(
                    this,
                    "End time must be after start time.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (hasOverlappingAvailability()) {

            Toast.makeText(
                    this,
                    "This availability overlaps with an existing time slot.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        PractitionerAvailabilityRequest request =
                new PractitionerAvailabilityRequest(
                        practitionerId,
                        selectedDate,
                        selectedStartTime,
                        selectedEndTime,
                        true
                );

        btnSaveAvailability.setEnabled(false);

        availabilityRepository.createAvailability(
                sessionManager.getAuthToken(),
                request,
                new PractitionerAvailabilityRepository.AvailabilityCreateCallback() {

                    @Override
                    public void onSuccess(
                            PractitionerAvailability availability
                    ) {
                        btnSaveAvailability.setEnabled(true);

                        Toast.makeText(
                                PractitionerAvailabilityActivity.this,
                                "Availability added successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearSelection();
                        loadAvailability();
                    }

                    @Override
                    public void onError(String message) {

                        btnSaveAvailability.setEnabled(true);

                        Toast.makeText(
                                PractitionerAvailabilityActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void clearSelection() {

        selectedDate = null;
        selectedStartTime = null;
        selectedEndTime = null;

        btnSelectDate.setText("Select Date");
        btnSelectStartTime.setText("Select Start Time");
        btnSelectEndTime.setText("Select End Time");
    }

    private boolean hasOverlappingAvailability() {

        for (PractitionerAvailability availability : currentAvailabilityList) {

            // Only compare slots on the selected date
            if (!selectedDate.equals(availability.getDate())) {
                continue;
            }

            String existingStart = availability.getStartTime();
            String existingEnd = availability.getEndTime();

            /*
             * Overlap exists when:
             *
             * newStart < existingEnd
             * AND
             * newEnd > existingStart
             */

            boolean overlaps =
                    selectedStartTime.compareTo(existingEnd) < 0
                            &&
                            selectedEndTime.compareTo(existingStart) > 0;

            if (overlaps) {
                return true;
            }
        }

        return false;
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
