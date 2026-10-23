package com.example.teledentistry_project.data.model.dental;

public class DentalRecordRequest {

    private String patient;

    public DentalRecordRequest(String patient) {
        this.patient = patient;
    }

    public String getPatient() {
        return patient;
    }
}
