package com.example.teledentistry_project.data.model.practitioner;

import com.google.gson.annotations.SerializedName;

public class PractitionerAvailabilityRequest {

    @SerializedName("practitioner")
    private String practitioner;

    @SerializedName("date")
    private String date;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("isAvailable")
    private boolean isAvailable;

    public PractitionerAvailabilityRequest(
            String practitioner,
            String date,
            String startTime,
            boolean isAvailable
    ) {
        this.practitioner = practitioner;
        this.date = date;
        this.startTime = startTime;
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

    public boolean isAvailable() {
        return isAvailable;
    }
}