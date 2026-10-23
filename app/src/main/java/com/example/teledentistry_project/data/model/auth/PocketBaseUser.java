package com.example.teledentistry_project.data.model.auth;

public class PocketBaseUser {

    private String id;
    private String collectionId;
    private String collectionName;

    private String email;
    private boolean emailVisibility;
    private boolean verified;

    private String role;

    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVisibility() {
        return emailVisibility;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getRole() {
        return role;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
