package com.example.teledentistry_project.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class PractitionerApprovalsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practitioner_approvals);

        Button btnApprove = findViewById(R.id.btnApprove);
        Button btnReject = findViewById(R.id.btnReject);

        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> {
                Toast.makeText(this, "Practitioner account APPROVED.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnReject != null) {
            btnReject.setOnClickListener(v -> {
                Toast.makeText(this, "Practitioner account REJECTED.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
