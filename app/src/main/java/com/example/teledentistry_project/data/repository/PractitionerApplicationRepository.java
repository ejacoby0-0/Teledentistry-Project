package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplication;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplicationUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PractitionerApplicationRepository {

    public interface ApplicationUpdateCallback {
        void onSuccess(PractitionerApplication application);
        void onError(String message);
    }

    public interface ApplicationsCallback {
        void onSuccess(List<PractitionerApplication> applications);
        void onError(String message);
    }

    public void getPendingApplications(
            String authToken,
            ApplicationsCallback callback
    ) {

        String filter = "status = \"pending\"";

        PocketBaseClient.api
                .getPractitionerApplications(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<PractitionerApplication>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<PractitionerApplication>> call,
                            Response<PocketBaseListResponse<PractitionerApplication>> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(response.body().getItems());

                        } else {

                            callback.onError(
                                    "Failed to load practitioner applications. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<PractitionerApplication>> call,
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

    public void rejectApplication(
            String authToken,
            String applicationId,
            ApplicationUpdateCallback callback
    ) {

        PractitionerApplicationUpdateRequest request =
                new PractitionerApplicationUpdateRequest("rejected");

        PocketBaseClient.api
                .updatePractitionerApplication(
                        authToken,
                        applicationId,
                        request
                )
                .enqueue(new Callback<PractitionerApplication>() {

                    @Override
                    public void onResponse(
                            Call<PractitionerApplication> call,
                            Response<PractitionerApplication> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    "Failed to reject application. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PractitionerApplication> call,
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

    public void approveApplication(
            String authToken,
            String applicationId,
            ApplicationUpdateCallback callback
    ) {

        PractitionerApplicationUpdateRequest request =
                new PractitionerApplicationUpdateRequest("approved");

        PocketBaseClient.api
                .updatePractitionerApplication(
                        authToken,
                        applicationId,
                        request
                )
                .enqueue(new Callback<PractitionerApplication>() {

                    @Override
                    public void onResponse(
                            Call<PractitionerApplication> call,
                            Response<PractitionerApplication> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    "Failed to approve application. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PractitionerApplication> call,
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
