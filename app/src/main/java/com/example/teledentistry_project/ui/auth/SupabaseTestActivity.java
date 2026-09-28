package com.example.teledentistry_project.ui.auth;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.data.model.auth.AuthResponse;
import com.example.teledentistry_project.data.repository.AuthRepository;

public class SupabaseTestActivity extends AppCompatActivity {

    private static final String TAG = "SupabaseTest";

    private AuthRepository authRepository;

    // Use a test account
    private final String testEmail = "your-test-email@example.com";
    private final String testPassword = "TestPassword123!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository();

        testRegistration();
    }

    private void testRegistration() {

        Log.d(TAG, "Starting registration test...");

        authRepository.register(
                testEmail,
                testPassword,
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onSuccess(AuthResponse response) {

                        Log.d(
                                TAG,
                                "REGISTRATION SUCCESSFUL"
                        );

                        if (response.getUser() != null) {

                            Log.d(
                                    TAG,
                                    "User ID: "
                                            + response
                                            .getUser()
                                            .getId()
                            );

                            Log.d(
                                    TAG,
                                    "Email: "
                                            + response
                                            .getUser()
                                            .getEmail()
                            );
                        }

                        if (response.getAccessToken() != null) {

                            Log.d(
                                    TAG,
                                    "Access token received"
                            );

                        } else {

                            Log.d(
                                    TAG,
                                    "No access token received"
                            );
                        }
                    }

                    @Override
                    public void onError(String error) {

                        Log.e(
                                TAG,
                                "REGISTRATION FAILED: "
                                        + error
                        );
                    }
                }
        );
    }
}












//Testing Supabase connection
/*
public class SupabaseTestActivity extends AppCompatActivity {

    private static final String TAG = "SupabaseTest";

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository();

        authRepository.register(
                "test@email.com",
                "TestPassword123!",
                new AuthRepository.AuthCallback() {

                    @Override
                    public void onSuccess(AuthResponse response) {

                        Log.d(
                                TAG,
                                "Registration successful"
                        );

                        if (response.getUser() != null) {

                            Log.d(
                                    TAG,
                                    "User ID: "
                                            + response
                                            .getUser()
                                            .getId()
                            );

                            Log.d(
                                    TAG,
                                    "Email: "
                                            + response
                                            .getUser()
                                            .getEmail()
                            );
                        }
                    }

                    @Override
                    public void onError(String error) {

                        Log.e(
                                TAG,
                                "Registration failed: "
                                        + error
                        );
                    }
                }
        );
    }

    private void testSupabaseConnection() {

        authRepository.checkSupabaseConnection(
                new AuthRepository.ConnectionCallback() {

                    @Override
                    public void onSuccess(HealthResponse response) {

                        Log.d(TAG,
                                "Supabase connection successful");

                        Log.d(TAG,
                                "Auth service: " + response.getName());

                        Log.d(TAG,
                                "Version: " + response.getVersion());
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
*/
