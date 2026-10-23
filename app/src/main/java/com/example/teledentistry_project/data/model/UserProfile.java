package com.example.teledentistry_project.data.model;

public class UserProfile {

    private String id;
    private String email;
    private String avatar;        // file name; the picture itself is loaded from a URL
    private String role;
    private String name;
    private String surname;
    private String dateOfBirth;
    private String phone;

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
}