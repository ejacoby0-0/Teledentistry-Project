package com.example.teledentistry_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;

import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.PatientRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;

public class RegisterActivity extends AppCompatActivity {

    private PatientRepository patientRepository;

    private TextInputLayout fullNameInputLayout;
    private TextInputLayout dateOfBirthInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private TextInputEditText entFullName;
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
        setupDateOfBirthPicker();

        authRepository = new AuthRepository();
        patientRepository = new PatientRepository();
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
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        dateOfBirthInputLayout = findViewById(R.id.dateOfBirthInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);



        entFullName = findViewById(R.id.entFullName);
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

        String fullName = getText(entFullName);
        String dateOfBirth = getText(entDateOfBirth);
        String phone = getText(entPhone);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateInput(
                fullName,
                dateOfBirth,
                phone,
                email,
                password,
                confirmPassword
        )) {
            return;
        }

        setLoading(true);

        authRepository.register(
                email,
                password,
                confirmPassword,
                fullName,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onRegistrationSuccess(
                            PocketBaseUser user
                    ) {

                        // fullName already contains the value
                        // entered in the Full Name field.
                        createPatientProfileAfterRegistration(
                                user,
                                fullName,
                                phone,
                                dateOfBirth,
                                email,
                                password
                        );
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
            String fullName,
            String dateOfBirth,
            String phone,
            String email,
            String password,
            String confirmPassword
    ) {

        boolean isValid = true;

        if (fullName.isEmpty()) {
            fullNameInputLayout.setError(
                    "Full name is required."
            );
            isValid = false;
        }

        if (dateOfBirth.isEmpty()) {
            dateOfBirthInputLayout.setError(
                    "Date of birth is required."
            );

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

        fullNameInputLayout.setError(null);
        dateOfBirthInputLayout.setError(null);
        phoneInputLayout.setError(null);
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

    private void setupDateOfBirthPicker() {

        View.OnClickListener listener = view -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            RegisterActivity.this,
                            (datePicker, year, month, day) -> {

                                Calendar selectedDate =
                                        Calendar.getInstance();

                                selectedDate.set(
                                        year,
                                        month,
                                        day
                                );

                                SimpleDateFormat format =
                                        new SimpleDateFormat(
                                                "yyyy-MM-dd",
                                                Locale.getDefault()
                                        );

                                entDateOfBirth.setText(
                                        format.format(
                                                selectedDate.getTime()
                                        )
                                );

                                dateOfBirthInputLayout.setError(null);
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        };

        entDateOfBirth.setOnClickListener(listener);

        dateOfBirthInputLayout.setEndIconOnClickListener(
                view -> entDateOfBirth.performClick()
        );
    }

    private void createPatientProfileAfterRegistration(
            PocketBaseUser registeredUser,
            String fullName,
            String phone,
            String dateOfBirth,
            String email,
            String password
    ) {

        authRepository.login(
                email,
                password,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onRegistrationSuccess(
                            PocketBaseUser user
                    ) {
                        // Not used.
                    }

                    @Override
                    public void onLoginSuccess(
                            PocketBaseAuthResponse response
                    ) {

                        if (response == null
                                || response.getToken() == null
                                || response.getToken().isEmpty()) {

                            registrationFailed(
                                    "Account created, but the patient profile could not be created."
                            );

                            return;
                        }

                        patientRepository.createPatient(
                                response.getToken(),
                                registeredUser.getId(),
                                fullName,
                                phone,
                                dateOfBirth,
                                new PatientRepository.PatientCallback() {

                                    @Override
                                    public void onSuccess(
                                            Patient patient
                                    ) {

                                        registrationComplete();
                                    }

                                    @Override
                                    public void onNotFound() {

                                        registrationFailed(
                                                "Account created, but patient profile creation failed."
                                        );
                                    }

                                    @Override
                                    public void onError(
                                            String message
                                    ) {

                                        registrationFailed(
                                                "Account created, but patient profile creation failed.\n"
                                                        + message
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String error) {

                        registrationFailed(
                                "Account was created, but profile setup failed.\n"
                                        + error
                        );
                    }
                }
        );
    }

    private void registrationComplete() {

        setLoading(false);

        Toast.makeText(
                RegisterActivity.this,
                "Account created successfully as Patient. Please sign in.",
                Toast.LENGTH_LONG
        ).show();

        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        LoginActivity.class
                );

        startActivity(intent);
        finish();
    }

    private void registrationFailed(
            String message
    ) {

        setLoading(false);

        Toast.makeText(
                RegisterActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }




}
