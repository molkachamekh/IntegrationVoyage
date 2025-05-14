package com.example.voyage.user.frontoffice.controllers;

import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.BookingService;
import com.example.voyage.utils.SessionManager;
import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.VoyageService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.util.Callback;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyBookingsController {
    @FXML
    private TableView<Booking> bookingsTable;

    @FXML
    private TableColumn<Booking, String> voyageColumn;

    @FXML
    private TableColumn<Booking, LocalDate> bookingDateColumn;

    @FXML
    private TableColumn<Booking, LocalDate> departureColumn;

    @FXML
    private TableColumn<Booking, LocalDate> returnColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private Button cancelBookingButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    private BookingService bookingService = new BookingService();
    private VoyageService voyageService = new VoyageService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        setupTable();
        loadBookings();

        // Setup selection listener for buttons
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            cancelBookingButton.setDisable(!hasSelection || !"Pending".equals(newSelection.getStatus()));
            viewDetailsButton.setDisable(!hasSelection);
        });
    }

    public void loadBookings() {
        // Get current user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in");
            return;
        }

        // Load the user's bookings from database
        ObservableList<Booking> userBookings = bookingService.getUserBookings(currentUser.getUserId());
        bookingsTable.setItems(userBookings);
    }

    private void setupTable() {
        // Configure columns
        voyageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVoyageName()));

        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        bookingDateColumn.setCellFactory(createDateCellFactory());

        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureDate"));
        departureColumn.setCellFactory(createDateCellFactory());

        returnColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnColumn.setCellFactory(createDateCellFactory());

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusCellFactory());
    }

    private Callback<TableColumn<Booking, LocalDate>, TableCell<Booking, LocalDate>> createDateCellFactory() {
        return column -> new TableCell<Booking, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        };
    }

    private Callback<TableColumn<Booking, String>, TableCell<Booking, String>> createStatusCellFactory() {
        return column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    setAlignment(Pos.CENTER);

                    // Apply styling based on status
                    getStyleClass().removeAll("status-pending", "status-confirmed", "status-cancelled");

                    switch (status) {
                        case "Pending":
                            getStyleClass().add("status-pending");
                            break;
                        case "Confirmed":
                            getStyleClass().add("status-confirmed");
                            break;
                        case "Cancelled":
                            getStyleClass().add("status-cancelled");
                            break;
                    }
                }
            }
        };
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBookings();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelBooking(ActionEvent event) {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null || !"Pending".equals(selectedBooking.getStatus())) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Are you sure you want to cancel this booking?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = bookingService.updateBookingStatus(selectedBooking.getBookingId(), "Cancelled");
                if (success) {
                    selectedBooking.setStatus("Cancelled");
                    bookingsTable.refresh();
                    cancelBookingButton.setDisable(true);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel booking.");
                }
            }
        });
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            return;
        }

        // Get full voyage details from DB if needed
        Voyage voyage = voyageService.getVoyageById(selectedBooking.getVoyageId());
        if (voyage == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Voyage details not found.");
            return;
        }

        // Create a custom dialog to show details
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Booking Details");
        dialog.setHeaderText("Booking Information for " + voyage.getDestination());

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add booking information
        int row = 0;

        // Booking ID
        grid.add(createLabel("Booking ID:"), 0, row);
        grid.add(createLabel(String.valueOf(selectedBooking.getBookingId())), 1, row++);

        // Booking Date
        grid.add(createLabel("Booking Date:"), 0, row);
        grid.add(createLabel(selectedBooking.getBookingDate().format(dateFormatter)), 1, row++);

        // Status
        grid.add(createLabel("Status:"), 0, row);
        Label statusLabel = createLabel(selectedBooking.getStatus());
        statusLabel.getStyleClass().add("status-" + selectedBooking.getStatus().toLowerCase());
        statusLabel.setStyle("-fx-font-weight: bold;");
        grid.add(statusLabel, 1, row++);

        // Add a separator
        Separator separator = new Separator();
        separator.setPrefWidth(400);
        GridPane.setColumnSpan(separator, 2);
        grid.add(separator, 0, row++);

        // Voyage details
        grid.add(createLabel("Destination:"), 0, row);
        grid.add(createLabel(voyage.getDestination()), 1, row++);

        grid.add(createLabel("Departure Date:"), 0, row);
        grid.add(createLabel(voyage.getDepartureDate().format(dateFormatter)), 1, row++);

        grid.add(createLabel("Return Date:"), 0, row);
        grid.add(createLabel(voyage.getReturnDate().format(dateFormatter)), 1, row++);

        grid.add(createLabel("Duration:"), 0, row);
        grid.add(createLabel(getDuration(voyage.getDepartureDate(), voyage.getReturnDate())), 1, row++);

        grid.add(createLabel("Price:"), 0, row);
        Label priceLabel = createLabel(String.format("$%.2f", voyage.getPrice()));
        priceLabel.getStyleClass().add("price-label");
        grid.add(priceLabel, 1, row++);

        // Add description if available
        if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
            grid.add(createLabel("Description:"), 0, row);
            Label descLabel = createLabel(voyage.getDescription());
            descLabel.setWrapText(true);
            descLabel.setPrefWidth(300);
            grid.add(descLabel, 1, row++);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        if (text.contains(":")) {
            label.getStyleClass().add("form-label");
        } else {
            label.getStyleClass().add("form-value-label");
        }
        return label;
    }

    private String getDuration(LocalDate departureDate, LocalDate returnDate) {
        if (departureDate == null || returnDate == null) {
            return "N/A";
        }

        long days = returnDate.toEpochDay() - departureDate.toEpochDay() + 1;
        return days + " days";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
