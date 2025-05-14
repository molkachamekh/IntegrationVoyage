package com.example.voyage.voyage.models;

public class VoyagePerPeriod {
    private String period;
    private int count;

    public VoyagePerPeriod(String period, int count) {
        this.period = period;
        this.count = count;
    }

    public String getPeriod() {
        return period;
    }

    public int getCount() {
        return count;
    }
}
