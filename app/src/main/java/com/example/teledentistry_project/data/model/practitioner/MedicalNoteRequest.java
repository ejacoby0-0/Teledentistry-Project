package com.example.teledentistry_project.data.model.practitioner;

public class MedicalNoteRequest {

    private final String practitioner;
    private final String patient;
    private final String notes;

    public MedicalNoteRequest(String practitioner, String patient, String notes) {
        this.practitioner = practitioner;
        this.patient = patient;
        this.notes = notes;
    }
}