package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.billing.Billing;
import com.example.teledentistry_project.data.model.billing.BillingRequest;
import com.example.teledentistry_project.data.model.billing.BillingUpdateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillingRepository {

    // Get billing records for a specific appointment
    public void getBillingByAppointment(
            String authToken,
            String appointmentId,
            BillingListCallback callback
    ) {

        String filter =
                "appointment = \"" + appointmentId + "\"";

        PocketBaseClient.api
                .getBillingRecords(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<Billing>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<Billing>> call,
                                    Response<PocketBaseListResponse<Billing>> response
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
                                                    "Failed to load billing."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<Billing>> call,
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


    // Create billing record
    public void createBilling(
            String authToken,
            BillingRequest request,
            BillingCallback callback
    ) {

        PocketBaseClient.api
                .createBilling(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<Billing>() {

                            @Override
                            public void onResponse(
                                    Call<Billing> call,
                                    Response<Billing> response
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
                                                    "Failed to create billing."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Billing> call,
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


    // Update billing record
    public void updateBilling(
            String authToken,
            String billingId,
            BillingUpdateRequest request,
            BillingCallback callback
    ) {

        PocketBaseClient.api
                .updateBilling(
                        authToken,
                        billingId,
                        request
                )
                .enqueue(
                        new Callback<Billing>() {

                            @Override
                            public void onResponse(
                                    Call<Billing> call,
                                    Response<Billing> response
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
                                                    "Failed to update billing."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Billing> call,
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


    public interface BillingListCallback {

        void onSuccess(
                List<Billing> billingRecords
        );

        void onError(
                String message
        );
    }


    public interface BillingCallback {

        void onSuccess(
                Billing billing
        );

        void onError(
                String message
        );
    }
}
