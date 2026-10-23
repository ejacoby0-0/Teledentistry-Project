package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseLoginRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseRegisterRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class AuthRepository {

    /*
     * Register a new patient account.
     */
    public void register(
            String email,
            String password,
            String confirmPassword,
            AuthCallback callback
    ) {

        PocketBaseRegisterRequest request =
                new PocketBaseRegisterRequest(
                        email,
                        password,
                        confirmPassword,
                        "patient"
                );

        PocketBaseClient.api.register(request)
                .enqueue(new Callback<PocketBaseUser>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseUser> call,
                            Response<PocketBaseUser> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onRegistrationSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    getErrorMessage(
                                            response.errorBody(),
                                            response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseUser> call,
                            Throwable t
                    ) {

                        Log.e(
                                "PocketBaseRegister",
                                "Registration response error",
                                t
                        );

                        callback.onError(
                                "Registration response error. Check Logcat."
                        );
                    }
                });
    }


    /*
     * Login using email and password.
     */
    public void login(
            String email,
            String password,
            AuthCallback callback
    ) {

        PocketBaseLoginRequest request =
                new PocketBaseLoginRequest(
                        email,
                        password
                );

        PocketBaseClient.api.login(request)
                .enqueue(new Callback<PocketBaseAuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseAuthResponse> call,
                            Response<PocketBaseAuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onLoginSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    getErrorMessage(
                                            response.errorBody(),
                                            response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseAuthResponse> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "Network error: " + t.getMessage()
                        );
                    }
                });
    }


    //Session Validation
    public void validateSession(
            String authToken,
            SessionCallback callback
    ) {

        PocketBaseClient.api.refreshAuth(authToken)
                .enqueue(new Callback<PocketBaseAuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseAuthResponse> call,
                            Response<PocketBaseAuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onValid(
                                    response.body()
                            );

                        } else {

                            callback.onInvalid();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseAuthResponse> call,
                            Throwable t
                    ) {

                        /*
                         * Network failure does not necessarily mean
                         * that the token itself is invalid.
                         */
                        callback.onError(
                                "Unable to validate session: "
                                        + t.getMessage()
                        );
                    }
                });
    }

    /*
     * Request Password Reset for user
     */
    public void requestPasswordReset(
            String email,
            PasswordResetCallback callback
    ) {
        com.google.gson.JsonObject request = new com.google.gson.JsonObject();
        request.addProperty("email", email);

        PocketBaseClient.api.requestPasswordReset(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess("Password reset instructions have been sent to " + email);
                        } else {
                            callback.onError("Password reset request failed. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Network error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                    }
                });
    }

    public interface PasswordResetCallback {
        void onSuccess(String message);
        void onError(String message);
    }




    /*
     * Temporary error reader.
     *
     * Later we can parse PocketBase's JSON error response
     * into cleaner user-friendly messages.
     */
    private String getErrorMessage(
            ResponseBody errorBody,
            int statusCode
    ) {

        String message =
                "Request failed. HTTP " + statusCode;

        if (errorBody != null) {

            try {

                String pocketBaseError =
                        errorBody.string();

                message += "\n" + pocketBaseError;

            } catch (IOException e) {

                message +=
                        "\nUnable to read PocketBase error.";
            }
        }

        return message;
    }


    public interface SessionCallback {

        void onValid(
                PocketBaseAuthResponse response
        );

        void onInvalid();

        void onError(
                String error
        );
    }


    public interface AuthCallback {

        void onRegistrationSuccess(
                PocketBaseUser user
        );

        void onLoginSuccess(
                PocketBaseAuthResponse response
        );

        void onError(
                String error
        );
    }
}