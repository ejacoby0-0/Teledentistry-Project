package com.example.teledentistry_project.data.model.appointment;

import com.google.gson.annotations.SerializedName;

public class AppointmentCreateRequest {
    @SerializedName("patient")
    private String patient;

    @SerializedName("practitioner")
    private String practitioner;

    @SerializedName("availability")
    private String availability;

    @SerializedName("status")
    private String status;

    public AppointmentCreateRequest(
            String patient,
            String practitioner,
            String availability,
            String status) {
        this.patient = patient;
        this.practitioner = practitioner;
        this.availability = availability;
        this.status = status;
    }
}