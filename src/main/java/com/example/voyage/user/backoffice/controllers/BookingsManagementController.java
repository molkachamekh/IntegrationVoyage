package com.example.voyage.user.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingsManagementController {
    @FXML
    private TableView<BookingData> bookingsTable;

    @FXML
    private TableColumn<BookingData, Integer> idColumn;

    @FXML
    private TableColumn<BookingData, String> userColumn;

    @FXML
    private TableColumn<BookingData, String> voyageColumn;

    @FXML
    private TableColumn<BookingData, LocalDate> bookingDateColumn;

    @FXML
    private TableColumn<BookingData, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addBookingButton;

    @FXML
    private Button editBookingButton;

    @FXML
    private Button deleteBookingButton;

    private ObservableList<BookingData> masterData = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        configureTableColumns();
        loadBookingData();

        // Disable edit and delete buttons until a row is selected
        editBookingButton.setDisable(true);
        deleteBookingButton.setDisable(true);

        // Add listener for row selection
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editBookingButton.setDisable(!hasSelection);
            deleteBookingButton.setDisable(!hasSelection);
        });

        // Add listener to search field
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterBookingData(newText);
        });
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser()));

        voyageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVoyage()));

        bookingDateColumn
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBookingDate()));
        bookingDateColumn.setCellFactory(column -> new TableCell<BookingData, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void loadBookingData() {
        // In a real app, this would fetch data from a service/database
        // For now, we'll use dummy data
        masterData.add(new BookingData(1, "John Doe", "Paris Adventure", LocalDate.now().minusDays(5), "Confirmed"));
        masterData.add(new BookingData(2, "Jane Smith", "Tokyo Explorer", LocalDate.now().minusDays(3), "Pending"));
        masterData.add(
                new BookingData(3, "Robert Johnson", "New York City Tour", LocalDate.now().minusDays(10), "Confirmed"));
        masterData.add(new BookingData(4, "Emily Davis", "Sydney Getaway", LocalDate.now(), "Cancelled"));

        bookingsTable.setItems(masterData);
    }

    private void filterBookingData(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            bookingsTable.setItems(masterData);
            return;
        }

        searchText = searchText.toLowerCase();

        ObservableList<BookingData> filteredData = FXCollections.observableArrayList();
        for (BookingData booking : masterData) {
            if (booking.getUser().toLowerCase().contains(searchText) ||
                    booking.getVoyage().toLowerCase().contains(searchText) ||
                    booking.getStatus().toLowerCase().contains(searchText)) {
                filteredData.add(booking);
            }
        }

        bookingsTable.setItems(filteredData);
    }

    @FXML
    private void handleAddBooking(ActionEvent event) {
        // In a real app, this would open a dialog to add a new booking
        System.out.println("Adding new booking");
    }

    @FXML
    private void handleEditBooking(ActionEvent event) {
        BookingData selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            // In a real app, this would open a dialog to edit the booking
            System.out.println("Editing booking: " + selectedBooking.getId());
        }
    }

    @FXML
    private void handleDeleteBooking(ActionEvent event) {
        BookingData selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Booking");
            confirmDialog.setContentText("Are you sure you want to delete the booking for " +
                    selectedBooking.getUser() + " to " + selectedBooking.getVoyage() + "?");

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Delete the booking
                    masterData.remove(selectedBooking);
                    bookingsTable.setItems(masterData);
                }
            });
        }
    }

    // Inner class for booking data
    public static class BookingData {
        private final int id;
        private final String user;
        private final String voyage;
        private final LocalDate bookingDate;
        private final String status;

        public BookingData(int id, String user, String voyage, LocalDate bookingDate, String status) {
            this.id = id;
            this.user = user;
            this.voyage = voyage;
            this.bookingDate = bookingDate;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getUser() {
            return user;
        }

        public String getVoyage() {
            return voyage;
        }

        public LocalDate getBookingDate() {
            return bookingDate;
        }

        public String getStatus() {
            return status;
        }
    }
}
