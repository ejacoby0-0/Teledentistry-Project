package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.insurance.InsuranceClaim;
import com.example.teledentistry_project.data.model.insurance.InsuranceClaimRequest;
import com.example.teledentistry_project.data.model.insurance.InsuranceClaimUpdateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsuranceClaimRepository {

    // Get insurance claims belonging to a billing record
    public void getClaimsByBilling(
            String authToken,
            String billingId,
            InsuranceClaimListCallback callback
    ) {

        String filter =
                "billing = \"" + billingId + "\"";

        PocketBaseClient.api
                .getInsuranceClaims(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<InsuranceClaim>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<InsuranceClaim>> call,
                                    Response<PocketBaseListResponse<InsuranceClaim>> response
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
                                                    "Failed to load insurance claims."
                                            )
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<InsuranceClaim>> call,
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


    // Create insurance claim
    public void createInsuranceClaim(
            String authToken,
            InsuranceClaimRequest request,
            InsuranceClaimCallback callback
    ) {

        PocketBaseClient.api
                .createInsuranceClaim(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<InsuranceClaim>() {

                            @Override
                            public void onResponse(
                                    Call<InsuranceClaim> call,
                                    Response<InsuranceClaim> response
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
                                                    "Failed to create insurance claim."
                                            )
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<InsuranceClaim> call,
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


    // Update insurance claim
    public void updateInsuranceClaim(
            String authToken,
            String claimId,
            InsuranceClaimUpdateRequest request,
            InsuranceClaimCallback callback
    ) {

        PocketBaseClient.api
                .updateInsuranceClaim(
                        authToken,
                        claimId,
                        request
                )
                .enqueue(
                        new Callback<InsuranceClaim>() {

                            @Override
                            public void onResponse(
                                    Call<InsuranceClaim> call,
                                    Response<InsuranceClaim> response
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
                                                    "Failed to update insurance claim."
                                            )
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<InsuranceClaim> call,
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


    public interface InsuranceClaimListCallback {

        void onSuccess(
                List<InsuranceClaim> claims
        );

        void onError(
                String message
        );
    }


    public interface InsuranceClaimCallback {

        void onSuccess(
                InsuranceClaim claim
        );

        void onError(
                String message
        );
    }
}
