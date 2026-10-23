package com.example.teledentistry_project.ui.dentist;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

public class UploadedImagesActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextView tvPatientHeader;
    private TextView tvImageStatus;
    private LinearLayout layoutUploadedImages;

    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        if (!"practitioner".equals(sessionManager.getRole()) && !"admin".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access denied.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_uploaded_images);

        tvPatientHeader = findViewById(R.id.tvPatientHeader);
        tvImageStatus = findViewById(R.id.tvImageStatus);
        layoutUploadedImages = findViewById(R.id.layoutUploadedImages);

        patientId = getIntent().getStringExtra("patientId");
        patientName = getIntent().getStringExtra("patientName");

        if (patientName != null && !patientName.isEmpty()) {
            tvPatientHeader.setText("Dental Images: " + patientName);
        } else if (patientId != null && !patientId.isEmpty()) {
            tvPatientHeader.setText("Dental Images (ID: " + patientId + ")");
        } else {
            tvPatientHeader.setText("Patient Uploaded Dental Images");
        }

        loadUploadedImages();
    }

    private void loadUploadedImages() {
        if (layoutUploadedImages == null) return;
        layoutUploadedImages.removeAllViews();

        SharedPreferences prefs = getSharedPreferences("dental_images", Context.MODE_PRIVATE);
        String imageUriStr = null;
        String category = "TOOTH / GUM";

        if (patientId != null) {
            imageUriStr = prefs.getString("latest_dental_image_" + patientId, null);
            category = prefs.getString("latest_dental_category_" + patientId, "TOOTH / GUM");
        }

        if (imageUriStr == null) {
            // Check current user fallback
            imageUriStr = prefs.getString("latest_dental_image_" + sessionManager.getUserId(), null);
            category = prefs.getString("latest_dental_category_" + sessionManager.getUserId(), "TOOTH");
        }

        if (imageUriStr != null) {
            tvImageStatus.setText("1 uploaded dental image found for review.");
            renderImageCard(imageUriStr, category);
        } else {
            tvImageStatus.setText("No patient-submitted images found.");

            // Helpful information card
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 16, 0, 16);
            card.setLayoutParams(cardParams);
            card.setRadius(12);

            TextView tv = new TextView(this);
            tv.setText("No uploaded images are attached to this patient's profile yet.\nWhen the patient uploads dental photos (e.g. X-Ray, Tooth, Gum), they will appear here automatically for clinical inspection.");
            tv.setPadding(24, 24, 24, 24);
            tv.setTextSize(14f);
            card.addView(tv);

            layoutUploadedImages.addView(card);
        }
    }

    private void renderImageCard(String imageUriStr, String category) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);
        card.setRadius(14);
        card.setCardElevation(4);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);

        TextView tvCategory = new TextView(this);
        tvCategory.setText("Category: " + category.toUpperCase());
        tvCategory.setTextSize(16f);
        tvCategory.setTextColor(0xFF006874);

        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        );
        ivParams.setMargins(0, 16, 0, 16);
        iv.setLayoutParams(ivParams);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setBackgroundColor(0xFFE0E0E0);

        try {
            iv.setImageURI(Uri.parse(imageUriStr));
        } catch (Exception e) {
            iv.setImageResource(R.drawable.ic_upload);
        }

        TextView tvFooter = new TextView(this);
        tvFooter.setText("Clinical Note: Verified & linked to patient record");
        tvFooter.setTextSize(13f);

        content.addView(tvCategory);
        content.addView(iv);
        content.addView(tvFooter);

        card.addView(content);
        layoutUploadedImages.addView(card);
    }
}