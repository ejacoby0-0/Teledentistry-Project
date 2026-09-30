package com.example.teledentistry_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.ui.admin.AdminHomeActivity;
import com.example.teledentistry_project.ui.dentist.DentistHomeActivity;
import com.example.teledentistry_project.ui.patient.PatientDashboardActivity;
import com.example.teledentistry_project.ui.patient.PatientHomeActivity;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;
    private MaterialButton btnQuickPatient;
    private MaterialButton btnQuickDentist;
    private MaterialButton btnQuickAdmin;
    private TextView tvRegister;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            validateExistingSession();
            return;
        }

        btnLogin.setOnClickListener(view -> attemptLogin());

        if (btnQuickPatient != null) {
            btnQuickPatient.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, PatientHomeActivity.class)));
        }

        if (btnQuickDentist != null) {
            btnQuickDentist.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, DentistHomeActivity.class)));
        }

        if (btnQuickAdmin != null) {
            btnQuickAdmin.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class)));
        }

        tvRegister.setOnClickListener(view -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {

        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnQuickPatient = findViewById(R.id.btnQuickPatient);
        btnQuickDentist = findViewById(R.id.btnQuickDentist);
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void attemptLogin() {

        clearErrors();

        String email = getText(etEmail);
        String password = getText(etPassword);

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);

        authRepository.login(
                email,
                password,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onRegistrationSuccess(
                            PocketBaseUser user
                    ) {
                        // Not used during login.
                    }

                    @Override
                    public void onLoginSuccess(
                            PocketBaseAuthResponse response
                    ) {

                        setLoading(false);

                        if (response.getToken() != null
                                && response.getRecord() != null) {

                            PocketBaseUser user =
                                    response.getRecord();

                            sessionManager.saveSession(
                                    response.getToken(),
                                    user.getId(),
                                    user.getEmail(),
                                    user.getRole()
                            );

                            routeToRoleHome(user.getRole());

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Unable to create login session.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onError(String error) {

                        setLoading(false);

                        Toast.makeText(
                                LoginActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private boolean validateInput(
            String email,
            String password
    ) {

        boolean isValid = true;

        if (email.isEmpty()) {

            emailInputLayout.setError(
                    "Email is required."
            );

            isValid = false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailInputLayout.setError(
                    "Please enter a valid email address."
            );

            isValid = false;
        }

        if (password.isEmpty()) {

            passwordInputLayout.setError(
                    "Password is required."
            );

            isValid = false;
        }

        return isValid;
    }

    private String getText(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText()
                .toString()
                .trim();
    }

    private void clearErrors() {

        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    private void setLoading(boolean loading) {

        btnLogin.setEnabled(!loading);

        if (loading) {
            btnLogin.setText("Signing in...");
        } else {
            btnLogin.setText("Sign In");
        }
    }

    //Validate existing session method
    private void validateExistingSession() {

        setLoading(true);

        authRepository.validateSession(
                sessionManager.getAuthToken(),
                new AuthRepository.SessionCallback() {

                    @Override
                    public void onValid(
                            PocketBaseAuthResponse response
                    ) {

                        setLoading(false);

                        PocketBaseUser user =
                                response.getRecord();

                        // Save the refreshed token and latest user data.
                        sessionManager.saveSession(
                                response.getToken(),
                                user.getId(),
                                user.getEmail(),
                                user.getRole()
                        );

                        routeToRoleHome(user.getRole());
                    }

                    @Override
                    public void onInvalid() {

                        setLoading(false);

                        sessionManager.clearSession();

                        Toast.makeText(
                                LoginActivity.this,
                                "Your session has expired. Please sign in again.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String error) {

                        setLoading(false);

                        Toast.makeText(
                                LoginActivity.this,
                                "Unable to verify your session. Check your connection.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void routeToRoleHome(String role) {
        Intent intent;

        if ("admin".equals(role)) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else if ("dentist".equals(role)) {
            intent = new Intent(LoginActivity.this, DentistHomeActivity.class);
        } else if ("patient".equals(role)) {
            intent = new Intent(LoginActivity.this, PatientDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, PatientHomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
