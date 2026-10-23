package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class TeethOverlayEvent {

    @SerializedName("is_visible")
    private boolean isVisible;

    @SerializedName("practitioner_name")
    private String practitionerName;

    @SerializedName("tooth_conditions")
    private Map<String, String> toothConditions; // Key: Tooth Number (e.g. "14"), Value: Condition ("healthy", "decay", "treated", "focus")

    @SerializedName("selected_tooth")
    private String selectedTooth;

    public TeethOverlayEvent() {}

    public TeethOverlayEvent(boolean isVisible, String practitionerName, Map<String, String> toothConditions, String selectedTooth) {
        this.isVisible = isVisible;
        this.practitionerName = practitionerName;
        this.toothConditions = toothConditions;
        this.selectedTooth = selectedTooth;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getPractitionerName() {
        return practitionerName;
    }

    public Map<String, String> getToothConditions() {
        return toothConditions;
    }

    public String getSelectedTooth() {
        return selectedTooth;
    }
}
