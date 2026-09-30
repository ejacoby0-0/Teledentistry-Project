package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class PatientProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        Button btnSave = findViewById(R.id.btnSaveProfile);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
