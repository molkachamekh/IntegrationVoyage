package com.example.voyage.voyage.services;

import com.example.voyage.voyage.models.Guide;
import com.example.voyage.utils.DatabaseConnection;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Service class to handle Guide database operations
 */
public class GuideService {
    private ObservableList<Guide> guidesList = FXCollections.observableArrayList();

    /**
     * Get all guides from database with voyage names
     * 
     * @return ObservableList of guides
     */
    public ObservableList<Guide> getAllGuides() {
        guidesList.clear();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT g.*, v.destination FROM guide g " +
                                "JOIN voyage v ON g.voyage_id = v.voyage_id " +
                                "ORDER BY g.guide_id")) {

            while (rs.next()) {
                Guide guide = new Guide(
                        rs.getInt("guide_id"),
                        rs.getInt("voyage_id"),
                        rs.getString("guide_name"),
                        rs.getString("contact_info"),
                        rs.getString("languages"),
                        rs.getString("destination") // voyage name (destination)
                );
                guidesList.add(guide);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving guides: " + e.getMessage());
        }

        return guidesList;
    }

    /**
     * Get guide by ID
     * 
     * @param guideId ID of guide to retrieve
     * @return Guide object if found, null otherwise
     */
    public Guide getGuideById(int guideId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT g.*, v.destination FROM guide g " +
                                "JOIN voyage v ON g.voyage_id = v.voyage_id " +
                                "WHERE g.guide_id = ?")) {

            pstmt.setInt(1, guideId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Guide(
                        rs.getInt("guide_id"),
                        rs.getInt("voyage_id"),
                        rs.getString("guide_name"),
                        rs.getString("contact_info"),
                        rs.getString("languages"),
                        rs.getString("destination"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving guide by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get guide by voyage ID
     * 
     * @param voyageId ID of voyage to find guide for
     * @return Guide object if found, null otherwise
     */
    public Guide getGuideByVoyageId(int voyageId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT g.*, v.destination FROM guide g " +
                                "JOIN voyage v ON g.voyage_id = v.voyage_id " +
                                "WHERE g.voyage_id = ?")) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Guide(
                        rs.getInt("guide_id"),
                        rs.getInt("voyage_id"),
                        rs.getString("guide_name"),
                        rs.getString("contact_info"),
                        rs.getString("languages"),
                        rs.getString("destination"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving guide by voyage ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Create a new guide
     * 
     * @param guide Guide object to create
     * @return true if creation successful, false otherwise
     */
    public boolean createGuide(Guide guide) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO guide (voyage_id, guide_name, contact_info, languages) " +
                                "VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, guide.getVoyageId());
            pstmt.setString(2, guide.getGuideName());
            pstmt.setString(3, guide.getContactInfo());
            pstmt.setString(4, guide.getLanguages());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    guide.setGuideId(generatedKeys.getInt(1));

                    // Get the voyage name for UI display
                    try (PreparedStatement voyageStmt = conn.prepareStatement(
                            "SELECT destination FROM voyage WHERE voyage_id = ?")) {
                        voyageStmt.setInt(1, guide.getVoyageId());
                        ResultSet voyageRs = voyageStmt.executeQuery();
                        if (voyageRs.next()) {
                            guide.setVoyageName(voyageRs.getString("destination"));
                        }
                    }

                    guidesList.add(guide);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating guide: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update an existing guide
     * 
     * @param guide Guide object to update
     * @return true if update successful, false otherwise
     */
    public boolean updateGuide(Guide guide) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE guide SET voyage_id = ?, guide_name = ?, contact_info = ?, languages = ? " +
                                "WHERE guide_id = ?")) {

            pstmt.setInt(1, guide.getVoyageId());
            pstmt.setString(2, guide.getGuideName());
            pstmt.setString(3, guide.getContactInfo());
            pstmt.setString(4, guide.getLanguages());
            pstmt.setInt(5, guide.getGuideId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                // Get the voyage name for UI display
                try (PreparedStatement voyageStmt = conn.prepareStatement(
                        "SELECT destination FROM voyage WHERE voyage_id = ?")) {
                    voyageStmt.setInt(1, guide.getVoyageId());
                    ResultSet voyageRs = voyageStmt.executeQuery();
                    if (voyageRs.next()) {
                        guide.setVoyageName(voyageRs.getString("destination"));
                    }
                }

                // Update the guide in our observable list
                for (int i = 0; i < guidesList.size(); i++) {
                    if (guidesList.get(i).getGuideId() == guide.getGuideId()) {
                        guidesList.set(i, guide);
                        break;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating guide: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete a guide
     * 
     * @param guideId ID of guide to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteGuide(int guideId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM guide WHERE guide_id = ?")) {

            pstmt.setInt(1, guideId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                // Remove the guide from our observable list
                guidesList.removeIf(guide -> guide.getGuideId() == guideId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting guide: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get the total count of guides
     * 
     * @return Total number of guides
     */
    public int getGuideCount() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM guide")) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting guides: " + e.getMessage());
        }

        return 0;
    }
}
