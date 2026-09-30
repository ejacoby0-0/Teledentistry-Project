package com.example.teledentistry_project.data.model.patient;

public class PatientUpdateRequest {

    private String fullName;
    private String phoneNumber;

    public PatientUpdateRequest(
            String fullName,
            String phoneNumber
    ) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}
