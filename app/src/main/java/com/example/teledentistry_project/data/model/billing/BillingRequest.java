package com.example.teledentistry_project.data.model.billing;

public class BillingRequest {

    private String appointment;
    private double amount;
    private double insuranceCoverage;
    private String paymentStatus;

    public BillingRequest(
            String appointment,
            double amount,
            double insuranceCoverage,
            String paymentStatus
    ) {
        this.appointment = appointment;
        this.amount = amount;
        this.insuranceCoverage = insuranceCoverage;
        this.paymentStatus = paymentStatus;
    }

    public String getAppointment() {
        return appointment;
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
