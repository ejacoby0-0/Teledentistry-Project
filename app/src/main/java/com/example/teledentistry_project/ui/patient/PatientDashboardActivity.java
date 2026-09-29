package com.example.teledentistry_project.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.utils.SessionManager;

public class PatientDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        sessionManager = new SessionManager(this);

        TextView tvWelcome =
                findViewById(R.id.tvWelcome);

        Button btnLogout =
                findViewById(R.id.btnLogout);

        tvWelcome.setText(
                "Logged in as:\n"
                        + sessionManager.getEmail()
        );

        btnLogout.setOnClickListener(view -> logout());
    }

    private void logout() {

        sessionManager.clearSession();

        Intent intent = new Intent(
                PatientDashboardActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}