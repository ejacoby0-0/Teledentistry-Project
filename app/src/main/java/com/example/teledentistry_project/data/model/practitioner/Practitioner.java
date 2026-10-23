package com.example.teledentistry_project.data.model.practitioner;

public class Practitioner {

    private String id = "prac_mock_001";
    private String user = "default_user_id";
    private String fullName = "Dr. Sarah Jenkins";
    private String phoneNumber = "+1987654321";
    private String licenseNumber = "DEN-99201";
    private String specialization = "General Dentistry";
    private String availabilityStatus = "available";
    private String status = "approved";
    private String created;
    private String updated;

    public Practitioner() {
    }

    public Practitioner(String id, String fullName, String phoneNumber, String licenseNumber, String specialization, String availabilityStatus) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.availabilityStatus = availabilityStatus;
    }

    public String getId() {
        return id != null ? id : "prac_mock_001";
    }

    public String getUser() {
        return user;
    }

    public String getFullName() {
        return (fullName == null || fullName.trim().isEmpty()) ? "Dr. Sarah Jenkins" : fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getSpecialization() {
        return (specialization == null || specialization.trim().isEmpty()) ? "General Dentistry" : specialization;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public String getStatus() {
        return status != null ? status : "approved";
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
