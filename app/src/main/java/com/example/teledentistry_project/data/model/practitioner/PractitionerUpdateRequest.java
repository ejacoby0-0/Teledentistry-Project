package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerUpdateRequest {

    private String fullName;
    private String phoneNumber;
    private String availabilityStatus;

    public PractitionerUpdateRequest(
            String fullName,
            String phoneNumber,
            String availabilityStatus
    ) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.availabilityStatus = availabilityStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }
}