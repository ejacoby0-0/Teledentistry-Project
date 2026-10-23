package com.example.teledentistry_project.data.model.prescription;

import com.google.gson.annotations.SerializedName;

public class Prescription {

    private String id;
    private String patient;
    private String practitioner;
    private String created;

    @SerializedName("prescriptionNote")
    private String prescriptionNote;

    @SerializedName("expand")
    private ExpandedRecords expand;

    public String getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getCreated() {
        return created;
    }

    public String getPrescriptionNote() {
        return prescriptionNote;
    }

    public String getPractitionerName() {
        if (expand == null || expand.practitioner == null) {
            return "Practitioner";
        }

        String name = expand.practitioner.name == null
                ? ""
                : expand.practitioner.name.trim();

        String surname = expand.practitioner.surname == null
                ? ""
                : expand.practitioner.surname.trim();

        String fullName = (name + " " + surname).trim();

        if (fullName.isEmpty()) {
            return "Practitioner";
        }

        return fullName;
    }

    public static class ExpandedRecords {
        @SerializedName("practitioner")
        public PractitionerUser practitioner;
    }

    public static class PractitionerUser {
        public String name;
        public String surname;
    }
}