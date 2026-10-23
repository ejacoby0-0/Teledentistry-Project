
package com.example.teledentistry_project.data.model.practitioner;

import com.google.gson.annotations.SerializedName;

public class EditPractitionerRequest {
    @SerializedName("practiceName")
    private String practiceName;

    @SerializedName("practiceLocation")
    private String practiceLocation;

    @SerializedName("consultationFee")
    private double consultationFee;

    public EditPractitionerRequest(
            String practiceName,
            String practiceLocation,
            double consultationFee) {
        this.practiceName = practiceName;
        this.practiceLocation = practiceLocation;
        this.consultationFee = consultationFee;
    }
}
