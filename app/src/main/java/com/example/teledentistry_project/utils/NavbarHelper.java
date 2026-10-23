package com.example.teledentistry_project.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.example.teledentistry_project.R;
import com.example.teledentistry_project.ui.dentist.PatientListActivity;
import com.example.teledentistry_project.ui.dentist.PractitionerScheduleActivity;
import com.example.teledentistry_project.ui.patient.PatientScheduleActivity;
import com.example.teledentistry_project.ui.userActivities.HomeActivity;
import com.example.teledentistry_project.ui.userActivities.ProfileActivity;
import com.example.teledentistry_project.ui.patient.SearchActivity;

public class NavbarHelper {

    public enum Tab { HOME, SEARCH, SCHEDULE, PATIENTS, PROFILE }

    public static void setup(Activity activity, Tab activeTab) {
        SessionManager session = new SessionManager(activity);
        boolean isPractitioner = "practitioner".equalsIgnoreCase(session.getRole());

        View patientBar = activity.findViewById(R.id.navbarPatient);
        View practitionerBar = activity.findViewById(R.id.navbarPractitioner);

        // A page may include only one of the two navbars
        if (patientBar != null) {
            patientBar.setVisibility(isPractitioner ? View.GONE : View.VISIBLE);
        }
        if (practitionerBar != null) {
            practitionerBar.setVisibility(isPractitioner ? View.VISIBLE : View.GONE);
        }

        View bar = isPractitioner ? practitionerBar : patientBar;
        if (bar == null) return;

        if (isPractitioner) {
            bind(activity, bar, R.id.navHome, Tab.HOME, activeTab, true);
            bind(activity, bar, R.id.navSchedule, Tab.SCHEDULE, activeTab, true);
            bind(activity, bar, R.id.navPatients, Tab.PATIENTS, activeTab, true);
            bind(activity, bar, R.id.navProfile, Tab.PROFILE, activeTab, true);
        } else {
            bind(activity, bar, R.id.navHome, Tab.HOME, activeTab, false);
            bind(activity, bar, R.id.navSearch, Tab.SEARCH, activeTab, false);
            bind(activity, bar, R.id.navSchedule, Tab.SCHEDULE, activeTab, false);
            bind(activity, bar, R.id.navProfile, Tab.PROFILE, activeTab, false);
        }
    }

    private static void bind(Activity activity, View bar, int viewId,
                             Tab tab, Tab activeTab, boolean isPractitioner) {

        ImageView button = bar.findViewById(viewId);
        if (button == null) return;

        // Highlight the current tab
        int colorRes = (tab == activeTab)
                ? R.color.dental_surface
                : R.color.dental_primary_container;
        ImageViewCompat.setImageTintList(
                button,
                ColorStateList.valueOf(ContextCompat.getColor(activity, colorRes))
        );

        button.setOnClickListener(v -> {
            if (tab == activeTab) return;   // already on this page

            Class<?> destination = destinationFor(isPractitioner, tab);
            if (destination == null) return;

            Intent intent = new Intent(activity, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
        });
    }

    private static Class<?> destinationFor(boolean isPractitioner, Tab tab) {
        switch (tab) {
            case HOME:
                return HomeActivity.class;
            case SEARCH:
                return SearchActivity.class;
            case SCHEDULE:
                return isPractitioner
                        ? PractitionerScheduleActivity.class
                        : PatientScheduleActivity.class;
            case PATIENTS:
                return PatientListActivity.class;
            case PROFILE:
                return ProfileActivity.class;
            default:
                return null;
        }
    }
}