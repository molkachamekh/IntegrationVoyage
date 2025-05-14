package com.example.voyage.user.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.*;

/**
 * Model class representing a booking
 */
public class Booking {
    private final IntegerProperty bookingId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty voyageId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> bookingDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();

    // Additional properties for UI display
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty voyageName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> departureDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> returnDate = new SimpleObjectProperty<>();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final BooleanProperty isPaid = new SimpleBooleanProperty();

    // Default constructor
    public Booking() {
        this.bookingDate.set(LocalDate.now());
        this.status.set("Pending");
    }

    // Parameterized constructor for creating new bookings
    public Booking(int userId, int voyageId, LocalDate bookingDate, String status) {
        this.userId.set(userId);
        this.voyageId.set(voyageId);
        this.bookingDate.set(bookingDate);
        this.status.set(status);
    }

    // Constructor with bookingId for database retrieval
    public Booking(int bookingId, int userId, int voyageId, LocalDate bookingDate, String status) {
        this.bookingId.set(bookingId);
        this.userId.set(userId);
        this.voyageId.set(voyageId);
        this.bookingDate.set(bookingDate);
        this.status.set(status);
    }

    // Constructor with additional display information
    public Booking(int bookingId, int userId, int voyageId, LocalDate bookingDate,
            String status, String userName, String voyageName) {
        this.bookingId.set(bookingId);
        this.userId.set(userId);
        this.voyageId.set(voyageId);
        this.bookingDate.set(bookingDate);
        this.status.set(status);
        this.userName.set(userName);
        this.voyageName.set(voyageName);
    }

    // Getters and setters
    public int getBookingId() {
        return bookingId.get();
    }

    public IntegerProperty bookingIdProperty() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId.set(bookingId);
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public int getVoyageId() {
        return voyageId.get();
    }

    public IntegerProperty voyageIdProperty() {
        return voyageId;
    }

    public void setVoyageId(int voyageId) {
        this.voyageId.set(voyageId);
    }

    public LocalDate getBookingDate() {
        return bookingDate.get();
    }

    public ObjectProperty<LocalDate> bookingDateProperty() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate.set(bookingDate);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getUserName() {
        return userName.get();
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getVoyageName() {
        return voyageName.get();
    }

    public StringProperty voyageNameProperty() {
        return voyageName;
    }

    public void setVoyageName(String voyageName) {
        this.voyageName.set(voyageName);
    }

    public LocalDate getDepartureDate() {
        return departureDate.get();
    }

    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate.set(departureDate);
    }

    public LocalDate getReturnDate() {
        return returnDate.get();
    }

    public ObjectProperty<LocalDate> returnDateProperty() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate.set(returnDate);
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public boolean isPaid() {
        return isPaid.get();
    }

    public BooleanProperty isPaidProperty() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid.set(isPaid);
    }

    /**
     * Get formatted dates string (departure - return)
     * 
     * @return Formatted date range string
     */
    public String getFormattedDates() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

        // If we have departure and return dates, format them
        if (departureDate.get() != null && returnDate.get() != null) {
            return departureDate.get().format(formatter) + " - " + returnDate.get().format(formatter);
        }
        // Otherwise return placeholder text
        return "Dates not available";
    }

    /**
     * Get total amount for this booking
     * 
     * @return The total cost of this booking
     */
    public double getTotalAmount() {
        return price.get(); // Fix: Added the missing semicolon here
    }
}
