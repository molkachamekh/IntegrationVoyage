package com.example.voyage.user.models;

import java.time.LocalDate;
import javafx.beans.property.*;

/**
 * Model class representing a user
 */
public class User {
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final BooleanProperty isAdmin = new SimpleBooleanProperty(false);
    private String location = ""; // Initialize with empty string to avoid null issues

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String username, String password, String email, String fullName,
            LocalDate dateOfBirth, String phoneNumber, boolean isAdmin) {
        this.username.set(username);
        this.password.set(password);
        this.email.set(email);
        this.fullName.set(fullName);
        this.dateOfBirth.set(dateOfBirth);
        this.phoneNumber.set(phoneNumber);
        this.isAdmin.set(isAdmin);
    }

    // Constructor with userId for database retrieval
    public User(int userId, String username, String email, String fullName,
            LocalDate dateOfBirth, String phoneNumber, boolean isAdmin) {
        this.userId.set(userId);
        this.username.set(username);
        this.email.set(email);
        this.fullName.set(fullName);
        this.dateOfBirth.set(dateOfBirth);
        this.phoneNumber.set(phoneNumber);
        this.isAdmin.set(isAdmin);
    }

    // Getters and setters
    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getFullName() {
        return fullName.get();
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth.get();
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth.set(dateOfBirth);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public boolean isAdmin() {
        return isAdmin.get();
    }

    public BooleanProperty isAdminProperty() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin.set(isAdmin);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location : "";
    }

    @Override
    public String toString() {
        return fullName.get();
    }
}
