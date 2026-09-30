package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class UploadImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        Spinner spType = findViewById(R.id.spImageType);
        Button btnUpload = findViewById(R.id.btnUpload);

        if (spType != null) {
            String[] types = new String[] { "TOOTH", "GUM", "MOUTH", "X-RAY", "OTHER" };
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
            spType.setAdapter(adapter);
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
