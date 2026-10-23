
package com.example.teledentistry_project.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.ActiveChat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PatientAdapter
        extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    public interface OnPatientClickListener {
        void onViewPatient(ActiveChat chat);
    }

    private final List<ActiveChat> chats = new ArrayList<>();
    private final OnPatientClickListener listener;

    public PatientAdapter(OnPatientClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<ActiveChat> newChats) {
        chats.clear();

        if (newChats != null) {
            chats.addAll(newChats);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);

        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PatientViewHolder holder,
            int position) {

        ActiveChat chat = chats.get(position);

        String patientName = "Patient";

        if (chat.expand != null && chat.expand.patient != null) {
            String name = chat.expand.patient.name == null
                    ? "" : chat.expand.patient.name.trim();

            String surname = chat.expand.patient.surname == null
                    ? "" : chat.expand.patient.surname.trim();

            String fullName = (name + " " + surname).trim();

            if (!fullName.isEmpty()) {
                patientName = fullName;
            }
        }

        holder.tvPatientName.setText(patientName);

        holder.btnViewPatient.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewPatient(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {

        TextView tvPatientName;
        MaterialButton btnViewPatient;

        PatientViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            btnViewPatient = itemView.findViewById(R.id.btnViewPatient);
        }
    }
}
