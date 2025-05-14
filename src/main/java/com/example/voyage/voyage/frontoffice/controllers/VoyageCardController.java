package com.example.voyage.voyage.frontoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class VoyageCardController {
    @FXML
    private Label destinationLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label guideLabel;

    @FXML
    private Label languagesLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private ImageView voyageImage;

    @FXML
    private Button bookNowButton;

    @FXML
    public void initialize() {
        // Initialize component properties if needed
    }

    public void setVoyageData(String destination, String description, String guideName, String languages,
            double price) {
        destinationLabel.setText(destination);
        descriptionLabel.setText(description);
        guideLabel.setText("Guide: " + guideName);
        languagesLabel.setText("Languages: " + languages);
        priceLabel.setText(String.format("$%.2f", price));

        // In a real app, you would also load an image from a URL or resource
        // voyageImage.setImage(new Image(...));
    }

    public void setOnBookNowAction(EventHandler<ActionEvent> handler) {
        bookNowButton.setOnAction(handler);
    }
}
