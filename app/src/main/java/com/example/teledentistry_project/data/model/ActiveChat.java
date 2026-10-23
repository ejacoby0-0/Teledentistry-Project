package com.example.teledentistry_project.data.model;

public class ActiveChat {
    public String id;
    public String patient;
    public String practitioner;
    public String updated;
    public Expand expand;

    public static class Expand {
        public UserRecords patient;
        public UserRecords practitioner;
    }
}