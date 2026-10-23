package com.example.teledentistry_project.data.model.consultation;

import com.example.teledentistry_project.ui.patient.ChatActivity;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Consultation {

    private String id;
    private String appointment;

    @SerializedName("diagnosisNotes")
    private String diagnosisNotes;

    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public String getAppointment() {
        return appointment;
    }

    public String getDiagnosisNotes() {
        if (diagnosisNotes == null) return "";
        try {
            ChatActivity.ConsultationPayload payload = new Gson().fromJson(diagnosisNotes, ChatActivity.ConsultationPayload.class);
            if (payload != null && payload.notes != null) {
                return payload.notes;
            }
        } catch (Exception ignored) {}
        return diagnosisNotes;
    }

    // Full stored JSON payload (notes + chatLog + overlayState), unlike getDiagnosisNotes()
    public String getRawPayload() {
        return diagnosisNotes != null ? diagnosisNotes : "";
    }

    public String getChatLog() {
        if (diagnosisNotes == null) return "";
        try {
            ChatActivity.ConsultationPayload payload = new Gson().fromJson(diagnosisNotes, ChatActivity.ConsultationPayload.class);
            if (payload != null && payload.chatLog != null) {
                return new Gson().toJson(payload.chatLog);
            }
        } catch (Exception ignored) {}
        return "";
    }

    public String getOverlayState() {
        if (diagnosisNotes == null) return "";
        try {
            ChatActivity.ConsultationPayload payload = new Gson().fromJson(diagnosisNotes, ChatActivity.ConsultationPayload.class);
            if (payload != null && payload.overlayState != null) {
                return payload.overlayState;
            }
        } catch (Exception ignored) {}
        return "";
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
