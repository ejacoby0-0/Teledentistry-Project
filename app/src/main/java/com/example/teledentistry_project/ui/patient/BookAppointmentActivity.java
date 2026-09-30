package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class BookAppointmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        Spinner spPractitioner = findViewById(R.id.spPractitioner);
        Button btnConfirm = findViewById(R.id.btnConfirmBooking);

        if (spPractitioner != null) {
            String[] practitioners = new String[] {
                    "Dr. Emily Johnson - General Dentistry",
                    "Dr. James Williams - Periodontics",
                    "Dr. Sarah Brown - Cosmetic Dentistry"
            };
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, practitioners);
            spPractitioner.setAdapter(adapter);
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                finish();
            });
        }
    }
}
