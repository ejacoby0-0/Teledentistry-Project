package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseLoginRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseRegisterRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.model.practitioner.PractitionerRegistrationRequest;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class AuthRepository {

    /* Register a new patient or practitioner account. */


    public void register(
            String name,
            String surname,
            String dateOfBirth,
            String phone,
            String email,
            String password,
            String confirmPassword,
            String role,
            String practitionerTitle,
            String practiceName,
            String practiceLocation,
            String consultationFee,
            AuthCallback callback
    ) {
        PocketBaseRegisterRequest request =
                new PocketBaseRegisterRequest(
                        name,
                        surname,
                        dateOfBirth,
                        phone,
                        email,
                        password,
                        confirmPassword,
                        role
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

                            PocketBaseUser user = response.body();


                            if ("practitioner".equals(role)) {

                                PocketBaseLoginRequest loginRequest =
                                        new PocketBaseLoginRequest(email, password);

                                PocketBaseClient.api.login(loginRequest)
                                        .enqueue(new Callback<PocketBaseAuthResponse>() {

                                            @Override
                                            public void onResponse(
                                                    Call<PocketBaseAuthResponse> call,
                                                    Response<PocketBaseAuthResponse> loginResponse
                                            ) {
                                                if (!loginResponse.isSuccessful()
                                                        || loginResponse.body() == null) {
                                                    callback.onError(
                                                            "Account created, but login failed. "
                                                                    + "Your practitioner profile was not saved."
                                                    );
                                                    return;
                                                }

                                                String token = loginResponse.body().getToken();

                                                double fee;
                                                try {
                                                    fee = Double.parseDouble(consultationFee);
                                                } catch (NumberFormatException e) {
                                                    callback.onError(
                                                            "Account created, but the consultation fee is invalid."
                                                    );
                                                    return;
                                                }

                                                PractitionerRegistrationRequest practitionerRequest =
                                                        new PractitionerRegistrationRequest(
                                                                user.getId(),
                                                                practiceName,
                                                                practiceLocation,
                                                                fee
                                                        );

                                                PocketBaseClient.api.createPractitioner(
                                                        token,
                                                        practitionerRequest
                                                ).enqueue(new Callback<Practitioner>() {

                                                    @Override
                                                    public void onResponse(
                                                            Call<Practitioner> call,
                                                            Response<Practitioner> profileResponse
                                                    ) {
                                                        if (profileResponse.isSuccessful()
                                                                && profileResponse.body() != null) {
                                                            callback.onRegistrationSuccess(user);
                                                        } else {
                                                            callback.onError(
                                                                    "Account created, but the practitioner "
                                                                            + "profile could not be saved."
                                                            );
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(
                                                            Call<Practitioner> call,
                                                            Throwable t
                                                    ) {
                                                        callback.onError(
                                                                "Account created, but saving the profile failed: "
                                                                        + t.getMessage()
                                                        );
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(
                                                    Call<PocketBaseAuthResponse> call,
                                                    Throwable t
                                            ) {
                                                callback.onError(
                                                        "Account created, but login failed: "
                                                                + t.getMessage()
                                                );
                                            }
                                        });

                            } else {
                                callback.onRegistrationSuccess(user);
                            }


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
                                "Registration failed",
                                t
                        );

                        callback.onError(
                                "Registration network error: "
                                        + t.getMessage()
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