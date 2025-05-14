package com.example.voyage.user.frontoffice.controllers;

import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.BookingService;
import com.example.voyage.utils.SessionManager;
import com.example.voyage.voyage.models.Voyage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingFormController {
    @FXML
    private Label destinationLabel;

    @FXML
    private Label departureDateLabel;

    @FXML
    private Label returnDateLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private TextArea specialRequestsField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmBookingButton;

    @FXML
    private Label errorLabel;

    private Voyage voyage;
    private BookingService bookingService = new BookingService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Get current user from session and populate user details
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            phoneLabel.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Not provided");
        } else {
            // Should not happen as this form should only be accessible to logged in users
            showError("Error: No user logged in");
            confirmBookingButton.setDisable(true);
        }
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;

        if (voyage != null) {
            // Fill in voyage details from database
            destinationLabel.setText(voyage.getDestination());
            departureDateLabel.setText(voyage.getDepartureDate().format(dateFormatter));
            returnDateLabel.setText(voyage.getReturnDate().format(dateFormatter));
            priceLabel.setText(String.format("$%.2f", voyage.getPrice()));
        } else {
            showError("Error: No voyage selected");
            confirmBookingButton.setDisable(true);
        }
    }

    @FXML
    private void handleConfirmBooking(ActionEvent event) {
        // Get the current user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (voyage == null || currentUser == null) {
            showError("Missing voyage or user information");
            return;
        }

        // Create a new booking with "Pending" status
        Booking newBooking = new Booking(
                currentUser.getUserId(),
                voyage.getVoyageId(),
                LocalDate.now(), // Current date as booking date
                "Pending");

        // Add special requests if any (could be added to the database schema)
        String specialRequests = specialRequestsField.getText();
        if (specialRequests != null && !specialRequests.isEmpty()) {
            // For future implementation: save special requests
        }

        // Save to database
        boolean success = bookingService.createBooking(newBooking);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Confirmed");
            alert.setHeaderText("Thank you for your booking!");
            alert.setContentText("Your booking for " + voyage.getDestination() +
                    " has been successfully created with status 'Pending'. " +
                    "You can view and manage your booking in the 'My Bookings' section.");
            alert.showAndWait();

            // Close the booking form
            ((Stage) confirmBookingButton.getScene().getWindow()).close();
        } else {
            showError("Failed to create booking. Please try again.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Just close the form
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
