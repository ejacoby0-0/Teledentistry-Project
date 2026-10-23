package com.example.teledentistry_project.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class ReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        Button btnExport = findViewById(R.id.btnExportReport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> Toast.makeText(this, "Exporting report PDF...", Toast.LENGTH_SHORT).show());
        }
    }
}
