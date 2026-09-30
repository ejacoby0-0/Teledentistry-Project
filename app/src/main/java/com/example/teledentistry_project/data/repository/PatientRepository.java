package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.patient.PatientUpdateRequest;

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

                        if (response.isSuccessful()
                                && response.body() != null) {

                            PocketBaseListResponse<Patient> result =
                                    response.body();

                            if (result.getItems() != null
                                    && !result.getItems().isEmpty()) {

                                Patient patient =
                                        result.getItems().get(0);

                                callback.onSuccess(patient);

                            } else {

                                // Logged-in user does not yet
                                // have a patient profile.
                                callback.onNotFound();
                            }

                        } else {

                            callback.onError(
                                    "Failed to load patient profile. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Patient>> call,
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
}
