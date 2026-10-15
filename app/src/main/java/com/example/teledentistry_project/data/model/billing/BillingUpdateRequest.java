package com.example.teledentistry_project.data.model.billing;

public class BillingUpdateRequest {

    private double amount;
    private double insuranceCoverage;
    private String paymentStatus;

    public BillingUpdateRequest(
            double amount,
            double insuranceCoverage,
            String paymentStatus
    ) {
        this.amount = amount;
        this.insuranceCoverage = insuranceCoverage;
        this.paymentStatus = paymentStatus;
    }

    public double getAmount() {
        return amount;
    }

    public double getInsuranceCoverage() {
        return insuranceCoverage;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
