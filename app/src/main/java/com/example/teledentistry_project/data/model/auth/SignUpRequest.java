package com.example.teledentistry_project.data.model.auth;

public class SignUpRequest {

    private final String email;
    private final String password;

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}