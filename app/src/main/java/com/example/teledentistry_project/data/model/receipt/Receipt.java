package com.example.teledentistry_project.data.model.receipt;

public class Receipt {

    private String id;
    private String practitioner;
    private String patient;
    private double cost;
    private String created;

    public Receipt() {
    }

    public String getId() {
        return id;
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

    public String getCreated() {
        return created;
    }
}
