package com.example.teledentistry_project.data.model.auth;

public class SignUpRequest {

    private final String email;
    private final String password;
    private final String passwordConfirm;
    private final String role;

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = password;
        this.role = "patient";
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public String getRole() {
        return role;
    }
}