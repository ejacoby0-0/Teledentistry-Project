
package com.example.teledentistry_project.data.model.receipt;

import com.google.gson.annotations.SerializedName;

public class ReceiptCreateRequest {

    @SerializedName("practitioner")
    private String practitioner;

    @SerializedName("patient")
    private String patient;

    @SerializedName("cost")
    private double cost;

    public ReceiptCreateRequest(
            String practitioner,
            String patient,
            double cost
    ) {
        this.practitioner = practitioner;
        this.patient = patient;
        this.cost = cost;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getPatient() {
        return patient;
    }

    public double getCost() {
        return cost;
    }
}
