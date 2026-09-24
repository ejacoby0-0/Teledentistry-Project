package com.example.teledentistry_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.auth.AuthResponse;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnRegister;
    private TextView tvLogin;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        btnRegister.setOnClickListener(view -> attemptRegistration());

        tvLogin.setOnClickListener(view -> {
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {

        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout =
                findViewById(R.id.confirmPasswordInputLayout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void attemptRegistration() {

        clearErrors();

        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateInput(email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        authRepository.register(
                email,
                password,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onSuccess(AuthResponse response) {

                        setLoading(false);

                        /*
                         * Supabase may return an access token immediately
                         * when email confirmation is disabled.
                         *
                         * If email confirmation is enabled, the account may
                         * be created without an active session.
                         */
                        if (response.getAccessToken() != null
                                && response.getUser() != null) {

                            sessionManager.saveSession(
                                    response.getAccessToken(),
                                    response.getRefreshToken(),
                                    response.getUser().getId(),
                                    response.getUser().getEmail()
                            );

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Account created successfully.",
                                    Toast.LENGTH_LONG
                            ).show();

                            // Later we will redirect to the patient
                            // onboarding/home screen.

                        } else {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Account created. Please check your email to confirm your account.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onError(String error) {

                        setLoading(false);

                        Toast.makeText(
                                RegisterActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private boolean validateInput(
            String email,
            String password,
            String confirmPassword
    ) {

        boolean isValid = true;

        if (email.isEmpty()) {

            emailInputLayout.setError("Email is required.");
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

        } else if (password.length() < 8) {

            passwordInputLayout.setError(
                    "Password must be at least 8 characters."
            );

            isValid = false;
        }

        if (confirmPassword.isEmpty()) {

            confirmPasswordInputLayout.setError(
                    "Please confirm your password."
            );

            isValid = false;

        } else if (!password.equals(confirmPassword)) {

            confirmPasswordInputLayout.setError(
                    "Passwords do not match."
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
        confirmPasswordInputLayout.setError(null);
    }

    private void setLoading(boolean loading) {

        btnRegister.setEnabled(!loading);

        if (loading) {
            btnRegister.setText("Creating account...");
        } else {
            btnRegister.setText("Create Account");
        }
    }
}
