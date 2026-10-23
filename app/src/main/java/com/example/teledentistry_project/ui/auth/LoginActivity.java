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

        /*
         * If a PocketBase session already exists,
         * validate it before showing the login screen.
         */
        if (sessionManager.isLoggedIn()) {
            validateExistingSession();
            return;
        }

        btnLogin.setOnClickListener(view -> attemptLogin());

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        tvRegister.setOnClickListener(view -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void initializeViews() {

        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);

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

                        if (response.getToken() == null
                                || response.getRecord() == null) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Unable to create login session.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        PocketBaseUser user =
                                response.getRecord();

                        /*
                         * Save PocketBase session.
                         */
                        sessionManager.saveSession(
                                response.getToken(),
                                user.getId(),
                                user.getEmail(),
                                user.getRole()
                        );

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful.",
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                         * RBAC navigation.
                         */
                        navigateByRole(user.getRole());
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

    /*
     * Central RBAC navigation method.
     *
     * PocketBase roles:
     * patient
     * practitioner
     * admin
     */
    private void navigateByRole(String role) {

        Intent intent;

        if ("patient".equals(role)) {

            intent = new Intent(
                    LoginActivity.this,
                    PatientHomeActivity.class
            );

        } else if ("practitioner".equals(role)) {

            intent = new Intent(
                    LoginActivity.this,
                    DentistHomeActivity.class
            );

        } else if ("admin".equals(role)) {

            intent = new Intent(
                    LoginActivity.this,
                    AdminHomeActivity.class
            );

        } else {

            sessionManager.clearSession();

            Toast.makeText(
                    LoginActivity.this,
                    "Your account does not have a valid role.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        /*
         * Remove LoginActivity from the back stack.
         */
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
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

    private String getText(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }

    private void clearErrors() {

        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    private void setLoading(boolean loading) {

        btnLogin.setEnabled(!loading);

        /*
         * Disable the other controls while authenticating.
         */
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        tvRegister.setEnabled(!loading);

        if (loading) {
            btnLogin.setText("Signing in...");
        } else {
            btnLogin.setText("Sign In");
        }
    }

    /*
     * Validate a previously stored PocketBase session.
     */
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

                        if (response.getToken() == null
                                || response.getRecord() == null) {

                            sessionManager.clearSession();
                            return;
                        }

                        PocketBaseUser user =
                                response.getRecord();

                        /*
                         * Save refreshed PocketBase session.
                         */
                        sessionManager.saveSession(
                                response.getToken(),
                                user.getId(),
                                user.getEmail(),
                                user.getRole()
                        );

                        /*
                         * Use exactly the same RBAC routing
                         * as a normal login.
                         */
                        navigateByRole(user.getRole());
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
                        sessionManager.clearSession();
                        Toast.makeText(
                                LoginActivity.this,
                                "Session reset. Please sign in with your credentials.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}