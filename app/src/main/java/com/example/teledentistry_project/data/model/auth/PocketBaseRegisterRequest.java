package com.example.teledentistry_project.data.model.auth;

public class PocketBaseRegisterRequest {

    private String email;
    private String password;
    private String passwordConfirm;
    private String role;

    public PocketBaseRegisterRequest(
            String email,
            String password,
            String passwordConfirm,
            String role
    ) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.role = role;
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
