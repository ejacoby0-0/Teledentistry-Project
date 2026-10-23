package com.example.teledentistry_project.data.model.treatment;

public class TreatmentPlanUpdateRequest {

    private double totalCost;
    private String startDate;
    private String status;

    public TreatmentPlanUpdateRequest(
            double totalCost,
            String startDate,
            String status
    ) {
        this.totalCost = totalCost;
        this.startDate = startDate;
        this.status = status;
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