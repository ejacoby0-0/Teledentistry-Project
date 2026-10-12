package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlan;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlanRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlanUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TreatmentPlanRepository {

    // Get all treatment plans belonging to a patient
    public void getTreatmentPlansByPatient(
            String authToken,
            String patientId,
            TreatmentPlanListCallback callback
    ) {

        String filter =
                "patient = \"" + patientId + "\"";

        PocketBaseClient.api
                .getTreatmentPlans(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<TreatmentPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<TreatmentPlan>> call,
                                    Response<PocketBaseListResponse<TreatmentPlan>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body().getItems()
                                    );

                                } else {

                                    callback.onError(
                                            "Failed to load treatment plans. HTTP "
                                                    + response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<TreatmentPlan>> call,
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


    // Create treatment plan
    public void createTreatmentPlan(
            String authToken,
            TreatmentPlanRequest request,
            TreatmentPlanCallback callback
    ) {

        PocketBaseClient.api
                .createTreatmentPlan(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<TreatmentPlan>() {

                            @Override
                            public void onResponse(
                                    Call<TreatmentPlan> call,
                                    Response<TreatmentPlan> response
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
                                                    "Failed to create treatment plan."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<TreatmentPlan> call,
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


    // Update treatment plan
    public void updateTreatmentPlan(
            String authToken,
            String treatmentPlanId,
            TreatmentPlanUpdateRequest request,
            TreatmentPlanCallback callback
    ) {

        PocketBaseClient.api
                .updateTreatmentPlan(
                        authToken,
                        treatmentPlanId,
                        request
                )
                .enqueue(
                        new Callback<TreatmentPlan>() {

                            @Override
                            public void onResponse(
                                    Call<TreatmentPlan> call,
                                    Response<TreatmentPlan> response
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
                                                    "Failed to update treatment plan."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<TreatmentPlan> call,
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


    public interface TreatmentPlanListCallback {

        void onSuccess(
                List<TreatmentPlan> treatmentPlans
        );

        void onError(
                String message
        );
    }


    public interface TreatmentPlanCallback {

        void onSuccess(
                TreatmentPlan treatmentPlan
        );

        void onError(
                String message
        );
    }
}
