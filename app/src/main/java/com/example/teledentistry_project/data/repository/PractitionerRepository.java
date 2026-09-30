package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PractitionerRepository {

    public interface PractitionerCallback {
        void onSuccess(Practitioner practitioner);
        void onNotFound();
        void onError(String message);
    }

    public void getPractitionerByUserId(
            String authToken,
            String userId,
            PractitionerCallback callback
    ) {

        // Find the practitioner record linked to
        // the currently logged-in user.
        String filter = "user = \"" + userId + "\"";

        PocketBaseClient.api
                .getPractitioners(authToken, filter)
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<Practitioner>> call,
                            Response<PocketBaseListResponse<Practitioner>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            PocketBaseListResponse<Practitioner> result =
                                    response.body();

                            if (result.getItems() != null
                                    && !result.getItems().isEmpty()) {

                                Practitioner practitioner =
                                        result.getItems().get(0);

                                callback.onSuccess(practitioner);

                            } else {

                                callback.onNotFound();
                            }

                        } else {

                            callback.onError(
                                    "Failed to load practitioner profile. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Practitioner>> call,
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
