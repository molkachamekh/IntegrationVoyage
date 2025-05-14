package com.example.voyage.user.backoffice.controllers;

import com.example.voyage.user.models.User;
import com.example.voyage.user.services.UserService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class UserFormController {

    @FXML
    private GridPane formGrid;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField fullNameField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField phoneField;

    @FXML
    private CheckBox adminCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // Labels for validation errors
    private Label usernameError;
    private Label passwordError;
    private Label confirmPasswordError;
    private Label emailError;
    private Label fullNameError;
    private Label phoneError;

    private UserService userService = new UserService();
    private User currentUser;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        // Initialize validation error labels
        usernameError = createErrorLabel();
        passwordError = createErrorLabel();
        confirmPasswordError = createErrorLabel();
        emailError = createErrorLabel();
        fullNameError = createErrorLabel();
        phoneError = createErrorLabel();

        // Set default date - make sure the field name matches what's in the FXML
        datePicker.setValue(LocalDate.now().minusYears(18));

        // Don't show errors on initialization
        clearValidationErrors();
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("error-label");
        label.setVisible(false);
        return label;
    }

    public void setUserForEdit(User user) {
        this.currentUser = user;
        this.isEditMode = true;

        // Populate form fields
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        fullNameField.setText(user.getFullName());
        datePicker.setValue(user.getDateOfBirth());
        phoneField.setText(user.getPhoneNumber());
        adminCheckBox.setSelected(user.isAdmin());

        // Disable username field in edit mode
        usernameField.setDisable(true);

        // Password fields are left empty in edit mode, and will only be updated if
        // filled
        passwordField.setPromptText("Leave blank to keep current password");
        confirmPasswordField.setPromptText("Leave blank to keep current password");
    }

    @FXML
    private void handleSave() {
        // Clear previous validation errors
        clearValidationErrors();

        // Validate form fields
        boolean isValid = validateForm();

        if (isValid) {
            saveUser();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate username (required, alphanumeric, at least 3 characters)
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError(usernameError, "Username is required", usernameField);
            isValid = false;
        } else if (!Pattern.matches("^[a-zA-Z0-9_]{3,20}$", username)) {
            showError(usernameError, "Username must be 3-20 alphanumeric characters", usernameField);
            isValid = false;
        } else if (!isEditMode && userService.isUsernameTaken(username)) {
            showError(usernameError, "Username already exists", usernameField);
            isValid = false;
        }

        // Validate password (required for new users, min 8 chars, with at least one
        // digit)
        String password = passwordField.getText();
        if (!isEditMode && password.isEmpty()) {
            showError(passwordError, "Password is required", passwordField);
            isValid = false;
        } else if (!password.isEmpty() && !Pattern.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", password)) {
            showError(passwordError, "Password must be at least 8 characters with at least one digit", passwordField);
            isValid = false;
        }

        // Validate password confirmation
        String confirmPassword = confirmPasswordField.getText();
        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showError(confirmPasswordError, "Passwords do not match", confirmPasswordField);
            isValid = false;
        }

        // Validate email (required, valid email format)
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailError, "Email is required", emailField);
            isValid = false;
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            showError(emailError, "Invalid email format", emailField);
            isValid = false;
        }

        // Validate full name (required)
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showError(fullNameError, "Full name is required", fullNameField);
            isValid = false;
        }

        // Validate phone (optional, but validate format if provided)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !Pattern.matches("^\\+?[0-9]{10,15}$", phone)) {
            showError(phoneError, "Invalid phone number format", phoneField);
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message, Control field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Add error label below the field if it's not already there
        int rowIndex = GridPane.getRowIndex(field) != null ? GridPane.getRowIndex(field) : 0;
        GridPane.setRowIndex(errorLabel, rowIndex + 1);
        GridPane.setColumnIndex(errorLabel, GridPane.getColumnIndex(field));
        GridPane.setColumnSpan(errorLabel, GridPane.getColumnSpan(field) != null ? GridPane.getColumnSpan(field) : 1);

        if (!formGrid.getChildren().contains(errorLabel)) {
            formGrid.getChildren().add(errorLabel);
        }

        // Add error style to the field
        field.getStyleClass().add("field-error");
    }

    private void clearValidationErrors() {
        // Remove all error labels from the grid
        formGrid.getChildren().removeAll(usernameError, passwordError, confirmPasswordError,
                emailError, fullNameError, phoneError);

        // Hide all error labels
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        emailError.setVisible(false);
        fullNameError.setVisible(false);
        phoneError.setVisible(false);

        // Remove error styles
        usernameField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        confirmPasswordField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        fullNameField.getStyleClass().remove("field-error");
        phoneField.getStyleClass().remove("field-error");
    }

    private void saveUser() {
        try {
            if (currentUser == null) {
                currentUser = new User();
            }

            currentUser.setUsername(usernameField.getText().trim());

            // Hash password if provided
            String password = passwordField.getText();
            if (!password.isEmpty()) {
                // Generate a salt and hash the password using BCrypt
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                currentUser.setPassword(hashedPassword);
            }

            currentUser.setEmail(emailField.getText().trim());
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setDateOfBirth(datePicker.getValue());
            currentUser.setPhoneNumber(phoneField.getText().trim());
            currentUser.setAdmin(adminCheckBox.isSelected());

            boolean success;
            if (isEditMode) {
                success = userService.updateUser(currentUser);
            } else {
                success = userService.createUser(currentUser);
            }

            if (success) {
                // Close the form
                ((Stage) saveButton.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
