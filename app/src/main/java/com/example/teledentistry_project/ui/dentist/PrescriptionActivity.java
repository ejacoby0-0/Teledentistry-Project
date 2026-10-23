package com.example.teledentistry_project.ui.dentist;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.repository.PractitionerRepository;
import com.example.teledentistry_project.data.repository.PrescriptionRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.widget.EditText;

import com.example.teledentistry_project.data.model.prescription.PrescriptionRequest;
import com.example.teledentistry_project.data.model.prescription.PrescriptionUpdateRequest;

import java.util.Calendar;

import java.util.List;

public class PrescriptionActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PrescriptionRepository prescriptionRepository;

    private PractitionerRepository practitionerRepository;

    private TextView tvPrescriptionPatient;
    private TextView tvPrescriptionStatus;
    private LinearLayout layoutPrescriptions;
    private Button btnAddPrescription;

    private String patientId;
    private String patientName;
    private String practitionerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Must be logged in
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Practitioner-only management for now
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_prescription);

        prescriptionRepository =
                new PrescriptionRepository();

        practitionerRepository =
                new PractitionerRepository();

        tvPrescriptionPatient =
                findViewById(R.id.tvPrescriptionPatient);

        tvPrescriptionStatus =
                findViewById(R.id.tvPrescriptionStatus);

        layoutPrescriptions =
                findViewById(R.id.layoutPrescriptions);

        btnAddPrescription =
                findViewById(R.id.btnAddPrescription);

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

            tvPrescriptionPatient.setText(
                    "Patient: " + patientName
            );

        } else {

            tvPrescriptionPatient.setText(
                    "Patient ID: " + patientId
            );
        }

        btnAddPrescription.setEnabled(false);

        btnAddPrescription.setOnClickListener(v -> {

            if (practitionerId == null || practitionerId.isEmpty()) {

                Toast.makeText(
                        PrescriptionActivity.this,
                        "Practitioner profile is still loading.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showCreatePrescriptionDialog();
        });

        loadPractitioner();
        loadPrescriptions();
    }

    private void showCreatePrescriptionDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = 40;

        layout.setPadding(
                padding,
                20,
                padding,
                0
        );


        EditText etMedication = new EditText(this);
        etMedication.setHint("Medication");
        layout.addView(etMedication);


        EditText etDosage = new EditText(this);
        etDosage.setHint("Dosage / Instructions");
        layout.addView(etDosage);


        EditText etIssueDate = new EditText(this);
        etIssueDate.setHint("Issue Date");
        etIssueDate.setFocusable(false);
        etIssueDate.setClickable(true);
        layout.addView(etIssueDate);


        EditText etExpiryDate = new EditText(this);
        etExpiryDate.setHint("Expiry Date");
        etExpiryDate.setFocusable(false);
        etExpiryDate.setClickable(true);
        layout.addView(etExpiryDate);


        etIssueDate.setOnClickListener(v ->
                showDatePicker(etIssueDate)
        );

        etExpiryDate.setOnClickListener(v ->
                showDatePicker(etExpiryDate)
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Create Prescription")
                        .setView(layout)
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

                String medication =
                        etMedication.getText()
                                .toString()
                                .trim();

                String dosage =
                        etDosage.getText()
                                .toString()
                                .trim();

                String issueDate =
                        etIssueDate.getText()
                                .toString()
                                .trim();

                String expiryDate =
                        etExpiryDate.getText()
                                .toString()
                                .trim();


                if (medication.isEmpty()
                        || dosage.isEmpty()
                        || issueDate.isEmpty()
                        || expiryDate.isEmpty()) {

                    Toast.makeText(
                            PrescriptionActivity.this,
                            "Please complete all fields.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (expiryDate.compareTo(issueDate) < 0) {

                    Toast.makeText(
                            PrescriptionActivity.this,
                            "Expiry date cannot be before the issue date.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                createPrescription(
                        medication,
                        dosage,
                        issueDate,
                        expiryDate,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void showDatePicker(EditText target) {

        Calendar calendar =
                Calendar.getInstance();

        int year =
                calendar.get(Calendar.YEAR);

        int month =
                calendar.get(Calendar.MONTH);

        int day =
                calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog picker =
                new DatePickerDialog(
                        this,
                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            String date =
                                    String.format(
                                            java.util.Locale.US,
                                            "%04d-%02d-%02d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay
                                    );

                            target.setText(date);
                        },
                        year,
                        month,
                        day
                );

        picker.show();
    }

    private void createPrescription(
            String medication,
            String dosage,
            String issueDate,
            String expiryDate,
            AlertDialog dialog
    ) {

        PrescriptionRequest request =
                new PrescriptionRequest(
                        patientId,
                        practitionerId,
                        medication,
                        dosage,
                        issueDate,
                        expiryDate
                );


        prescriptionRepository.createPrescription(
                sessionManager.getAuthToken(),
                request,
                new PrescriptionRepository.PrescriptionCallback() {

                    @Override
                    public void onSuccess(
                            Prescription prescription
                    ) {

                        Toast.makeText(
                                PrescriptionActivity.this,
                                "Prescription created successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        // Refresh from PocketBase
                        loadPrescriptions();
                    }

                    @Override
                    public void onError(String message) {

                        new AlertDialog.Builder(
                                PrescriptionActivity.this
                        )
                                .setTitle(
                                        "Prescription Error"
                                )
                                .setMessage(message)
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }

    private void loadPractitioner() {

        String userId = sessionManager.getUserId();

        practitionerRepository.getPractitionerByUserId(
                sessionManager.getAuthToken(),
                userId,
                new PractitionerRepository.PractitionerCallback() {

                    @Override
                    public void onSuccess(Practitioner practitioner) {

                        if (practitioner == null
                                || practitioner.getId() == null
                                || practitioner.getId().isEmpty()) {

                            Toast.makeText(
                                    PrescriptionActivity.this,
                                    "Practitioner profile not found.",
                                    Toast.LENGTH_LONG
                            ).show();

                            btnAddPrescription.setEnabled(false);
                            return;
                        }

                        practitionerId = practitioner.getId();

                        // Practitioner is ready to create prescriptions
                        btnAddPrescription.setEnabled(true);
                    }

                    @Override
                    public void onNotFound() {

                        Toast.makeText(
                                PrescriptionActivity.this,
                                "Practitioner profile not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        btnAddPrescription.setEnabled(false);
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PrescriptionActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        btnAddPrescription.setEnabled(false);
                    }
                }
        );
    }


    private void loadPrescriptions() {

        tvPrescriptionStatus.setText(
                "Loading prescriptions..."
        );

        layoutPrescriptions.removeAllViews();

        prescriptionRepository.getPrescriptionsByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new PrescriptionRepository.PrescriptionListCallback() {

                    @Override
                    public void onSuccess(
                            List<Prescription> prescriptions
                    ) {

                        layoutPrescriptions.removeAllViews();

                        if (prescriptions == null
                                || prescriptions.isEmpty()) {

                            tvPrescriptionStatus.setText(
                                    "No prescriptions found."
                            );

                            return;
                        }

                        tvPrescriptionStatus.setText(
                                prescriptions.size()
                                        + " prescription(s)"
                        );

                        for (Prescription prescription
                                : prescriptions) {

                            displayPrescription(
                                    prescription
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {

                        tvPrescriptionStatus.setText(
                                "Unable to load prescriptions."
                        );

                        Toast.makeText(
                                PrescriptionActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void displayPrescription(
            Prescription prescription
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

        tvTitle.setText("Prescription");
        tvTitle.setTextSize(18);

        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(tvTitle);


        // Medication
        TextView tvMedication =
                new TextView(this);

        tvMedication.setText(
                "Medication: "
                        + safeText(
                        prescription.getMedication()
                )
        );

        tvMedication.setTextSize(16);
        tvMedication.setPadding(
                0,
                12,
                0,
                0
        );

        content.addView(tvMedication);


        // Dosage
        TextView tvDosage =
                new TextView(this);

        tvDosage.setText(
                "Dosage: "
                        + safeText(
                        prescription.getDosage()
                )
        );

        tvDosage.setTextSize(16);
        tvDosage.setPadding(
                0,
                8,
                0,
                0
        );

        content.addView(tvDosage);


        // Issue Date
        TextView tvIssueDate =
                new TextView(this);

        tvIssueDate.setText(
                "Issue Date: "
                        + safeText(
                        prescription.getIssueDate()
                )
        );

        tvIssueDate.setTextSize(16);
        tvIssueDate.setPadding(
                0,
                8,
                0,
                0
        );

        content.addView(tvIssueDate);


        // Expiry Date
        TextView tvExpiryDate =
                new TextView(this);

        tvExpiryDate.setText(
                "Expiry Date: "
                        + safeText(
                        prescription.getExpiryDate()
                )
        );

        tvExpiryDate.setTextSize(16);
        tvExpiryDate.setPadding(
                0,
                8,
                0,
                0
        );

        content.addView(tvExpiryDate);

        Button btnEdit = new Button(this);

        btnEdit.setText("Edit Prescription");
        btnEdit.setAllCaps(false);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(
                0,
                24,
                0,
                0
        );

        btnEdit.setLayoutParams(buttonParams);

        btnEdit.setOnClickListener(v ->
                showEditPrescriptionDialog(prescription)
        );

        content.addView(btnEdit);


        card.addView(content);

        layoutPrescriptions.addView(card);
    }

    private void showEditPrescriptionDialog(
            Prescription prescription
    ) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);


        EditText etMedication = new EditText(this);
        etMedication.setHint("Medication");
        etMedication.setText(
                prescription.getMedication()
        );
        layout.addView(etMedication);


        EditText etDosage = new EditText(this);
        etDosage.setHint("Dosage / Instructions");
        etDosage.setText(
                prescription.getDosage()
        );
        layout.addView(etDosage);


        EditText etIssueDate = new EditText(this);
        etIssueDate.setHint("Issue Date");
        etIssueDate.setFocusable(false);
        etIssueDate.setClickable(true);
        etIssueDate.setText(
                normaliseDate(
                        prescription.getIssueDate()
                )
        );
        layout.addView(etIssueDate);


        EditText etExpiryDate = new EditText(this);
        etExpiryDate.setHint("Expiry Date");
        etExpiryDate.setFocusable(false);
        etExpiryDate.setClickable(true);
        etExpiryDate.setText(
                normaliseDate(
                        prescription.getExpiryDate()
                )
        );
        layout.addView(etExpiryDate);


        etIssueDate.setOnClickListener(v ->
                showDatePicker(etIssueDate)
        );

        etExpiryDate.setOnClickListener(v ->
                showDatePicker(etExpiryDate)
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Prescription")
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String medication =
                        etMedication.getText()
                                .toString()
                                .trim();

                String dosage =
                        etDosage.getText()
                                .toString()
                                .trim();

                String issueDate =
                        etIssueDate.getText()
                                .toString()
                                .trim();

                String expiryDate =
                        etExpiryDate.getText()
                                .toString()
                                .trim();


                if (medication.isEmpty()
                        || dosage.isEmpty()
                        || issueDate.isEmpty()
                        || expiryDate.isEmpty()) {

                    Toast.makeText(
                            PrescriptionActivity.this,
                            "Please complete all fields.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (expiryDate.compareTo(issueDate) < 0) {

                    Toast.makeText(
                            PrescriptionActivity.this,
                            "Expiry date cannot be before the issue date.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                updatePrescription(
                        prescription.getId(),
                        medication,
                        dosage,
                        issueDate,
                        expiryDate,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private String normaliseDate(String date) {

        if (date == null || date.isEmpty()) {
            return "";
        }

        if (date.length() >= 10) {
            return date.substring(0, 10);
        }

        return date;
    }

    private void updatePrescription(
            String prescriptionId,
            String medication,
            String dosage,
            String issueDate,
            String expiryDate,
            AlertDialog dialog
    ) {

        PrescriptionUpdateRequest request =
                new PrescriptionUpdateRequest(
                        medication,
                        dosage,
                        issueDate,
                        expiryDate
                );


        prescriptionRepository.updatePrescription(
                sessionManager.getAuthToken(),
                prescriptionId,
                request,
                new PrescriptionRepository.PrescriptionCallback() {

                    @Override
                    public void onSuccess(
                            Prescription prescription
                    ) {

                        Toast.makeText(
                                PrescriptionActivity.this,
                                "Prescription updated successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        loadPrescriptions();
                    }


                    @Override
                    public void onError(String message) {

                        new AlertDialog.Builder(
                                PrescriptionActivity.this
                        )
                                .setTitle("Prescription Error")
                                .setMessage(message)
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
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
