package com.example.teledentistry_project.data.model.appointment;

import com.google.gson.annotations.SerializedName;

public class Appointment {

    @SerializedName("id")
    private String id;

    @SerializedName("patient")
    private String patient;

    @SerializedName("practitioner")
    private String practitioner;

    @SerializedName("availability")
    private String availability;

    @SerializedName("status")
    private String status;

    @SerializedName("expand")
    private ExpandedRecords expand;

    public Appointment() {
    }

    public String getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public String getPractitioner() {
        return practitioner;
    }

    public String getAvailability() {
        return availability;
    }

    public String getStatus() {
        return status;
    }

    public ExpandedRecords getExpand() {
        return expand;
    }

    public String getPatientName() {
        if (expand != null && expand.patient != null) {
            String name = expand.patient.getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
        }
        return "Patient";
    }

    public String getAppointmentDate() {
        if (expand != null && expand.availability != null) {
            return expand.availability.getDate();
        }
        return null;
    }

    public String getAppointmentTime() {
        if (expand != null && expand.availability != null) {
            return expand.availability.getStartTime();
        }
        return null;
    }

    public static class ExpandedRecords {

        @SerializedName("patient")
        private PatientRecord patient;

        @SerializedName("availability")
        private AvailabilityRecord availability;
    }

    public static class PatientRecord {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("fullName")
        private String fullName;

        public String getId() {
            return id;
        }

        public String getName() {
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            }
            return name;
        }
    }

    public static class AvailabilityRecord {

        @SerializedName("id")
        private String id;

        @SerializedName("date")
        private String date;

        @SerializedName("startTime")
        private String startTime;

        public String getId() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }
    }
}