package com.example.teledentistry_project.data.model.treatment;

public class TreatmentPlan {

    private String id;
    private String patient;
    private String practitioner;

    private double totalCost;

    private String startDate;
    private String status;

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

    public double getTotalCost() {
        return totalCost;
    }

    public String getStartDate() {
        return startDate;
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
