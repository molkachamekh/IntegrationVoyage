package com.example.voyage.info;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;

import com.example.voyage.user.models.BookingPerUser;
import com.example.voyage.voyage.models.GuidePerVoyage;
import com.example.voyage.voyage.models.VoyagePerPeriod;
import com.example.voyage.voyage.models.VoyageStatistics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides static methods for generating information and statistics
 * views
 */
public class InfoModule {

    private InfoModule() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create a WebView for the bookings per user chart
     * 
     * @return Node containing the chart
     */
    public static Node createBookingsPerUserChart() {
        try {
            WebView webView = new WebView();
            webView.setPrefHeight(600);
            WebEngine webEngine = webView.getEngine();

            // Load HTML file
            URL url = InfoModule.class.getResource("/com/example/voyage/charts/bookingsPerUser.html");
            webEngine.load(url.toExternalForm());

            // Prepare data
            List<BookingPerUser> bookingsData = BookingPerUser.getBookingsPerUserData();
            Gson gson = new GsonBuilder().create();
            String jsonData = gson.toJson(bookingsData);

            // Wait for page load then update chart
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Fix potential JSON escaping issues
                    String escapedJson = jsonData.replace("\\", "\\\\").replace("'", "\\'");
                    webEngine.executeScript("updateChart('" + escapedJson + "')");
                }
            });

            return webView;
        } catch (Exception e) {
            e.printStackTrace();
            return new VBox(); // Empty container if error
        }
    }

    /**
     * Create a WebView for the voyage statistics charts
     * 
     * @return Node containing the charts
     */
    public static Node createVoyageStatisticsCharts() {
        try {
            WebView webView = new WebView();
            webView.setPrefHeight(800);
            WebEngine webEngine = webView.getEngine();

            // Load HTML file
            URL url = InfoModule.class.getResource("/com/example/voyage/charts/voyageStats.html");
            webEngine.load(url.toExternalForm());

            // Prepare data
            List<GuidePerVoyage> guidesData = VoyageStatistics.getGuidesPerVoyageData();
            List<VoyagePerPeriod> periodData = VoyageStatistics.getVoyagesPerPeriodData();

            Gson gson = new GsonBuilder().create();
            String guidesJson = gson.toJson(guidesData);
            String periodJson = gson.toJson(periodData);

            // Wait for page load then update charts
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Fix potential JSON escaping issues
                    String escapedGuidesJson = guidesJson.replace("\\", "\\\\").replace("'", "\\'");
                    String escapedPeriodJson = periodJson.replace("\\", "\\\\").replace("'", "\\'");

                    webEngine.executeScript("updateCharts('" + escapedGuidesJson + "', '" +
                            escapedPeriodJson + "')");
                }
            });

            return webView;
        } catch (Exception e) {
            e.printStackTrace();
            return new VBox(); // Empty container if error
        }
    }
}
