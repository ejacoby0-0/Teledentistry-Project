package com.example.teledentistry_project.data.model.appointment;

public class AppointmentStatusUpdateRequest {

    private String status;

    public AppointmentStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}