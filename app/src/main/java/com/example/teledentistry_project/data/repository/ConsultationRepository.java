package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.consultation.Consultation;
import com.example.teledentistry_project.data.model.consultation.ConsultationRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultationRepository {

    // Find the consultation belonging to an appointment
    public void getConsultationByAppointment(
            String authToken,
            String appointmentId,
            ConsultationCallback callback
    ) {

        String filter =
                "appointment = \"" + appointmentId + "\"";

        PocketBaseClient.api
                .getConsultations(
                        authToken,
                        filter
                )
                .enqueue(new Callback<PocketBaseListResponse<Consultation>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<Consultation>> call,
                            Response<PocketBaseListResponse<Consultation>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<Consultation> consultations =
                                    response.body().getItems();

                            if (consultations != null
                                    && !consultations.isEmpty()) {

                                callback.onSuccess(
                                        consultations.get(0)
                                );

                            } else {

                                callback.onNotFound();
                            }

                        } else {

                            callback.onError(
                                    "Failed to load consultation. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Consultation>> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error."
                        );
                    }
                });
    }


    // Create a new consultation
    public void createConsultation(
            String authToken,
            ConsultationRequest request,
            ConsultationCallback callback
    ) {

        PocketBaseClient.api
                .createConsultation(
                        authToken,
                        request
                )
                .enqueue(new Callback<Consultation>() {

                    @Override
                    public void onResponse(
                            Call<Consultation> call,
                            Response<Consultation> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Failed to create consultation. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Consultation> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error."
                        );
                    }
                });
    }


    // Update an existing consultation
    public void updateConsultation(
            String authToken,
            String consultationId,
            ConsultationRequest request,
            ConsultationCallback callback
    ) {

        PocketBaseClient.api
                .updateConsultation(
                        authToken,
                        consultationId,
                        request
                )
                .enqueue(new Callback<Consultation>() {

                    @Override
                    public void onResponse(
                            Call<Consultation> call,
                            Response<Consultation> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Failed to update consultation. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Consultation> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage() != null
                                        ? t.getMessage()
                                        : "Network error."
                        );
                    }
                });
    }


    public interface ConsultationCallback {

        void onSuccess(Consultation consultation);

        void onNotFound();

        void onError(String message);
    }
}
