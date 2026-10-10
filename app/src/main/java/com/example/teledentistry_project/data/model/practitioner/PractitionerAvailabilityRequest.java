package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerAvailabilityRequest {

    private String practitioner;
    private String date;
    private String startTime;
    private String endTime;
    private boolean isAvailable;

    public PractitionerAvailabilityRequest(
            String practitioner,
            String date,
            String startTime,
            String endTime,
            boolean isAvailable
    ) {
        this.practitioner = practitioner;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
