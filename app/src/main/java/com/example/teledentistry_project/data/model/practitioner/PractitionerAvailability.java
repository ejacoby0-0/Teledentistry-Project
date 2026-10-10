package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerAvailability {

    private String id;
    private String practitioner;
    private String date;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
    private String created;
    private String updated;

    public PractitionerAvailability() {
    }

    public String getId() {
        return id;
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

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}