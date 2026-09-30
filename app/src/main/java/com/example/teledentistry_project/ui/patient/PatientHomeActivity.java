package com.example.teledentistry_project.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class PatientHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnJoinCall = findViewById(R.id.btnJoinCall);
        Button btnBookAppointment = findViewById(R.id.btnBookAppointment);
        Button btnChatDentist = findViewById(R.id.btnChatDentist);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnTreatmentPlan = findViewById(R.id.btnTreatmentPlan);
        Button btnBilling = findViewById(R.id.btnBilling);

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileActivity.class)));
        }

        if (btnJoinCall != null) {
            btnJoinCall.setOnClickListener(v -> startActivity(new Intent(this, VideoCallActivity.class)));
        }

        if (btnBookAppointment != null) {
            btnBookAppointment.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
        }

        if (btnChatDentist != null) {
            btnChatDentist.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        }

        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> startActivity(new Intent(this, UploadImageActivity.class)));
        }

        if (btnTreatmentPlan != null) {
            btnTreatmentPlan.setOnClickListener(v -> startActivity(new Intent(this, TreatmentPlanActivity.class)));
        }

        if (btnBilling != null) {
            btnBilling.setOnClickListener(v -> startActivity(new Intent(this, BillingActivity.class)));
        }
    }
}
