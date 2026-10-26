package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.patient.PatientUpdateRequest;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRepository {

    public interface PatientCallback {
        void onSuccess(Patient patient);
        void onNotFound();
        void onError(String message);
    }

    public interface UpdatePatientCallback {
        void onSuccess(Patient patient);
        void onError(String message);
    }

    public void updatePatient(
            String authToken,
            String patientId,
            String fullName,
            String phoneNumber,
            UpdatePatientCallback callback
    ) {

        PatientUpdateRequest request =
                new PatientUpdateRequest(
                        fullName,
                        phoneNumber
                );

        PocketBaseClient.api
                .updatePatient(
                        authToken,
                        patientId,
                        request
                )
                .enqueue(new Callback<Patient>() {

                    @Override
                    public void onResponse(
                            Call<Patient> call,
                            Response<Patient> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    "Failed to update profile. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Patient> call,
                            Throwable throwable
                    ) {

                        callback.onError(
                                throwable.getMessage() != null
                                        ? throwable.getMessage()
                                        : "Network error"
                        );
                    }
                });
    }

    public void getPatientByUserId(
            String authToken,
            String userId,
            PatientCallback callback
    ) {

        String filter = "user = \"" + userId + "\"";

        PocketBaseClient.api
                .getPatients(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<Patient>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<Patient>> call,
                            Response<PocketBaseListResponse<Patient>> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            PocketBaseListResponse<Patient> result = response.body();

                            if (result.getItems() != null && !result.getItems().isEmpty()) {
                                callback.onSuccess(result.getItems().get(0));
                            } else {
                                callback.onSuccess(new Patient());
                            }

                        } else {
                            callback.onSuccess(new Patient());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Patient>> call,
                            Throwable throwable
                    ) {
                        callback.onSuccess(new Patient());
                    }
                });
    }

    public void createPatient(
            String authToken,
            String userId,
            String fullName,
            String phoneNumber,
            String dateOfBirth,
            PatientCallback callback
    ) {

        com.google.gson.JsonObject request =
                new com.google.gson.JsonObject();

        request.addProperty("user", userId);
        request.addProperty("fullName", fullName);
        request.addProperty("phoneNumber", phoneNumber);

        // PocketBase date fields accept a full datetime value.
        request.addProperty(
                "dateOfBirth",
                dateOfBirth + " 00:00:00.000Z"
        );

        PocketBaseClient.api
                .createPatient(authToken, request)
                .enqueue(new Callback<Patient>() {

                    @Override
                    public void onResponse(
                            Call<Patient> call,
                            Response<Patient> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        }  else {

                            String error =
                                    "Failed to create patient profile. Code: "
                                            + response.code();

                            try {
                                if (response.errorBody() != null) {
                                    error += "\n" + response.errorBody().string();
                                }
                            } catch (Exception e) {
                                error += "\nUnable to read PocketBase error.";
                            }

                            Log.e("PatientCreate", error);

                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Patient> call,
                            Throwable throwable
                    ) {

                        callback.onError(
                                throwable.getMessage() != null
                                        ? throwable.getMessage()
                                        : "Network error"
                        );
                    }
                });
    }

    public void createPatient(
            String authToken,
            String userId,
            PatientCallback callback
    ) {

        createPatient(
                authToken,
                userId,
                "Patient User",
                "",
                "2000-01-01",
                callback
        );
    }

    public void ensurePatientRecord(
            String authToken,
            String userId,
            PatientCallback callback
    ) {
        getPatientByUserId(authToken, userId, new PatientCallback() {
            @Override
            public void onSuccess(Patient patient) {
                if (patient != null && patient.getId() != null && !patient.getId().isEmpty()) {
                    callback.onSuccess(patient);
                } else {
                    createPatient(authToken, userId, callback);
                }
            }

            @Override
            public void onNotFound() {
                createPatient(authToken, userId, callback);
            }

            @Override
            public void onError(String message) {
                createPatient(authToken, userId, callback);
            }
        });
    }
}
