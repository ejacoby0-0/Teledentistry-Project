package com.example.teledentistry_project.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.utils.SessionManager;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teledentistry_project.data.model.practitioner.PractitionerApplication;
import com.example.teledentistry_project.data.repository.PractitionerApplicationRepository;

import java.util.List;

public class PractitionerApprovalsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PractitionerApplicationRepository applicationRepository;

    private TextView tvApplicantName;
    private TextView tvApplicantDetails;
    private TextView tvApplicationStatus;

    private Button btnApprove;
    private Button btnReject;

    private PractitionerApplication currentApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // User must be logged in
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Only admins can access this screen
        if (!"admin".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Admin access required.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_practitioner_approvals);

        applicationRepository = new PractitionerApplicationRepository();

        tvApplicantName = findViewById(R.id.tvApplicantName);
        tvApplicantDetails = findViewById(R.id.tvApplicantDetails);
        tvApplicationStatus = findViewById(R.id.tvApplicationStatus);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        btnApprove.setOnClickListener(v -> approveCurrentApplication());
        btnReject.setOnClickListener(v -> rejectCurrentApplication());

        loadPendingApplications();
    }

    private void loadPendingApplications() {

        String authToken = sessionManager.getAuthToken();

        applicationRepository.getPendingApplications(
                authToken,
                new PractitionerApplicationRepository.ApplicationsCallback() {

                    @Override
                    public void onSuccess(List<PractitionerApplication> applications) {

                        if (applications == null || applications.isEmpty()) {

                            tvApplicantName.setText("No pending applications");
                            tvApplicantDetails.setText(
                                    "There are currently no practitioners awaiting approval."
                            );
                            tvApplicationStatus.setText("");

                            btnApprove.setEnabled(false);
                            btnReject.setEnabled(false);

                            return;
                        }

                        PractitionerApplication application = applications.get(0);

                        displayApplication(application);
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                PractitionerApprovalsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayApplication(PractitionerApplication application) {

        currentApplication = application;

        tvApplicantName.setText(application.getFullName());

        String details =
                "Reg #: " + application.getLicenseNumber()
                        + "\nSpecialization: " + application.getSpecialization()
                        + "\nPractice: "
                        + (
                        application.getPracticeName() != null
                                && !application.getPracticeName().isEmpty()
                                ? application.getPracticeName()
                                : "Not provided"
                );

        tvApplicantDetails.setText(details);

        tvApplicationStatus.setText(
                "Status: " + application.getStatus().toUpperCase()
        );
    }


    private void approveCurrentApplication() {

        if (currentApplication == null) {

            Toast.makeText(
                    this,
                    "No application selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);

        applicationRepository.approveApplication(
                sessionManager.getAuthToken(),
                currentApplication.getId(),
                new PractitionerApplicationRepository.ApplicationUpdateCallback() {

                    @Override
                    public void onSuccess(PractitionerApplication application) {

                        Toast.makeText(
                                PractitionerApprovalsActivity.this,
                                "Practitioner application approved.",
                                Toast.LENGTH_SHORT
                        ).show();

                        currentApplication = null;

                        // Reload the remaining pending applications
                        loadPendingApplications();
                    }

                    @Override
                    public void onError(String message) {

                        btnApprove.setEnabled(true);
                        btnReject.setEnabled(true);

                        Toast.makeText(
                                PractitionerApprovalsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }



    private void rejectCurrentApplication() {

        if (currentApplication == null) {

            Toast.makeText(
                    this,
                    "No application selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);

        applicationRepository.rejectApplication(
                sessionManager.getAuthToken(),
                currentApplication.getId(),
                new PractitionerApplicationRepository.ApplicationUpdateCallback() {

                    @Override
                    public void onSuccess(PractitionerApplication application) {

                        Toast.makeText(
                                PractitionerApprovalsActivity.this,
                                "Practitioner application rejected.",
                                Toast.LENGTH_SHORT
                        ).show();

                        currentApplication = null;

                        // Reload remaining pending applications
                        loadPendingApplications();
                    }

                    @Override
                    public void onError(String message) {

                        btnApprove.setEnabled(true);
                        btnReject.setEnabled(true);

                        Toast.makeText(
                                PractitionerApprovalsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

}