package com.example.teledentistry_project.ui.dentist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.dental.DentalRecord;
import com.example.teledentistry_project.data.repository.DentalRecordRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.example.teledentistry_project.data.model.dental.Tooth;
import com.example.teledentistry_project.data.repository.ToothRepository;
import com.example.teledentistry_project.data.model.dental.ToothRequest;

import java.util.List;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

public class DentalRecordActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DentalRecordRepository dentalRecordRepository;

    private ToothRepository toothRepository;

    private TextView tvPatientInfo;
    private TextView tvDentalRecordStatus;
    private LinearLayout layoutTeeth;
    private Button btnAddTooth;

    private String patientId;
    private String patientName;

    private String dentalRecordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Only practitioners can manage dental records
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_dental_record);

        dentalRecordRepository =
                new DentalRecordRepository();

        toothRepository =
                new ToothRepository();

        tvPatientInfo =
                findViewById(R.id.tvPatientInfo);

        tvDentalRecordStatus =
                findViewById(R.id.tvDentalRecordStatus);

        layoutTeeth =
                findViewById(R.id.layoutTeeth);

        btnAddTooth =
                findViewById(R.id.btnAddTooth);


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

            tvPatientInfo.setText(
                    "Patient: " + patientName
            );

        } else {

            tvPatientInfo.setText(
                    "Patient ID: " + patientId
            );
        }


        btnAddTooth.setEnabled(false);

        loadDentalRecord();

        btnAddTooth.setOnClickListener(v -> showAddToothDialog());
    }


    private void loadDentalRecord() {

        tvDentalRecordStatus.setText(
                "Loading dental record..."
        );

        dentalRecordRepository.getDentalRecordByPatient(
                sessionManager.getAuthToken(),
                patientId,
                new DentalRecordRepository.DentalRecordCallback() {

                    @Override
                    public void onSuccess(
                            DentalRecord dentalRecord
                    ) {

                        handleDentalRecord(
                                dentalRecord
                        );
                    }

                    @Override
                    public void onNotFound() {

                        tvDentalRecordStatus.setText(
                                "Creating dental record..."
                        );

                        createDentalRecord();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        tvDentalRecordStatus.setText(
                                "Unable to load dental record."
                        );

                        Toast.makeText(
                                DentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void createDentalRecord() {

        dentalRecordRepository.createDentalRecord(
                sessionManager.getAuthToken(),
                patientId,
                new DentalRecordRepository.DentalRecordCallback() {

                    @Override
                    public void onSuccess(
                            DentalRecord dentalRecord
                    ) {

                        handleDentalRecord(
                                dentalRecord
                        );

                        Toast.makeText(
                                DentalRecordActivity.this,
                                "Dental record created.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotFound() {
                        // Not used when creating a record.
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        tvDentalRecordStatus.setText(
                                "Unable to create dental record."
                        );

                        Toast.makeText(
                                DentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void handleDentalRecord(
            DentalRecord dentalRecord
    ) {

        if (dentalRecord == null
                || dentalRecord.getId() == null
                || dentalRecord.getId().isEmpty()) {

            tvDentalRecordStatus.setText(
                    "Invalid dental record."
            );

            return;
        }

        dentalRecordId =
                dentalRecord.getId();

        tvDentalRecordStatus.setText(
                "Dental record available."
        );

        btnAddTooth.setEnabled(true);

        loadTeeth();
    }

    private void loadTeeth() {

        if (dentalRecordId == null
                || dentalRecordId.isEmpty()) {

            return;
        }

        layoutTeeth.removeAllViews();

        toothRepository.getTeethByDentalRecord(
                sessionManager.getAuthToken(),
                dentalRecordId,
                new ToothRepository.ToothListCallback() {

                    @Override
                    public void onSuccess(List<Tooth> teeth) {

                        layoutTeeth.removeAllViews();

                        if (teeth == null || teeth.isEmpty()) {

                            TextView tvEmpty =
                                    new TextView(
                                            DentalRecordActivity.this
                                    );

                            tvEmpty.setText(
                                    "No tooth records have been added yet."
                            );

                            tvEmpty.setTextSize(16);

                            tvEmpty.setPadding(
                                    0,
                                    16,
                                    0,
                                    16
                            );

                            layoutTeeth.addView(tvEmpty);

                            return;
                        }

                        for (Tooth tooth : teeth) {

                            displayTooth(tooth);
                        }
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                DentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayTooth(Tooth tooth) {

        LinearLayout toothContainer =
                new LinearLayout(this);

        toothContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        toothContainer.setPadding(
                24,
                24,
                24,
                24
        );

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        containerParams.setMargins(
                0,
                8,
                0,
                16
        );

        toothContainer.setLayoutParams(
                containerParams
        );


        // Tooth information
        TextView toothView =
                new TextView(this);

        String text =
                "Tooth #" + tooth.getToothNumber()
                        + "\nCondition: "
                        + safeText(tooth.getCondition())
                        + "\nTreatment History: "
                        + safeText(tooth.getTreatmentHistory());

        toothView.setText(text);
        toothView.setTextSize(16);

        toothContainer.addView(
                toothView
        );


        // Edit button
        Button btnEdit =
                new Button(this);

        btnEdit.setText(
                "Edit Tooth Record"
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(
                0,
                16,
                0,
                0
        );

        btnEdit.setLayoutParams(
                buttonParams
        );

        btnEdit.setOnClickListener(v ->
                showEditToothDialog(tooth)
        );

        toothContainer.addView(
                btnEdit
        );


        layoutTeeth.addView(
                toothContainer
        );
    }

    private void showEditToothDialog(
            Tooth tooth
    ) {

        LinearLayout dialogLayout =
                new LinearLayout(this);

        dialogLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = 40;

        dialogLayout.setPadding(
                padding,
                16,
                padding,
                0
        );


        EditText etToothNumber =
                new EditText(this);

        etToothNumber.setHint(
                "Tooth Number"
        );

        etToothNumber.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        etToothNumber.setText(
                tooth.getToothNumber()
        );

        dialogLayout.addView(
                etToothNumber
        );


        EditText etCondition =
                new EditText(this);

        etCondition.setHint(
                "Condition"
        );

        etCondition.setText(
                tooth.getCondition()
        );

        dialogLayout.addView(
                etCondition
        );


        EditText etTreatmentHistory =
                new EditText(this);

        etTreatmentHistory.setHint(
                "Treatment History"
        );

        etTreatmentHistory.setMinLines(3);

        etTreatmentHistory.setGravity(
                android.view.Gravity.TOP
        );

        etTreatmentHistory.setText(
                tooth.getTreatmentHistory()
        );

        dialogLayout.addView(
                etTreatmentHistory
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Tooth Record")
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

                String toothNumber =
                        etToothNumber
                                .getText()
                                .toString()
                                .trim();

                String condition =
                        etCondition
                                .getText()
                                .toString()
                                .trim();

                String treatmentHistory =
                        etTreatmentHistory
                                .getText()
                                .toString()
                                .trim();


                if (toothNumber.isEmpty()) {

                    etToothNumber.setError(
                            "Tooth number is required."
                    );

                    return;
                }


                if (condition.isEmpty()) {

                    etCondition.setError(
                            "Condition is required."
                    );

                    return;
                }


                updateTooth(
                        tooth,
                        toothNumber,
                        condition,
                        treatmentHistory,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void updateTooth(
            Tooth tooth,
            String toothNumber,
            String condition,
            String treatmentHistory,
            AlertDialog dialog
    ) {

        ToothRequest request =
                new ToothRequest(
                        dentalRecordId,
                        toothNumber,
                        condition,
                        treatmentHistory
                );

        toothRepository.updateTooth(
                sessionManager.getAuthToken(),
                tooth.getId(),
                request,
                new ToothRepository.ToothCallback() {

                    @Override
                    public void onSuccess(Tooth updatedTooth) {

                        dialog.dismiss();

                        Toast.makeText(
                                DentalRecordActivity.this,
                                "Tooth record updated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadTeeth();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                DentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String safeText(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "None recorded";
        }

        return value;
    }

    private void showAddToothDialog() {

        if (dentalRecordId == null
                || dentalRecordId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Dental record is not available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LinearLayout dialogLayout =
                new LinearLayout(this);

        dialogLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = 40;

        dialogLayout.setPadding(
                padding,
                16,
                padding,
                0
        );


        // Tooth Number
        EditText etToothNumber =
                new EditText(this);

        etToothNumber.setHint(
                "Tooth Number"
        );

        etToothNumber.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        dialogLayout.addView(
                etToothNumber
        );


        // Condition
        EditText etCondition =
                new EditText(this);

        etCondition.setHint(
                "Condition"
        );

        dialogLayout.addView(
                etCondition
        );


        // Treatment History
        EditText etTreatmentHistory =
                new EditText(this);

        etTreatmentHistory.setHint(
                "Treatment History"
        );

        etTreatmentHistory.setMinLines(3);

        etTreatmentHistory.setGravity(
                android.view.Gravity.TOP
        );

        dialogLayout.addView(
                etTreatmentHistory
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Add Tooth Record")
                        .setView(dialogLayout)
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

                String toothNumber =
                        etToothNumber
                                .getText()
                                .toString()
                                .trim();

                String condition =
                        etCondition
                                .getText()
                                .toString()
                                .trim();

                String treatmentHistory =
                        etTreatmentHistory
                                .getText()
                                .toString()
                                .trim();


                if (toothNumber.isEmpty()) {

                    etToothNumber.setError(
                            "Tooth number is required."
                    );

                    return;
                }


                if (condition.isEmpty()) {

                    etCondition.setError(
                            "Condition is required."
                    );

                    return;
                }


                createTooth(
                        toothNumber,
                        condition,
                        treatmentHistory,
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void createTooth(
            String toothNumber,
            String condition,
            String treatmentHistory,
            AlertDialog dialog
    ) {

        ToothRequest request =
                new ToothRequest(
                        dentalRecordId,
                        toothNumber,
                        condition,
                        treatmentHistory
                );

        toothRepository.createTooth(
                sessionManager.getAuthToken(),
                request,
                new ToothRepository.ToothCallback() {

                    @Override
                    public void onSuccess(Tooth tooth) {

                        dialog.dismiss();

                        Toast.makeText(
                                DentalRecordActivity.this,
                                "Tooth record added.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadTeeth();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                DentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


}