package com.example.teledentistry_project.data.repository;

import android.util.Log;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.model.appointment.AppointmentStatusUpdateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";

    // Create appointment

    public void createAppointment(
            String authToken,
            AppointmentCreateRequest request,
            AppointmentCreateCallback callback
    ) {

        PocketBaseClient.api
                .createAppointment(authToken, request)
                .enqueue(new Callback<Appointment>() {

                    @Override
                    public void onResponse(
                            Call<Appointment> call,
                            Response<Appointment> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    readError(response, "Failed to book appointment.")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Appointment> call,
                            Throwable t
                    ) {
                        callback.onError(networkError(t));
                    }
                });
    }


    // Get patient appointments

    public void getPatientAppointments(
            String authToken,
            String patientId,
            AppointmentListCallback callback
    ) {

        String filter = "patient = \"" + patientId + "\"";

        getAppointments(authToken, filter, callback);
    }


    // Get practitioner appointments

    public void getPractitionerAppointments(
            String authToken,
            String practitionerId,
            AppointmentListCallback callback
    ) {

        String filter = "practitioner = \"" + practitionerId + "\"";

        getAppointments(authToken, filter, callback);
    }


    // Shared GET method

    private void getAppointments(
            String authToken,
            String filter,
            AppointmentListCallback callback
    ) {

        PocketBaseClient.api
                .getAppointments(
                        authToken,
                        filter,
                        "patient,availability"
                )
                .enqueue(
                        new Callback<PocketBaseListResponse<Appointment>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<Appointment>> call,
                                    Response<PocketBaseListResponse<Appointment>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<Appointment> appointments =
                                            response.body().getItems();

                                    callback.onSuccess(appointments);

                                } else {

                                    String error = readError(
                                            response,
                                            "Failed to load appointments."
                                    );

                                    Log.e(TAG, error);
                                    callback.onError(error);
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<Appointment>> call,
                                    Throwable t
                            ) {
                                String error = networkError(t);
                                Log.e(TAG, error, t);
                                callback.onError(error);
                            }
                        }
                );
    }


    // Update Appointment Status

    public void updateAppointmentStatus(
            String authToken,
            String appointmentId,
            String status,
            AppointmentUpdateCallback callback
    ) {

        AppointmentStatusUpdateRequest request =
                new AppointmentStatusUpdateRequest(status);

        PocketBaseClient.api
                .updateAppointmentStatus(
                        authToken,
                        appointmentId,
                        request
                )
                .enqueue(new Callback<Appointment>() {

                    @Override
                    public void onResponse(
                            Call<Appointment> call,
                            Response<Appointment> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            String error = readError(
                                    response,
                                    "Failed to update appointment."
                            );

                            Log.e(TAG, error);
                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Appointment> call,
                            Throwable t
                    ) {
                        callback.onError(networkError(t));
                    }
                });
    }


    // Error Helpers

    private String readError(
            Response<?> response,
            String defaultMessage
    ) {

        String message = defaultMessage + " HTTP " + response.code();

        try {
            if (response.errorBody() != null) {
                message += "\n" + response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not read PocketBase error response.", e);
        }

        return message;
    }

    private String networkError(Throwable t) {

        return t.getMessage() != null
                ? t.getMessage()
                : "A network error occurred.";
    }


    // Callbacks

    public interface AppointmentCreateCallback {

        void onSuccess(Appointment appointment);

        void onError(String message);
    }


    public interface AppointmentListCallback {

        void onSuccess(List<Appointment> appointments);

        void onError(String message);
    }


    public interface AppointmentUpdateCallback {

        void onSuccess(Appointment appointment);

        void onError(String message);
    }
}