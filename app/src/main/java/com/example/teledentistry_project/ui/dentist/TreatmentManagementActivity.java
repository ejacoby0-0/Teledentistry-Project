package com.example.teledentistry_project.ui.dentist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class TreatmentManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_management);

        Button btnSave = findViewById(R.id.btnSaveTreatment);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(this, "Diagnosis saved and prescription issued!", Toast.LENGTH_LONG).show();
                finish();
            });
        }
    }
}
