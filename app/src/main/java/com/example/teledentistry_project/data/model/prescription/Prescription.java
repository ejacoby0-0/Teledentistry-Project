package com.example.teledentistry_project.data.model.prescription;

public class Prescription {

    private String id;
    private String patient;
    private String practitioner;
    private String medication;
    private String dosage;
    private String issueDate;
    private String expiryDate;
    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getMedication() {
        return medication;
    }

    public String getDosage() {
        return dosage;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
