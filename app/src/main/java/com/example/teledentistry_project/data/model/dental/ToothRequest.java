package com.example.teledentistry_project.data.model.dental;

public class ToothRequest {

    private String dentalRecord;
    private String toothNumber;
    private String condition;
    private String treatmentHistory;

    public ToothRequest(
            String dentalRecord,
            String toothNumber,
            String condition,
            String treatmentHistory
    ) {

        this.dentalRecord = dentalRecord;
        this.toothNumber = toothNumber;
        this.condition = condition;
        this.treatmentHistory = treatmentHistory;
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
}
