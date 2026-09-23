package com.example.teledentistry_project.ui.auth;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.data.repository.AuthRepository;

public class SupabaseTestActivity extends AppCompatActivity {

    private static final String TAG = "SupabaseTest";

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository();

        testSupabaseConnection();
    }

    private void testSupabaseConnection() {

        authRepository.checkSupabaseConnection(
                new AuthRepository.ConnectionCallback() {

                    @Override
                    public void onSuccess(String response) {

                        Log.d(TAG,
                                "Supabase connection successful");

                        Log.d(TAG,
                                "Response: " + response);
                    }

                    @Override
                    public void onError(String error) {

                        Log.e(TAG,
                                "Supabase connection failed");

                        Log.e(TAG,
                                "Error: " + error);
                    }
                }
        );
    }
}