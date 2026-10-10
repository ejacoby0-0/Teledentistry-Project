package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerApplication {

    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private String specialization;
    private String practiceName;
    private String status;
    private String created;
    private String updated;

    public PractitionerApplication() {
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
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

    public String getPracticeName() {
        return practiceName;
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