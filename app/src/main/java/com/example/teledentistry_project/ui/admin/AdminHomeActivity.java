package com.example.teledentistry_project.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.patient.PatientProfileActivity;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Button btnManageApprovals = findViewById(R.id.btnManageApprovals);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        Button btnUserEnquiries = findViewById(R.id.btnUserEnquiries);
        Button btnViewReports = findViewById(R.id.btnViewReports);
        Button btnProfile = findViewById(R.id.btnAdminProfile);

        if (btnManageApprovals != null) {
            btnManageApprovals.setOnClickListener(v -> startActivity(new Intent(this, PractitionerApprovalsActivity.class)));
        }

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
        }

        if (btnUserEnquiries != null) {
            btnUserEnquiries.setOnClickListener(v -> startActivity(new Intent(this, UserEnquiriesActivity.class)));
        }

        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileActivity.class)));
        }
    }
}
