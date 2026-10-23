package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.data.model.ActiveChat;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.UserProfile;
import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseLoginRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseRegisterRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.model.patient.PatientUpdateRequest;
import com.example.teledentistry_project.data.model.practitioner.MedicalNote;
import com.example.teledentistry_project.data.model.practitioner.MedicalNoteRequest;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
<<<<<<< Updated upstream
=======
import com.example.teledentistry_project.data.model.practitioner.PractitionerRegistrationRequest;
import com.example.teledentistry_project.data.model.practitioner.PractitionerUpdateRequest;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplication;
import com.example.teledentistry_project.data.model.practitioner.PractitionerApplicationUpdateRequest;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailabilityRequest;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.model.appointment.AppointmentStatusUpdateRequest;
import com.example.teledentistry_project.data.model.consultation.Consultation;
import com.example.teledentistry_project.data.model.consultation.ConsultationRequest;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.model.prescription.PrescriptionRequest;
import com.example.teledentistry_project.data.model.prescription.PrescriptionUpdateRequest;
import com.example.teledentistry_project.data.model.receipt.Receipt;
import com.example.teledentistry_project.data.model.receipt.ReceiptCreateRequest;


import com.example.teledentistry_project.data.model.EditProfileUser;
import com.example.teledentistry_project.data.model.practitioner.EditPractitionerRequest;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.MultipartBody;

import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

>>>>>>> Stashed changes

import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

<<<<<<< Updated upstream
=======
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

    //creating and retrieving availability
    // Get practitioner availability records
    @GET("api/collections/practitioner_availability/records")
    Call<PocketBaseListResponse<PractitionerAvailability>> getPractitionerAvailability(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/consultations/records")
    Call<Consultation> createConsultation(
            @Header("Authorization") String authToken,
            @Body ConsultationRequest request
    );

    @PATCH("api/collections/consultations/records/{id}")
    Call<Consultation> updateConsultation(
            @Header("Authorization") String authToken,
            @Path("id") String consultationId,
            @Body ConsultationRequest request
    );

    //Prescriptions Endpoints
    //Retrieve prescriptions for a particular patient
    @GET("api/collections/prescriptions/records")
    Call<PocketBaseListResponse<Prescription>> getPrescriptions(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/prescriptions/records")
    Call<Prescription> createPrescription(
            @Header("Authorization") String authToken,
            @Body PrescriptionRequest request
    );

    @PATCH("api/collections/prescriptions/records/{id}")
    Call<Prescription> updatePrescription(
            @Header("Authorization") String authToken,
            @Path("id") String prescriptionId,
            @Body PrescriptionUpdateRequest request
    );
    //Endpoint for chats viewed in profile activity
    @GET("api/collections/active_chats/records")
    Call<PocketBaseListResponse<ActiveChat>> getActiveChats(
            @Header("Authorization") String authToken,
            @Query("filter") String filter,
            @Query("sort") String sort,
            @Query("expand") String expand,
            @Query("perPage") int perPage
    );

    // One user's full record (the profile being viewed)
    @GET("api/collections/users/records/{id}")
    Call<UserProfile> getUserProfile(
            @Header("Authorization") String authToken,
            @Path("id") String userId
    );

    // Notes a practitioner has written about a patient
    @GET("api/collections/medicalNotes/records")
    Call<PocketBaseListResponse<MedicalNote>> getMedicalNotes(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/medicalNotes/records")
    Call<MedicalNote> createMedicalNote(
            @Header("Authorization") String authToken,
            @Body MedicalNoteRequest request
    );

    @PATCH("api/collections/medicalNotes/records/{id}")
    Call<MedicalNote> updateMedicalNote(
            @Header("Authorization") String authToken,
            @Path("id") String noteId,
            @Body MedicalNoteRequest request
    );

    @GET("api/collections/practitioner_availability/records")
    Call<PocketBaseListResponse<PractitionerAvailability>>
    getPractitionerAvailability(
            @Header("Authorization") String authToken,
            @Query("filter") String filter,
            @Query("sort") String sort
    );

    @POST("api/collections/practitioner_availability/records")
    Call<PractitionerAvailability> createPractitionerAvailability(
            @Header("Authorization") String authToken,
            @Body PractitionerAvailabilityRequest request
    );

    @DELETE("api/collections/practitioner_availability/records/{id}")
    Call<Void> deletePractitionerAvailability(
            @Header("Authorization") String authToken,
            @Path("id") String availabilityId
    );

    @GET("api/collections/appointments/records")
    Call<PocketBaseListResponse<Appointment>> getAppointments(
            @Header("Authorization") String authToken,
            @Query("filter") String filter,
            @Query("expand") String expand
    );

    @POST("api/collections/appointments/records")
    Call<Appointment> createAppointment(
            @Header("Authorization") String authToken,
            @Body AppointmentCreateRequest request
    );
    @PATCH("api/collections/appointments/records/{id}")
    Call<Appointment> updateAppointmentStatus(
            @Header("Authorization") String authToken,
            @Path("id") String appointmentId,
            @Body AppointmentStatusUpdateRequest request
    );


    @POST("api/collections/practitioners/records")
    Call<Practitioner> createPractitioner(
            @Header("Authorization") String authToken,
            @Body PractitionerRegistrationRequest request
    );


    @GET("api/collections/users/records/{id}")
    Call<EditProfileUser> getEditableUser(
            @Header("Authorization") String authToken,
            @Path("id") String userId
    );

    @Multipart
    @PATCH("api/collections/users/records/{id}")
    Call<EditProfileUser> updateUserProfileMultipart(
            @Header("Authorization") String authToken,
            @Path("id") String userId,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part avatar
    );

    @PATCH("api/collections/practitioners/records/{id}")
    Call<Practitioner> updatePractitionerForEdit(
            @Header("Authorization") String authToken,
            @Path("id") String practitionerId,
            @Body EditPractitionerRequest request
    );

    @POST("api/collections/receipts/records")
    Call<Receipt> createReceipt(
            @Header("Authorization") String authToken,
            @Body ReceiptCreateRequest request
    );


    @GET("api/collections/receipts/records")
    Call<PocketBaseListResponse<Receipt>> getReceipts(
            @Header("Authorization") String authToken,
            @Query("filter") String filter,
            @Query("sort") String sort,
            @Query("perPage") int perPage
    );

    @GET("api/collections/practitioners/records")
    Call<PocketBaseListResponse<Practitioner>> searchPractitioners(
            @Header("Authorization") String authToken,
            @Query("filter") String filter,
            @Query("expand") String expand,
            @Query("perPage") int perPage
    );


    @POST("api/collections/active_chats/records")
    Call<ActiveChat> createActiveChat(
            @Header("Authorization") String authToken,
            @Body ActiveChat request
    );


    @POST("api/collections/active_chats/records")
    Call<ActiveChat> createActiveChat(
            @Header("Authorization") String authToken,
            @Body Map<String, String> request
    );



>>>>>>> Stashed changes
}
