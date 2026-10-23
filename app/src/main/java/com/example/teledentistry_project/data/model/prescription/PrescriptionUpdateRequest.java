package com.example.teledentistry_project.data.model.prescription;

public class PrescriptionUpdateRequest {

    private String medication;
    private String dosage;
    private String issueDate;
    private String expiryDate;

    public PrescriptionUpdateRequest(
            String medication,
            String dosage,
            String issueDate,
            String expiryDate
    ) {
        this.medication = medication;
        this.dosage = dosage;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
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
