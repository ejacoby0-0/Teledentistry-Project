package com.example.teledentistry_project.data.model.auth;

public class PocketBaseRegisterRequest {

    private final String email;
    private final String password;
    private final String passwordConfirm;
    private final String role;
    private final String fullName;

    public PocketBaseRegisterRequest(
            String email,
            String password,
            String passwordConfirm,
            String role,
            String fullName
    ) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.role = role;
        this.fullName = fullName;
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

    public String getFullName() {
        return fullName;
    }
}

