package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.dental.Tooth;
import com.example.teledentistry_project.data.model.dental.ToothRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToothRepository {

    // Load all teeth belonging to a dental record
    public void getTeethByDentalRecord(
            String authToken,
            String dentalRecordId,
            ToothListCallback callback
    ) {

        String filter =
                "dentalRecord = \"" + dentalRecordId + "\"";

        PocketBaseClient.api
                .getTeeth(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<Tooth>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<Tooth>> call,
                                    Response<PocketBaseListResponse<Tooth>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body().getItems()
                                    );

                                } else {

                                    callback.onError(
                                            "Failed to load tooth records. HTTP "
                                                    + response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<Tooth>> call,
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


    // Create a tooth record
    public void createTooth(
            String authToken,
            ToothRequest request,
            ToothCallback callback
    ) {

        PocketBaseClient.api
                .createTooth(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<Tooth>() {

                            @Override
                            public void onResponse(
                                    Call<Tooth> call,
                                    Response<Tooth> response
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
                                                    "Failed to create tooth record."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Tooth> call,
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


    // Update an existing tooth record
    public void updateTooth(
            String authToken,
            String toothId,
            ToothRequest request,
            ToothCallback callback
    ) {

        PocketBaseClient.api
                .updateTooth(
                        authToken,
                        toothId,
                        request
                )
                .enqueue(
                        new Callback<Tooth>() {

                            @Override
                            public void onResponse(
                                    Call<Tooth> call,
                                    Response<Tooth> response
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
                                                    "Failed to update tooth record."
                                            )
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Tooth> call,
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


    public interface ToothListCallback {

        void onSuccess(
                List<Tooth> teeth
        );

        void onError(
                String message
        );
    }


    public interface ToothCallback {

        void onSuccess(
                Tooth tooth
        );

        void onError(
                String message
        );
    }
}
