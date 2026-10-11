package com.example.teledentistry_project.data.model.appointment;

public class Appointment {

    private String id;
    private String patient;
    private String practitioner;

    private String dateTime;

    private String startTime;
    private String endTime;

    private String status;

    private String created;
    private String updated;

    public Appointment() {
    }

    public String getId() {
        return id;
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

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
