package com.example.teledentistry_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.auth.AuthResponse;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.ui.admin.AdminHomeActivity;
import com.example.teledentistry_project.ui.dentist.DentistHomeActivity;
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

        btnLogin.setOnClickListener(view -> attemptLogin());

        if (btnQuickPatient != null) {
            btnQuickPatient.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, PatientHomeActivity.class));
            });
        }

        if (btnQuickDentist != null) {
            btnQuickDentist.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, DentistHomeActivity.class));
            });
        }

        if (btnQuickAdmin != null) {
            btnQuickAdmin.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
            });
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
                    public void onSuccess(AuthResponse response) {

                        setLoading(false);

                        if (response.getAccessToken() != null
                                && response.getUser() != null) {

                            sessionManager.saveSession(
                                    response.getAccessToken(),
                                    response.getRefreshToken(),
                                    response.getUser().getId(),
                                    response.getUser().getEmail()
                            );

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful. Redirecting to Patient Dashboard...",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(LoginActivity.this, PatientHomeActivity.class);
                            startActivity(intent);
                            finish();

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
}
