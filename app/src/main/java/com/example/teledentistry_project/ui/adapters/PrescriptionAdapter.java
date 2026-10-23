package com.example.teledentistry_project.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.prescription.Prescription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PrescriptionAdapter
        extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {

    private final List<Prescription> prescriptions = new ArrayList<>();

    public void setPrescriptions(List<Prescription> newPrescriptions) {
        prescriptions.clear();

        if (newPrescriptions != null) {
            prescriptions.addAll(newPrescriptions);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prescription, parent, false);

        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PrescriptionViewHolder holder,
            int position
    ) {
        Prescription prescription = prescriptions.get(position);

        holder.tvPrescriptionDentist.setText(
                prescription.getPractitionerName()
        );

        holder.tvPrescriptionDate.setText(
                formatDate(prescription.getCreated())
        );

        String note = prescription.getPrescriptionNote();

        holder.tvPrescriptionText.setText(
                note == null || note.trim().isEmpty()
                        ? "No prescription details provided."
                        : note.trim()
        );
    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    private String formatDate(String created) {
        if (created == null || created.trim().isEmpty()) {
            return "Date unavailable";
        }

        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss.SSSX",
                "yyyy-MM-dd HH:mm:ssX",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX"
        };

        for (String pattern : patterns) {
            SimpleDateFormat input =
                    new SimpleDateFormat(pattern, Locale.US);
            input.setLenient(false);
            input.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date date = input.parse(created);

                if (date != null) {
                    SimpleDateFormat output =
                            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    return output.format(date);
                }
            } catch (ParseException ignored) {
                // Try the next PocketBase timestamp format.
            }
        }

        return created;
    }

    static class PrescriptionViewHolder extends RecyclerView.ViewHolder {

        TextView tvPrescriptionDentist;
        TextView tvPrescriptionDate;
        TextView tvPrescriptionText;

        PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPrescriptionDentist =
                    itemView.findViewById(R.id.tvPrescriptionDentist);

            tvPrescriptionDate =
                    itemView.findViewById(R.id.tvPrescriptionDate);

            tvPrescriptionText =
                    itemView.findViewById(R.id.tvPrescriptionText);
        }
    }
}