package com.example.teledentistry_project.data.model.insurance;

public class InsuranceClaimUpdateRequest {

    private String policyNumber;
    private String claimStatus;

    public InsuranceClaimUpdateRequest(
            String policyNumber,
            String claimStatus
    ) {
        this.policyNumber = policyNumber;
        this.claimStatus = claimStatus;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getClaimStatus() {
        return claimStatus;
    }
}