package com.example.teledentistry_project.ui.dentist;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlan;
import com.example.teledentistry_project.data.repository.TreatmentPlanRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlanUpdateRequest;

import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.teledentistry_project.data.model.treatment.TreatmentPlanRequest;

import java.util.Calendar;

public class TreatmentPlanActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TreatmentPlanRepository treatmentPlanRepository;
    private PractitionerRepository practitionerRepository;

    private String practitionerId;

    private TextView tvTreatmentPatient;
    private TextView tvTreatmentPlanStatus;
    private LinearLayout layoutTreatmentPlans;
    private Button btnAddTreatmentPlan;

    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Practitioner-only editing for now
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_treatment_plan);

        treatmentPlanRepository =
                new TreatmentPlanRepository();

        practitionerRepository =
                new PractitionerRepository();

        tvTreatmentPatient =
                findViewById(R.id.tvTreatmentPatient);

        tvTreatmentPlanStatus =
                findViewById(R.id.tvTreatmentPlanStatus);

        layoutTreatmentPlans =
                findViewById(R.id.layoutTreatmentPlans);

        btnAddTreatmentPlan =
                findViewById(R.id.btnAddTreatmentPlan);

        btnAddTreatmentPlan.setOnClickListener(v ->
                showCreateTreatmentPlanDialog()
        );

        patientId =
                getIntent().getStringExtra("patientId");

        patientName =
                getIntent().getStringExtra("patientName");

        if (patientId == null || patientId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid patient.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        if (patientName != null
                && !patientName.isEmpty()) {

            tvTreatmentPatient.setText(
                    "Patient: " + patientName
            );

        } else {

            tvTreatmentPatient.setText(
                    "Patient ID: " + patientId
            );
        }

        btnAddTreatmentPlan.setEnabled(false);

        loadPractitioner();
        loadTreatmentPlans();
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

                        btnAddTreatmentPlan.setEnabled(true);
                    }

                    @Override
                    public void onNotFound() {

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                "Practitioner profile not found.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        android.util.Log.e(
                                "TREATMENT_UPDATE",
                                message
                        );

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void loadTreatmentPlans() {

        tvTreatmentPlanStatus.setText(
                "Loading treatment plans..."
        );

        layoutTreatmentPlans.removeAllViews();

        treatmentPlanRepository.getTreatmentPlansByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new TreatmentPlanRepository.TreatmentPlanListCallback() {

                    @Override
                    public void onSuccess(
                            List<TreatmentPlan> treatmentPlans
                    ) {

                        layoutTreatmentPlans.removeAllViews();

                        if (treatmentPlans == null
                                || treatmentPlans.isEmpty()) {

                            tvTreatmentPlanStatus.setText(
                                    "No treatment plans found."
                            );

                            return;
                        }

                        tvTreatmentPlanStatus.setText(
                                treatmentPlans.size()
                                        + " treatment plan(s)"
                        );

                        for (TreatmentPlan treatmentPlan
                                : treatmentPlans) {

                            displayTreatmentPlan(
                                    treatmentPlan
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {

                        tvTreatmentPlanStatus.setText(
                                "Unable to load treatment plans."
                        );

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void displayTreatmentPlan(
            TreatmentPlan treatmentPlan
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


        // Heading
        TextView tvTitle =
                new TextView(this);

        tvTitle.setText(
                "Treatment Plan"
        );

        tvTitle.setTextSize(18);
        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(tvTitle);


        // Start date
        TextView tvStartDate =
                new TextView(this);

        tvStartDate.setText(
                "Start Date: "
                        + safeText(
                        treatmentPlan.getStartDate()
                )
        );

        tvStartDate.setTextSize(16);
        tvStartDate.setPadding(
                0,
                12,
                0,
                0
        );

        content.addView(tvStartDate);

        Button btnEdit =
                new Button(this);

        btnEdit.setText("Edit Treatment Plan");

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        editParams.setMargins(
                0,
                20,
                0,
                0
        );

        btnEdit.setLayoutParams(editParams);

        btnEdit.setOnClickListener(v ->
                showEditTreatmentPlanDialog(
                        treatmentPlan
                )
        );

        content.addView(btnEdit);

        // Status
        TextView tvStatus =
                new TextView(this);

        tvStatus.setText(
                "Status: "
                        + safeText(
                        treatmentPlan.getStatus()
                ).toUpperCase()
        );

        tvStatus.setTextSize(16);
        tvStatus.setPadding(
                0,
                8,
                0,
                0
        );

        content.addView(tvStatus);


        // Cost
        TextView tvCost =
                new TextView(this);

        tvCost.setText(
                String.format(
                        Locale.getDefault(),
                        "Total Cost: R%.2f",
                        treatmentPlan.getTotalCost()
                )
        );

        tvCost.setTextSize(16);
        tvCost.setPadding(
                0,
                8,
                0,
                0
        );

        content.addView(tvCost);


        card.addView(content);

        layoutTreatmentPlans.addView(card);
    }

    private void showEditTreatmentPlanDialog(
            TreatmentPlan treatmentPlan
    ) {

        LinearLayout dialogLayout =
                new LinearLayout(this);

        dialogLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        dialogLayout.setPadding(
                40,
                16,
                40,
                0
        );


        // Start Date
        EditText etStartDate =
                new EditText(this);

        etStartDate.setHint("Start Date");
        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);

        etStartDate.setText(
                treatmentPlan.getStartDate()
        );

        dialogLayout.addView(etStartDate);


        etStartDate.setOnClickListener(v -> {

            Calendar calendar =
                    Calendar.getInstance();

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            TreatmentPlanActivity.this,
                            (view, year, month, dayOfMonth) -> {

                                String selectedDate =
                                        String.format(
                                                Locale.getDefault(),
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                dayOfMonth
                                        );

                                etStartDate.setText(
                                        selectedDate
                                );
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            datePickerDialog.show();
        });


        // Total Cost
        EditText etTotalCost =
                new EditText(this);

        etTotalCost.setHint(
                "Total Cost (R)"
        );

        etTotalCost.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        etTotalCost.setText(
                String.valueOf(
                        treatmentPlan.getTotalCost()
                )
        );

        dialogLayout.addView(etTotalCost);


        // Status
        Spinner spinnerStatus =
                new Spinner(this);

        String[] statuses = {
                "planned",
                "active",
                "completed",
                "cancelled"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statuses
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerStatus.setAdapter(adapter);


        // Select the current status
        for (int i = 0; i < statuses.length; i++) {

            if (statuses[i].equalsIgnoreCase(
                    treatmentPlan.getStatus()
            )) {

                spinnerStatus.setSelection(i);
                break;
            }
        }

        dialogLayout.addView(spinnerStatus);


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Treatment Plan")
                        .setView(dialogLayout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save Changes",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String startDate =
                        etStartDate
                                .getText()
                                .toString()
                                .trim();

                String totalCostText =
                        etTotalCost
                                .getText()
                                .toString()
                                .trim();

                String status =
                        spinnerStatus
                                .getSelectedItem()
                                .toString();


                if (startDate.isEmpty()) {

                    etStartDate.setError(
                            "Start date is required."
                    );

                    return;
                }


                if (totalCostText.isEmpty()) {

                    etTotalCost.setError(
                            "Total cost is required."
                    );

                    return;
                }


                double totalCost;

                try {

                    totalCost =
                            Double.parseDouble(
                                    totalCostText
                            );

                } catch (NumberFormatException e) {

                    etTotalCost.setError(
                            "Enter a valid amount."
                    );

                    return;
                }


                if (totalCost < 0) {

                    etTotalCost.setError(
                            "Cost cannot be negative."
                    );

                    return;
                }


                updateTreatmentPlan(
                        treatmentPlan,
                        startDate,
                        totalCost,
                        status,
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void updateTreatmentPlan(
            TreatmentPlan treatmentPlan,
            String startDate,
            double totalCost,
            String status,
            AlertDialog dialog
    ) {

        TreatmentPlanUpdateRequest request =
                new TreatmentPlanUpdateRequest(
                        totalCost,
                        startDate,
                        status
                );

        treatmentPlanRepository.updateTreatmentPlan(
                sessionManager.getAuthToken(),
                treatmentPlan.getId(),
                request,
                new TreatmentPlanRepository.TreatmentPlanCallback() {

                    @Override
                    public void onSuccess(
                            TreatmentPlan updatedTreatmentPlan
                    ) {

                        dialog.dismiss();

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                "Treatment plan updated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadTreatmentPlans();
                    }

                    @Override
                    public void onError(String message) {

                        new AlertDialog.Builder(
                                TreatmentPlanActivity.this
                        )
                                .setTitle("Treatment Plan Update Error")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
        );
    }

    private void showCreateTreatmentPlanDialog() {

        if (practitionerId == null
                || practitionerId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Practitioner profile is not ready.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LinearLayout dialogLayout =
                new LinearLayout(this);

        dialogLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        dialogLayout.setPadding(
                40,
                16,
                40,
                0
        );


        // Start Date
        EditText etStartDate =
                new EditText(this);

        etStartDate.setHint("Start Date");
        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);

        dialogLayout.addView(etStartDate);


        etStartDate.setOnClickListener(v -> {

            Calendar calendar =
                    Calendar.getInstance();

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            TreatmentPlanActivity.this,
                            (view, year, month, dayOfMonth) -> {

                                String selectedDate =
                                        String.format(
                                                Locale.getDefault(),
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                dayOfMonth
                                        );

                                etStartDate.setText(
                                        selectedDate
                                );
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            datePickerDialog.show();
        });


        // Total Cost
        EditText etTotalCost =
                new EditText(this);

        etTotalCost.setHint(
                "Total Cost (R)"
        );

        etTotalCost.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        dialogLayout.addView(etTotalCost);


        // Status
        Spinner spinnerStatus =
                new Spinner(this);

        String[] statuses = {
                "planned",
                "in_progress",
                "completed",
                "cancelled"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statuses
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerStatus.setAdapter(adapter);

        dialogLayout.addView(spinnerStatus);


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Create Treatment Plan")
                        .setView(dialogLayout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Create",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String startDate =
                        etStartDate
                                .getText()
                                .toString()
                                .trim();

                String totalCostText =
                        etTotalCost
                                .getText()
                                .toString()
                                .trim();

                String status =
                        spinnerStatus
                                .getSelectedItem()
                                .toString();


                if (startDate.isEmpty()) {

                    etStartDate.setError(
                            "Start date is required."
                    );

                    return;
                }


                if (totalCostText.isEmpty()) {

                    etTotalCost.setError(
                            "Total cost is required."
                    );

                    return;
                }


                double totalCost;

                try {

                    totalCost =
                            Double.parseDouble(
                                    totalCostText
                            );

                } catch (NumberFormatException e) {

                    etTotalCost.setError(
                            "Enter a valid amount."
                    );

                    return;
                }


                if (totalCost < 0) {

                    etTotalCost.setError(
                            "Cost cannot be negative."
                    );

                    return;
                }


                createTreatmentPlan(
                        startDate,
                        totalCost,
                        status,
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void createTreatmentPlan(
            String startDate,
            double totalCost,
            String status,
            AlertDialog dialog
    ) {

        TreatmentPlanRequest request =
                new TreatmentPlanRequest(
                        patientId,
                        practitionerId,
                        totalCost,
                        startDate,
                        status
                );

        treatmentPlanRepository.createTreatmentPlan(
                sessionManager.getAuthToken(),
                request,
                new TreatmentPlanRepository.TreatmentPlanCallback() {

                    @Override
                    public void onSuccess(
                            TreatmentPlan treatmentPlan
                    ) {

                        dialog.dismiss();

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                "Treatment plan created.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadTreatmentPlans();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                TreatmentPlanActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private String safeText(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not specified";
        }

        return value;
    }
}