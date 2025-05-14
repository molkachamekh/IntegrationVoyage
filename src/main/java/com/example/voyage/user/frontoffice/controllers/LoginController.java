package com.example.voyage.user.frontoffice.controllers;

import com.example.voyage.HelloApplication;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.UserService;
import com.example.voyage.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private VBox loginContainer;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Create errorMessageLabel programmatically if it's not defined in FXML
        if (errorMessageLabel == null) {
            errorMessageLabel = new Label();
            errorMessageLabel.getStyleClass().add("error-message");
            errorMessageLabel.setVisible(false);
            errorMessageLabel.setManaged(false);

            // Add it to the container at an appropriate position
            if (loginContainer != null) {
                // Add it before the login button or at index 2 if the container has enough
                // children
                int index = loginContainer.getChildren().indexOf(loginButton);
                if (index > 0) {
                    loginContainer.getChildren().add(index, errorMessageLabel);
                } else if (loginContainer.getChildren().size() >= 3) {
                    loginContainer.getChildren().add(2, errorMessageLabel);
                } else {
                    loginContainer.getChildren().add(errorMessageLabel);
                }
            }
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Username and password cannot be empty");
            return;
        }

        try {
            // Simplify authentication - try direct password comparison first
            User user = userService.getUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                loginSuccessful(user);
                return;
            }

            // If direct comparison fails, try authenticateUserWithoutLocation which may
            // handle hashing
            User loggedInUser = userService.authenticateUserWithoutLocation(username, password);

            if (loggedInUser != null) {
                loginSuccessful(loggedInUser);
            } else {
                showErrorMessage("Invalid username or password");
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Login failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private void loginSuccessful(User user) {
        try {
            // Store user in session
            SessionManager.getInstance().setCurrentUser(user);

            // Navigate to appropriate dashboard based on user role
            if (user.isAdmin()) {
                loadAdminDashboard();
            } else {
                loadClientDashboard();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error loading dashboard: " + e.getMessage());
        }
    }

    private void showErrorMessage(String message) {
        // Handle null errorMessageLabel safely
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
            errorMessageLabel.setVisible(true);
            errorMessageLabel.setManaged(true);
        } else {
            // Fallback to console or alert if label is null
            System.err.println("Error: " + message);
            showErrorAlert("Login Error", message);
        }
    }

    private void hideErrorMessage() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setVisible(false);
            errorMessageLabel.setManaged(false);
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadClientDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/voyage/voyage/frontoffice/clientDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Client Dashboard");
        stage.show();
    }

    private void loadAdminDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/voyage/user/backoffice/dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/user/frontoffice/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            showErrorMessage("Failed to load registration page: " + e.getMessage());
        }
    }
}
