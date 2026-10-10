package com.example.teledentistry_project.data.model.appointment;

public class AppointmentCreateRequest {

    private String patient;
    private String practitioner;

    private String dateTime;
    private String startTime;
    private String endTime;

    private String status;

    public AppointmentCreateRequest(
            String patient,
            String practitioner,
            String dateTime,
            String startTime,
            String endTime,
            String status
    ) {
        this.patient = patient;
        this.practitioner = practitioner;
        this.dateTime = dateTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getPatient() {
        return patient;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
