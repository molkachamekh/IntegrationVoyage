package com.example.voyage.payment.services;

import com.example.voyage.payment.models.StripeCard;
import com.example.voyage.utils.DatabaseConnection;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Token;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StripeService {


    // Initialize Stripe API key in a static block
    static {
        try {
            Stripe.apiKey = STRIPE_SECRET_KEY;
            System.out.println("Stripe API initialized with version: " + Stripe.API_VERSION);
        } catch (Exception e) {
            System.err.println("Failed to initialize Stripe API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a test token based on card number
     * Instead of sending raw card data, we use predefined test tokens
     * See https://stripe.com/docs/testing for more test card numbers
     * 
     * @param cardNumber The card number to get a token for
     * @return A test token string
     */
    private String getTestToken(String cardNumber) {
        // Remove spaces
        cardNumber = cardNumber.replaceAll("\\s", "");

        // Map common test card numbers to their tokens
        switch (cardNumber) {
            case "4242424242424242":
                return "tok_visa"; // Always succeeds
            case "4000000000000002":
                return "tok_visa_declined"; // Always declined
            case "4000000000009995":
                return "tok_visa_insufficient_funds"; // Insufficient funds decline
            case "4000000000000341":
                return "tok_visa_fraudulent"; // Fraudulent decline
            default:
                // Default to a successful token for other numbers in test mode
                return "tok_visa";
        }
    }

    /**
     * Create a payment method using a test token
     * This avoids sending raw card data to Stripe API
     * 
     * @param cardNumber Card number (will be converted to a token)
     * @param expMonth   Expiry month
     * @param expYear    Expiry year
     * @param cvc        CVC code
     * @return PaymentMethod object from Stripe
     * @throws StripeException if Stripe API call fails
     */
    public PaymentMethod createPaymentMethod(String cardNumber, int expMonth, int expYear, String cvc)
            throws StripeException {
        try {
            // Clean the card number and get appropriate test token
            String token = getTestToken(cardNumber.replaceAll("\\s", ""));
            System.out.println("Using test token: " + token + " for card ending in " +
                    cardNumber.replaceAll("\\s", "").substring(Math.max(0, cardNumber.length() - 4)));

            // Create payment method using token
            Map<String, Object> card = new HashMap<>();
            card.put("token", token);

            Map<String, Object> params = new HashMap<>();
            params.put("type", "card");
            params.put("card", card);

            return PaymentMethod.create(params);
        } catch (StripeException e) {
            System.err.println("Stripe error creating payment method: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating payment method: " + e.getMessage());
            throw new RuntimeException("Failed to create payment method: " + e.getMessage(), e);
        }
    }

    /**
     * Create a payment intent in Stripe - Using Test Mode
     * In test mode, we'll simulate a successful payment without actual API calls
     * 
     * @param amount          Amount to charge in cents (e.g. $10.00 = 1000)
     * @param currency        Currency code (e.g. "USD")
     * @param paymentMethodId ID of the payment method to use
     * @param description     Description of the payment
     * @return PaymentIntent object from Stripe
     * @throws StripeException if Stripe API call fails
     */
    public PaymentIntent createPaymentIntent(long amount, String currency, String paymentMethodId, String description)
            throws StripeException {
        try {
            System.out.println("Creating payment intent for amount: " + amount + " " + currency +
                    " with payment method: " + paymentMethodId);

            // Create the PaymentIntent with the correct parameters to avoid redirect issues
            Map<String, Object> automaticPaymentMethods = new HashMap<>();
            automaticPaymentMethods.put("enabled", true);
            automaticPaymentMethods.put("allow_redirects", "never");

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", currency);
            params.put("payment_method", paymentMethodId);
            params.put("description", description);
            params.put("confirm", true);
            params.put("automatic_payment_methods", automaticPaymentMethods);

            // Create the payment intent with the provided payment method
            PaymentIntent intent = PaymentIntent.create(params);

            System.out.println("Payment intent created with status: " + intent.getStatus());
            return intent;
        } catch (StripeException e) {
            System.err.println("Stripe error creating payment intent: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating payment intent: " + e.getMessage());
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Save a card to the database
     * 
     * @param card StripeCard object with card details
     * @return true if successful, false otherwise
     */
    public boolean saveCard(StripeCard card) {
        String query = "INSERT INTO stripe_cards (user_id, stripe_payment_method_id, card_type, card_last4, " +
                "card_expiry_month, card_expiry_year) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, card.getUserId());
            stmt.setString(2, card.getStripePaymentMethodId());
            stmt.setString(3, card.getCardType());
            stmt.setString(4, card.getCardLast4());
            stmt.setInt(5, card.getCardExpiryMonth());
            stmt.setInt(6, card.getCardExpiryYear());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        card.setCardId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error saving card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all saved cards for a user
     * 
     * @param userId User ID
     * @return List of StripeCard objects
     */
    public List<StripeCard> getUserCards(int userId) {
        List<StripeCard> cards = new ArrayList<>();
        String query = "SELECT * FROM stripe_cards WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StripeCard card = new StripeCard();
                card.setCardId(rs.getInt("card_id"));
                card.setUserId(rs.getInt("user_id"));
                card.setStripePaymentMethodId(rs.getString("stripe_payment_method_id"));
                card.setCardType(rs.getString("card_type"));
                card.setCardLast4(rs.getString("card_last4"));
                card.setCardExpiryMonth(rs.getInt("card_expiry_month"));
                card.setCardExpiryYear(rs.getInt("card_expiry_year"));
                card.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cards;
    }

    /**
     * Delete a saved card
     * 
     * @param cardId Card ID
     * @return true if successful, false otherwise
     */
    public boolean deleteCard(int cardId) {
        String query = "DELETE FROM stripe_cards WHERE card_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cardId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
