package com.example.teledentistry_project.data.model.practitioner;

public class MedicalNote {

    private String id;
    private String practitioner;
    private String patient;
    private String notes;

    public String getId() { return id; }
    public String getPractitioner() { return practitioner; }
    public String getPatient() { return patient; }
    public String getNotes() { return notes; }
}