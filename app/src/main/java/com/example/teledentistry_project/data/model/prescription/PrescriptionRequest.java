package com.example.teledentistry_project.data.model.prescription;

public class PrescriptionRequest {

    private String patient;
    private String practitioner;
    private String medication;
    private String dosage;
    private String issueDate;
    private String expiryDate;

    public PrescriptionRequest(
            String patient,
            String practitioner,
            String medication,
            String dosage,
            String issueDate,
            String expiryDate
    ) {
        this.patient = patient;
        this.practitioner = practitioner;
        this.medication = medication;
        this.dosage = dosage;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
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
}
