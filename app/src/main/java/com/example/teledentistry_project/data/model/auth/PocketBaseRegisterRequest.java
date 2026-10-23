
package com.example.teledentistry_project.data.model.auth;

public class PocketBaseRegisterRequest {

    private String name;
    private String surname;
    private String dateOfBirth;
    private String phone;
    private String email;
    private String password;
    private String passwordConfirm;
    private String role;

    public PocketBaseRegisterRequest(
            String name,
            String surname,
            String dateOfBirth,
            String phone,
            String email,
            String password,
            String passwordConfirm,
            String role
    ) {
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
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
