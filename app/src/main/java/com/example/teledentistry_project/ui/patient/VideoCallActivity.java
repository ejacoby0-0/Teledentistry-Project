package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class VideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        Button btnEndCall = findViewById(R.id.btnEndCall);
        if (btnEndCall != null) {
            btnEndCall.setOnClickListener(v -> finish());
        }
    }
}
