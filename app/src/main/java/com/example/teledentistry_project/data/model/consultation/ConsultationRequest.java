package com.example.teledentistry_project.data.model.consultation;

import com.example.teledentistry_project.ui.patient.ChatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

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

    public ConsultationRequest(
            String appointment,
            String diagnosisNotes,
            String chatLog,
            String overlayState
    ) {
        this.appointment = appointment;
        try {
            ChatActivity.ConsultationPayload payload = new ChatActivity.ConsultationPayload();
            payload.notes = diagnosisNotes != null && !diagnosisNotes.isEmpty() ? diagnosisNotes : "Consultation notes";
            if (chatLog != null && !chatLog.isEmpty()) {
                Type type = new TypeToken<List<ChatActivity.ChatMessage>>() {}.getType();
                payload.chatLog = new Gson().fromJson(chatLog, type);
            }
            if (overlayState != null) {
                payload.overlayState = overlayState;
            }
            this.diagnosisNotes = new Gson().toJson(payload);
        } catch (Exception e) {
            this.diagnosisNotes = diagnosisNotes;
        }
    }

    public String getAppointment() {
        return appointment;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }
}
