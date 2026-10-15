package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.model.prescription.PrescriptionRequest;
import com.example.teledentistry_project.data.model.prescription.PrescriptionUpdateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionRepository {

    // Get prescriptions belonging to a patient
    public void getPrescriptionsByPatient(
            String authToken,
            String patientId,
            PrescriptionListCallback callback
    ) {

        String filter =
                "patient = \"" + patientId + "\"";

        PocketBaseClient.api
                .getPrescriptions(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<Prescription>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<Prescription>> call,
                                    Response<PocketBaseListResponse<Prescription>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body().getItems()
                                    );

                                } else {

                                    callback.onError(
                                            getErrorMessage(
                                                    response,
                                                    "Failed to load prescriptions."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<Prescription>> call,
                                    Throwable t
                            ) {

                                callback.onError(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Network error."
                                );
                            }
                        }
                );
    }


    // Create prescription
    public void createPrescription(
            String authToken,
            PrescriptionRequest request,
            PrescriptionCallback callback
    ) {

        PocketBaseClient.api
                .createPrescription(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<Prescription>() {

                            @Override
                            public void onResponse(
                                    Call<Prescription> call,
                                    Response<Prescription> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body()
                                    );

                                } else {

                                    callback.onError(
                                            getErrorMessage(
                                                    response,
                                                    "Failed to create prescription."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Prescription> call,
                                    Throwable t
                            ) {

                                callback.onError(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Network error."
                                );
                            }
                        }
                );
    }


    // Update prescription
    public void updatePrescription(
            String authToken,
            String prescriptionId,
            PrescriptionUpdateRequest request,
            PrescriptionCallback callback
    ) {

        PocketBaseClient.api
                .updatePrescription(
                        authToken,
                        prescriptionId,
                        request
                )
                .enqueue(
                        new Callback<Prescription>() {

                            @Override
                            public void onResponse(
                                    Call<Prescription> call,
                                    Response<Prescription> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body()
                                    );

                                } else {

                                    callback.onError(
                                            getErrorMessage(
                                                    response,
                                                    "Failed to update prescription."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Prescription> call,
                                    Throwable t
                            ) {

                                callback.onError(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Network error."
                                );
                            }
                        }
                );
    }


    private String getErrorMessage(
            Response<?> response,
            String defaultMessage
    ) {

        String message =
                defaultMessage
                        + " HTTP "
                        + response.code();

        try {

            if (response.errorBody() != null) {

                message += "\n"
                        + response.errorBody().string();
            }

        } catch (Exception ignored) {
        }

        return message;
    }


    public interface PrescriptionListCallback {

        void onSuccess(
                List<Prescription> prescriptions
        );

        void onError(
                String message
        );
    }


    public interface PrescriptionCallback {

        void onSuccess(
                Prescription prescription
        );

        void onError(
                String message
        );
    }
}
