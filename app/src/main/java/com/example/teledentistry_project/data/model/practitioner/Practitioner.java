package com.example.teledentistry_project.data.model.practitioner;

import com.example.teledentistry_project.data.model.UserRecords;
import com.google.gson.annotations.SerializedName;

public class Practitioner {

    private String id;
    private String user;
    private String created;
    private String updated;

    @SerializedName("practiceName")           // use the exact PocketBase field name
    private String practiceName;

    @SerializedName("practiceLocation")
    private String practiceLocation;

    @SerializedName("consultationFee")
    private double consultationFee;

    private Expand expand;                     // filled when the request asks for expand=user

    public static class Expand {
        private UserRecords user;
    }

    public Practitioner() {
    }

    public String getId() {
        return id;
    }

    public String getUserId() { return user; }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
    public String getPracticeName() { return practiceName; }
    public String getPracticeLocation() { return practiceLocation; }
    public double getConsultationFee() { return consultationFee; }

    public UserRecords getUser() {
        return expand == null ? null : expand.user;
    }
}
