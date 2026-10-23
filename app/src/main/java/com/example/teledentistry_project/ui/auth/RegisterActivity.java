
package com.example.teledentistry_project.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.repository.AuthRepository;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    // Shared account fields
    private TextInputEditText entName;
    private TextInputEditText entSurname;
    private TextInputEditText entDateOfBirth;
    private TextInputEditText entPhone;
    private TextInputEditText entEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    // Practitioner fields
    private TextInputEditText etPractitionerTitle;
    private TextInputEditText etPracticeName;
    private TextInputEditText etPracticeLocation;
    private TextInputEditText etConsultationFee;

    // Input layouts for validation errors
    private TextInputLayout nameInputLayout;
    private TextInputLayout surnameInputLayout;
    private TextInputLayout dateOfBirthInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private TextInputLayout practitionerTitleInputLayout;
    private TextInputLayout practiceNameInputLayout;
    private TextInputLayout practiceLocationInputLayout;
    private TextInputLayout consultationFeeInputLayout;

    private SwitchMaterial switchAccountRole;
    private LinearLayout layoutPractitionerFields;

    private MaterialButton btnRegister;
    private TextView tvLogin;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository();

        initializeViews();
        setupRoleSwitch();
        setupDatePicker();

        btnRegister.setOnClickListener(v -> registerAccount());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            ));
            finish();
        });
    }

    private void initializeViews() {
        // Shared fields
        entName = findViewById(R.id.entName);
        entSurname = findViewById(R.id.entSurname);
        entDateOfBirth = findViewById(R.id.entDateOfBirth);
        entPhone = findViewById(R.id.entPhone);
        entEmail = findViewById(R.id.entEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Shared input layouts
        nameInputLayout = findViewById(R.id.nameInputLayout);
        surnameInputLayout = findViewById(R.id.surnameInputLayout);
        dateOfBirthInputLayout = findViewById(R.id.dateOfBirthInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout =
                findViewById(R.id.confirmPasswordInputLayout);

        // Account role selector
        switchAccountRole = findViewById(R.id.switchAccountRole);
        layoutPractitionerFields =
                findViewById(R.id.layoutPractitionerFields);

        // Practitioner fields
        etPractitionerTitle = findViewById(R.id.etPractitionerTitle);
        etPracticeName = findViewById(R.id.etPracticeName);
        etPracticeLocation = findViewById(R.id.etPracticeLocation);
        etConsultationFee = findViewById(R.id.etConsultationFee);

        // Practitioner input layouts
        practitionerTitleInputLayout =
                findViewById(R.id.practitionerTitleInputLayout);
        practiceNameInputLayout =
                findViewById(R.id.practiceNameInputLayout);
        practiceLocationInputLayout =
                findViewById(R.id.practiceLocationInputLayout);
        consultationFeeInputLayout =
                findViewById(R.id.consultationFeeInputLayout);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupRoleSwitch() {
        // Patient is the default role.
        switchAccountRole.setChecked(false);
        layoutPractitionerFields.setVisibility(View.GONE);

        switchAccountRole.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        // Practitioner selected
                        layoutPractitionerFields.setVisibility(View.VISIBLE);
                    } else {
                        // Patient selected
                        layoutPractitionerFields.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setupDatePicker() {
        View.OnClickListener dateClickListener = v -> showDatePicker();

        entDateOfBirth.setOnClickListener(dateClickListener);
        dateOfBirthInputLayout.setEndIconOnClickListener(
                v -> showDatePicker()
        );
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    entDateOfBirth.setText(
                            dateFormat.format(selectedDate.getTime())
                    );

                    dateOfBirthInputLayout.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // A date of birth cannot be in the future.
        datePickerDialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        datePickerDialog.show();
    }

    private void registerAccount() {
        clearAllErrors();

        String name = getText(entName);
        String surname = getText(entSurname);
        String dateOfBirth = getText(entDateOfBirth);
        String phone = getText(entPhone);
        String email = getText(entEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        boolean isPractitioner = switchAccountRole.isChecked();

        String role = isPractitioner ? "practitioner" : "patient";

        String practitionerTitle = "";
        String practiceName = "";
        String practiceLocation = "";
        String consultationFee = "";

        if (isPractitioner) {
            practitionerTitle = getText(etPractitionerTitle);
            practiceName = getText(etPracticeName);
            practiceLocation = getText(etPracticeLocation);
            consultationFee = getText(etConsultationFee);
        }

        if (!validateSharedFields(
                name,
                surname,
                dateOfBirth,
                phone,
                email,
                password,
                confirmPassword
        )) {
            return;
        }

        if (isPractitioner && !validatePractitionerFields(
                practitionerTitle,
                practiceName,
                practiceLocation,
                consultationFee
        )) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        // This overload must exist in AuthRepository.java.
        // It should create the PocketBase user and, for practitioner
        // accounts, save the linked practitioner details.
        authRepository.register(
                name,
                surname,
                dateOfBirth,
                phone,
                email,
                password,
                confirmPassword,
                role,
                practitionerTitle,
                practiceName,
                practiceLocation,
                consultationFee,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onRegistrationSuccess(PocketBaseUser user) {
                        runOnUiThread(() -> {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(
                                    getString(R.string.create_account)
                            );

                            // Return to login after registration.
                            Intent intent = new Intent(
                                    RegisterActivity.this,
                                    LoginActivity.class
                            );
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onLoginSuccess(
                            com.example.teledentistry_project.data.model.auth
                                    .PocketBaseAuthResponse response
                    ) {
                        // Registration does not automatically log in here.
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(
                                    getString(R.string.create_account)
                            );

                            android.widget.Toast.makeText(
                                    RegisterActivity.this,
                                    error,
                                    android.widget.Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private boolean validateSharedFields(
            String name,
            String surname,
            String dateOfBirth,
            String phone,
            String email,
            String password,
            String confirmPassword
    ) {
        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Enter your name");
            valid = false;
        }

        if (TextUtils.isEmpty(surname)) {
            surnameInputLayout.setError("Enter your surname");
            valid = false;
        }

        if (TextUtils.isEmpty(dateOfBirth)) {
            dateOfBirthInputLayout.setError("Select your date of birth");
            valid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInputLayout.setError("Enter your phone number");
            valid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Enter your email address");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter a valid email address");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Enter a password");
            valid = false;
        } else if (password.length() < 8) {
            passwordInputLayout.setError(
                    "Password must be at least 8 characters"
            );
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError(
                    "Confirm your password"
            );
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError(
                    "Passwords do not match"
            );
            valid = false;
        }

        return valid;
    }

    private boolean validatePractitionerFields(
            String practitionerTitle,
            String practiceName,
            String practiceLocation,
            String consultationFee
    ) {
        boolean valid = true;

        if (TextUtils.isEmpty(practitionerTitle)) {
            practitionerTitleInputLayout.setError(
                    "Enter your professional title"
            );
            valid = false;
        }

        if (TextUtils.isEmpty(practiceName)) {
            practiceNameInputLayout.setError(
                    "Enter your practice name"
            );
            valid = false;
        }

        if (TextUtils.isEmpty(practiceLocation)) {
            practiceLocationInputLayout.setError(
                    "Enter your practice location"
            );
            valid = false;
        }

        if (TextUtils.isEmpty(consultationFee)) {
            consultationFeeInputLayout.setError(
                    "Enter your consultation fee"
            );
            valid = false;
        } else {
            try {
                double fee = Double.parseDouble(consultationFee);

                if (fee < 0 || Double.isNaN(fee)
                        || Double.isInfinite(fee)) {
                    consultationFeeInputLayout.setError(
                            "Enter a valid non-negative fee"
                    );
                    valid = false;
                }
            } catch (NumberFormatException e) {
                consultationFeeInputLayout.setError(
                        "Enter a valid consultation fee"
                );
                valid = false;
            }
        }

        return valid;
    }

    private void clearAllErrors() {
        nameInputLayout.setError(null);
        surnameInputLayout.setError(null);
        dateOfBirthInputLayout.setError(null);
        phoneInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        practitionerTitleInputLayout.setError(null);
        practiceNameInputLayout.setError(null);
        practiceLocationInputLayout.setError(null);
        consultationFeeInputLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
