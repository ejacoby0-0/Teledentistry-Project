package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.remote.PocketBaseClient;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentRepository {

    // ---------------------------------------------------------
    // CREATE APPOINTMENT
    // ---------------------------------------------------------

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

                        if (response.isSuccessful() && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {

                            callback.onError(
                                    "Failed to create appointment."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Appointment> call,
                            Throwable t
                    ) {

                        callback.onError(t.getMessage());
                    }
                });
    }


    // ---------------------------------------------------------
    // GET PATIENT APPOINTMENTS
    // ---------------------------------------------------------

    public void getPatientAppointments(
            String authToken,
            String patientId,
            AppointmentListCallback callback
    ) {

        String filter =
                "patient = \"" + patientId + "\"";

        getAppointments(
                authToken,
                filter,
                callback
        );
    }


    // ---------------------------------------------------------
    // GET PRACTITIONER APPOINTMENTS
    // ---------------------------------------------------------

    public void getPractitionerAppointments(
            String authToken,
            String practitionerId,
            AppointmentListCallback callback
    ) {

        String filter =
                "practitioner = \"" + practitionerId + "\"";

        getAppointments(
                authToken,
                filter,
                callback
        );
    }


    // ---------------------------------------------------------
    // SHARED GET METHOD
    // ---------------------------------------------------------

    private void getAppointments(
            String authToken,
            String filter,
            AppointmentListCallback callback
    ) {

        PocketBaseClient.api
                .getAppointments(authToken, filter)
                .enqueue(
                        new Callback<PocketBaseListResponse<Appointment>>() {

                            @Override
                            public void onResponse(
                                    Call<PocketBaseListResponse<Appointment>> call,
                                    Response<PocketBaseListResponse<Appointment>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    callback.onSuccess(
                                            response.body().getItems()
                                    );

                                } else {

                                    callback.onError(
                                            "Failed to load appointments."
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PocketBaseListResponse<Appointment>> call,
                                    Throwable t
                            ) {

                                callback.onError(t.getMessage());
                            }
                        }
                );
    }


    // ---------------------------------------------------------
    // CALLBACKS
    // ---------------------------------------------------------

    public interface AppointmentCreateCallback {

        void onSuccess(Appointment appointment);

        void onError(String message);
    }


    public interface AppointmentListCallback {

        void onSuccess(List<Appointment> appointments);

        void onError(String message);
    }
}
