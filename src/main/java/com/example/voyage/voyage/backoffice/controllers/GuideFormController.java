package com.example.voyage.voyage.backoffice.controllers;

import com.example.voyage.voyage.models.Guide;
import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.GuideService;
import com.example.voyage.voyage.services.VoyageService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;

public class GuideFormController {
    @FXML
    private TextField guideNameField;

    @FXML
    private TextField contactInfoField;

    @FXML
    private TextField languagesField;

    @FXML
    private ComboBox<Voyage> voyageComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    private GuideService guideService = new GuideService();
    private VoyageService voyageService = new VoyageService();
    private Guide currentGuide;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Load voyages for combo box
        ObservableList<Voyage> voyages = voyageService.getAllVoyages();
        voyageComboBox.setItems(voyages);

        // Set display format for combo box
        voyageComboBox.setConverter(new StringConverter<Voyage>() {
            @Override
            public String toString(Voyage voyage) {
                return voyage == null ? "" : voyage.getDestination();
            }

            @Override
            public Voyage fromString(String string) {
                return null; // Not needed for our use case
            }
        });

        // Add validation listeners
        guideNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        contactInfoField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        languagesField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        voyageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        boolean isValid = !guideNameField.getText().isEmpty() &&
                !contactInfoField.getText().isEmpty() &&
                !languagesField.getText().isEmpty() &&
                voyageComboBox.getValue() != null;

        saveButton.setDisable(!isValid);
    }

    public void setGuideForEdit(Guide guide) {
        this.currentGuide = guide;

        // Fill in form fields with guide data
        guideNameField.setText(guide.getGuideName());
        contactInfoField.setText(guide.getContactInfo());
        languagesField.setText(guide.getLanguages());

        // Select the voyage in the combo box
        for (Voyage voyage : voyageComboBox.getItems()) {
            if (voyage.getVoyageId() == guide.getVoyageId()) {
                voyageComboBox.setValue(voyage);
                break;
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Get form values
            String guideName = guideNameField.getText();
            String contactInfo = contactInfoField.getText();
            String languages = languagesField.getText();
            Voyage selectedVoyage = voyageComboBox.getValue();

            // Validate again
            if (guideName.isEmpty() || contactInfo.isEmpty() || languages.isEmpty() || selectedVoyage == null) {
                errorLabel.setText("Please fill in all required fields.");
                errorLabel.setVisible(true);
                return;
            }

            // Create or update guide
            boolean success;
            if (currentGuide == null) {
                // Create new guide
                Guide newGuide = new Guide(selectedVoyage.getVoyageId(), guideName, contactInfo, languages);
                success = guideService.createGuide(newGuide);
            } else {
                // Update existing guide
                currentGuide.setGuideName(guideName);
                currentGuide.setContactInfo(contactInfo);
                currentGuide.setLanguages(languages);
                currentGuide.setVoyageId(selectedVoyage.getVoyageId());
                success = guideService.updateGuide(currentGuide);
            }

            if (success) {
                // Close the form
                ((Stage) saveButton.getScene().getWindow()).close();
            } else {
                errorLabel.setText("Failed to save guide. Please try again.");
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Just close the form
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
}
