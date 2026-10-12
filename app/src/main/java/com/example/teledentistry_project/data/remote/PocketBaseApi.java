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
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailability;
import com.example.teledentistry_project.data.model.practitioner.PractitionerAvailabilityRequest;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.appointment.AppointmentCreateRequest;
import com.example.teledentistry_project.data.model.appointment.AppointmentStatusUpdateRequest;
import com.example.teledentistry_project.data.model.consultation.Consultation;
import com.example.teledentistry_project.data.model.consultation.ConsultationRequest;
import com.example.teledentistry_project.data.model.dental.DentalRecord;
import com.example.teledentistry_project.data.model.dental.DentalRecordRequest;
import com.example.teledentistry_project.data.model.dental.Tooth;
import com.example.teledentistry_project.data.model.dental.ToothRequest;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlan;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlanRequest;
import com.example.teledentistry_project.data.model.treatment.TreatmentPlanUpdateRequest;
import com.example.teledentistry_project.data.model.prescription.Prescription;
import com.example.teledentistry_project.data.model.prescription.PrescriptionRequest;
import com.example.teledentistry_project.data.model.prescription.PrescriptionUpdateRequest;

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

    //creating and retrieving availability
    // Get practitioner availability records
    @GET("api/collections/practitioner_availability/records")
    Call<PocketBaseListResponse<PractitionerAvailability>> getPractitionerAvailability(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    // Create practitioner availability record
    @POST("api/collections/practitioner_availability/records")
    Call<PractitionerAvailability> createPractitionerAvailability(
            @Header("Authorization") String authToken,
            @Body PractitionerAvailabilityRequest request
    );

    // Get appointments using a PocketBase filter
    @GET("api/collections/appointments/records")
    Call<PocketBaseListResponse<Appointment>> getAppointments(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    // Create a new appointment
    @POST("api/collections/appointments/records")
    Call<Appointment> createAppointment(
            @Header("Authorization") String authToken,
            @Body AppointmentCreateRequest request
    );

    //Confriming appointment
    @PATCH("api/collections/appointments/records/{id}")
    Call<Appointment> updateAppointmentStatus(
            @Header("Authorization") String authToken,
            @Path("id") String appointmentId,
            @Body AppointmentStatusUpdateRequest request
    );

    // Search by appointment
    @GET("api/collections/consultations/records")
    Call<PocketBaseListResponse<Consultation>> getConsultations(
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

    //
    @GET("api/collections/dental_records/records")
    Call<PocketBaseListResponse<DentalRecord>> getDentalRecords(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/dental_records/records")
    Call<DentalRecord> createDentalRecord(
            @Header("Authorization") String authToken,
            @Body DentalRecordRequest request
    );


    @GET("api/collections/teeth/records")
    Call<PocketBaseListResponse<Tooth>> getTeeth(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/teeth/records")
    Call<Tooth> createTooth(
            @Header("Authorization") String authToken,
            @Body ToothRequest request
    );

    @PATCH("api/collections/teeth/records/{id}")
    Call<Tooth> updateTooth(
            @Header("Authorization") String authToken,
            @Path("id") String toothId,
            @Body ToothRequest request
    );

    //Treastmetn plan
    @GET("api/collections/treatment_plans/records")
    Call<PocketBaseListResponse<TreatmentPlan>> getTreatmentPlans(
            @Header("Authorization") String authToken,
            @Query("filter") String filter
    );

    @POST("api/collections/treatment_plans/records")
    Call<TreatmentPlan> createTreatmentPlan(
            @Header("Authorization") String authToken,
            @Body TreatmentPlanRequest request
    );

    @PATCH("api/collections/treatment_plans/records/{id}")
    Call<TreatmentPlan> updateTreatmentPlan(
            @Header("Authorization") String authToken,
            @Path("id") String treatmentPlanId,
            @Body TreatmentPlanUpdateRequest request
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
}
