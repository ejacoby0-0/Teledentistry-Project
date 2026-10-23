package com.example.teledentistry_project.data.model.insurance;

public class InsuranceClaimRequest {

    private String billing;
    private String policyNumber;
    private String claimStatus;

    public InsuranceClaimRequest(
            String billing,
            String policyNumber,
            String claimStatus
    ) {
        this.billing = billing;
        this.policyNumber = policyNumber;
        this.claimStatus = claimStatus;
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
}
