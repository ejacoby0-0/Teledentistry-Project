package com.example.teledentistry_project.data.model.patient;

public class Patient {

    private String id;
    private String user;

    private String fullName;
    private String phoneNumber;

    private String medicalHistory;
    private String dentalHistory;
    private String emergencyContact;
    private String dateOfBirth;

    private String created;
    private String updated;

    public Patient() {
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

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public String getDentalHistory() {
        return dentalHistory;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}