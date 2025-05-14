package com.example.voyage.voyage.frontoffice.controllers;

import com.example.voyage.payment.controllers.CardFormController;
import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.BookingService;
import com.example.voyage.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BookingsContentController {

    @FXML
    private Tab allTab;

    @FXML
    private Tab pendingTab;

    @FXML
    private Tab confirmedTab;

    @FXML
    private Tab cancelledTab;

    @FXML
    private VBox allBookingsContainer;

    @FXML
    private VBox pendingBookingsContainer;

    @FXML
    private VBox confirmedBookingsContainer;

    @FXML
    private VBox cancelledBookingsContainer;

    @FXML
    private Label noBookingsLabel;

    private BookingService bookingService = new BookingService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        loadUserBookings();
    }

    public void loadUserBookings() {
        // Clear existing content
        allBookingsContainer.getChildren().clear();
        pendingBookingsContainer.getChildren().clear();
        confirmedBookingsContainer.getChildren().clear();
        cancelledBookingsContainer.getChildren().clear();

        // Get current user
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            noBookingsLabel.setText("Please log in to view your bookings");
            noBookingsLabel.setVisible(true);
            noBookingsLabel.setManaged(true);
            return;
        }

        // Get user bookings
        ObservableList<Booking> bookings = bookingService.getUserBookings(currentUser.getUserId());

        if (bookings.isEmpty()) {
            noBookingsLabel.setText("You don't have any bookings yet");
            noBookingsLabel.setVisible(true);
            noBookingsLabel.setManaged(true);
            return;
        } else {
            noBookingsLabel.setVisible(false);
            noBookingsLabel.setManaged(false);
        }

        // Process each booking
        for (Booking booking : bookings) {
            // Get full booking details with price and date info
            Booking fullBooking = bookingService.getBookingById(booking.getBookingId());
            if (fullBooking == null)
                fullBooking = booking;

            // Create card for this booking
            VBox bookingCard = createBookingCard(fullBooking);

            // Add to All tab
            allBookingsContainer.getChildren().add(bookingCard);

            // Add to appropriate status tab
            if ("Pending".equals(booking.getStatus()) || "En Attente".equals(booking.getStatus())) {
                pendingBookingsContainer.getChildren().add(createBookingCard(fullBooking));
            } else if ("Confirmed".equals(booking.getStatus())) {
                confirmedBookingsContainer.getChildren().add(createBookingCard(fullBooking));
            } else if ("Cancelled".equals(booking.getStatus())) {
                cancelledBookingsContainer.getChildren().add(createBookingCard(fullBooking));
            }
        }

        // Add placeholder text if any category is empty
        if (pendingBookingsContainer.getChildren().isEmpty()) {
            pendingBookingsContainer.getChildren().add(createNoBookingsLabel("No pending bookings"));
        }

        if (confirmedBookingsContainer.getChildren().isEmpty()) {
            confirmedBookingsContainer.getChildren().add(createNoBookingsLabel("No confirmed bookings"));
        }

        if (cancelledBookingsContainer.getChildren().isEmpty()) {
            cancelledBookingsContainer.getChildren().add(createNoBookingsLabel("No cancelled bookings"));
        }
    }

    private Label createNoBookingsLabel(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("info-text");
        label.setPadding(new Insets(20));
        return label;
    }

    private VBox createBookingCard(Booking booking) {
        // Create the main card container
        VBox card = new VBox(0);
        card.getStyleClass().add("booking-card");

        // Create the header with colored status bar
        HBox header = new HBox(0);

        // Status indicator (colored bar on left)
        Rectangle statusBar = new Rectangle(8, 150);

        // Handle both "Pending" and "En Attente" statuses
        String statusLower = booking.getStatus().toLowerCase();
        String statusClass = statusLower.equals("en attente") ? "status-pending-bg" : "status-" + statusLower + "-bg";
        statusBar.getStyleClass().add(statusClass);

        // Create the content area
        VBox content = new VBox(12);
        content.getStyleClass().add("card-content");
        HBox.setHgrow(content, Priority.ALWAYS);

        // Booking destination as title
        Label destinationLabel = new Label(booking.getVoyageName());
        destinationLabel.getStyleClass().add("card-title");

        // Status label handling both "En Attente" and "Pending"
        Label statusLabel = new Label(booking.getStatus());
        String statusLabelClass = statusLower.equals("en attente") ? "status-pending" : "status-" + statusLower;
        statusLabel.getStyleClass().add(statusLabelClass);

        // Create fields grid for booking details
        GridPane details = new GridPane();
        details.setHgap(15);
        details.setVgap(8);
        details.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        // Add all booking details with proper labels
        addDetailField(details, "Booking #:", String.valueOf(booking.getBookingId()), row++);
        addDetailField(details, "Booked on:", booking.getBookingDate().format(dateFormatter), row++);

        // Travel dates if available
        if (booking.getDepartureDate() != null && booking.getReturnDate() != null) {
            addDetailField(details, "Travel Dates:", booking.getFormattedDates(), row++);
            addDetailField(details, "Price:", String.format("$%.2f", booking.getPrice()), row++);
        }

        // Add action buttons based on status
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        // If status is Pending or En Attente, show Pay Now button
        if ("Pending".equals(booking.getStatus()) || "En Attente".equals(booking.getStatus())) {
            Button payButton = new Button("Pay Now");
            payButton.getStyleClass().addAll("primary-button", "action-button");

            FontIcon payIcon = new FontIcon("fas-credit-card");
            payIcon.setIconSize(16);
            payButton.setGraphic(payIcon);

            payButton.setOnAction(e -> handlePayBooking(booking));
            actions.getChildren().add(payButton);
        }

        // Show Cancel/Delete button for appropriate statuses
        if ("Pending".equals(booking.getStatus()) || "En Attente".equals(booking.getStatus())
                || "Confirmed".equals(booking.getStatus())) {
            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().addAll("danger-button", "action-button");

            FontIcon cancelIcon = new FontIcon("fas-times");
            cancelIcon.setIconSize(16);
            cancelButton.setGraphic(cancelIcon);

            cancelButton.setOnAction(e -> handleCancelBooking(booking));
            actions.getChildren().add(cancelButton);
        } else if ("Cancelled".equals(booking.getStatus())) {
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().addAll("danger-button", "action-button");

            FontIcon deleteIcon = new FontIcon("fas-trash-alt");
            deleteIcon.setIconSize(16);
            deleteButton.setGraphic(deleteIcon);

            deleteButton.setOnAction(e -> handleDeleteBooking(booking));
            actions.getChildren().add(deleteButton);
        }

        // Assemble all components
        content.getChildren().addAll(destinationLabel, statusLabel, details);
        if (!actions.getChildren().isEmpty()) {
            content.getChildren().add(actions);
        }

        header.getChildren().addAll(statusBar, content);
        card.getChildren().add(header);

        return card;
    }

    // Helper method to add fields to the details grid
    private void addDetailField(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("field-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("field-value");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void handlePayBooking(Booking booking) {
        try {
            // Load payment form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/voyage/payment/cardForm.fxml"));
            Parent root = loader.load();

            CardFormController controller = loader.getController();
            controller.setBooking(booking);

            // Show dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Payment");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(allBookingsContainer.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Wait for dialog to close then refresh
            dialogStage.showAndWait();
            loadUserBookings();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open payment form: " + e.getMessage());
        }
    }

    private void handleCancelBooking(Booking booking) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Booking");
        confirmation.setHeaderText("Are you sure you want to cancel this booking?");
        confirmation.setContentText("This will cancel your booking to " + booking.getVoyageName());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "Cancelled");

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your booking has been cancelled.");
                loadUserBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel your booking. Please try again.");
            }
        }
    }

    private void handleDeleteBooking(Booking booking) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Booking");
        confirmation.setHeaderText("Are you sure you want to delete this booking?");
        confirmation.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = bookingService.deleteUserBooking(booking.getBookingId(), currentUser.getUserId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your booking has been deleted.");
                loadUserBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to delete your booking. Only cancelled bookings can be deleted.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
