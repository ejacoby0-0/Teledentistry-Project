package com.example.teledentistry_project.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.auth.LoginActivity;
import com.example.teledentistry_project.utils.SessionManager;

public class AdminHomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create SessionManager
        sessionManager = new SessionManager(this);

        /*
         * AUTHENTICATION CHECK
         *
         * User must have an active session.
         */
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        /*
         * AUTHORIZATION / RBAC CHECK
         *
         * Only admins may access the Admin Portal.
         */
        if (!"admin".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "You are not authorized to access the Admin Portal.",
                    Toast.LENGTH_LONG
            ).show();

            sessionManager.clearSession();
            redirectToLogin();
            return;
        }

        /*
         * Authentication and authorization passed.
         */
        setContentView(R.layout.activity_admin_home);

        initializeNavigation();
    }


    private void initializeNavigation() {

        Button btnManageApprovals =
                findViewById(R.id.btnManageApprovals);

        Button btnManageUsers =
                findViewById(R.id.btnManageUsers);

        Button btnUserEnquiries =
                findViewById(R.id.btnUserEnquiries);

        Button btnViewReports =
                findViewById(R.id.btnViewReports);

        Button btnLogout =
                findViewById(R.id.btnAdminLogout);


        // PRACTITIONER APPROVALS
        btnManageApprovals.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                AdminHomeActivity.this,
                                PractitionerApprovalsActivity.class
                        )
                )
        );


        // MANAGE USERS
        btnManageUsers.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                AdminHomeActivity.this,
                                ManageUsersActivity.class
                        )
                )
        );


        // USER ENQUIRIES
        btnUserEnquiries.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                AdminHomeActivity.this,
                                UserEnquiriesActivity.class
                        )
                )
        );


        // REPORTS
        btnViewReports.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                AdminHomeActivity.this,
                                ReportsActivity.class
                        )
                )
        );


        // LOGOUT
        btnLogout.setOnClickListener(v ->
                logoutAdmin()
        );
    }


    private void logoutAdmin() {

        /*
         * Remove the stored PocketBase session.
         */
        sessionManager.clearSession();

        Toast.makeText(
                this,
                "Logged out successfully.",
                Toast.LENGTH_SHORT
        ).show();

        redirectToLogin();
    }


    private void redirectToLogin() {

        Intent intent = new Intent(
                AdminHomeActivity.this,
                LoginActivity.class
        );

        /*
         * Clear the Activity stack.
         *
         * This prevents the admin from pressing Back
         * after logout and returning to the Admin Portal.
         */
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}