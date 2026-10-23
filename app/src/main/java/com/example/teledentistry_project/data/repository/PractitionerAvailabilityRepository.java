
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

    public interface AvailabilityListCallback {
        void onSuccess(List<PractitionerAvailability> slots);
        void onError(String message);
    }

    public interface AvailabilityCallback {
        void onSuccess(PractitionerAvailability slot);
        void onError(String message);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public void getSlots(
            String authToken,
            String practitionerId,
            String date,
            AvailabilityListCallback callback
    ) {
        String filter = "practitioner = \"" + practitionerId
                + "\" && date = \"" + date + "\"";

        PocketBaseClient.api.getPractitionerAvailability(
                authToken,
                filter,
                "startTime"
        ).enqueue(new Callback<PocketBaseListResponse<PractitionerAvailability>>() {

            @Override
            public void onResponse(
                    Call<PocketBaseListResponse<PractitionerAvailability>> call,
                    Response<PocketBaseListResponse<PractitionerAvailability>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getItems());
                } else {
                    callback.onError(readError(response));
                }
            }

            @Override
            public void onFailure(
                    Call<PocketBaseListResponse<PractitionerAvailability>> call,
                    Throwable t
            ) {
                callback.onError(
                        t.getMessage() != null ? t.getMessage() : "Network error."
                );
            }
        });
    }

    public void createSlot(
            String authToken,
            PractitionerAvailabilityRequest request,
            AvailabilityCallback callback
    ) {
        PocketBaseClient.api.createPractitionerAvailability(
                authToken,
                request
        ).enqueue(new Callback<PractitionerAvailability>() {

            @Override
            public void onResponse(
                    Call<PractitionerAvailability> call,
                    Response<PractitionerAvailability> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(readError(response));
                }
            }

            @Override
            public void onFailure(
                    Call<PractitionerAvailability> call,
                    Throwable t
            ) {
                callback.onError(
                        t.getMessage() != null ? t.getMessage() : "Network error."
                );
            }
        });
    }

    public void deleteSlot(
            String authToken,
            String availabilityId,
            DeleteCallback callback
    ) {
        PocketBaseClient.api.deletePractitionerAvailability(
                authToken,
                availabilityId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(readError(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(
                        t.getMessage() != null ? t.getMessage() : "Network error."
                );
            }
        });
    }

    private String readError(Response<?> response) {
        String message = "Request failed. HTTP " + response.code();

        try {
            if (response.errorBody() != null) {
                message += "\n" + response.errorBody().string();
            }
        } catch (Exception ignored) {
        }

        return message;
    }
}
