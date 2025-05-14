package com.example.voyage.voyage.backoffice.controllers;

import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.VoyageService;
import com.example.voyage.utils.GeocodingService;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import netscape.javascript.JSObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class VoyageFormController {
    @FXML
    private GridPane formGrid;

    @FXML
    private TextField destinationField;

    @FXML
    private DatePicker departureDatePicker;

    @FXML
    private DatePicker returnDatePicker;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Rectangle imageBackground;

    @FXML
    private Label noImageLabel;

    @FXML
    private Label imageNameLabel;

    @FXML
    private Button selectImageButton;

    @FXML
    private Button clearImageButton;

    @FXML
    private Button mapButton;

    @FXML
    private WebView mapView;

    @FXML
    private VBox mapContainer;

    @FXML
    private Button refreshMapButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // Labels for validation errors
    private Label destinationError;
    private Label departureDateError;
    private Label returnDateError;
    private Label priceError;

    private VoyageService voyageService = new VoyageService();
    private GeocodingService geocodingService = new GeocodingService();

    private Voyage currentVoyage;
    private File selectedImageFile;
    private boolean isEditMode = false;
    private boolean hasMapBeenLoaded = false;

    @FXML
    public void initialize() {
        // Initialize date pickers to sensible defaults
        departureDatePicker.setValue(LocalDate.now().plusDays(7));
        returnDatePicker.setValue(LocalDate.now().plusDays(14));

        // Create error labels but don't add them to the grid yet
        destinationError = createErrorLabel();
        departureDateError = createErrorLabel();
        returnDateError = createErrorLabel();
        priceError = createErrorLabel();

        // Don't show errors on initialization
        clearValidationErrors();

        // Make description field grow vertically
        descriptionField.setWrapText(true);

        // Configure image background
        imageBackground.setArcWidth(20);
        imageBackground.setArcHeight(20);
        imageBackground.setFill(Color.web("#f0f0f0"));

        // IMPORTANT: Always make the map button clickable
        mapButton.setDisable(false);

        // Debug print to verify initialization
        System.out.println("VoyageFormController initialized, map button enabled: " + !mapButton.isDisable());
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("error-label");
        label.setVisible(false);
        return label;
    }

    public void setVoyageForEdit(Voyage voyage) {
        this.currentVoyage = voyage;
        this.isEditMode = true;

        // Populate form fields
        destinationField.setText(voyage.getDestination());
        departureDatePicker.setValue(voyage.getDepartureDate());
        returnDatePicker.setValue(voyage.getReturnDate());
        priceField.setText(String.format("%.2f", voyage.getPrice()));
        descriptionField.setText(voyage.getDescription());

        // Load image if available
        if (voyage.getImage() != null) {
            imagePreview.setImage(voyage.getImage());
            updateImageDisplay(true);
        } else if (voyage.getImagePath() != null && !voyage.getImagePath().isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(voyage.getImagePath());
                Image image = new Image(new java.io.ByteArrayInputStream(imageData));
                imagePreview.setImage(image);
                updateImageDisplay(true);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                updateImageDisplay(false);
            }
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        selectedImageFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imageNameLabel.setText(selectedImageFile.getName());
                updateImageDisplay(true);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load selected image: " + e.getMessage());
                updateImageDisplay(false);
            }
        }
    }

    @FXML
    private void handleClearImage() {
        imagePreview.setImage(null);
        selectedImageFile = null;
        imageNameLabel.setText("");
        updateImageDisplay(false);
    }

    private void updateImageDisplay(boolean hasImage) {
        noImageLabel.setVisible(!hasImage);
        clearImageButton.setDisable(!hasImage);
    }

    @FXML
    private void handleOpenMapDialog() {
        try {
            System.out.println("Opening map dialog");

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Location");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mapButton.getScene().getWindow());
            dialogStage.setMinWidth(650);
            dialogStage.setMinHeight(550);

            // Create container
            BorderPane borderPane = new BorderPane();

            // Create map container and controls
            VBox mapBox = new VBox(10);
            mapBox.setPadding(new Insets(15));

            // Show loading indicator initially
            ProgressIndicator progressIndicator = new ProgressIndicator();
            Label loadingLabel = new Label("Loading map...");
            loadingLabel.getStyleClass().add("hint-text");

            VBox loadingBox = new VBox(10, progressIndicator, loadingLabel);
            loadingBox.setAlignment(Pos.CENTER);
            mapBox.getChildren().add(loadingBox);

            // Create WebView for the map
            WebView webView = new WebView();
            webView.setPrefSize(600, 400);

            // Create destination input for map search
            HBox searchBox = new HBox(10);
            searchBox.setAlignment(Pos.CENTER);

            TextField searchField = new TextField();
            searchField.setText(destinationField.getText());
            searchField.setPromptText("Enter location to search");
            searchField.setPrefWidth(300);
            HBox.setHgrow(searchField, Priority.ALWAYS);

            Button searchButton = new Button("Search");
            searchButton.getStyleClass().add("primary-button");

            Label selectedLocationLabel = new Label("No location selected");
            selectedLocationLabel.getStyleClass().add("hint-text");

            searchBox.getChildren().addAll(searchField, searchButton);

            // Create buttons
            Button selectButton = new Button("Use Selected Location");
            selectButton.getStyleClass().add("primary-button");
            selectButton.setDisable(true);

            Button cancelButton = new Button("Cancel");

            HBox buttonBox = new HBox(10, cancelButton, selectButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            // Add components to layout
            mapBox.getChildren().addAll(searchBox, selectedLocationLabel);
            borderPane.setCenter(webView);
            borderPane.setTop(mapBox);
            borderPane.setBottom(buttonBox);
            BorderPane.setMargin(buttonBox, new Insets(10));

            // Handle search button action
            searchButton.setOnAction(e -> {
                String searchText = searchField.getText().trim();
                if (!searchText.isEmpty()) {
                    selectedLocationLabel.setText("Searching for: " + searchText);
                    loadMapWithLocation(webView, searchText, selectedLocationLabel, selectButton);
                }
            });

            // Load initial map
            String destination = destinationField.getText().trim();
            if (!destination.isEmpty()) {
                loadMapWithLocation(webView, destination, selectedLocationLabel, selectButton);
            } else {
                // Default location (Paris)
                loadMapWithCoordinates(webView, 48.8566, 2.3522, "Select a location", selectedLocationLabel,
                        selectButton);
            }

            // Handle select button action
            selectButton.setOnAction(e -> {
                try {
                    String location = selectedLocationLabel.getText();
                    if (!location.startsWith("No location") && !location.startsWith("Searching")) {
                        destinationField.setText(location);
                        dialogStage.close();
                    }
                } catch (Exception ex) {
                    System.err.println("Error selecting location: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Handle cancel button action
            cancelButton.setOnAction(e -> dialogStage.close());

            // Set scene and show dialog
            Scene scene = new Scene(borderPane);
            String cssPath = getClass().getResource("/com/example/voyage/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (Exception ex) {
            System.err.println("Error opening map dialog: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open map: " + ex.getMessage());
        }
    }

    private void loadMapWithLocation(WebView webView, String location, Label statusLabel, Button selectButton) {
        statusLabel.setText("Searching for: " + location);
        selectButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> geocodingService.getCoordinates(location))
                .thenAccept(coordinates -> {
                    Platform.runLater(() -> {
                        if (coordinates != null) {
                            loadMapWithCoordinates(webView, coordinates[0], coordinates[1], location, statusLabel,
                                    selectButton);
                            statusLabel.setText(location);
                        } else {
                            statusLabel.setText("Location not found. Try another search or click on the map.");
                            loadMapWithCoordinates(webView, 48.8566, 2.3522, "Location not found", statusLabel,
                                    selectButton);
                        }
                    });
                });
    }

    private void loadMapWithCoordinates(WebView webView, double lat, double lon, String title, Label statusLabel,
            Button selectButton) {
        try {
            String mapHtml = buildInteractiveMapHtml(lat, lon, title);
            webView.getEngine().loadContent(mapHtml);

            // Setup javascript bridge to enable communication between JavaFX and JavaScript
            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("Map loaded successfully");

                    try {
                        // Connect JavaScript to Java
                        JSObject window = (JSObject) webView.getEngine().executeScript("window");
                        window.setMember("javaMapConnector", new JavaMapConnector(statusLabel, selectButton));
                        System.out.println("JavaScript bridge connected");
                    } catch (Exception e) {
                        System.err.println("Error connecting JavaScript bridge: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            System.err.println("Error loading map: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // JavaMapConnector class specifically for the map dialog
    public class JavaMapConnector {
        private Label statusLabel;
        private Button selectButton;

        public JavaMapConnector(Label statusLabel, Button selectButton) {
            this.statusLabel = statusLabel;
            this.selectButton = selectButton;
        }

        public void updateSelectedLocation(String location) {
            Platform.runLater(() -> {
                statusLabel.setText(location);
                selectButton.setDisable(false);
                System.out.println("Location selected in dialog: " + location);
            });
        }
    }

    private String buildInteractiveMapHtml(double lat, double lon, String title) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"\n" +
                "        integrity=\"sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=\"\n" +
                "        crossorigin=\"\"/>\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"\n" +
                "        integrity=\"sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=\"\n" +
                "        crossorigin=\"\"></script>\n" +
                "    <style>\n" +
                "        html, body {\n" +
                "            height: 100%;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        #map {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([" + lat + ", " + lon + "], 10);\n" +
                "        var selectedMarker = null;\n" +
                "        var selectedLocation = { name: '', lat: 0, lng: 0 };\n" +
                "\n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "            attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n"
                +
                "        }).addTo(map);\n" +
                "\n" +
                "        var initialMarker = L.marker([" + lat + ", " + lon + "]).addTo(map)\n" +
                "            .bindPopup('" + escapeJsString(title) + "')\n" +
                "            .openPopup();\n" +
                "\n" +
                "        map.on('click', function(e) {\n" +
                "            console.log('Map clicked at: ' + e.latlng.lat + ', ' + e.latlng.lng);\n" +
                "            \n" +
                "            fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${e.latlng.lat}&lon=${e.latlng.lng}&zoom=18&addressdetails=1`)\n"
                +
                "                .then(response => response.json())\n" +
                "                .then(data => {\n" +
                "                    let locationName = '';\n" +
                "                    console.log('Geocoding data:', data);\n" +
                "                    \n" +
                "                    // Create location name from address data\n" +
                "                    if (data.address) {\n" +
                "                        if (data.address.city) {\n" +
                "                            locationName = data.address.city;\n" +
                "                        } else if (data.address.town) {\n" +
                "                            locationName = data.address.town;\n" +
                "                        } else if (data.address.village) {\n" +
                "                            locationName = data.address.village;\n" +
                "                        }\n" +
                "                        \n" +
                "                        // Add country\n" +
                "                        if (data.address.country) {\n" +
                "                            if (locationName) {\n" +
                "                                locationName += ', ' + data.address.country;\n" +
                "                            } else {\n" +
                "                                locationName = data.address.country;\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                    \n" +
                "                    if (!locationName) {\n" +
                "                        locationName = 'Unknown location';\n" +
                "                    }\n" +
                "\n" +
                "                    console.log('Selected location: ' + locationName);\n" +
                "                    \n" +
                "                    // Store selected location info\n" +
                "                    selectedLocation = {\n" +
                "                        name: locationName,\n" +
                "                        lat: e.latlng.lat,\n" +
                "                        lng: e.latlng.lng\n" +
                "                    };\n" +
                "\n" +
                "                    // Display popup with location name\n" +
                "                    if (selectedMarker) {\n" +
                "                        map.removeLayer(selectedMarker);\n" +
                "                    }\n" +
                "                    selectedMarker = L.marker(e.latlng).addTo(map);\n" +
                "                    selectedMarker.bindPopup(locationName).openPopup();\n" +
                "                    \n" +
                "                    // Send location to Java\n" +
                "                    try {\n" +
                "                        if (window.javaMapConnector) {\n" +
                "                            console.log('Updating Java with location: ' + locationName);\n" +
                "                            window.javaMapConnector.updateSelectedLocation(locationName);\n" +
                "                        } else {\n" +
                "                            console.error('javaMapConnector not found!');\n" +
                "                        }\n" +
                "                    } catch (e) {\n" +
                "                        console.error('Error updating Java: ' + e);\n" +
                "                    }\n" +
                "                })\n" +
                "                .catch(error => {\n" +
                "                    console.error('Error:', error);\n" +
                "                    selectedMarker.bindPopup('Location selected').openPopup();\n" +
                "                });\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String escapeJsString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @FXML
    private void handleRefreshMap() {
        System.out.println("Refresh map button clicked");
        handleOpenMapDialog();
    }

    @FXML
    private void handleSave() {
        // Clear previous validation errors
        clearValidationErrors();

        // Validate form fields and show errors if needed
        boolean isValid = validateForm();

        if (isValid) {
            saveVoyage();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate destination (required)
        if (destinationField.getText() == null || destinationField.getText().trim().isEmpty()) {
            showError(destinationError, "Destination is required", destinationField);
            isValid = false;
        }

        // Validate departure date (required)
        if (departureDatePicker.getValue() == null) {
            showError(departureDateError, "Departure date is required", departureDatePicker);
            isValid = false;
        }

        // Validate return date (required and after departure date)
        if (returnDatePicker.getValue() == null) {
            showError(returnDateError, "Return date is required", returnDatePicker);
            isValid = false;
        } else if (departureDatePicker.getValue() != null &&
                returnDatePicker.getValue().isBefore(departureDatePicker.getValue())) {
            showError(returnDateError, "Return date must be after departure date", returnDatePicker);
            isValid = false;
        }

        // Validate price (required and numeric)
        String priceText = priceField.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            showError(priceError, "Price is required", priceField);
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showError(priceError, "Price must be greater than zero", priceField);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(priceError, "Price must be a valid number", priceField);
                isValid = false;
            }
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
        formGrid.getChildren().removeAll(destinationError, departureDateError, returnDateError, priceError);

        // Hide all error labels
        destinationError.setVisible(false);
        departureDateError.setVisible(false);
        returnDateError.setVisible(false);
        priceError.setVisible(false);

        // Remove error styles
        destinationField.getStyleClass().remove("field-error");
        departureDatePicker.getStyleClass().remove("field-error");
        returnDatePicker.getStyleClass().remove("field-error");
        priceField.getStyleClass().remove("field-error");
    }

    private void saveVoyage() {
        try {
            // Create or update Voyage object
            if (currentVoyage == null) {
                currentVoyage = new Voyage();
            }

            currentVoyage.setDestination(destinationField.getText());
            currentVoyage.setDepartureDate(departureDatePicker.getValue());
            currentVoyage.setReturnDate(returnDatePicker.getValue());
            currentVoyage.setPrice(Double.parseDouble(priceField.getText()));
            currentVoyage.setDescription(descriptionField.getText());

            // Handle image
            if (selectedImageFile != null) {
                try (FileInputStream fis = new FileInputStream(selectedImageFile);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    byte[] imageData = baos.toByteArray();
                    String base64Image = Base64.getEncoder().encodeToString(imageData);
                    currentVoyage.setImagePath(base64Image);
                    currentVoyage.setImage(new Image(new java.io.ByteArrayInputStream(imageData)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (imagePreview.getImage() == null && currentVoyage.getImagePath() != null) {
                // Clear image if user removed it
                currentVoyage.setImagePath(null);
                currentVoyage.setImage(null);
            }

            boolean success;
            if (isEditMode) {
                success = voyageService.updateVoyage(currentVoyage);
            } else {
                success = voyageService.createVoyage(currentVoyage);
            }

            if (success) {
                // Close the form
                ((Stage) saveButton.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save voyage.");
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
