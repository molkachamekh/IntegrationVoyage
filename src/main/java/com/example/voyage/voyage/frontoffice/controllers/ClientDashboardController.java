package com.example.voyage.voyage.frontoffice.controllers;

import com.example.voyage.user.models.User;
import com.example.voyage.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class ClientDashboardController {
    @FXML
    private Label pageTitle;

    @FXML
    private Label userNameLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button homeButton;

    @FXML
    private Button exploreButton;

    @FXML
    private Button bookingsButton;

    @FXML
    private Button profileButton;

    private User currentUser;

    @FXML
    public void initialize() {
        try {
            // Get current user from session
            currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser != null) {
                userNameLabel.setText("Welcome, " + currentUser.getFullName());
            } else {
                userNameLabel.setText("Welcome, Guest");
            }

            // Set home as default view - but don't do this automatically as it's causing
            // issues
            // Instead, load content directly to avoid the invalid path error
            directLoadContent("/com/example/voyage/voyage/frontoffice/homeContent.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleHomeButton(ActionEvent event) {
        pageTitle.setText("Home");
        loadContent("/com/example/voyage/voyage/frontoffice/homeContent.fxml");
    }

    @FXML
    void handleExploreButton(ActionEvent event) {
        pageTitle.setText("Explore Voyages");
        loadContent("/com/example/voyage/voyage/frontoffice/exploreContent.fxml");
    }

    @FXML
    private void handleBookingsButton(ActionEvent event) {
        pageTitle.setText("My Bookings");
        loadContent("/com/example/voyage/voyage/frontoffice/bookingsContent.fxml");
    }

    @FXML
    private void handleProfileButton(ActionEvent event) {
        pageTitle.setText("My Profile");
        loadContent("/com/example/voyage/voyage/frontoffice/profileContent.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear session
            SessionManager.getInstance().logout();

            // Navigate back to login screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/user/frontoffice/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    // Fixed method for loading content
    private void loadContent(String fxmlPath) {
        try {
            // Ensure we have a valid resource URL
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            // Store the controller reference properly
            Object controller = loader.getController();
            if (controller != null) {
                contentArea.setUserData(controller); // Store on contentArea, not window
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load page: " + e.getMessage());
        }
    }

    // Direct content loading without event handlers for initialization
    private void directLoadContent(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            // Set title
            pageTitle.setText("Home");

            // Store controller reference
            contentArea.setUserData(loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load home content: " + e.getMessage());
        }
    }

    private void showPlaceholderContent(String message, String iconLiteral) {
        contentArea.getChildren().clear();

        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(64);
        icon.setIconColor(javafx.scene.paint.Color.web("#3498db"));

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("placeholder-text");

        Label comingSoonLabel = new Label("Coming Soon");
        comingSoonLabel.getStyleClass().add("coming-soon-label");

        placeholder.getChildren().addAll(icon, messageLabel, comingSoonLabel);
        contentArea.getChildren().add(placeholder);
    }

    private void showError(String message) {
        contentArea.getChildren().clear();

        VBox errorBox = new VBox(10);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);

        FontIcon errorIcon = new FontIcon("fas-exclamation-triangle");
        errorIcon.setIconSize(48);
        errorIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));

        Label errorLabel = new Label("Error");
        errorLabel.getStyleClass().add("error-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setWrapText(true);

        errorBox.getChildren().addAll(errorIcon, errorLabel, messageLabel);
        contentArea.getChildren().add(errorBox);
    }

    /**
     * Displays an alert dialog with the specified type, title, and message.
     * 
     * @param type    The alert type (e.g., ERROR, INFORMATION, etc.)
     * @param title   The title of the alert dialog
     * @param message The content message of the alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
