package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class BillingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        Button btnPay = findViewById(R.id.btnPayNow);
        if (btnPay != null) {
            btnPay.setOnClickListener(v -> Toast.makeText(this, "Payment demo complete. Invoice updated to PAID.", Toast.LENGTH_LONG).show());
        }
    }
}
