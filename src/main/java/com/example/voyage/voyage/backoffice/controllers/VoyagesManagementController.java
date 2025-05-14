package com.example.voyage.voyage.backoffice.controllers;

import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.VoyageService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleDoubleProperty;
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

public class VoyagesManagementController {
    @FXML
    private TableView<VoyageData> voyagesTable;

    @FXML
    private TableColumn<VoyageData, Integer> idColumn;

    @FXML
    private TableColumn<VoyageData, String> destinationColumn;

    @FXML
    private TableColumn<VoyageData, LocalDate> departureDateColumn;

    @FXML
    private TableColumn<VoyageData, LocalDate> returnDateColumn;

    @FXML
    private TableColumn<VoyageData, Double> priceColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addVoyageButton;

    @FXML
    private Button editVoyageButton;

    @FXML
    private Button deleteVoyageButton;

    private ObservableList<VoyageData> masterData = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private VoyageService voyageService = new VoyageService();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadVoyages();

        // Disable edit and delete buttons until a row is selected
        editVoyageButton.setDisable(true);
        deleteVoyageButton.setDisable(true);

        // Add listener for row selection
        voyagesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editVoyageButton.setDisable(!hasSelection);
            deleteVoyageButton.setDisable(!hasSelection);
        });

        // Add listener to search field
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterVoyageData(newText);
        });
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        destinationColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestination()));

        departureDateColumn
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDepartureDate()));
        departureDateColumn.setCellFactory(column -> new TableCell<VoyageData, LocalDate>() {
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

        returnDateColumn
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReturnDate()));
        returnDateColumn.setCellFactory(column -> new TableCell<VoyageData, LocalDate>() {
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

        priceColumn
                .setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        priceColumn.setCellFactory(column -> new TableCell<VoyageData, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });
    }

    private void loadVoyages() {
        try {
            // Clear existing data
            masterData.clear();

            // Get voyages from database using VoyageService
            ObservableList<Voyage> voyages = voyageService.getAllVoyages();

            // Convert Voyage objects to VoyageData objects
            for (Voyage voyage : voyages) {
                masterData.add(new VoyageData(
                        voyage.getVoyageId(),
                        voyage.getDestination(),
                        voyage.getDepartureDate(),
                        voyage.getReturnDate(),
                        voyage.getPrice(),
                        voyage.getDescription()));
            }

            // If no data was loaded, add sample data for demonstration
            if (masterData.isEmpty()) {
                loadSampleVoyageData();
            }

            voyagesTable.setItems(masterData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load voyages: " + e.getMessage());
        }
    }

    private void loadSampleVoyageData() {
        // Sample data for demonstration purposes
        masterData.add(new VoyageData(1, "Paris, France", LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(37), 1200.00,
                "Experience the romance and beauty of Paris."));
        masterData.add(new VoyageData(2, "Tokyo, Japan", LocalDate.now().plusDays(45),
                LocalDate.now().plusDays(55), 1800.00,
                "Discover the blend of tradition and modernity."));
        masterData.add(new VoyageData(3, "New York, USA", LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(21), 1500.00,
                "Experience the Big Apple's vibrant culture."));
        masterData.add(new VoyageData(4, "Sydney, Australia", LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(71), 2000.00,
                "Enjoy the beautiful beaches and iconic Opera House."));
    }

    private void filterVoyageData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            voyagesTable.setItems(masterData);
            return;
        }

        searchText = searchText.toLowerCase();

        ObservableList<VoyageData> filteredData = FXCollections.observableArrayList();
        for (VoyageData voyage : masterData) {
            if (voyage.getDestination().toLowerCase().contains(searchText) ||
                    voyage.getDescription().toLowerCase().contains(searchText)) {
                filteredData.add(voyage);
            }
        }

        voyagesTable.setItems(filteredData);
    }

    @FXML
    private void handleAddVoyage(ActionEvent event) {
        openVoyageForm(null);
    }

    @FXML
    private void handleEditVoyage(ActionEvent event) {
        VoyageData selectedVoyage = voyagesTable.getSelectionModel().getSelectedItem();
        if (selectedVoyage != null) {
            openVoyageForm(selectedVoyage);
        }
    }

    @FXML
    private void handleDeleteVoyage(ActionEvent event) {
        VoyageData selectedVoyage = voyagesTable.getSelectionModel().getSelectedItem();
        if (selectedVoyage != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Voyage");
            confirmDialog.setContentText(
                    "Are you sure you want to delete the voyage to " + selectedVoyage.getDestination() + "?");

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Delete the voyage from database
                    boolean success = voyageService.deleteVoyage(selectedVoyage.getId());
                    if (success) {
                        // Remove from UI list
                        masterData.remove(selectedVoyage);
                        voyagesTable.setItems(masterData);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to delete voyage. It may be referenced by bookings or guides.");
                    }
                }
            });
        }
    }

    private void openVoyageForm(VoyageData voyageData) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/voyageForm.fxml"));
            Parent root = loader.load();

            VoyageFormController controller = loader.getController();

            if (voyageData != null) {
                // Create a Voyage object from VoyageData
                Voyage voyage = new Voyage();
                voyage.setVoyageId(voyageData.getId());
                voyage.setDestination(voyageData.getDestination());
                voyage.setDepartureDate(voyageData.getDepartureDate());
                voyage.setReturnDate(voyageData.getReturnDate());
                voyage.setPrice(voyageData.getPrice());
                voyage.setDescription(voyageData.getDescription());

                // Pass the Voyage object to the controller
                controller.setVoyageForEdit(voyage);
            }

            Stage formStage = new Stage();
            formStage.setScene(new Scene(root));
            formStage.setTitle(voyageData == null ? "Add New Voyage" : "Edit Voyage");
            formStage.showAndWait();

            // Reload data after form closes
            loadVoyages();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening voyage form: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for voyage data
    public static class VoyageData {
        private final int id;
        private final String destination;
        private final LocalDate departureDate;
        private final LocalDate returnDate;
        private final double price;
        private final String description;

        public VoyageData(int id, String destination, LocalDate departureDate,
                LocalDate returnDate, double price, String description) {
            this.id = id;
            this.destination = destination;
            this.departureDate = departureDate;
            this.returnDate = returnDate;
            this.price = price;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDestination() {
            return destination;
        }

        public LocalDate getDepartureDate() {
            return departureDate;
        }

        public LocalDate getReturnDate() {
            return returnDate;
        }

        public double getPrice() {
            return price;
        }

        public String getDescription() {
            return description;
        }
    }
}
