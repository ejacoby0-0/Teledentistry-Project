package com.example.teledentistry_project.data.model.insurance;

public class InsuranceClaim {

    private String id;
    private String billing;
    private String policyNumber;
    private String claimStatus;
    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public String getBilling() {
        return billing;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getClaimStatus() {
        return claimStatus;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
