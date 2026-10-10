package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseLoginRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseRegisterRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.patient.PatientUpdateRequest;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.model.practitioner.PractitionerUpdateRequest;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplication;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplicationUpdateRequest;


import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface PocketBaseApi {

    @POST("api/collections/users/records")
    Call<PocketBaseUser> register(
            @Body PocketBaseRegisterRequest request
    );

    @POST("api/collections/users/auth-with-password")
    Call<PocketBaseAuthResponse> login(
            @Body PocketBaseLoginRequest request
    );

    //Verifies he current authentication state and returns a new auth
    //response/token when successful.
    @POST("api/collections/users/auth-refresh")
    Call<PocketBaseAuthResponse> refreshAuth(
            @Header("Authorization") String authToken
    );

    //Endpoint for PocketBaseListResponse
    @GET("api/collections/patients/records")
    Call<PocketBaseListResponse<Patient>> getPatients(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    //Endpoint for PatientUpdateRequest
    @PATCH("api/collections/patients/records/{id}")
    Call<Patient> updatePatient(
            @Header("Authorization") String authToken,
            @Path("id") String patientId,
            @Body PatientUpdateRequest request
    );

    //Endpoint for Practitioner
    @GET("api/collections/practitioners/records")
    Call<PocketBaseListResponse<Practitioner>> getPractitioners(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    //PATCH endpoint for updating the practitioner profile.
    @PATCH("api/collections/practitioners/records/{id}")
    Call<Practitioner> updatePractitioner(
            @Header("Authorization") String authToken,
            @Path("id") String practitionerId,
            @Body PractitionerUpdateRequest request
    );

    //Gets the data in practitioner_application records
    @GET("api/collections/practitioner_applications/records")
    Call<PocketBaseListResponse<PractitionerApplication>> getPractitionerApplications(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    //
    @PATCH("api/collections/practitioner_applications/records/{id}")
    Call<PractitionerApplication> updatePractitionerApplication(
            @Header("Authorization") String authToken,
            @Path("id") String applicationId,
            @Body PractitionerApplicationUpdateRequest request
    );

}
