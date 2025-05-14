package com.example.voyage.payment.controllers;

import com.example.voyage.payment.models.StripeCard;
import com.example.voyage.payment.services.StripeService;
import com.example.voyage.payment.util.TestCards;
import com.example.voyage.user.models.Booking;
import com.example.voyage.user.models.User;
import com.example.voyage.user.services.BookingService;
import com.example.voyage.utils.SessionManager;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class CardFormController {

    @FXML
    private Label destinationLabel;

    @FXML
    private Label datesLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private ToggleGroup paymentMethodToggle;

    @FXML
    private RadioButton newCardRadio;

    @FXML
    private RadioButton savedCardRadio;

    @FXML
    private GridPane newCardForm;

    @FXML
    private VBox savedCardForm;

    @FXML
    private TextField cardNumberField;

    @FXML
    private ComboBox<String> expiryMonthCombo;

    @FXML
    private ComboBox<String> expiryYearCombo;

    @FXML
    private TextField cvvField;

    @FXML
    private TextField cardholderNameField;

    @FXML
    private CheckBox saveCardCheckbox;

    @FXML
    private ComboBox<StripeCard> savedCardsCombo;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button payButton;

    @FXML
    private Button cancelButton;

    private StripeService stripeService = new StripeService();
    private BookingService bookingService = new BookingService();
    private Booking currentBooking;
    private User currentUser;
    private List<StripeCard> userCards;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        // Create toggle group programmatically
        ToggleGroup group = new ToggleGroup();
        newCardRadio.setToggleGroup(group);
        savedCardRadio.setToggleGroup(group);
        paymentMethodToggle = group;

        // Set up expiry month dropdown
        ObservableList<String> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        expiryMonthCombo.setItems(months);
        expiryMonthCombo.getSelectionModel().select("12"); // Default to December

        // Set up expiry year dropdown
        ObservableList<String> years = FXCollections.observableArrayList();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 20; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        expiryYearCombo.setItems(years);
        expiryYearCombo.getSelectionModel().select(0); // Default to current year

        // Set default test card number - VISA that always succeeds
        cardNumberField.setText("4242 4242 4242 4242");
        cvvField.setText("123");
        cardholderNameField.setText("Test User");

        // Setup validators
        setupValidators();

        // Add info text about test mode
        Label testModeLabel = new Label("TEST MODE: Using card number 4242 4242 4242 4242 will always succeed.");
        testModeLabel.getStyleClass().add("test-mode-label");
        cardNumberField.setTooltip(new Tooltip(
                "For testing, use:\n" +
                        "4242 4242 4242 4242 - Always succeeds\n" +
                        "4000 0000 0000 0002 - Always declined\n" +
                        "See more at stripe.com/docs/testing"));

        // Get current user
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Load saved cards
            loadUserCards();
        } else {
            savedCardRadio.setDisable(true);
        }

        // Initialize with new card form visible
        handlePaymentMethodChange();
    }

    public void setBooking(Booking booking) {
        this.currentBooking = booking;

        // Populate booking information
        if (booking != null) {
            destinationLabel.setText(booking.getVoyageName());
            datesLabel.setText(booking.getFormattedDates());
            amountLabel.setText(String.format("$%.2f", booking.getTotalAmount()));
        }
    }

    private void loadUserCards() {
        if (currentUser == null)
            return;

        // Show loading state
        savedCardRadio.setDisable(true);
        savedCardRadio.setText("Loading saved cards...");

        // Load cards in background
        CompletableFuture.supplyAsync(() -> stripeService.getUserCards(currentUser.getUserId()))
                .thenAccept(cards -> {
                    Platform.runLater(() -> {
                        userCards = cards;

                        if (cards.isEmpty()) {
                            savedCardRadio.setDisable(true);
                            savedCardRadio.setText("No saved cards");
                        } else {
                            savedCardRadio.setDisable(false);
                            savedCardRadio.setText("Use saved card (" + cards.size() + ")");

                            ObservableList<StripeCard> cardItems = FXCollections.observableArrayList(cards);
                            savedCardsCombo.setItems(cardItems);
                            savedCardsCombo.getSelectionModel().selectFirst();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        savedCardRadio.setDisable(true);
                        savedCardRadio.setText("Failed to load cards");
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void handlePaymentMethodChange() {
        if (newCardRadio.isSelected()) {
            newCardForm.setVisible(true);
            newCardForm.setManaged(true);
            savedCardForm.setVisible(false);
            savedCardForm.setManaged(false);
        } else {
            newCardForm.setVisible(false);
            newCardForm.setManaged(false);
            savedCardForm.setVisible(true);
            savedCardForm.setManaged(true);
        }
    }

    private void setupValidators() {
        // Add input formatters and validators
        cardNumberField.textProperty().addListener((obs, oldText, newText) -> {
            // Only allow digits and spaces, max 19 chars (16 digits + 3 spaces)
            if (newText.length() > 19) {
                cardNumberField.setText(oldText);
            } else if (!newText.matches("[0-9 ]*")) {
                cardNumberField.setText(newText.replaceAll("[^0-9 ]", ""));
            }

            // Format as 4-digit groups
            if (newText.length() > 0 && !newText.endsWith(" ")) {
                String cleaned = newText.replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < cleaned.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(cleaned.charAt(i));
                }
                if (!formatted.toString().equals(newText)) {
                    cardNumberField.setText(formatted.toString());
                }
            }
        });

        cvvField.textProperty().addListener((obs, oldText, newText) -> {
            // Only allow 3-4 digits
            if (newText.length() > 4) {
                cvvField.setText(oldText);
            } else if (!newText.matches("[0-9]*")) {
                cvvField.setText(newText.replaceAll("[^0-9]", ""));
            }
        });

        cardholderNameField.textProperty().addListener((obs, oldText, newText) -> {
            // Only allow letters and spaces
            if (!newText.matches("[a-zA-Z ]*")) {
                cardholderNameField.setText(newText.replaceAll("[^a-zA-Z ]", ""));
            }
        });
    }

    @FXML
    private void handlePayment() {
        // Clear previous errors
        errorMessageLabel.setVisible(false);

        if (currentBooking == null) {
            showError("Booking information is missing");
            return;
        }

        if (currentUser == null) {
            showError("You must be logged in to make a payment");
            return;
        }

        // Show processing state
        payButton.setDisable(true);
        payButton.setText("Processing...");

        try {
            if (newCardRadio.isSelected()) {
                processNewCardPayment();
            } else {
                processSavedCardPayment();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred: " + e.getMessage());
            payButton.setDisable(false);
            payButton.setText("Make Payment");
        }
    }

    private void processNewCardPayment() {
        // Validate all fields
        if (!validateNewCardFields()) {
            payButton.setDisable(false);
            payButton.setText("Make Payment");
            return;
        }

        // Clean card number (remove spaces)
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        int expMonth = Integer.parseInt(expiryMonthCombo.getValue());
        int expYear = Integer.parseInt(expiryYearCombo.getValue());
        String cvv = cvvField.getText();

        // Show processing state
        payButton.setDisable(true);
        payButton.setText("Processing...");
        errorMessageLabel.setVisible(false);

        // Process payment in background
        CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Creating payment method with card number: " +
                        maskCardNumber(cardNumber) + " (" + TestCards.getCardDescription(cardNumber) + ")");

                // Create payment method in Stripe
                PaymentMethod paymentMethod = stripeService.createPaymentMethod(
                        cardNumber, expMonth, expYear, cvv);

                if (paymentMethod == null) {
                    throw new RuntimeException("Failed to create payment method - returned null");
                }

                System.out.println("Payment method created: " + paymentMethod.getId());

                // Create payment intent
                long amountInCents = Math.round(currentBooking.getTotalAmount() * 100);

                System.out.println("Creating payment intent for amount: " + amountInCents + " cents");

                PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                        amountInCents,
                        "USD",
                        paymentMethod.getId(),
                        "Booking #" + currentBooking.getBookingId() + " - " + currentBooking.getVoyageName());

                // If payment is successful, save card if requested
                if ("succeeded".equals(paymentIntent.getStatus()) && saveCardCheckbox.isSelected()) {
                    StripeCard card = new StripeCard(
                            currentUser.getUserId(),
                            paymentMethod.getId(),
                            paymentMethod.getCard().getBrand(),
                            paymentMethod.getCard().getLast4(),
                            expMonth,
                            expYear);
                    stripeService.saveCard(card);
                    System.out.println("Card saved successfully");
                }

                return paymentIntent;
            } catch (StripeException e) {
                System.err.println("Stripe exception: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Stripe payment error: " + e.getUserMessage(), e);
            } catch (Exception e) {
                System.err.println("Payment processing error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
            }
        })
                .thenAccept(paymentIntent -> {
                    Platform.runLater(() -> {
                        if (paymentIntent != null && "succeeded".equals(paymentIntent.getStatus())) {
                            // Update booking status
                            bookingService.updateBookingStatus(currentBooking.getBookingId(), "Confirmed");

                            // No longer update is_paid since that column doesn't exist
                            // bookingService.updatePaymentStatus(currentBooking.getBookingId(), true);

                            // Show success and close dialog
                            showSuccessAndClose();
                        } else {
                            String status = paymentIntent != null ? paymentIntent.getStatus() : "unknown";
                            showError("Payment failed: " + status);
                            payButton.setDisable(false);
                            payButton.setText("Make Payment");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause();
                        if (cause instanceof StripeException) {
                            showError("Payment error: " + ((StripeException) cause).getUserMessage());
                        } else {
                            showError("Payment failed: " + ex.getMessage());
                        }
                        payButton.setDisable(false);
                        payButton.setText("Make Payment");
                    });
                    return null;
                });
    }

    // Helper method to mask card number for logging
    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() > 4) {
            String last4 = cardNumber.substring(cardNumber.length() - 4);
            return "xxxx-xxxx-xxxx-" + last4;
        }
        return "xxxx-xxxx-xxxx-xxxx";
    }

    private void processSavedCardPayment() {
        // Check if a card is selected
        StripeCard selectedCard = savedCardsCombo.getValue();
        if (selectedCard == null) {
            showError("Please select a card");
            payButton.setDisable(false);
            payButton.setText("Make Payment");
            return;
        }

        // Process payment in background
        CompletableFuture.supplyAsync(() -> {
            try {
                // Create payment intent with saved payment method
                long amountInCents = Math.round(currentBooking.getTotalAmount() * 100);
                PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                        amountInCents,
                        "USD",
                        selectedCard.getStripePaymentMethodId(),
                        "Booking #" + currentBooking.getBookingId() + " - " + currentBooking.getVoyageName());

                return paymentIntent;
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }
        })
                .thenAccept(paymentIntent -> {
                    Platform.runLater(() -> {
                        if ("succeeded".equals(paymentIntent.getStatus())) {
                            // Update booking status only, don't try to update is_paid
                            bookingService.updateBookingStatus(currentBooking.getBookingId(), "Confirmed");

                            // Show success and close dialog
                            showSuccessAndClose();
                        } else {
                            showError("Payment failed: " + paymentIntent.getStatus());
                            payButton.setDisable(false);
                            payButton.setText("Make Payment");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause();
                        if (cause instanceof StripeException) {
                            showError("Payment error: " + ((StripeException) cause).getUserMessage());
                        } else {
                            showError("Payment failed: " + ex.getMessage());
                        }
                        payButton.setDisable(false);
                        payButton.setText("Make Payment");
                    });
                    return null;
                });
    }

    private boolean validateNewCardFields() {
        // Card number validation (Luhn algorithm not implemented for simplicity)
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        if (cardNumber.isEmpty() || cardNumber.length() < 13 || cardNumber.length() > 19) {
            showError("Please enter a valid card number");
            return false;
        }

        // Expiry date validation
        if (expiryMonthCombo.getValue() == null || expiryYearCombo.getValue() == null) {
            showError("Please select expiry date");
            return false;
        }

        // Check if card is expired
        int selectedMonth = Integer.parseInt(expiryMonthCombo.getValue());
        int selectedYear = Integer.parseInt(expiryYearCombo.getValue());
        LocalDate currentDate = LocalDate.now();
        if (selectedYear < currentDate.getYear() ||
                (selectedYear == currentDate.getYear() && selectedMonth < currentDate.getMonthValue())) {
            showError("Card is expired");
            return false;
        }

        // CVV validation
        if (cvvField.getText().isEmpty() || cvvField.getText().length() < 3) {
            showError("Please enter a valid CVV");
            return false;
        }

        // Cardholder name validation
        if (cardholderNameField.getText().trim().isEmpty()) {
            showError("Please enter the cardholder name");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }

    private void showSuccessAndClose() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText("Your payment was successful!");
        alert.setContentText("Your booking is now confirmed. Thank you for your payment.");
        alert.showAndWait();

        // Close the dialog
        ((Stage) payButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
}
