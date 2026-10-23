package com.example.teledentistry_project.data.model.practitioner;

public class PractitionerRegistrationRequest {

    private String user;
    private String practiceName;
    private String practiceLocation;
    private double consultationFee;

    public PractitionerRegistrationRequest(
            String user,
            String practiceName,
            String practiceLocation,
            double consultationFee
    ) {
        this.user = user;
        this.practiceName = practiceName;
        this.practiceLocation = practiceLocation;
        this.consultationFee = consultationFee;
    }

    public String getUser() {
        return user;
    }

    public String getPracticeName() {
        return practiceName;
    }

    public String getPracticeLocation() {
        return practiceLocation;
    }

    public double getConsultationFee() {
        return consultationFee;
    }
}