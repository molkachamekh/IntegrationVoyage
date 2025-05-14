package com.example.voyage.voyage.models;

public class GuidePerVoyage {
    private int voyageId;
    private String destination;
    private int guideCount;

    public GuidePerVoyage(int voyageId, String destination, int guideCount) {
        this.voyageId = voyageId;
        this.destination = destination;
        this.guideCount = guideCount;
    }

    public int getVoyageId() {
        return voyageId;
    }

    public String getDestination() {
        return destination;
    }

    public int getGuideCount() {
        return guideCount;
    }
}
