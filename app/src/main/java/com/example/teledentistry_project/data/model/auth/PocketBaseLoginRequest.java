package com.example.teledentistry_project.data.model.auth;

public class PocketBaseLoginRequest {

    private String identity;
    private String password;

    public PocketBaseLoginRequest(
            String identity,
            String password
    ) {
        this.identity = identity;
        this.password = password;
    }

    public String getIdentity() {
        return identity;
    }

    public String getPassword() {
        return password;
    }
}
