package com.example.voyage.voyage.models;

import com.example.voyage.utils.ImageUtils;
import java.time.LocalDate;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * Model class representing a voyage
 */
public class Voyage {
    private final IntegerProperty voyageId = new SimpleIntegerProperty();
    private final StringProperty destination = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> departureDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> returnDate = new SimpleObjectProperty<>();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final StringProperty imagePath = new SimpleStringProperty(); // Stores Base64 image data

    // Default constructor
    public Voyage() {
    }

    // Constructor for new voyages
    public Voyage(String destination, LocalDate departureDate, LocalDate returnDate,
            double price, String description, String imagePath) {
        this.destination.set(destination);
        this.departureDate.set(departureDate);
        this.returnDate.set(returnDate);
        this.price.set(price);
        this.description.set(description);
        this.imagePath.set(imagePath);
        loadImage();
    }

    // Constructor for existing voyages from database
    public Voyage(int voyageId, String destination, LocalDate departureDate, LocalDate returnDate,
            double price, String description, String imagePath) {
        this.voyageId.set(voyageId);
        this.destination.set(destination);
        this.departureDate.set(departureDate);
        this.returnDate.set(returnDate);
        this.price.set(price);
        this.description.set(description);
        this.imagePath.set(imagePath);
        loadImage();
    }

    // Getters and setters
    public int getVoyageId() {
        return voyageId.get();
    }

    public IntegerProperty voyageIdProperty() {
        return voyageId;
    }

    public void setVoyageId(int voyageId) {
        this.voyageId.set(voyageId);
    }

    public String getDestination() {
        return destination.get();
    }

    public StringProperty destinationProperty() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination.set(destination);
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

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
        loadImage();
    }

    /**
     * Load image from Base64 string
     */
    public void loadImage() {
        if (imagePath.get() != null && !imagePath.get().isEmpty()) {
            try {
                // Check if imagePath is a Base64 string
                String base64String = imagePath.get();
                // Remove data URL prefix if present
                if (base64String.contains(",")) {
                    base64String = base64String.split(",")[1];
                }

                try {
                    // Decode and create image
                    byte[] imageData = Base64.getDecoder().decode(base64String);
                    image.set(new Image(new ByteArrayInputStream(imageData)));
                } catch (IllegalArgumentException e) {
                    // Not a valid Base64 string, try as URL
                    System.err.println("Not a valid Base64 string, trying as URL: " + e.getMessage());
                    try {
                        Image img = new Image(imagePath.get());
                        if (!img.isError()) {
                            image.set(img);
                        }
                    } catch (Exception urlEx) {
                        System.err.println("Error loading image as URL: " + urlEx.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return destination.get();
    }
}
