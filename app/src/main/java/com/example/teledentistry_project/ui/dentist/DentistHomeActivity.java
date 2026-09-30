package com.example.teledentistry_project.ui.dentist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.patient.PatientProfileActivity;
import com.example.teledentistry_project.ui.patient.VideoCallActivity;

public class DentistHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dentist_home);

        Button btnStartConsultation = findViewById(R.id.btnStartConsultation);
        Button btnViewAppointments = findViewById(R.id.btnViewAppointments);
        Button btnPatientRecords = findViewById(R.id.btnPatientRecords);
        Button btnUploadedImages = findViewById(R.id.btnUploadedImages);
        Button btnTreatmentManagement = findViewById(R.id.btnTreatmentManagement);
        Button btnProfile = findViewById(R.id.btnPractitionerProfile);

        if (btnStartConsultation != null) {
            btnStartConsultation.setOnClickListener(v -> startActivity(new Intent(this, VideoCallActivity.class)));
        }

        if (btnViewAppointments != null) {
            btnViewAppointments.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        }

        if (btnPatientRecords != null) {
            btnPatientRecords.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        }

        if (btnUploadedImages != null) {
            btnUploadedImages.setOnClickListener(v -> startActivity(new Intent(this, UploadedImagesActivity.class)));
        }

        if (btnTreatmentManagement != null) {
            btnTreatmentManagement.setOnClickListener(v -> startActivity(new Intent(this, TreatmentManagementActivity.class)));
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileActivity.class)));
        }
    }
}
