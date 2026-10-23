package com.example.teledentistry_project.ui.patient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.utils.SessionManager;

import java.io.InputStream;

public class UploadImageActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 2002;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private Spinner spType;
    private ImageView ivImagePreview;
    private Button btnSelectImage;
    private Button btnUpload;

    private Uri selectedImageUri;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        sessionManager = new SessionManager(this);

        spType = findViewById(R.id.spImageType);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);

        if (spType != null) {
            String[] types = new String[] { "TOOTH", "GUM", "MOUTH", "X-RAY", "OTHER" };
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
            spType.setAdapter(adapter);
        }

        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> chooseImageFile());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> uploadImageFile());
        }
    }

    private void chooseImageFile() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Dental Image"), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (validateImage(uri)) {
                selectedImageUri = uri;
                if (ivImagePreview != null) {
                    ivImagePreview.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private boolean validateImage(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null && !mimeType.startsWith("image/")) {
                Toast.makeText(this, "Invalid file format. Please upload an image (JPG, PNG, WEBP).", Toast.LENGTH_LONG).show();
                return false;
            }

            InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                int size = is.available();
                is.close();
                if (size > MAX_FILE_SIZE) {
                    Toast.makeText(this, "Image file size exceeds 5MB limit.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Unable to validate selected image.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void uploadImageFile() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image file first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spType != null ? spType.getSelectedItem().toString() : "OTHER";

        // Store persistent record in local SharedPreferences for this patient session
        SharedPreferences prefs = getSharedPreferences("dental_images", Context.MODE_PRIVATE);
        prefs.edit().putString("latest_dental_image_" + sessionManager.getUserId(), selectedImageUri.toString()).apply();
        prefs.edit().putString("latest_dental_category_" + sessionManager.getUserId(), category).apply();

        Toast.makeText(this, "Dental image (" + category + ") uploaded successfully and linked to your account!", Toast.LENGTH_LONG).show();
        finish();
    }
}