/*package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        LinearLayout container = findViewById(R.id.chatMessagesContainer);

        if (btnSend != null && etMessage != null && container != null) {
            btnSend.setOnClickListener(v -> {
                String msg = etMessage.getText().toString().trim();
                if (!msg.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("Sarah: " + msg);
                    tv.setPadding(24, 24, 24, 24);
                    tv.setBackgroundColor(0xFFD1E7DD);
                    container.addView(tv);
                    etMessage.setText("");
                }
            });
        }
    }
}*/
