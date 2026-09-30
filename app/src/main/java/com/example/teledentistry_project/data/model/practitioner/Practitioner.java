package com.example.teledentistry_project.data.model.practitioner;

public class Practitioner {

    private String id;
    private String user;
    private String fullName;
    private String phoneNumber;
    private String licenseNumber;
    private String specialization;
    private String availabilityStatus;
    private String created;
    private String updated;

    public Practitioner() {
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
