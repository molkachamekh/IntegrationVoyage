package com.example.voyage.voyage.frontoffice.controllers;

import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.BookingService;
import com.example.voyage.utils.SessionManager;
import com.example.voyage.voyage.models.Guide;
import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.GuideService;
import com.example.voyage.voyage.services.VoyageService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ExploreContentController {

    @FXML
    private FlowPane voyagesContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Label totalVoyagesLabel;

    @FXML
    private Button filterButton;

    private VoyageService voyageService = new VoyageService();
    private GuideService guideService = new GuideService();
    private BookingService bookingService = new BookingService();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        // Initialize layout with optimized spacing for modern cards
        voyagesContainer.setHgap(30); // Increased horizontal gap between cards
        voyagesContainer.setVgap(30); // Increased vertical gap between cards
        voyagesContainer.setPrefWrapLength(1000); // Wider wrap length for better layout
        voyagesContainer.setPadding(new Insets(25));

        // Configure scroll pane for smooth scrolling experience
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("modern-scroll-pane");

        // Remove padding from scroll pane to maximize space
        scrollPane.setPadding(new Insets(0));

        // Set up sort options
        sortComboBox.getItems().addAll(
                "Price (Low to High)",
                "Price (High to Low)",
                "Departure Date (Soonest)",
                "Destination (A-Z)");
        sortComboBox.setValue("Departure Date (Soonest)");
        sortComboBox.setOnAction(e -> loadVoyages());

        // Add search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadVoyages());

        // Setup filter button action
        filterButton.setOnAction(e -> showFilterDialog());

        // Load voyages
        loadVoyages();
    }

    private void loadVoyages() {
        voyagesContainer.getChildren().clear();

        try {
            // Get all voyages
            ObservableList<Voyage> voyages = voyageService.getAllVoyages();

            // Update total count
            totalVoyagesLabel.setText(voyages.size() + " voyages available");

            // Filter by search text if needed
            String searchText = searchField.getText().toLowerCase();
            if (searchText != null && !searchText.isEmpty()) {
                ObservableList<Voyage> filteredVoyages = FXCollections.observableArrayList();
                for (Voyage v : voyages) {
                    if ((v.getDestination() != null && v.getDestination().toLowerCase().contains(searchText)) ||
                            (v.getDescription() != null && v.getDescription().toLowerCase().contains(searchText))) {
                        filteredVoyages.add(v);
                    }
                }
                voyages = filteredVoyages;
                totalVoyagesLabel.setText(voyages.size() + " voyages found");
            }

            // Sort based on selected option - Create a new modifiable list to avoid
            // UnsupportedOperationException
            // The issue was that we were trying to sort a filtered list which is
            // unmodifiable
            ObservableList<Voyage> sortableVoyages = FXCollections.observableArrayList(voyages);
            String sortOption = sortComboBox.getValue();
            if (sortOption != null) {
                switch (sortOption) {
                    case "Price (Low to High)":
                        sortableVoyages.sort((v1, v2) -> Double.compare(v1.getPrice(), v2.getPrice()));
                        break;
                    case "Price (High to Low)":
                        sortableVoyages.sort((v1, v2) -> Double.compare(v2.getPrice(), v1.getPrice()));
                        break;
                    case "Departure Date (Soonest)":
                        sortableVoyages.sort((v1, v2) -> v1.getDepartureDate().compareTo(v2.getDepartureDate()));
                        break;
                    case "Destination (A-Z)":
                        sortableVoyages.sort((v1, v2) -> v1.getDestination().compareTo(v2.getDestination()));
                        break;
                }
            }

            // Get all guides for quick lookup
            ObservableList<Guide> guides = guideService.getAllGuides();
            Map<Integer, Guide> guideMap = new HashMap<>();
            for (Guide guide : guides) {
                guideMap.put(guide.getVoyageId(), guide);
            }

            // Create cards for each voyage using the sorted list
            for (Voyage voyage : sortableVoyages) {
                voyage.loadImage(); // Load image data
                Guide guide = guideMap.get(voyage.getVoyageId());
                VBox voyageCard = createVoyageCard(voyage, guide);
                voyagesContainer.getChildren().add(voyageCard);
            }

            // If no voyages, show message
            if (sortableVoyages.isEmpty()) {
                Label noVoyagesLabel = new Label("No voyages found");
                noVoyagesLabel.getStyleClass().add("placeholder-text");
                voyagesContainer.getChildren().add(noVoyagesLabel);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load voyages: " + e.getMessage());
        }
    }

    private void showFilterDialog() {
        // Create a dialog for advanced filtering
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Filter Voyages");
        dialog.setHeaderText("Apply advanced filters");

        // Set the button types
        ButtonType applyFiltersButtonType = new ButtonType("Apply Filters", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyFiltersButtonType, ButtonType.CANCEL);

        // Create the filter controls
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Price range filter
        Label priceRangeLabel = new Label("Price Range:");
        Slider priceSlider = new Slider(0, 5000, 5000);
        priceSlider.setShowTickLabels(true);
        priceSlider.setShowTickMarks(true);

        Label selectedPriceLabel = new Label("Max: $5000");
        priceSlider.valueProperty().addListener((obs, oldVal, newVal) -> selectedPriceLabel
                .setText("Max: $" + String.format("%.0f", newVal.doubleValue())));

        // Date range filter
        Label departureDateLabel = new Label("Departure After:");
        DatePicker departureDatePicker = new DatePicker(LocalDate.now());

        // Destination filter
        Label destinationLabel = new Label("Popular Destinations:");
        ComboBox<String> destinationComboBox = new ComboBox<>();
        destinationComboBox.getItems().addAll("Any", "Paris", "Tokyo", "New York", "Sydney", "Rome");
        destinationComboBox.setValue("Any");

        // Add all to grid
        grid.add(priceRangeLabel, 0, 0);
        grid.add(priceSlider, 1, 0);
        grid.add(selectedPriceLabel, 2, 0);
        grid.add(departureDateLabel, 0, 1);
        grid.add(departureDatePicker, 1, 1);
        grid.add(destinationLabel, 0, 2);
        grid.add(destinationComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == applyFiltersButtonType) {
                // Apply filters - in a real app, you would use these values to filter voyages
                System.out.println("Applying filters: Max price = " + priceSlider.getValue() +
                        ", Departure after = " + departureDatePicker.getValue() +
                        ", Destination = " + destinationComboBox.getValue());

                // For this example, just reload with basic search
                loadVoyages();
            }
        });
    }

    private VBox createVoyageCard(Voyage voyage, Guide guide) {
        // Main card container - modernized with consistent sizing
        VBox card = new VBox();
        card.getStyleClass().addAll("voyage-card", "modern-card");
        card.setPrefWidth(340); // Slightly wider for better content display
        card.setMaxWidth(340);
        card.setMinHeight(450); // Fixed height to ensure consistency
        card.setMaxHeight(450);

        // Image section with improved styling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(340);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("card-image");

        // Try to load image with error handling
        boolean imageLoaded = false;
        if (voyage.getImage() != null) {
            imageView.setImage(voyage.getImage());
            imageLoaded = true;
        } else if (voyage.getImagePath() != null && !voyage.getImagePath().isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(voyage.getImagePath());
                Image image = new Image(new ByteArrayInputStream(imageData));
                imageView.setImage(image);
                imageLoaded = true;
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        // Use default image if loading failed
        if (!imageLoaded) {
            try {
                imageView.setImage(
                        new Image(getClass().getResourceAsStream("/com/example/voyage/images/default-voyage.jpg")));
            } catch (Exception e) {
                // Apply a gradient background as fallback
                imageView.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498db, #2c3e50);");
            }
        }

        // Content section with adjusted padding for better space utilization
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("card-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Destination with dates - streamlined layout
        Label destinationLabel = new Label(voyage.getDestination());
        destinationLabel.getStyleClass().add("card-title");

        HBox dateRow = new HBox(8);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        FontIcon calendarIcon = new FontIcon("fas-calendar-alt");
        calendarIcon.setIconColor(Color.web("#7f8c8d"));

        Label datesLabel = new Label(voyage.getDepartureDate().format(dateFormatter) + " → " +
                voyage.getReturnDate().format(dateFormatter));
        datesLabel.getStyleClass().add("card-dates");

        dateRow.getChildren().addAll(calendarIcon, datesLabel);

        // Price badge - more visually distinct
        HBox priceRow = new HBox();
        priceRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("$%.2f", voyage.getPrice()));
        priceLabel.getStyleClass().add("price-badge");

        priceRow.getChildren().add(priceLabel);

        // Guide information - more compact
        VBox guideBox = new VBox(3);
        guideBox.getStyleClass().add("guide-info");
        guideBox.setMaxHeight(60); // Limit height to ensure it doesn't take too much space

        if (guide != null) {
            HBox guideRow = new HBox(5);
            guideRow.setAlignment(Pos.CENTER_LEFT);

            FontIcon userIcon = new FontIcon("fas-user-tie");
            userIcon.setIconColor(Color.web("#e67e22"));

            Label guideNameLabel = new Label(guide.getGuideName());
            guideNameLabel.getStyleClass().add("guide-name");

            guideRow.getChildren().addAll(userIcon, guideNameLabel);

            // Languages in a separate row for clarity
            HBox languageRow = new HBox(5);
            languageRow.setAlignment(Pos.CENTER_LEFT);

            FontIcon languageIcon = new FontIcon("fas-language");
            languageIcon.setIconColor(Color.web("#7f8c8d"));

            Label languagesLabel = new Label(guide.getLanguages());
            languagesLabel.getStyleClass().add("guide-languages");

            languageRow.getChildren().addAll(languageIcon, languagesLabel);
            guideBox.getChildren().addAll(guideRow, languageRow);
        } else {
            HBox noGuideRow = new HBox(5);
            noGuideRow.setAlignment(Pos.CENTER_LEFT);

            FontIcon infoIcon = new FontIcon("fas-info-circle");
            infoIcon.setIconColor(Color.web("#95a5a6"));

            Label noGuideLabel = new Label("No guide assigned");
            noGuideLabel.getStyleClass().add("no-guide");

            noGuideRow.getChildren().addAll(infoIcon, noGuideLabel);
            guideBox.getChildren().add(noGuideRow);
        }

        // Description with controlled height
        Label descriptionLabel = new Label(
                voyage.getDescription() != null ? truncateText(voyage.getDescription(), 100) : "");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(45);
        descriptionLabel.setMaxHeight(45); // Limit height to control overall card size
        descriptionLabel.getStyleClass().add("card-description");

        // Button section with improved styling
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0));

        Button bookButton = new Button("Book Now");
        bookButton.getStyleClass().add("book-button");
        bookButton.setPrefWidth(200);
        HBox.setHgrow(bookButton, Priority.ALWAYS);

        FontIcon bookIcon = new FontIcon("fas-calendar-plus");
        bookIcon.setIconColor(Color.WHITE);
        bookButton.setGraphic(bookIcon);
        bookButton.setOnAction(e -> handleBookVoyage(voyage));

        buttonBox.getChildren().add(bookButton);

        // Assemble all elements with spacers to ensure consistent layout
        content.getChildren().addAll(
                destinationLabel,
                dateRow,
                new Separator(),
                priceRow,
                guideBox,
                new Separator(),
                descriptionLabel,
                buttonBox);

        // Add hover effect container
        StackPane cardContainer = new StackPane(imageView);
        cardContainer.getStyleClass().add("image-container");

        // Add all to main container
        card.getChildren().addAll(cardContainer, content);

        return card;
    }

    private void handleBookVoyage(Voyage voyage) {
        // Check if user is logged in
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Login Required",
                    "You must be logged in to book a voyage.",
                    "Please login or create an account.");
            return;
        }

        // Confirm booking
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Booking");
        confirmDialog.setHeaderText("Book Voyage to " + voyage.getDestination());
        confirmDialog.setContentText("Do you want to book this voyage departing on " +
                voyage.getDepartureDate().format(dateFormatter) +
                " for $" + String.format("%.2f", voyage.getPrice()) + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Create the booking
                Booking newBooking = new Booking();
                newBooking.setUserId(currentUser.getUserId());
                newBooking.setVoyageId(voyage.getVoyageId());
                newBooking.setBookingDate(LocalDate.now());
                newBooking.setStatus("En Attente"); // Initial status is "Pending"

                boolean success = bookingService.createBooking(newBooking);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Booking Successful",
                            "Your booking request has been submitted!",
                            "The status is currently 'En Attente'. You can check your booking status in My Bookings.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Booking Failed",
                            "Failed to create your booking.",
                            "Please try again later or contact customer support.");
                }
            }
        });
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-text");
        voyagesContainer.getChildren().clear();
        voyagesContainer.getChildren().add(errorLabel);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
