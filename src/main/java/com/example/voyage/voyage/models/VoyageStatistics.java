package com.example.voyage.voyage.models;

import com.example.voyage.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for voyage statistics
 */
public class VoyageStatistics {

    private VoyageStatistics() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get data about guides per voyage from database
     * 
     * @return List of GuidePerVoyage objects
     */
    public static List<GuidePerVoyage> getGuidesPerVoyageData() {
        List<GuidePerVoyage> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT v.voyage_id, v.destination, COUNT(g.guide_id) as guide_count " +
                                "FROM voyage v LEFT JOIN guide g ON v.voyage_id = g.voyage_id " +
                                "GROUP BY v.voyage_id, v.destination " +
                                "ORDER BY guide_count DESC")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int voyageId = rs.getInt("voyage_id");
                String destination = rs.getString("destination");
                int count = rs.getInt("guide_count");

                result.add(new GuidePerVoyage(voyageId, destination, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return sample data if query fails
            return getSampleGuidePerVoyageData();
        }

        return result.isEmpty() ? getSampleGuidePerVoyageData() : result;
    }

    /**
     * Get data about voyages per period from database
     * 
     * @return List of VoyagePerPeriod objects
     */
    public static List<VoyagePerPeriod> getVoyagesPerPeriodData() {
        List<VoyagePerPeriod> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT YEAR(departure_date) as year, MONTH(departure_date) as month, " +
                                "COUNT(*) as voyage_count FROM voyage " +
                                "GROUP BY YEAR(departure_date), MONTH(departure_date) " +
                                "ORDER BY year, month")) {

            ResultSet rs = stmt.executeQuery();
            Map<String, Integer> periodCounts = new HashMap<>();

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                int count = rs.getInt("voyage_count");

                // Create two-month periods (Jan-Feb, Mar-Apr, etc.)
                int periodMonth = ((month - 1) / 2) * 2 + 1; // Convert to odd month (1, 3, 5, 7, 9, 11)
                YearMonth ym = YearMonth.of(year, periodMonth);
                String period = ym.format(formatter) + "/" + ym.plusMonths(1).format(formatter);

                // Add to period counts
                periodCounts.put(period, periodCounts.getOrDefault(period, 0) + count);
            }

            // Convert map to list
            for (Map.Entry<String, Integer> entry : periodCounts.entrySet()) {
                result.add(new VoyagePerPeriod(entry.getKey(), entry.getValue()));
            }

            // Sort by period
            result.sort((a, b) -> a.getPeriod().compareTo(b.getPeriod()));

        } catch (SQLException e) {
            e.printStackTrace();
            // Return sample data if query fails
            return getSampleVoyagePerPeriodData();
        }

        return result.isEmpty() ? getSampleVoyagePerPeriodData() : result;
    }

    // Sample data generators for when database queries fail or return empty results
    private static List<GuidePerVoyage> getSampleGuidePerVoyageData() {
        return Arrays.asList(
                new GuidePerVoyage(1, "Paris", 2),
                new GuidePerVoyage(2, "Tokyo", 3),
                new GuidePerVoyage(3, "New York", 1),
                new GuidePerVoyage(4, "Rome", 2),
                new GuidePerVoyage(5, "Cairo", 1));
    }

    private static List<VoyagePerPeriod> getSampleVoyagePerPeriodData() {
        return Arrays.asList(
                new VoyagePerPeriod("Jan-2023/Feb-2023", 3),
                new VoyagePerPeriod("Mar-2023/Apr-2023", 5),
                new VoyagePerPeriod("May-2023/Jun-2023", 8),
                new VoyagePerPeriod("Jul-2023/Aug-2023", 12),
                new VoyagePerPeriod("Sep-2023/Oct-2023", 7),
                new VoyagePerPeriod("Nov-2023/Dec-2023", 4));
    }
}