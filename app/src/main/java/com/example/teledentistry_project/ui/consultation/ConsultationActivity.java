package com.example.teledentistry_project.ui.consultation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.consultation.Consultation;
import com.example.teledentistry_project.data.model.consultation.ConsultationRequest;
import com.example.teledentistry_project.data.repository.ConsultationRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class ConsultationActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ConsultationRepository consultationRepository;

    private TextView tvAppointmentInfo;
    private TextInputEditText etDiagnosisNotes;
    private Button btnSaveConsultation;

    private String appointmentId;
    private String appointmentDate;
    private String appointmentStartTime;
    private String appointmentEndTime;

    private Consultation existingConsultation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Only practitioners can edit consultations
        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_consultation);

        consultationRepository =
                new ConsultationRepository();

        tvAppointmentInfo =
                findViewById(R.id.tvAppointmentInfo);

        etDiagnosisNotes =
                findViewById(R.id.etDiagnosisNotes);

        btnSaveConsultation =
                findViewById(R.id.btnSaveConsultation);


        // Receive appointment information
        appointmentId =
                getIntent().getStringExtra("appointmentId");

        appointmentDate =
                getIntent().getStringExtra("appointmentDate");

        appointmentStartTime =
                getIntent().getStringExtra("appointmentStartTime");

        appointmentEndTime =
                getIntent().getStringExtra("appointmentEndTime");


        if (appointmentId == null
                || appointmentId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid appointment.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        displayAppointmentInfo();

        loadConsultation();


        btnSaveConsultation.setOnClickListener(v ->
                saveConsultation()
        );
    }


    private void displayAppointmentInfo() {

        String info =
                formatDate(appointmentDate)
                        + "\n"
                        + appointmentStartTime
                        + " - "
                        + appointmentEndTime;

        tvAppointmentInfo.setText(info);
    }


    private void loadConsultation() {

        consultationRepository.getConsultationByAppointment(
                sessionManager.getAuthToken(),
                appointmentId,
                new ConsultationRepository.ConsultationCallback() {

                    @Override
                    public void onSuccess(
                            Consultation consultation
                    ) {

                        existingConsultation =
                                consultation;

                        etDiagnosisNotes.setText(
                                consultation.getDiagnosisNotes()
                        );
                    }

                    @Override
                    public void onNotFound() {

                        existingConsultation = null;

                        etDiagnosisNotes.setText("");
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                ConsultationActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void saveConsultation() {

        String diagnosisNotes =
                etDiagnosisNotes.getText() != null
                        ? etDiagnosisNotes.getText()
                        .toString()
                        .trim()
                        : "";


        if (diagnosisNotes.isEmpty()) {

            etDiagnosisNotes.setError(
                    "Enter diagnosis or clinical notes."
            );

            return;
        }


        ConsultationRequest request =
                new ConsultationRequest(
                        appointmentId,
                        diagnosisNotes
                );


        btnSaveConsultation.setEnabled(false);


        if (existingConsultation == null) {

            createConsultation(request);

        } else {

            updateConsultation(request);
        }
    }


    private void createConsultation(
            ConsultationRequest request
    ) {

        consultationRepository.createConsultation(
                sessionManager.getAuthToken(),
                request,
                new ConsultationRepository.ConsultationCallback() {

                    @Override
                    public void onSuccess(
                            Consultation consultation
                    ) {

                        existingConsultation =
                                consultation;

                        btnSaveConsultation.setEnabled(true);

                        Toast.makeText(
                                ConsultationActivity.this,
                                "Consultation saved.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotFound() {
                        btnSaveConsultation.setEnabled(true);
                    }

                    @Override
                    public void onError(String message) {

                        btnSaveConsultation.setEnabled(true);

                        Toast.makeText(
                                ConsultationActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void updateConsultation(
            ConsultationRequest request
    ) {

        consultationRepository.updateConsultation(
                sessionManager.getAuthToken(),
                existingConsultation.getId(),
                request,
                new ConsultationRepository.ConsultationCallback() {

                    @Override
                    public void onSuccess(
                            Consultation consultation
                    ) {

                        existingConsultation =
                                consultation;

                        btnSaveConsultation.setEnabled(true);

                        Toast.makeText(
                                ConsultationActivity.this,
                                "Consultation updated.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotFound() {
                        btnSaveConsultation.setEnabled(true);
                    }

                    @Override
                    public void onError(String message) {

                        btnSaveConsultation.setEnabled(true);

                        Toast.makeText(
                                ConsultationActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private String formatDate(String date) {

        if (date == null || date.isEmpty()) {
            return "Unknown date";
        }

        if (date.length() >= 10) {
            return date.substring(0, 10);
        }

        return date;
    }
}
