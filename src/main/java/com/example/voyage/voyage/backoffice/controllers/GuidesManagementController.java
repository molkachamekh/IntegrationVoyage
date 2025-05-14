package com.example.voyage.voyage.backoffice.controllers;

import com.example.voyage.voyage.models.Guide;
import com.example.voyage.voyage.services.GuideService;
import com.example.voyage.voyage.services.VoyageService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuidesManagementController {
    @FXML
    private TableView<GuideData> guidesTable;

    @FXML
    private TableColumn<GuideData, Integer> idColumn;

    @FXML
    private TableColumn<GuideData, String> nameColumn;

    @FXML
    private TableColumn<GuideData, String> contactInfoColumn;

    @FXML
    private TableColumn<GuideData, String> languagesColumn;

    @FXML
    private TableColumn<GuideData, String> voyageColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addGuideButton;

    @FXML
    private Button editGuideButton;

    @FXML
    private Button deleteGuideButton;

    private ObservableList<GuideData> masterData = FXCollections.observableArrayList();
    private GuideService guideService = new GuideService();
    private VoyageService voyageService = new VoyageService();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadGuideData();

        // Disable edit and delete buttons until a row is selected
        editGuideButton.setDisable(true);
        deleteGuideButton.setDisable(true);

        // Add listener for row selection
        guidesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editGuideButton.setDisable(!hasSelection);
            deleteGuideButton.setDisable(!hasSelection);
        });

        // Add listener to search field
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterGuideData(newText);
        });
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        contactInfoColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactInfo()));
        languagesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLanguages()));
        voyageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVoyage()));
    }

    private void loadGuideData() {
        try {
            // Clear existing data
            masterData.clear();

            // Get guides from database
            ObservableList<Guide> guides = guideService.getAllGuides();

            // Convert Guide objects to GuideData for display
            for (Guide guide : guides) {
                masterData.add(new GuideData(
                        guide.getGuideId(),
                        guide.getGuideName(),
                        guide.getContactInfo(),
                        guide.getLanguages(),
                        guide.getVoyageName() != null ? guide.getVoyageName() : "Not assigned"));
            }

            // If no data was loaded (empty database), add sample data for demonstration
            if (masterData.isEmpty()) {
                loadSampleGuideData();
            }

            guidesTable.setItems(masterData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load guides: " + e.getMessage());
        }
    }

    private void loadSampleGuideData() {
        // Sample data for demonstration purposes
        masterData.add(new GuideData(1, "Jean Dupont", "+33 1 23 45 67 89", "French, English", "Paris Adventure"));
        masterData.add(new GuideData(2, "Yuki Tanaka", "+81 90 1234 5678", "Japanese, English", "Tokyo Explorer"));
        masterData.add(new GuideData(3, "Michael Smith", "+1 212 555 1234", "English", "New York City Tour"));
        masterData.add(new GuideData(4, "Emma Wilson", "+61 2 1234 5678", "English", "Sydney Getaway"));
    }

    private void filterGuideData(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            guidesTable.setItems(masterData);
            return;
        }

        searchText = searchText.toLowerCase();

        ObservableList<GuideData> filteredData = FXCollections.observableArrayList();
        for (GuideData guide : masterData) {
            if (guide.getName().toLowerCase().contains(searchText) ||
                    guide.getContactInfo().toLowerCase().contains(searchText) ||
                    guide.getLanguages().toLowerCase().contains(searchText) ||
                    guide.getVoyage().toLowerCase().contains(searchText)) {
                filteredData.add(guide);
            }
        }

        guidesTable.setItems(filteredData);
    }

    @FXML
    private void handleAddGuide(ActionEvent event) {
        openGuideForm(null);
    }

    @FXML
    private void handleEditGuide(ActionEvent event) {
        GuideData selectedGuide = guidesTable.getSelectionModel().getSelectedItem();
        if (selectedGuide != null) {
            openGuideForm(selectedGuide);
        }
    }

    @FXML
    private void handleDeleteGuide(ActionEvent event) {
        GuideData selectedGuide = guidesTable.getSelectionModel().getSelectedItem();
        if (selectedGuide != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Guide");
            confirmDialog.setContentText("Are you sure you want to delete the guide: " + selectedGuide.getName() + "?");

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Delete the guide from database
                    boolean success = guideService.deleteGuide(selectedGuide.getId());
                    if (success) {
                        // Remove from the UI list
                        masterData.remove(selectedGuide);
                        guidesTable.setItems(masterData);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete guide.");
                    }
                }
            });
        }
    }

    private void openGuideForm(GuideData guideData) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/guideForm.fxml"));
            Parent root = loader.load();

            GuideFormController controller = loader.getController();

            if (guideData != null) {
                // Create a Guide object from the GuideData
                Guide guide = new Guide();
                guide.setGuideId(guideData.getId());
                guide.setGuideName(guideData.getName());
                guide.setContactInfo(guideData.getContactInfo());
                guide.setLanguages(guideData.getLanguages());

                // Find the voyageId for the given voyage name
                int voyageId = findVoyageIdByName(guideData.getVoyage());
                guide.setVoyageId(voyageId);
                guide.setVoyageName(guideData.getVoyage());

                // Pass the Guide object to the controller
                controller.setGuideForEdit(guide);
            }

            Stage formStage = new Stage();
            formStage.setScene(new Scene(root));
            formStage.setTitle(guideData == null ? "Add New Guide" : "Edit Guide");
            formStage.showAndWait();

            // Reload data after form closes
            loadGuideData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening guide form: " + e.getMessage());
        }
    }

    private int findVoyageIdByName(String voyageName) {
        // In a real application, this would search the database
        // For now, returning a placeholder value
        return 1; // Default voyage ID if not found
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for guide data
    public static class GuideData {
        private final int id;
        private final String name;
        private final String contactInfo;
        private final String languages;
        private final String voyage;

        public GuideData(int id, String name, String contactInfo, String languages, String voyage) {
            this.id = id;
            this.name = name;
            this.contactInfo = contactInfo;
            this.languages = languages;
            this.voyage = voyage;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getContactInfo() {
            return contactInfo;
        }

        public String getLanguages() {
            return languages;
        }

        public String getVoyage() {
            return voyage;
        }
    }
}
