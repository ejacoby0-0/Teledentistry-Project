package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailabilityRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PractitionerAvailabilityRepository {

    // Get availability belonging to a specific practitioner
    public void getAvailabilityByPractitioner(
            String authToken,
            String practitionerId,
            AvailabilityListCallback callback
    ) {

        String filter = "practitioner = \"" + practitionerId + "\"";

        PocketBaseClient.api
                .getPractitionerAvailability(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<PractitionerAvailability>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<PractitionerAvailability>> call,
                            Response<PocketBaseListResponse<PractitionerAvailability>> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body().getItems());
                        } else {
                            callback.onError("Failed to load availability.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<PractitionerAvailability>> call,
                            Throwable t
                    ) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    // Create a new availability period
    public void createAvailability(
            String authToken,
            PractitionerAvailabilityRequest request,
            AvailabilityCreateCallback callback
    ) {

        PocketBaseClient.api
                .createPractitionerAvailability(authToken, request)
                .enqueue(new Callback<PractitionerAvailability>() {

                    @Override
                    public void onResponse(
                            Call<PractitionerAvailability> call,
                            Response<PractitionerAvailability> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Failed to create availability.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PractitionerAvailability> call,
                            Throwable t
                    ) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public interface AvailabilityListCallback {
        void onSuccess(List<PractitionerAvailability> availabilityList);

        void onError(String message);
    }

    public interface AvailabilityCreateCallback {
        void onSuccess(PractitionerAvailability availability);

        void onError(String message);
    }
}
