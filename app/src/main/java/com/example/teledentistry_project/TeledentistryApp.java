package com.example.teledentistry_project;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class TeledentistryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Force Light Mode globally across the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
