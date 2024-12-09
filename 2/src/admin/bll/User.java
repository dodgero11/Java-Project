package bll;

import java.sql.Date;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String address;
    private String email;
    private String gender;
    private Date birthDate;
    private Date creationDate;
    private String isActive;

    // Constructor
    public User(String username, String password, String fullName, String address, Date birthDate, String gender, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
    }

    public User() {}

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}