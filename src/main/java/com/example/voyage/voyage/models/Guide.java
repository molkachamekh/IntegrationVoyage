package com.example.voyage.voyage.models;

import javafx.beans.property.*;

/**
 * Model class representing a guide
 */
public class Guide {
    private final IntegerProperty guideId = new SimpleIntegerProperty();
    private final IntegerProperty voyageId = new SimpleIntegerProperty();
    private final StringProperty guideName = new SimpleStringProperty();
    private final StringProperty contactInfo = new SimpleStringProperty();
    private final StringProperty languages = new SimpleStringProperty();
    private StringProperty voyageName = new SimpleStringProperty(); // For UI display

    // Default constructor
    public Guide() {
    }

    // Parameterized constructor
    public Guide(int voyageId, String guideName, String contactInfo, String languages) {
        this.voyageId.set(voyageId);
        this.guideName.set(guideName);
        this.contactInfo.set(contactInfo);
        this.languages.set(languages);
    }

    // Constructor with guideId for database retrieval
    public Guide(int guideId, int voyageId, String guideName, String contactInfo, String languages) {
        this.guideId.set(guideId);
        this.voyageId.set(voyageId);
        this.guideName.set(guideName);
        this.contactInfo.set(contactInfo);
        this.languages.set(languages);
    }

    // Constructor with voyage name for UI display
    public Guide(int guideId, int voyageId, String guideName, String contactInfo,
            String languages, String voyageName) {
        this.guideId.set(guideId);
        this.voyageId.set(voyageId);
        this.guideName.set(guideName);
        this.contactInfo.set(contactInfo);
        this.languages.set(languages);
        this.voyageName.set(voyageName);
    }

    // Getters and setters
    public int getGuideId() {
        return guideId.get();
    }

    public IntegerProperty guideIdProperty() {
        return guideId;
    }

    public void setGuideId(int guideId) {
        this.guideId.set(guideId);
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

    public String getGuideName() {
        return guideName.get();
    }

    public StringProperty guideNameProperty() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName.set(guideName);
    }

    public String getContactInfo() {
        return contactInfo.get();
    }

    public StringProperty contactInfoProperty() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo.set(contactInfo);
    }

    public String getLanguages() {
        return languages.get();
    }

    public StringProperty languagesProperty() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages.set(languages);
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

    @Override
    public String toString() {
        return guideName.get();
    }
}
