package com.example.teledentistry_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout;
    private TextInputLayout surnameInputLayout;
    private TextInputLayout dateOfBirthInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private TextInputEditText entName;
    private TextInputEditText entSurname;
    private TextInputEditText entDateOfBirth;
    private TextInputEditText entPhone;
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
        nameInputLayout = findViewById(R.id.nameInputLayout);
        surnameInputLayout = findViewById(R.id.surnameInputLayout);
        dateOfBirthInputLayout = findViewById(R.id.dateOfBirthInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        entName = findViewById(R.id.entName);
        entSurname = findViewById(R.id.entSurname);
        entDateOfBirth = findViewById(R.id.entDateOfBirth);
        entPhone = findViewById(R.id.entPhone);
        etEmail = findViewById(R.id.entEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void attemptRegistration() {

        clearErrors();

        String name = getText(entName);
        String surname = getText(entSurname);
        String phone = getText(entPhone);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateInput(name, surname, phone, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        authRepository.register(
                email,
                password,
                confirmPassword,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onRegistrationSuccess(
                            PocketBaseUser user
                    ) {

                        setLoading(false);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created successfully as Patient. Please sign in.",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onLoginSuccess(
                            PocketBaseAuthResponse response
                    ) {
                        // Not used during registration.
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
            String name,
            String surname,
            String phone,
            String email,
            String password,
            String confirmPassword
    ) {

        boolean isValid = true;

        if (name.isEmpty()) {
            if (nameInputLayout != null) nameInputLayout.setError("First name is required.");
            isValid = false;
        }

        if (surname.isEmpty()) {
            if (surnameInputLayout != null) surnameInputLayout.setError("Surname is required.");
            isValid = false;
        }

        if (phone.isEmpty()) {
            if (phoneInputLayout != null) phoneInputLayout.setError("Phone number is required.");
            isValid = false;
        }

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
