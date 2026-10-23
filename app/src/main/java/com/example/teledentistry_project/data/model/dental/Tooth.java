package com.example.teledentistry_project.data.model.dental;

public class Tooth {

    private String id;
    private String dentalRecord;
    private String toothNumber;
    private String condition;
    private String treatmentHistory;
    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public String getDentalRecord() {
        return dentalRecord;
    }

    public String getToothNumber() {
        return toothNumber;
    }

    public String getCondition() {
        return condition;
    }

    public String getTreatmentHistory() {
        return treatmentHistory;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
