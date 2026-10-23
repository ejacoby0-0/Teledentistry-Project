package com.example.teledentistry_project.ui.userActivities;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.EditProfileUser;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.practitioner.EditPractitionerRequest;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8090/";

    private TextInputEditText etName, etSurname, etBirthDate;
    private TextInputEditText etEmail, etPhoneNumber;
    private TextInputEditText etPractiseName, etPractiseLocation;
    private TextInputEditText etConsultationFee;

    private View layoutPractitionerProfileFields;
    private MaterialCardView profilePhoto;
    private ImageView ivProfilePicture;
    private TextView tvChangePhoto;
    private MaterialButton btnSaveProfile, btnCancelProfileChange;
    private TextView btnBack;

    private String authToken;
    private String userId;
    private String userRole;
    private String practitionerRecordId;

    private Uri selectedAvatarUri;
    private boolean saving = false;

    private final ExecutorService imageExecutor =
            Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedAvatarUri = uri;
                            ivProfilePicture.setImageURI(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        bindViews();

        SharedPreferences preferences =
                getSharedPreferences("auth", MODE_PRIVATE);

        authToken = preferences.getString("authToken", null);
        userId = preferences.getString("userId", null);

        if (authToken == null || userId == null) {
            Toast.makeText(
                    this,
                    "Your session information is missing. Please sign in again.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        profilePhoto.setOnClickListener(v -> openGallery());
        tvChangePhoto.setOnClickListener(v -> openGallery());

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> finish());
        btnCancelProfileChange.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveChanges());

        loadUserProfile();
    }

    private void bindViews() {
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etBirthDate = findViewById(R.id.etBirthDate);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        etPractiseName = findViewById(R.id.etPractiseName);
        etPractiseLocation = findViewById(R.id.etPractiseLocation);
        etConsultationFee = findViewById(R.id.etConsultationFee);

        layoutPractitionerProfileFields =
                findViewById(R.id.layoutPractitionerProfileFields);

        profilePhoto = findViewById(R.id.profilePhoto);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelProfileChange =
                findViewById(R.id.btnCancelProfileChange);
        btnBack = findViewById(R.id.btnBack);
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );
                    etBirthDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );
        dialog.show();
    }

    private void loadUserProfile() {
        PocketBaseClient.api.getEditableUser(authToken, userId)
                .enqueue(new Callback<EditProfileUser>() {
                    @Override
                    public void onResponse(
                            Call<EditProfileUser> call,
                            Response<EditProfileUser> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showMessage("Could not load your profile.");
                            return;
                        }

                        EditProfileUser user = response.body();

                        etName.setText(user.getName());
                        etSurname.setText(user.getSurname());
                        etBirthDate.setText(user.getDateOfBirth());
                        etEmail.setText(user.getEmail());
                        etPhoneNumber.setText(user.getPhone());

                        userRole = user.getRole() == null
                                ? ""
                                : user.getRole().toLowerCase(Locale.ROOT);

                        boolean isPractitioner =
                                "practitioner".equals(userRole);

                        layoutPractitionerProfileFields.setVisibility(
                                isPractitioner ? View.VISIBLE : View.GONE
                        );

                        if (isPractitioner) {
                            loadPractitionerProfile();
                        }

                        if (user.getAvatar() != null
                                && !user.getAvatar().trim().isEmpty()
                                && user.getCollectionName() != null
                                && user.getCollectionId() != null) {
                            loadAvatar(
                                    user.getCollectionName(),
                                    user.getId(),
                                    user.getAvatar()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<EditProfileUser> call,
                            Throwable t) {
                        showMessage("Profile loading failed: " + t.getMessage());
                    }
                });
    }

    private void loadPractitionerProfile() {
        String filter = "user='" + userId + "'";

        PocketBaseClient.api.getPractitioners(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {
                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<Practitioner>> call,
                            Response<PocketBaseListResponse<Practitioner>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getItems() == null
                                || response.body().getItems().isEmpty()) {
                            showMessage(
                                    "Could not find your practitioner profile."
                            );
                            return;
                        }

                        Practitioner practitioner =
                                response.body().getItems().get(0);

                        practitionerRecordId = practitioner.getId();

                        etPractiseName.setText(
                                practitioner.getPracticeName()
                        );
                        etPractiseLocation.setText(
                                practitioner.getPracticeLocation()
                        );
                        etConsultationFee.setText(
                                String.valueOf(
                                        practitioner.getConsultationFee()
                                )
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Practitioner>> call,
                            Throwable t) {
                        showMessage(
                                "Practitioner details failed to load: "
                                        + t.getMessage()
                        );
                    }
                });
    }

    private void saveChanges() {
        if (saving) return;

        String name = textOf(etName);
        String surname = textOf(etSurname);
        String birthDate = textOf(etBirthDate);
        String email = textOf(etEmail);
        String phone = textOf(etPhoneNumber);

        if (name.isEmpty() || surname.isEmpty()
                || birthDate.isEmpty() || email.isEmpty()
                || phone.isEmpty()) {
            showMessage("Please complete all personal details.");
            return;
        }

        boolean isPractitioner =
                "practitioner".equalsIgnoreCase(userRole);

        String practiceName = textOf(etPractiseName);
        String practiceLocation = textOf(etPractiseLocation);
        String feeText = textOf(etConsultationFee);
        double fee = 0;

        if (isPractitioner) {
            if (practitionerRecordId == null) {
                showMessage(
                        "Your practitioner record has not loaded yet."
                );
                return;
            }

            if (practiceName.isEmpty()
                    || practiceLocation.isEmpty()
                    || feeText.isEmpty()) {
                showMessage(
                        "Please complete all practitioner details."
                );
                return;
            }

            try {
                fee = Double.parseDouble(feeText);
                if (fee < 0) {
                    showMessage("Consultation fee cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                showMessage("Enter a valid consultation fee.");
                return;
            }
        }

        saving = true;
        btnSaveProfile.setEnabled(false);

        final double finalFee = fee;

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("name", textPart(name));
        fields.put("surname", textPart(surname));
        fields.put("dateOfBirth", textPart(birthDate));
        fields.put("email", textPart(email));
        fields.put("phone", textPart(phone));

        MultipartBody.Part avatarPart = null;

        try {
            if (selectedAvatarUri != null) {
                avatarPart = createAvatarPart(selectedAvatarUri);
                if (avatarPart == null) {
                    saving = false;
                    btnSaveProfile.setEnabled(true);
                    showMessage("Could not read the selected photo.");
                    return;
                }
            }
        } catch (Exception e) {
            saving = false;
            btnSaveProfile.setEnabled(true);
            showMessage("Could not prepare the selected photo.");
            return;
        }

        PocketBaseClient.api.updateUserProfileMultipart(
                authToken,
                userId,
                fields,
                avatarPart
        ).enqueue(new Callback<EditProfileUser>() {
            @Override
            public void onResponse(
                    Call<EditProfileUser> call,
                    Response<EditProfileUser> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    finishSaving();
                    showMessage(
                            "Could not save personal details. HTTP "
                                    + response.code()
                    );
                    return;
                }

                if (isPractitioner) {
                    updatePractitionerDetails(
                            practiceName,
                            practiceLocation,
                            finalFee
                    );
                } else {
                    finishSaving();
                    showMessage("Profile updated successfully.");
                    finish();
                }
            }

            @Override
            public void onFailure(
                    Call<EditProfileUser> call,
                    Throwable t) {
                finishSaving();
                showMessage(
                        "Saving personal details failed: " + t.getMessage()
                );
            }
        });
    }

    private void updatePractitionerDetails(
            String practiceName,
            String practiceLocation,
            double fee) {

        EditPractitionerRequest request =
                new EditPractitionerRequest(
                        practiceName,
                        practiceLocation,
                        fee
                );

        PocketBaseClient.api.updatePractitionerForEdit(
                authToken,
                practitionerRecordId,
                request
        ).enqueue(new Callback<Practitioner>() {
            @Override
            public void onResponse(
                    Call<Practitioner> call,
                    Response<Practitioner> response) {

                finishSaving();

                if (response.isSuccessful() && response.body() != null) {
                    showMessage("Profile updated successfully.");
                    finish();
                } else {
                    showMessage(
                            "Personal details were saved, but practitioner "
                                    + "details could not be updated. HTTP "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<Practitioner> call,
                    Throwable t) {
                finishSaving();
                showMessage(
                        "Personal details were saved, but practitioner "
                                + "details failed to save: " + t.getMessage()
                );
            }
        });
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(
                MediaType.parse("text/plain"),
                value
        );
    }

    private MultipartBody.Part createAvatarPart(Uri uri)
            throws IOException {

        ContentResolver resolver = getContentResolver();

        byte[] bytes;
        try (InputStream input = resolver.openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            if (input == null) return null;

            byte[] buffer = new byte[8192];
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            bytes = output.toByteArray();
        }

        String mimeType = resolver.getType(uri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "image/jpeg";
        }

        String filename = "avatar.jpg";

        try (android.database.Cursor cursor =
                     resolver.query(uri, null, null, null, null)) {
            if (cursor != null
                    && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                );
                if (index >= 0) {
                    String foundName = cursor.getString(index);
                    if (foundName != null && !foundName.isEmpty()) {
                        filename = foundName;
                    }
                }
            }
        }

        RequestBody imageBody = RequestBody.create(
                MediaType.parse(mimeType),
                bytes
        );

        return MultipartBody.Part.createFormData(
                "avatar",
                filename,
                imageBody
        );
    }

    private void loadAvatar(
            String collectionName,
            String recordId,
            String filename) {

        String imageUrl = BASE_URL
                + "api/files/"
                + collectionName + "/"
                + recordId + "/"
                + Uri.encode(filename);

        imageExecutor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(imageUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                try (InputStream input = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    if (bitmap != null) {
                        runOnUiThread(
                                () -> ivProfilePicture.setImageBitmap(bitmap)
                        );
                    }
                }
            } catch (Exception ignored) {
                // Keep the default profile image if loading fails.
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }

    private void finishSaving() {
        saving = false;
        btnSaveProfile.setEnabled(true);
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageExecutor.shutdown();
    }
}
