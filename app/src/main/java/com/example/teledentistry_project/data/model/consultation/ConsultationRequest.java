package com.example.teledentistry_project.data.model.consultation;

public class ConsultationRequest {

    private String appointment;
    private String diagnosisNotes;

    public ConsultationRequest(
            String appointment,
            String diagnosisNotes
    ) {
        this.appointment = appointment;
        this.diagnosisNotes = diagnosisNotes;
    }

    public String getAppointment() {
        return appointment;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }
}
