package com.example.teledentistry_project.ui.patient;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.dental.DentalRecord;
import com.example.teledentistry_project.data.model.dental.Tooth;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.DentalRecordRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.data.repository.ToothRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PatientDentalRecordActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private DentalRecordRepository dentalRecordRepository;
    private ToothRepository toothRepository;

    private TextView tvPatientDentalInfo;
    private TextView tvPatientDentalRecordStatus;
    private LinearLayout layoutPatientTeeth;

    private String patientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Must be logged in
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Patient-only screen
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
                R.layout.activity_patient_dental_record
        );


        tvPatientDentalInfo =
                findViewById(
                        R.id.tvPatientDentalInfo
                );

        tvPatientDentalRecordStatus =
                findViewById(
                        R.id.tvPatientDentalRecordStatus
                );

        layoutPatientTeeth =
                findViewById(
                        R.id.layoutPatientTeeth
                );


        patientRepository =
                new PatientRepository();

        dentalRecordRepository =
                new DentalRecordRepository();

        toothRepository =
                new ToothRepository();


        loadPatient();
    }


    // ---------------------------------------------------------
    // 1. Find the Patient record belonging to the logged-in user
    // ---------------------------------------------------------

    private void loadPatient() {

        tvPatientDentalRecordStatus.setText(
                "Loading patient information..."
        );


        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(
                            Patient patient
                    ) {

                        patientId =
                                patient.getId();


                        String patientName =
                                patient.getFullName();

                        if (patientName == null
                                || patientName.trim().isEmpty()) {

                            patientName =
                                    "Patient";
                        }


                        tvPatientDentalInfo.setText(
                                patientName
                        );


                        loadDentalRecord();
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientDentalRecordStatus.setText(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientDentalRecordStatus.setText(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientDentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 2. Load this patient's dental record
    // ---------------------------------------------------------

    private void loadDentalRecord() {

        tvPatientDentalRecordStatus.setText(
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

                        tvPatientDentalRecordStatus.setText(
                                "Dental record found."
                        );


                        loadTeeth(
                                dentalRecord.getId()
                        );
                    }


                    @Override
                    public void onNotFound() {

                        tvPatientDentalRecordStatus.setText(
                                "No dental record has been created yet."
                        );

                        layoutPatientTeeth.removeAllViews();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientDentalRecordStatus.setText(
                                "Unable to load dental record."
                        );

                        Toast.makeText(
                                PatientDentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 3. Load teeth belonging to the dental record
    // ---------------------------------------------------------

    private void loadTeeth(
            String dentalRecordId
    ) {

        toothRepository.getTeethByDentalRecord(
                sessionManager.getAuthToken(),
                dentalRecordId,
                new ToothRepository.ToothListCallback() {

                    @Override
                    public void onSuccess(
                            List<Tooth> teeth
                    ) {

                        layoutPatientTeeth.removeAllViews();


                        if (teeth == null
                                || teeth.isEmpty()) {

                            tvPatientDentalRecordStatus.setText(
                                    "Dental record found. No tooth records added yet."
                            );

                            return;
                        }


                        tvPatientDentalRecordStatus.setText(
                                teeth.size()
                                        + " tooth record(s)"
                        );


                        for (Tooth tooth : teeth) {

                            displayTooth(
                                    tooth
                            );
                        }
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tvPatientDentalRecordStatus.setText(
                                "Unable to load tooth records."
                        );

                        Toast.makeText(
                                PatientDentalRecordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // 4. Display a read-only tooth card
    // ---------------------------------------------------------

    private void displayTooth(
            Tooth tooth
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

        card.setLayoutParams(
                cardParams
        );

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


        // Tooth number
        TextView tvToothNumber =
                new TextView(this);

        tvToothNumber.setText(
                "Tooth #"
                        + safeText(
                        tooth.getToothNumber()
                )
        );

        tvToothNumber.setTextSize(18);

        tvToothNumber.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(
                tvToothNumber
        );


        // Condition
        addText(
                content,
                "Condition: "
                        + safeText(
                        tooth.getCondition()
                )
        );


        // Treatment history
        addText(
                content,
                "Treatment History: "
                        + safeText(
                        tooth.getTreatmentHistory()
                )
        );


        card.addView(
                content
        );

        layoutPatientTeeth.addView(
                card
        );
    }


    private void addText(
            LinearLayout layout,
            String text
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(15);

        textView.setPadding(
                0,
                10,
                0,
                0
        );

        layout.addView(
                textView
        );
    }


    private String safeText(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not specified";
        }

        return value;
    }
}
