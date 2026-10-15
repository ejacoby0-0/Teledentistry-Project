package com.example.teledentistry_project.data.model.billing;

public class Billing {

    private String id;
    private String appointment;
    private double amount;
    private double insuranceCoverage;
    private String paymentStatus;
    private String created;
    private String updated;

    public String getId() {
        return id;
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

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
