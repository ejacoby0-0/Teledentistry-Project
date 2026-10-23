package com.example.teledentistry_project.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;

import java.util.List;

public class AppointmentAdapter
        extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointmentList;

    public AppointmentAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_upcoming_appointment,
                        parent,
                        false
                );

        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AppointmentViewHolder holder,
            int position
    ) {

        Appointment appointment =
                appointmentList.get(position);

        // Time from expanded availability record
        String time =
                appointment.getAppointmentTime();

        if (time == null || time.trim().isEmpty()) {
            time = "--:--";
        }

        holder.tvUpcomingTime.setText(time);


        // Date from expanded availability record
        String date =
                appointment.getAppointmentDate();

        if (date == null || date.trim().isEmpty()) {
            date = "Unknown date";
        }

        holder.tvUpcomingDate.setText(date);


        // Patient schedule should show practitioner.
        // The Appointment model currently does not expand
        // practitioner details, so use a temporary label.
        holder.tvUpcomingWith.setText("Dental Appointment");
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }


    static class AppointmentViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvUpcomingTime;
        TextView tvUpcomingDate;
        TextView tvUpcomingWith;

        public AppointmentViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvUpcomingTime =
                    itemView.findViewById(
                            R.id.tvUpcomingTime
                    );

            tvUpcomingDate =
                    itemView.findViewById(
                            R.id.tvUpcomingDate
                    );

            tvUpcomingWith =
                    itemView.findViewById(
                            R.id.tvUpcomingWith
                    );
        }
    }
}
