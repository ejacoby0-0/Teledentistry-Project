package com.example.teledentistry_project.data.model.treatment;

public class TreatmentPlanRequest {

    private String patient;
    private String practitioner;

    private double totalCost;

    private String startDate;
    private String status;


    public TreatmentPlanRequest(
            String patient,
            String practitioner,
            double totalCost,
            String startDate,
            String status
    ) {

        this.patient = patient;
        this.practitioner = practitioner;
        this.totalCost = totalCost;
        this.startDate = startDate;
        this.status = status;
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
}
