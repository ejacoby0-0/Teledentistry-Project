package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.model.practitioner.PractitionerUpdateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PractitionerRepository {

    public interface PractitionerCallback {
        void onSuccess(Practitioner practitioner);
        void onNotFound();
        void onError(String message);
    }

    public interface PractitionerUpdateCallback {
        void onSuccess(Practitioner practitioner);
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

    public void updatePractitioner(
            String authToken,
            String practitionerId,
            PractitionerUpdateRequest request,
            PractitionerUpdateCallback callback
    ) {

        PocketBaseClient.api
                .updatePractitioner(authToken, practitionerId, request)
                .enqueue(new Callback<Practitioner>() {

                    @Override
                    public void onResponse(
                            Call<Practitioner> call,
                            Response<Practitioner> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    "Failed to update practitioner profile. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Practitioner> call,
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

    public interface PractitionerListCallback {

        void onSuccess(List<Practitioner> practitioners);

        void onError(String message);
    }

    public void getAllPractitioners(
            String authToken,
            PractitionerListCallback callback
    ) {

        PocketBaseClient.api
                .getPractitioners(authToken, "")
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {

                    @Override
                    public void onResponse(
                            Call<PocketBaseListResponse<Practitioner>> call,
                            Response<PocketBaseListResponse<Practitioner>> response
                    ) {

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(
                                    response.body().getItems()
                            );

                        } else {

                            callback.onError(
                                    "Failed to load practitioners."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PocketBaseListResponse<Practitioner>> call,
                            Throwable t
                    ) {

                        callback.onError(t.getMessage());
                    }
                });
    }

}
