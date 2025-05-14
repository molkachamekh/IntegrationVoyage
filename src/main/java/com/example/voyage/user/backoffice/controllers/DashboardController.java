package com.example.voyage.user.backoffice.controllers;

import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
// Fix incorrect package imports
import com.example.voyage.user.services.BookingService;
import com.example.voyage.user.services.UserService;
import com.example.voyage.utils.DatabaseConnection;
import com.example.voyage.utils.SessionManager;
import com.example.voyage.voyage.backoffice.controllers.GuideFormController;
import com.example.voyage.voyage.backoffice.controllers.VoyageFormController;
import com.example.voyage.voyage.models.Guide;
import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.voyage.services.GuideService;
import com.example.voyage.voyage.services.VoyageService;

import com.google.gson.FieldNamingPolicy;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.concurrent.Worker;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.voyage.user.models.BookingPerUser;
import com.example.voyage.voyage.models.GuidePerVoyage;
import com.example.voyage.voyage.models.VoyagePerPeriod;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {
    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label pageTitle;

    @FXML
    private Label userNameLabel;

    @FXML
    private VBox contentArea;

    @FXML
    private Button usersButton;

    @FXML
    private Button voyagesButton;

    @FXML
    private Button guidesButton;

    @FXML
    private Button bookingsButton;

    @FXML
    private Button userBookingsStatsButton;

    @FXML
    private Button voyageStatsButton;

    private UserService userService = new UserService();
    private VoyageService voyageService = new VoyageService();
    private GuideService guideService = new GuideService();
    private BookingService bookingService = new BookingService();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        // Get current admin user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isAdmin()) {
            userNameLabel.setText("Welcome, " + currentUser.getFullName());
        } else {
            userNameLabel.setText("Welcome, Admin");
        }

        // Set default page title
        pageTitle.setText("Dashboard");

        // Show dashboard summary when first loaded
        showDashboardSummary();
    }

    private void showDashboardSummary() {
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Create stats grid
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(20);
            statsGrid.setVgap(20);
            statsGrid.setPadding(new Insets(20));

            // Load counts from database
            int userCount = userService.getUserCount();
            int voyageCount = voyageService.getVoyageCount();
            int guideCount = guideService.getGuideCount();
            int bookingCount = bookingService.getBookingCount();

            // Create summary cards
            statsGrid.add(createSummaryCard("Users", userCount, "fas-users",
                    Color.web("#3498db"), Color.web("#2980b9")), 0, 0);
            statsGrid.add(createSummaryCard("Voyages", voyageCount, "fas-plane",
                    Color.web("#2ecc71"), Color.web("#27ae60")), 1, 0);
            statsGrid.add(createSummaryCard("Guides", guideCount, "fas-user-tie",
                    Color.web("#e67e22"), Color.web("#d35400")), 0, 1);
            statsGrid.add(createSummaryCard("Bookings", bookingCount, "fas-calendar-check",
                    Color.web("#9b59b6"), Color.web("#8e44ad")), 1, 1);

            // Add to content area
            contentArea.getChildren().add(statsGrid);

            // Recent items section
            Label recentLabel = new Label("Recent Activity");
            recentLabel.getStyleClass().add("section-title");
            recentLabel.setPadding(new Insets(20, 0, 10, 20));
            contentArea.getChildren().add(recentLabel);

            // Add recent bookings panel
            contentArea.getChildren().add(createRecentBookingsPanel());

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error Loading Dashboard", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    private StackPane createSummaryCard(String title, int count, String iconLiteral, Color startColor, Color endColor) {
        // Card container
        StackPane card = new StackPane();
        card.setPrefSize(250, 150);
        card.getStyleClass().add("summary-card");

        // Create background with gradient
        Rectangle background = new Rectangle(250, 150);
        background.setArcWidth(20);
        background.setArcHeight(20);

        // Create gradient
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, startColor), new Stop(1, endColor));
        background.setFill(gradient);

        // Create content
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(15));

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(48);
        icon.setIconColor(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setTextFill(Color.WHITE);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("card-count");
        countLabel.setTextFill(Color.WHITE);

        content.getChildren().addAll(icon, titleLabel, countLabel);

        // Add to card
        card.getChildren().addAll(background, content);

        return card;
    }

    private VBox createRecentBookingsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 20, 20, 20));

        // Get recent bookings
        ObservableList<Booking> recentBookings = bookingService.getRecentBookings(5);

        if (recentBookings.isEmpty()) {
            Label emptyLabel = new Label("No recent bookings found");
            emptyLabel.getStyleClass().add("info-text");
            panel.getChildren().add(emptyLabel);
            return panel;
        }

        // Create a VBox for each booking
        for (Booking booking : recentBookings) {
            HBox bookingRow = new HBox(15);
            bookingRow.getStyleClass().add("booking-row");
            bookingRow.setPadding(new Insets(10));
            bookingRow.setAlignment(Pos.CENTER_LEFT);

            // Status indicator
            StackPane statusIndicator = new StackPane();
            Rectangle statusRect = new Rectangle(5, 40);
            statusRect.getStyleClass().add("status-" + booking.getStatus().toLowerCase() + "-bg");
            statusIndicator.getChildren().add(statusRect);

            // Booking details
            VBox details = new VBox(5);
            details.setAlignment(Pos.CENTER_LEFT);
            details.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

            Label destinationLabel = new Label(booking.getVoyageName());
            destinationLabel.getStyleClass().add("booking-destination");

            Label userLabel = new Label("User: " + booking.getUserName());
            userLabel.getStyleClass().add("booking-user");

            details.getChildren().addAll(destinationLabel, userLabel);

            // Date and status
            VBox dateAndStatus = new VBox(5);
            dateAndStatus.setAlignment(Pos.CENTER_RIGHT);

            Label dateLabel = new Label(booking.getBookingDate().format(dateFormatter));
            dateLabel.getStyleClass().add("booking-date");

            Label statusLabel = new Label(booking.getStatus());
            statusLabel.getStyleClass().add("status-" + booking.getStatus().toLowerCase());

            dateAndStatus.getChildren().addAll(dateLabel, statusLabel);

            // Add all to row
            bookingRow.getChildren().addAll(statusIndicator, details, dateAndStatus);

            panel.getChildren().add(bookingRow);
        }

        return panel;
    }

    @FXML
    private void handleUsersButton(ActionEvent event) {
        pageTitle.setText("Users Management");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Create header with actions
            HBox header = createSectionHeader("Users", "fas-user-plus", "Add User", e -> handleAddUser());
            contentArea.getChildren().add(header);

            // Create scrollable container for user cards
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("content-scroll-pane");

            // Create flow pane for user cards
            FlowPane usersContainer = new FlowPane();
            usersContainer.setHgap(20);
            usersContainer.setVgap(20);
            usersContainer.setPrefWrapLength(900); // Adjust based on your layout
            usersContainer.setPadding(new Insets(20));

            // Load users from database
            ObservableList<User> users = userService.getAllUsers();

            // Create a card for each user
            for (User user : users) {
                usersContainer.getChildren().add(createUserCard(user));
            }

            scrollPane.setContent(usersContainer);
            contentArea.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Error loading Users view: " + e.getMessage());
        }
    }

    private VBox createUserCard(User user) {
        // Card container
        VBox card = new VBox(10);
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(250);
        card.setPadding(new Insets(15));

        // User icon
        FontIcon userIcon = new FontIcon(user.isAdmin() ? "fas-user-shield" : "fas-user");
        userIcon.setIconSize(32);
        userIcon.setIconColor(Color.web("#3498db"));

        // User details
        Label nameLabel = new Label(user.getFullName());
        nameLabel.getStyleClass().add("card-title");

        Label usernameLabel = new Label("@" + user.getUsername());
        usernameLabel.getStyleClass().add("card-subtitle");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("card-text");

        Label phoneLabel = new Label(user.getPhoneNumber() != null ? user.getPhoneNumber() : "No phone");
        phoneLabel.getStyleClass().add("card-text");

        // Role badge
        Label roleLabel = new Label(user.isAdmin() ? "Admin" : "User");
        roleLabel.getStyleClass().add("role-badge");
        roleLabel.getStyleClass().add(user.isAdmin() ? "admin-role" : "user-role");

        // Add details to card
        card.getChildren().addAll(userIcon, nameLabel, usernameLabel, emailLabel, phoneLabel, roleLabel);

        // Add actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = createIconButton("fas-edit", "Edit User");
        editButton.setOnAction(e -> handleEditUser(user));

        Button deleteButton = createIconButton("fas-trash-alt", "Delete User");
        deleteButton.setOnAction(e -> handleDeleteUser(user));

        actions.getChildren().addAll(editButton, deleteButton);
        card.getChildren().add(actions);

        return card;
    }

    @FXML
    private void handleVoyagesButton(ActionEvent event) {
        pageTitle.setText("Voyages Management");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Create header with actions
            HBox header = createSectionHeader("Voyages", "fas-plus", "Add Voyage", e -> handleAddVoyage());
            contentArea.getChildren().add(header);

            // Create scrollable container for voyage cards
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("content-scroll-pane");

            // Create flow pane for voyage cards
            FlowPane voyagesContainer = new FlowPane();
            voyagesContainer.setHgap(20);
            voyagesContainer.setVgap(20);
            voyagesContainer.setPrefWrapLength(900); // Adjust based on your layout
            voyagesContainer.setPadding(new Insets(20));

            // Load voyages from database
            ObservableList<Voyage> voyages = voyageService.getAllVoyages();

            // Load guides for quick lookup
            ObservableList<Guide> guides = guideService.getAllGuides();
            Map<Integer, Guide> guidesMap = new HashMap<>();
            for (Guide guide : guides) {
                guidesMap.put(guide.getVoyageId(), guide);
            }

            // Create a card for each voyage
            for (Voyage voyage : voyages) {
                voyage.loadImage(); // Load image from database
                Guide guide = guidesMap.get(voyage.getVoyageId());
                voyagesContainer.getChildren().add(createVoyageCard(voyage, guide));
            }

            scrollPane.setContent(voyagesContainer);
            contentArea.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Error loading Voyages view: " + e.getMessage());
        }
    }

    private VBox createVoyageCard(Voyage voyage, Guide guide) {
        // Card container
        VBox card = new VBox(0); // 0 spacing to avoid gap between image and content
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(300);

        // Voyage image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        boolean imageLoaded = false;
        // Try to load image from voyage object
        if (voyage.getImage() != null) {
            imageView.setImage(voyage.getImage());
            imageLoaded = true;
        } else {
            try {
                // Try to load image from Base64 string if available
                if (voyage.getImagePath() != null && !voyage.getImagePath().isEmpty()) {
                    try {
                        byte[] imageData = Base64.getDecoder().decode(voyage.getImagePath());
                        Image image = new Image(new java.io.ByteArrayInputStream(imageData));
                        imageView.setImage(image);
                        imageLoaded = true;
                    } catch (Exception e) {
                        System.err.println("Error decoding Base64 image: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        // Use default image if no image was loaded
        if (!imageLoaded) {
            try {
                imageView.setImage(
                        new Image(getClass().getResourceAsStream("/com/example/voyage/images/default-voyage.jpg")));
            } catch (Exception e) {
                System.err.println("Error loading default image: " + e.getMessage());
                // Create a placeholder if everything fails
                imageView.setStyle("-fx-background-color: #f5f5f5;");
            }
        }

        // Content container
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Voyage details
        Label destinationLabel = new Label(voyage.getDestination());
        destinationLabel.getStyleClass().add("card-title");

        Label datesLabel = new Label(voyage.getDepartureDate().format(dateFormatter) + " - " +
                voyage.getReturnDate().format(dateFormatter));
        datesLabel.getStyleClass().add("card-dates");

        Label priceLabel = new Label(String.format("$%.2f", voyage.getPrice()));
        priceLabel.getStyleClass().add("card-price");

        VBox guideInfo = new VBox(5);
        if (guide != null) {
            Label guideNameLabel = new Label("Guide: " + guide.getGuideName());
            guideNameLabel.getStyleClass().add("card-text");

            Label languagesLabel = new Label("Languages: " + guide.getLanguages());
            languagesLabel.getStyleClass().add("card-text");

            guideInfo.getChildren().addAll(guideNameLabel, languagesLabel);
        } else {
            Label noGuideLabel = new Label("No guide assigned");
            noGuideLabel.getStyleClass().add("card-text");
            guideInfo.getChildren().add(noGuideLabel);
        }

        // Add details to content
        content.getChildren().addAll(destinationLabel, datesLabel, priceLabel, guideInfo);

        // Add actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = createIconButton("fas-edit", "Edit Voyage");
        editButton.setOnAction(e -> handleEditVoyage(voyage));

        Button deleteButton = createIconButton("fas-trash-alt", "Delete Voyage");
        deleteButton.setOnAction(e -> handleDeleteVoyage(voyage));

        actions.getChildren().addAll(editButton, deleteButton);
        content.getChildren().add(actions);

        // Add image and content to card
        card.getChildren().addAll(imageView, content);

        return card;
    }

    @FXML
    private void handleGuidesButton(ActionEvent event) {
        pageTitle.setText("Guides Management");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Create header with actions
            HBox header = createSectionHeader("Guides", "fas-user-plus", "Add Guide", e -> handleAddGuide());
            contentArea.getChildren().add(header);

            // Create scrollable container for guide cards
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("content-scroll-pane");

            // Create flow pane for guide cards
            FlowPane guidesContainer = new FlowPane();
            guidesContainer.setHgap(20);
            guidesContainer.setVgap(20);
            guidesContainer.setPrefWrapLength(900); // Adjust based on your layout
            guidesContainer.setPadding(new Insets(20));

            // Load guides from database
            ObservableList<Guide> guides = guideService.getAllGuides();

            // Create a card for each guide
            for (Guide guide : guides) {
                guidesContainer.getChildren().add(createGuideCard(guide));
            }

            scrollPane.setContent(guidesContainer);
            contentArea.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Error loading Guides view: " + e.getMessage());
        }
    }

    private VBox createGuideCard(Guide guide) {
        // Card container
        VBox card = new VBox(10);
        card.getStyleClass().add("entity-card");
        card.setPrefWidth(250);
        card.setPadding(new Insets(15));

        // Guide icon
        FontIcon guideIcon = new FontIcon("fas-user-tie");
        guideIcon.setIconSize(32);
        guideIcon.setIconColor(Color.web("#e67e22"));

        // Guide details
        Label nameLabel = new Label(guide.getGuideName());
        nameLabel.getStyleClass().add("card-title");

        Label contactLabel = new Label(guide.getContactInfo());
        contactLabel.getStyleClass().add("card-text");

        Label languagesLabel = new Label("Languages: " + guide.getLanguages());
        languagesLabel.getStyleClass().add("card-text");

        Label voyageLabel = new Label("Voyage: " + guide.getVoyageName());
        voyageLabel.getStyleClass().add("card-subtitle");

        // Add details to card
        card.getChildren().addAll(guideIcon, nameLabel, contactLabel, languagesLabel, voyageLabel);

        // Add actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = createIconButton("fas-edit", "Edit Guide");
        editButton.setOnAction(e -> handleEditGuide(guide));

        Button deleteButton = createIconButton("fas-trash-alt", "Delete Guide");
        deleteButton.setOnAction(e -> handleDeleteGuide(guide));

        actions.getChildren().addAll(editButton, deleteButton);
        card.getChildren().add(actions);

        return card;
    }

    @FXML
    private void handleBookingsButton(ActionEvent event) {
        pageTitle.setText("Bookings Management");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Create header
            Label headerLabel = new Label("Manage Bookings");
            headerLabel.getStyleClass().add("section-header");
            headerLabel.setPadding(new Insets(0, 0, 10, 0));
            contentArea.getChildren().add(headerLabel);

            // Create tabbed layout for different booking statuses
            TabPane bookingTabs = new TabPane();
            bookingTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Load all bookings
            ObservableList<Booking> allBookings = bookingService.getAllBookings();

            // Create tabs for different statuses
            Tab allTab = new Tab("All Bookings");
            allTab.setContent(createBookingsPanel(allBookings));

            Tab pendingTab = new Tab("Pending");
            pendingTab.setContent(createBookingsPanel(filterBookingsByStatus(allBookings, "Pending")));

            Tab confirmedTab = new Tab("Confirmed");
            confirmedTab.setContent(createBookingsPanel(filterBookingsByStatus(allBookings, "Confirmed")));

            Tab cancelledTab = new Tab("Cancelled");
            cancelledTab.setContent(createBookingsPanel(filterBookingsByStatus(allBookings, "Cancelled")));

            bookingTabs.getTabs().addAll(allTab, pendingTab, confirmedTab, cancelledTab);

            // Add to content area
            contentArea.getChildren().add(bookingTabs);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Error loading Bookings view: " + e.getMessage());
        }
    }

    private ScrollPane createBookingsPanel(ObservableList<Booking> bookings) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll-pane");

        // Create container for booking cards
        VBox bookingsContainer = new VBox(15);
        bookingsContainer.setPadding(new Insets(20));

        if (bookings.isEmpty()) {
            Label emptyLabel = new Label("No bookings found");
            emptyLabel.getStyleClass().add("info-text");
            bookingsContainer.getChildren().add(emptyLabel);
        } else {
            for (Booking booking : bookings) {
                bookingsContainer.getChildren().add(createBookingCard(booking));
            }
        }

        scrollPane.setContent(bookingsContainer);
        return scrollPane;
    }

    private ObservableList<Booking> filterBookingsByStatus(ObservableList<Booking> bookings, String status) {
        return bookings.filtered(booking -> booking.getStatus().equals(status));
    }

    private HBox createBookingCard(Booking booking) {
        // Card container
        HBox card = new HBox(15);
        card.getStyleClass().add("booking-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);

        // Status indicator
        Rectangle statusIndicator = new Rectangle(8, 80);
        statusIndicator.getStyleClass().add("status-" + booking.getStatus().toLowerCase() + "-bg");

        // Booking details
        VBox details = new VBox(5);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Label bookingIdLabel = new Label("Booking #" + booking.getBookingId());
        bookingIdLabel.getStyleClass().add("card-id");

        Label voyageLabel = new Label(booking.getVoyageName());
        voyageLabel.getStyleClass().add("card-title");

        Label userLabel = new Label("Customer: " + booking.getUserName());
        userLabel.getStyleClass().add("card-text");

        HBox dateInfo = new HBox(20);
        Label bookingDateLabel = new Label("Booked: " + booking.getBookingDate().format(dateFormatter));
        bookingDateLabel.getStyleClass().add("card-text");
        Label statusLabel = new Label(booking.getStatus());
        statusLabel.getStyleClass().add("status-" + booking.getStatus().toLowerCase());

        dateInfo.getChildren().addAll(bookingDateLabel, statusLabel);
        details.getChildren().addAll(bookingIdLabel, voyageLabel, userLabel, dateInfo);

        // Actions panel
        VBox actionsPanel = new VBox(10);
        actionsPanel.setAlignment(Pos.CENTER_RIGHT);

        // Different buttons based on status
        if ("Pending".equals(booking.getStatus())) {
            Button confirmButton = createActionButton("Confirm", "fas-check", e -> handleConfirmBooking(booking));
            confirmButton.getStyleClass().add("confirm-button");

            Button cancelButton = createActionButton("Cancel", "fas-times", e -> handleCancelBooking(booking));
            cancelButton.getStyleClass().add("cancel-button");

            actionsPanel.getChildren().addAll(confirmButton, cancelButton);
        } else if ("Confirmed".equals(booking.getStatus())) {
            Button cancelButton = createActionButton("Cancel", "fas-times", e -> handleCancelBooking(booking));
            cancelButton.getStyleClass().add("cancel-button");

            actionsPanel.getChildren().add(cancelButton);
        }

        Button viewButton = createActionButton("Details", "fas-info-circle", e -> handleViewBookingDetails(booking));
        actionsPanel.getChildren().add(viewButton);

        // Add all components to card
        card.getChildren().addAll(statusIndicator, details, actionsPanel);

        return card;
    }

    private HBox createSectionHeader(String title, String iconLiteral, String buttonText,
            javafx.event.EventHandler<ActionEvent> onAction) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 10, 20));

        Label headerLabel = new Label(title);
        headerLabel.getStyleClass().add("section-header");
        HBox.setHgrow(headerLabel, javafx.scene.layout.Priority.ALWAYS);

        Button addButton = new Button(buttonText);
        addButton.getStyleClass().add("primary-button");
        FontIcon addIcon = new FontIcon(iconLiteral);
        addIcon.setIconSize(16);
        addButton.setGraphic(addIcon);
        addButton.setOnAction(onAction);

        header.getChildren().addAll(headerLabel, addButton);
        return header;
    }

    private Button createIconButton(String iconLiteral, String tooltip) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(16);
        button.setGraphic(icon);

        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));

        return button;
    }

    private Button createActionButton(String text, String iconLiteral,
            javafx.event.EventHandler<ActionEvent> onAction) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(16);
        button.setGraphic(icon);
        button.setOnAction(onAction);

        return button;
    }

    // Handler methods for different actions
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/user/backoffice/userForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh users list after adding
            handleUsersButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open user form: " + e.getMessage());
        }
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/user/backoffice/userForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setUserForEdit(user);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh users list after editing
            handleUsersButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open user form: " + e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete User");
        confirmation.setContentText("Are you sure you want to delete user: " + user.getFullName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = userService.deleteUser(user.getUserId());
                if (success) {
                    // Refresh users list after deleting
                    handleUsersButton(null);
                } else {
                    showErrorAlert("Error", "Failed to delete user. The user may have related bookings.");
                }
            }
        });
    }

    private void handleAddVoyage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/voyageForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Voyage");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh voyages list after adding
            handleVoyagesButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open voyage form: " + e.getMessage());
        }
    }

    private void handleEditVoyage(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/voyageForm.fxml"));
            Parent root = loader.load();

            VoyageFormController controller = loader.getController();
            controller.setVoyageForEdit(voyage);

            Stage stage = new Stage();
            stage.setTitle("Edit Voyage");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh voyages list after editing
            handleVoyagesButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open voyage form: " + e.getMessage());
        }
    }

    private void handleDeleteVoyage(Voyage voyage) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Voyage");
        confirmation.setContentText("Are you sure you want to delete voyage: " + voyage.getDestination() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = voyageService.deleteVoyage(voyage.getVoyageId());
                if (success) {
                    // Refresh voyages list after deleting
                    handleVoyagesButton(null);
                } else {
                    showErrorAlert("Error", "Failed to delete voyage. The voyage may have related bookings or guides.");
                }
            }
        });
    }

    private void handleAddGuide() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/guideForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Guide");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh guides list after adding
            handleGuidesButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open guide form: " + e.getMessage());
        }
    }

    private void handleEditGuide(Guide guide) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/voyage/backoffice/guideForm.fxml"));
            Parent root = loader.load();

            GuideFormController controller = loader.getController();
            controller.setGuideForEdit(guide);

            Stage stage = new Stage();
            stage.setTitle("Edit Guide");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh guides list after editing
            handleGuidesButton(null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open guide form: " + e.getMessage());
        }
    }

    private void handleDeleteGuide(Guide guide) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Guide");
        confirmation.setContentText("Are you sure you want to delete guide: " + guide.getGuideName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = guideService.deleteGuide(guide.getGuideId());
                if (success) {
                    // Refresh guides list after deleting
                    handleGuidesButton(null);
                } else {
                    showErrorAlert("Error", "Failed to delete guide.");
                }
            }
        });
    }

    private void handleConfirmBooking(Booking booking) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Booking");
        confirmation.setHeaderText("Confirm Status Change");
        confirmation.setContentText("Are you sure you want to confirm this booking?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "Confirmed");
                if (success) {
                    // Refresh bookings list after updating
                    handleBookingsButton(null);
                } else {
                    showErrorAlert("Error", "Failed to update booking status.");
                }
            }
        });
    }

    private void handleCancelBooking(Booking booking) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Booking");
        confirmation.setHeaderText("Confirm Status Change");
        confirmation.setContentText("Are you sure you want to cancel this booking?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "Cancelled");
                if (success) {
                    // Refresh bookings list after updating
                    handleBookingsButton(null);
                } else {
                    showErrorAlert("Error", "Failed to update booking status.");
                }
            }
        });
    }

    private void handleViewBookingDetails(Booking booking) {
        try {
            // Get full booking details with voyage info
            Booking fullBooking = bookingService.getBookingById(booking.getBookingId());
            Voyage voyage = voyageService.getVoyageById(booking.getVoyageId());

            if (fullBooking == null || voyage == null) {
                showErrorAlert("Error", "Failed to load booking details.");
                return;
            }

            // Create dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Booking Details");
            dialog.setHeaderText("Booking #" + booking.getBookingId() + " - " + booking.getVoyageName());

            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Add booking details to grid
            int row = 0;

            grid.add(new Label("Booking ID:"), 0, row);
            grid.add(new Label(String.valueOf(fullBooking.getBookingId())), 1, row++);

            grid.add(new Label("Customer:"), 0, row);
            grid.add(new Label(fullBooking.getUserName()), 1, row++);

            grid.add(new Label("Booking Date:"), 0, row);
            grid.add(new Label(fullBooking.getBookingDate().format(dateFormatter)), 1, row++);

            grid.add(new Label("Status:"), 0, row);
            Label statusLabel = new Label(fullBooking.getStatus());
            statusLabel.getStyleClass().add("status-" + fullBooking.getStatus().toLowerCase());
            grid.add(statusLabel, 1, row++);

            // Add separator
            Separator separator = new Separator();
            separator.setPrefWidth(400);
            GridPane.setColumnSpan(separator, 2);
            grid.add(separator, 0, row++);

            // Add voyage details to grid
            grid.add(new Label("Destination:"), 0, row);
            grid.add(new Label(voyage.getDestination()), 1, row++);

            grid.add(new Label("Departure Date:"), 0, row);
            grid.add(new Label(voyage.getDepartureDate().format(dateFormatter)), 1, row++);

            grid.add(new Label("Return Date:"), 0, row);
            grid.add(new Label(voyage.getReturnDate().format(dateFormatter)), 1, row++);

            grid.add(new Label("Price:"), 0, row);
            grid.add(new Label(String.format("$%.2f", voyage.getPrice())), 1, row++);

            // Add description if available
            if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
                grid.add(new Label("Description:"), 0, row);
                Label descLabel = new Label(voyage.getDescription());
                descLabel.setWrapText(true);
                descLabel.setPrefWidth(300);
                grid.add(descLabel, 1, row++);
            }

            // Add actions buttons if status is "Pending"
            if ("Pending".equals(fullBooking.getStatus())) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button confirmButton = createActionButton("Confirm", "fas-check", e -> {
                    dialog.close();
                    handleConfirmBooking(fullBooking);
                });
                confirmButton.getStyleClass().add("confirm-button");

                Button cancelButton = createActionButton("Cancel", "fas-times", e -> {
                    dialog.close();
                    handleCancelBooking(fullBooking);
                });
                cancelButton.getStyleClass().add("cancel-button");

                actions.getChildren().addAll(confirmButton, cancelButton);

                GridPane.setColumnSpan(actions, 2);
                grid.add(actions, 0, row++);
            }

            dialog.getDialogPane().setContent(grid);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to show booking details: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear the session
        SessionManager.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/voyage/user/frontoffice/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Voyage - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleUserBookingsStats(ActionEvent event) {
        pageTitle.setText("Booking Analytics");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Add header
            Label header = new Label("Booking Statistics");
            header.getStyleClass().add("section-header");
            header.setPadding(new Insets(0, 0, 20, 20));
            contentArea.getChildren().add(header);

            // Create WebView for the chart
            WebView webView = new WebView();
            webView.setPrefHeight(600);
            WebEngine webEngine = webView.getEngine();

            // Load the HTML file
            URL url = getClass().getResource("/com/example/voyage/charts/bookingsPerUser.html");
            webEngine.load(url.toExternalForm());

            // Wait for page to load before inserting data
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    // Generate data
                    List<BookingPerUser> bookingsData = generateBookingsPerUserData();

                    // Create Gson with field naming policy to handle private fields
                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                            .create();

                    String jsonData = gson.toJson(bookingsData);

                    // Call JavaScript function with data
                    webEngine.executeScript("updateChart('" + jsonData.replace("'", "\\'") + "')");
                }
            });

            // Add WebView to content area
            contentArea.getChildren().add(webView);

            // Add description and legend
            VBox explanation = new VBox(10);
            explanation.setPadding(new Insets(20));

            Label descTitle = new Label("Understanding This Chart");
            descTitle.getStyleClass().add("card-title");

            Label descText = new Label(
                    "This chart shows the total number of bookings made by each user in the system. " +
                            "Higher bars indicate users who have made more bookings. This can help identify your most active customers.");
            descText.setWrapText(true);

            explanation.getChildren().addAll(descTitle, descText);
            contentArea.getChildren().add(explanation);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load booking statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoyageStats(ActionEvent event) {
        pageTitle.setText("Voyage Analytics");
        try {
            // Clear content area
            contentArea.getChildren().clear();

            // Add header
            Label header = new Label("Voyage Statistics");
            header.getStyleClass().add("section-header");
            header.setPadding(new Insets(0, 0, 20, 20));
            contentArea.getChildren().add(header);

            // Create WebView for the chart
            WebView webView = new WebView();
            webView.setPrefHeight(800); // Taller to accommodate both charts
            WebEngine webEngine = webView.getEngine();

            // Load the HTML file
            URL url = getClass().getResource("/com/example/voyage/charts/voyageStats.html");
            webEngine.load(url.toExternalForm());

            // Wait for page to load before inserting data
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    // Generate data
                    List<GuidePerVoyage> guidesData = generateGuidesPerVoyageData();
                    List<VoyagePerPeriod> periodData = generateVoyagesPerPeriodData();

                    // Create Gson with field naming policy to handle private fields
                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                            .create();

                    String guidesJson = gson.toJson(guidesData);
                    String periodJson = gson.toJson(periodData);

                    // Call JavaScript function with data
                    webEngine.executeScript("updateCharts('" + guidesJson.replace("'", "\\'") + "', '" +
                            periodJson.replace("'", "\\'") + "')");
                }
            });

            // Add WebView to content area
            contentArea.getChildren().add(webView);

            // Add explanation
            VBox explanation = new VBox(10);
            explanation.setPadding(new Insets(20));

            Label descTitle = new Label("About These Charts");
            descTitle.getStyleClass().add("card-title");

            Label descText = new Label(
                    "The top chart shows the number of guides assigned to each voyage destination. " +
                            "The bottom chart displays the number of voyages scheduled per two-month period, " +
                            "helping identify seasonal trends and planning needs.");
            descText.setWrapText(true);

            explanation.getChildren().addAll(descTitle, descText);
            contentArea.getChildren().add(explanation);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load voyage statistics: " + e.getMessage());
        }
    }

    // Helper methods to generate data for charts
    private List<BookingPerUser> generateBookingsPerUserData() {
        List<BookingPerUser> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT u.user_id, u.full_name, COUNT(b.booking_id) as booking_count " +
                                "FROM user u LEFT JOIN booking b ON u.user_id = b.user_id " +
                                "GROUP BY u.user_id, u.full_name " +
                                "ORDER BY booking_count DESC")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                int count = rs.getInt("booking_count");

                result.add(new BookingPerUser(userId, fullName, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return sample data if query fails
            return getSampleBookingPerUserData();
        }

        return result.isEmpty() ? getSampleBookingPerUserData() : result;
    }

    private List<GuidePerVoyage> generateGuidesPerVoyageData() {
        List<GuidePerVoyage> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT v.voyage_id, v.destination, COUNT(g.guide_id) as guide_count " +
                                "FROM voyage v LEFT JOIN guide g ON v.voyage_id = g.voyage_id " +
                                "GROUP BY v.voyage_id, v.destination " +
                                "ORDER BY guide_count DESC")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int voyageId = rs.getInt("voyage_id");
                String destination = rs.getString("destination");
                int count = rs.getInt("guide_count");

                result.add(new GuidePerVoyage(voyageId, destination, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return sample data if query fails
            return getSampleGuidePerVoyageData();
        }

        return result.isEmpty() ? getSampleGuidePerVoyageData() : result;
    }

    private List<VoyagePerPeriod> generateVoyagesPerPeriodData() {
        List<VoyagePerPeriod> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT YEAR(departure_date) as year, MONTH(departure_date) as month, " +
                                "COUNT(*) as voyage_count FROM voyage " +
                                "GROUP BY YEAR(departure_date), MONTH(departure_date) " +
                                "ORDER BY year, month")) {

            ResultSet rs = stmt.executeQuery();
            Map<String, Integer> periodCounts = new HashMap<>();

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                int count = rs.getInt("voyage_count");

                // Create two-month periods (Jan-Feb, Mar-Apr, etc.)
                int periodMonth = ((month - 1) / 2) * 2 + 1; // Convert to odd month (1, 3, 5, 7, 9, 11)
                YearMonth ym = YearMonth.of(year, periodMonth);
                String period = ym.format(formatter) + "/" + ym.plusMonths(1).format(formatter);

                // Add to period counts
                periodCounts.put(period, periodCounts.getOrDefault(period, 0) + count);
            }

            // Convert map to list
            for (Map.Entry<String, Integer> entry : periodCounts.entrySet()) {
                result.add(new VoyagePerPeriod(entry.getKey(), entry.getValue()));
            }

            // Sort by period
            result.sort((a, b) -> a.getPeriod().compareTo(b.getPeriod()));

        } catch (SQLException e) {
            e.printStackTrace();
            // Return sample data if query fails
            return getSampleVoyagePerPeriodData();
        }

        return result.isEmpty() ? getSampleVoyagePerPeriodData() : result;
    }

    // Sample data generators for when database queries fail or return empty results
    private List<BookingPerUser> getSampleBookingPerUserData() {
        return Arrays.asList(
                new BookingPerUser(1, "John Smith", 5),
                new BookingPerUser(2, "Emma Johnson", 3),
                new BookingPerUser(3, "Michael Brown", 7),
                new BookingPerUser(4, "Sophia Davis", 2),
                new BookingPerUser(5, "James Wilson", 4));
    }

    private List<GuidePerVoyage> getSampleGuidePerVoyageData() {
        return Arrays.asList(
                new GuidePerVoyage(1, "Paris", 2),
                new GuidePerVoyage(2, "Tokyo", 3),
                new GuidePerVoyage(3, "New York", 1),
                new GuidePerVoyage(4, "Rome", 2),
                new GuidePerVoyage(5, "Cairo", 1));
    }

    private List<VoyagePerPeriod> getSampleVoyagePerPeriodData() {
        return Arrays.asList(
                new VoyagePerPeriod("Jan-2023/Feb-2023", 3),
                new VoyagePerPeriod("Mar-2023/Apr-2023", 5),
                new VoyagePerPeriod("May-2023/Jun-2023", 8),
                new VoyagePerPeriod("Jul-2023/Aug-2023", 12),
                new VoyagePerPeriod("Sep-2023/Oct-2023", 7),
                new VoyagePerPeriod("Nov-2023/Dec-2023", 4));
    }
}