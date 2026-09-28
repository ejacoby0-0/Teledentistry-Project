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

        btnLogin.setOnClickListener(view -> attemptLogin());

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
                                    "Login successful.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                             * Later:
                             *
                             * Redirect the user according to their role:
                             *
                             * Patient      -> Patient dashboard
                             * Practitioner -> Dentist dashboard
                             * Admin        -> Admin dashboard
                             */
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