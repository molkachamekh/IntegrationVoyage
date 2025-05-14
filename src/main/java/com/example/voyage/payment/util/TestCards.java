package com.example.voyage.payment.util;

/**
 * Utility class with test card numbers for Stripe payment processing
 * For more information, see: https://stripe.com/docs/testing
 */
public class TestCards {
    // Cards that work
    public static final String VISA_SUCCESS = "4242 4242 4242 4242";
    public static final String MASTERCARD_SUCCESS = "5555 5555 5555 4444";
    public static final String AMEX_SUCCESS = "3782 822463 10005";

    // Cards with specific error conditions
    public static final String VISA_DECLINED = "4000 0000 0000 0002";
    public static final String VISA_INSUFFICIENT_FUNDS = "4000 0000 0000 9995";
    public static final String VISA_LOST_CARD = "4000 0000 0000 9987";
    public static final String VISA_STOLEN_CARD = "4000 0000 0000 9979";

    // Cards that require authentication (not handled in this implementation)
    public static final String VISA_3DSECURE = "4000 0000 0000 3220";

    /**
     * Get a helpful description of the card for display purposes
     */
    public static String getCardDescription(String cardNumber) {
        cardNumber = cardNumber.replaceAll("\\s", "");

        switch (cardNumber) {
            case "4242424242424242":
                return "Valid Visa (Success)";
            case "5555555555554444":
                return "Valid Mastercard (Success)";
            case "378282246310005":
                return "Valid American Express (Success)";
            case "4000000000000002":
                return "Declined Visa";
            case "4000000000009995":
                return "Insufficient Funds Visa";
            case "4000000000009987":
                return "Lost Card Visa";
            case "4000000000009979":
                return "Stolen Card Visa";
            case "4000000000003220":
                return "3D Secure Authentication Visa";
            default:
                return "Test Card";
        }
    }
}
