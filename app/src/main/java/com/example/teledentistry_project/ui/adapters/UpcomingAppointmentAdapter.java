package com.example.teledentistry_project.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;

import java.util.ArrayList;
import java.util.List;

public class UpcomingAppointmentAdapter
        extends RecyclerView.Adapter<UpcomingAppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointments = new ArrayList<>();

    public void setAppointments(List<Appointment> newAppointments) {
        appointments.clear();

        if (newAppointments != null) {
            appointments.addAll(newAppointments);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_appointment, parent, false);

        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AppointmentViewHolder holder, int position) {

        Appointment appointment = appointments.get(position);

        String date = appointment.getAppointmentDate();
        String time = appointment.getAppointmentTime();

        holder.tvUpcomingWith.setText(
                "Patient: " + appointment.getPatientName()
        );

        holder.tvUpcomingDate.setText(
                date == null || date.isEmpty() ? "Date unavailable" : date
        );

        holder.tvUpcomingTime.setText(
                time == null || time.isEmpty() ? "Time unavailable" : time
        );
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {

        TextView tvUpcomingTime;
        TextView tvUpcomingDate;
        TextView tvUpcomingWith;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUpcomingTime = itemView.findViewById(R.id.tvUpcomingTime);
            tvUpcomingDate = itemView.findViewById(R.id.tvUpcomingDate);
            tvUpcomingWith = itemView.findViewById(R.id.tvUpcomingWith);
        }
    }
}