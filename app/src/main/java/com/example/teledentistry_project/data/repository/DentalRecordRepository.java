package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.dental.DentalRecord;
import com.example.teledentistry_project.data.model.dental.DentalRecordRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DentalRecordRepository {

    // Find the dental record belonging to a patient
    public void getDentalRecordByPatient(
            String authToken,
            String patientId,
            DentalRecordCallback callback
    ) {

        String filter =
                "patient = \"" + patientId + "\"";

        PocketBaseClient.api
                .getDentalRecords(
                        authToken,
                        filter
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<DentalRecord>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<DentalRecord>> call,
                                    Response<PocketBaseListResponse<DentalRecord>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<DentalRecord> records =
                                            response.body().getItems();

                                    if (records != null
                                            && !records.isEmpty()) {

                                        callback.onSuccess(
                                                records.get(0)
                                        );

                                    } else {

                                        callback.onNotFound();
                                    }

                                } else {

                                    callback.onError(
                                            "Failed to load dental record. HTTP "
                                                    + response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<DentalRecord>> call,
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


    // Create a dental record for a patient
    public void createDentalRecord(
            String authToken,
            String patientId,
            DentalRecordCallback callback
    ) {

        DentalRecordRequest request =
                new DentalRecordRequest(
                        patientId
                );

        PocketBaseClient.api
                .createDentalRecord(
                        authToken,
                        request
                )
                .enqueue(
                        new Callback<DentalRecord>() {

                            @Override
                            public void onResponse(
                                    Call<DentalRecord> call,
                                    Response<DentalRecord> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body()
                                    );

                                } else {

                                    String errorMessage =
                                            "Failed to create dental record. HTTP "
                                                    + response.code();

                                    try {

                                        if (response.errorBody() != null) {

                                            errorMessage +=
                                                    "\n"
                                                            + response
                                                            .errorBody()
                                                            .string();
                                        }

                                    } catch (Exception ignored) {
                                    }

                                    callback.onError(
                                            errorMessage
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<DentalRecord> call,
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


    public interface DentalRecordCallback {

        void onSuccess(
                DentalRecord dentalRecord
        );

        void onNotFound();

        void onError(
                String message
        );
    }
}
