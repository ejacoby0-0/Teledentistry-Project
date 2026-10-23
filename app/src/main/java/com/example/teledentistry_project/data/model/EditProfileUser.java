package com.example.teledentistry_project.data.model;

import com.google.gson.annotations.SerializedName;

public class EditProfileUser {
    private String id;
    private String name;
    private String surname;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    private String email;
    private String phone;
    private String role;
    private String avatar;
    private String collectionId;
    private String collectionName;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getCollectionId() { return collectionId; }
    public String getCollectionName() { return collectionName; }
}
