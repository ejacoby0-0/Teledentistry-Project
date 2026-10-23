package com.example.teledentistry_project.ui.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.appointment.Appointment;
import com.example.teledentistry_project.data.model.patient.Patient;
import com.example.teledentistry_project.data.repository.AppointmentRepository;
import com.example.teledentistry_project.data.repository.PatientRepository;
import com.example.teledentistry_project.ui.adapters.AppointmentAdapter;
import com.example.teledentistry_project.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PatientScheduleActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;

    private RecyclerView rvUpcoming;
    private TextView tvNoUpcoming;

    private AppointmentAdapter appointmentAdapter;

    private String patientId;

    private final List<Appointment> appointmentList =
            new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Authentication check
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        // Patient role check
        if (!"patient".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Final appointment layout
        setContentView(
                R.layout.activity_appointments
        );


        patientRepository =
                new PatientRepository();

        appointmentRepository =
                new AppointmentRepository();


        rvUpcoming =
                findViewById(R.id.rvUpcoming);

        tvNoUpcoming =
                findViewById(R.id.tvNoUpcoming);


        setupRecyclerView();

        loadPatient();
    }


    // Recycler View

    private void setupRecyclerView() {

        appointmentAdapter =
                new AppointmentAdapter(
                        appointmentList
                );

        rvUpcoming.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvUpcoming.setAdapter(
                appointmentAdapter
        );
    }


    // Load Patient

    private void loadPatient() {

        patientRepository.getPatientByUserId(
                sessionManager.getAuthToken(),
                sessionManager.getUserId(),
                new PatientRepository.PatientCallback() {

                    @Override
                    public void onSuccess(Patient patient) {

                        patientId =
                                patient.getId();

                        loadAppointments();
                    }


                    @Override
                    public void onNotFound() {

                        showEmptyMessage(
                                "Patient profile not found."
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        showEmptyMessage(
                                "Unable to load patient profile."
                        );

                        Toast.makeText(
                                PatientScheduleActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // LoadAppointments

    private void loadAppointments() {

        if (patientId == null
                || patientId.trim().isEmpty()) {

            showEmptyMessage(
                    "Patient profile not found."
            );

            return;
        }


        appointmentRepository.getPatientAppointments(
                sessionManager.getAuthToken(),
                patientId,
                new AppointmentRepository.AppointmentListCallback() {

                    @Override
                    public void onSuccess(
                            List<Appointment> appointments
                    ) {

                        appointmentList.clear();

                        if (appointments != null) {
                            appointmentList.addAll(
                                    appointments
                            );
                        }

                        updateAppointmentList();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        showEmptyMessage(
                                "Unable to load appointments."
                        );

                        Toast.makeText(
                                PatientScheduleActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // Update Screen

    private void updateAppointmentList() {

        if (appointmentList.isEmpty()) {

            showEmptyMessage(
                    "You have no upcoming appointments."
            );

            return;
        }


        tvNoUpcoming.setVisibility(
                View.GONE
        );

        rvUpcoming.setVisibility(
                View.VISIBLE
        );

        appointmentAdapter.notifyDataSetChanged();
    }


    // Empty/Error state

    private void showEmptyMessage(
            String message
    ) {

        rvUpcoming.setVisibility(
                View.GONE
        );

        tvNoUpcoming.setText(
                message
        );

        tvNoUpcoming.setVisibility(
                View.VISIBLE
        );
    }
}