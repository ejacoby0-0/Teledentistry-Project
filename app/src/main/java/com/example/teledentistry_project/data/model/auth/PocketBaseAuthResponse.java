package com.example.teledentistry_project.data.model.auth;

public class PocketBaseAuthResponse {

    private String token;
    private PocketBaseUser record;

    public String getToken() {
        return token;
    }

    public PocketBaseUser getRecord() {
        return record;
    }
}
