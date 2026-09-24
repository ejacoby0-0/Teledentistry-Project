package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.auth.HealthResponse;
import com.example.teledentistry_project.data.model.auth.SignInRequest;
import com.example.teledentistry_project.data.remote.SupabaseClient;
import com.example.teledentistry_project.data.model.auth.AuthResponse;
import com.example.teledentistry_project.data.model.auth.SignUpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import okhttp3.ResponseBody;

public class AuthRepository {

    public void checkSupabaseConnection(
            ConnectionCallback callback
    ) {

        SupabaseClient.api
                .checkAuthHealth()
                .enqueue(new Callback<HealthResponse>() {

                    @Override
                    public void onResponse(
                            Call<HealthResponse> call,
                            Response<HealthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Supabase returned HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<HealthResponse> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage()
                        );
                    }
                });
    }

    //Register Method
    public void register(
            String email,
            String password,
            AuthCallback callback
    ) {

        SignUpRequest request = new SignUpRequest(email, password);

        SupabaseClient.api.signUp(request)
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse> call,
                            Response<AuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            String errorMessage =
                                    "Registration failed. HTTP "
                                            + response.code();

                            ResponseBody errorBody =
                                    response.errorBody();

                            if (errorBody != null) {

                                try {

                                    String supabaseError =
                                            errorBody.string();

                                    errorMessage =
                                            errorMessage
                                                    + "\n"
                                                    + supabaseError;

                                } catch (IOException e) {

                                    errorMessage =
                                            errorMessage
                                                    + "\nUnable to read error response.";
                                }
                            }

                            callback.onError(errorMessage);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "Network error: " + t.getMessage()
                        );
                    }
                });
    }


    //Login Method
    public void login(
            String email,
            String password,
            AuthCallback callback
    ) {

        SignInRequest request =
                new SignInRequest(email, password);

        SupabaseClient.api
                .signIn(request)
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse> call,
                            Response<AuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Login failed. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage()
                        );
                    }
                });
    }


    //Connection
    public interface ConnectionCallback {

        void onSuccess(HealthResponse response);

        void onError(String error);
    }

    //Callback
    public interface AuthCallback {

        void onSuccess(AuthResponse response);

        void onError(String error);
    }

}