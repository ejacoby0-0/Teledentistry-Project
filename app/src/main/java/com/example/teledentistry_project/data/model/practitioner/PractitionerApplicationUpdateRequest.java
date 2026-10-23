package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerApplicationUpdateRequest {

    private String status;

    public PractitionerApplicationUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
