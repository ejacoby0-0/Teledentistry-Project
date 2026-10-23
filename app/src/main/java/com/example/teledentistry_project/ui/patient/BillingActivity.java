package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.teledentistry_project.data.model.receipt.Receipt;
import com.example.teledentistry_project.data.model.receipt.ReceiptCreateRequest;
import com.example.teledentistry_project.data.repository.ReceiptRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillingActivity extends AppCompatActivity {

    public static final String EXTRA_PRACTITIONER_USER_ID =
            "practitionerUserId";
    public static final String EXTRA_SESSION_COST = "sessionCost";

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private TextView tvNoTimes;
    private TextView tvSessionCost;
    private TextView tvTotal;
    private ChipGroup chipGroupTimes;
    private MaterialButton btnConfirmPay;
    private TextView btnBack;

    private String authToken;
    private String patientId;
    private String practitionerUserId;
    private String selectedDate;
    private String selectedAvailabilityId;

    private final ReceiptRepository receiptRepository =
            new ReceiptRepository();

    private double sessionCost;

    private final List<PractitionerAvailability> availableSlots =
            new ArrayList<>();

    private boolean bookingInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        bindViews();

        practitionerUserId = getIntent().getStringExtra(
                EXTRA_PRACTITIONER_USER_ID
        );

        sessionCost = getIntent().getDoubleExtra(
                EXTRA_SESSION_COST, 0.0
        );

        tvSessionCost.setText(formatMoney(sessionCost));
        tvTotal.setText(formatMoney(sessionCost));

        android.content.SharedPreferences prefs =
                getSharedPreferences("auth", MODE_PRIVATE);

        authToken = prefs.getString("authToken", null);
        patientId = prefs.getString("userId", null);

        if (authToken == null || patientId == null
                || practitionerUserId == null
                || practitionerUserId.trim().isEmpty()) {
            showMessage("Missing login or practitioner information.");
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());
        btnConfirmPay.setOnClickListener(v -> bookSelectedSlot());

        calendarView.setMinDate(System.currentTimeMillis());

        calendarView.setOnDateChangeListener(
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);

                    selectedDate = new SimpleDateFormat(
                            "yyyy-MM-dd", Locale.US
                    ).format(calendar.getTime());

                    tvSelectedDate.setText(
                            new SimpleDateFormat(
                                    "EEEE, dd MMMM yyyy", Locale.getDefault()
                            ).format(calendar.getTime())
                    );

                    selectedAvailabilityId = null;
                    loadSlotsForDate(selectedDate);
                }
        );

        Calendar today = Calendar.getInstance();
        selectedDate = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US
        ).format(today.getTime());

        tvSelectedDate.setText(
                new SimpleDateFormat(
                        "EEEE, dd MMMM yyyy", Locale.getDefault()
                ).format(today.getTime())
        );

        loadSlotsForDate(selectedDate);
    }

    private void bindViews() {
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvNoTimes = findViewById(R.id.tvNoTimes);
        tvSessionCost = findViewById(R.id.tvSessionCost);
        tvTotal = findViewById(R.id.tvTotal);
        chipGroupTimes = findViewById(R.id.chipGroupTimes);
        btnConfirmPay = findViewById(R.id.btnConfirmPay);
        btnBack = findViewById(R.id.btnBack);

        chipGroupTimes.removeAllViews();
        btnConfirmPay.setEnabled(false);
    }

    private void loadSlotsForDate(String date) {
        chipGroupTimes.removeAllViews();
        availableSlots.clear();
        selectedAvailabilityId = null;
        btnConfirmPay.setEnabled(false);
        tvNoTimes.setVisibility(View.GONE);

        /*
         * This date range assumes PocketBase stores the date field
         * as a date/time value. Adjust the filter if your field is
         * configured or formatted differently.
         */
        String nextDate = nextDay(date);

        String availabilityFilter =
                "practitioner = '" + practitionerUserId + "'"
                        + " && date >= '" + date + " 00:00:00.000Z'"
                        + " && date < '" + nextDate + " 00:00:00.000Z'"
                        + " && status = 'available'";

        PocketBaseClient.api.getPractitionerAvailability(
                authToken,
                availabilityFilter,
                "startTime"
        ).enqueue(
                new Callback<PocketBaseListResponse<PractitionerAvailability>>() {
                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<PractitionerAvailability>> call,
                            Response<PocketBaseListResponse<PractitionerAvailability>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getItems() == null) {
                            showNoTimes("Could not load available times.");
                            return;
                        }

                        List<PractitionerAvailability> results =
                                response.body().getItems();

                        if (results.isEmpty()) {
                            showNoTimes("No times available on this date.");
                            return;
                        }

                        loadBookedAppointments(results);
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<PractitionerAvailability>> call,
                            Throwable t) {
                        showNoTimes("Could not load available times.");
                    }
                }
        );
    }

    private void loadBookedAppointments(
            List<PractitionerAvailability> slots) {

        String appointmentFilter =
                "practitioner = '" + practitionerUserId
                        + "' && status = 'booked'";

        PocketBaseClient.api.getAppointments(
                authToken,
                appointmentFilter,
                ""
        ).enqueue(new Callback<PocketBaseListResponse<Appointment>>() {
            @Override
            public void onResponse(
                    Call<PocketBaseListResponse<Appointment>> call,
                    Response<PocketBaseListResponse<Appointment>> response) {

                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().getItems() == null) {
                    showNoTimes("Could not verify which times are booked.");
                    return;
                }

                Set<String> bookedAvailabilityIds = new HashSet<>();

                for (Appointment appointment :
                        response.body().getItems()) {
                    if (appointment.getAvailability() != null) {
                        bookedAvailabilityIds.add(
                                appointment.getAvailability()
                        );
                    }
                }

                availableSlots.clear();

                for (PractitionerAvailability slot : slots) {
                    if (!bookedAvailabilityIds.contains(slot.getId())) {
                        availableSlots.add(slot);
                    }
                }

                displayTimeChips();
            }

            @Override
            public void onFailure(
                    Call<PocketBaseListResponse<Appointment>> call,
                    Throwable t) {
                showNoTimes("Could not verify booked times.");
            }
        });
    }

    private void displayTimeChips() {
        chipGroupTimes.removeAllViews();
        selectedAvailabilityId = null;
        btnConfirmPay.setEnabled(false);

        if (availableSlots.isEmpty()) {
            showNoTimes("No times available on this date.");
            return;
        }

        tvNoTimes.setVisibility(View.GONE);

        for (PractitionerAvailability slot : availableSlots) {
            Chip chip = new Chip(this);

            chip.setText(slot.getStartTime());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(false);

            chipGroupTimes.addView(chip);

            chip.setOnClickListener(v -> {
                selectedAvailabilityId = slot.getId();
                btnConfirmPay.setEnabled(true);
            });
        }

        chipGroupTimes.setSingleSelection(true);
        chipGroupTimes.setSelectionRequired(false);
    }

    private void bookSelectedSlot() {
        if (bookingInProgress) return;

        if (selectedAvailabilityId == null) {
            showMessage("Please select an available time first.");
            return;
        }

        bookingInProgress = true;
        btnConfirmPay.setEnabled(false);

        AppointmentCreateRequest request =
                new AppointmentCreateRequest(
                        patientId,
                        practitionerUserId,
                        selectedAvailabilityId,
                        "booked"
                );

        PocketBaseClient.api.createAppointment(
                authToken,
                request
        ).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(
                    Call<Appointment> call,
                    Response<Appointment> response) {

                bookingInProgress = false;

                if (response.isSuccessful() && response.body() != null) {

                    createReceiptRecord();

                } else {
                    btnConfirmPay.setEnabled(
                            selectedAvailabilityId != null
                    );

                    showMessage(
                            "Booking failed. HTTP " + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<Appointment> call,
                    Throwable t) {
                bookingInProgress = false;
                btnConfirmPay.setEnabled(
                        selectedAvailabilityId != null
                );
                showMessage("Booking failed: " + t.getMessage());
            }
        });
    }

    private String nextDay(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.US
            );
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            Calendar calendar = Calendar.getInstance(
                    TimeZone.getTimeZone("UTC")
            );
            calendar.setTime(format.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            return format.format(calendar.getTime());
        } catch (Exception e) {
            return date;
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "R%.2f", amount);
    }

    private void showNoTimes(String message) {
        chipGroupTimes.removeAllViews();
        tvNoTimes.setText(message);
        tvNoTimes.setVisibility(View.VISIBLE);
        btnConfirmPay.setEnabled(false);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private void createReceiptRecord() {

        ReceiptCreateRequest request = new ReceiptCreateRequest(
                practitionerUserId,
                patientId,
                sessionCost
        );

        receiptRepository.createReceipt(
                authToken,
                request,
                new ReceiptRepository.ReceiptCallback() {

                    @Override
                    public void onSuccess(Receipt receipt) {
                        showMessage(
                                "Appointment booked and receipt created successfully."
                        );
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        // The appointment was already created successfully.
                        // Do not tell the patient that booking failed.
                        showMessage(
                                "Appointment booked, but the receipt could "
                                        + "not be created. Please contact support."
                        );

                        android.util.Log.e(
                                "BillingActivity",
                                "Receipt creation failed: " + message
                        );

                        finish();
                    }
                }
        );
    }

}
