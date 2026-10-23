package com.example.teledentistry_project.data.model.practitioner;

import com.google.gson.annotations.SerializedName;

public class PractitionerAvailability {

    @SerializedName("id")
    private String id;

    @SerializedName("practitioner")
    private String practitioner;

    @SerializedName("date")
    private String date;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("isAvailable")
    private boolean isAvailable;

    @SerializedName("created")
    private String created;

    @SerializedName("updated")
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