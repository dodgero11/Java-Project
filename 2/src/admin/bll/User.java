package bll;

import java.sql.Date;
import java.util.List;

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
    private int friendsCount;
    private int friendsFriendsCount;
    private List<Date> loginHistory;

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

    public User(String username, String fullName, String address, Date birthDate, String gender, String email, Date creationDate, String isActive) {
        this.username = username;
        this.fullName = fullName;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.creationDate = creationDate;
        this.isActive = isActive;
    }

    public User(String username, Date creationDate, int friendsCount, int friendsFriendsCount) {
        this.username = username;
        this.friendsCount = friendsCount;
        this.friendsFriendsCount = friendsFriendsCount;
        this.creationDate = creationDate;
    }

    public User(String username, String fullname, List<Date> loginHistory) {
        this.username = username;
        this.fullName = fullname;
        this.loginHistory = loginHistory;
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

    public int getFriendsCount() {
        return friendsCount;
    }

    public int getFriendsFriendsCount() {    
        return friendsFriendsCount;
    }

    public List<Date> getLoginHistory() {
        return loginHistory;
    }
}