package com.example.voyage.voyage.services;

import com.example.voyage.voyage.models.Voyage;
import com.example.voyage.utils.DatabaseConnection;

import java.io.*;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Base64;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;

public class VoyageService {
    private ObservableList<Voyage> voyageList = FXCollections.observableArrayList();

    /**
     * Get all voyages from database
     * 
     * @return ObservableList of voyages
     */
    public ObservableList<Voyage> getAllVoyages() {
        voyageList.clear();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM voyage ORDER BY departure_date")) {

            while (rs.next()) {
                Voyage voyage = new Voyage(
                        rs.getInt("voyage_id"),
                        rs.getString("destination"),
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getDate("return_date").toLocalDate(),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("image"));

                // If image is stored as Base64 string, convert to Image
                String imageBase64 = rs.getString("image");
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] imageData = Base64.getDecoder().decode(imageBase64);
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                        voyage.setImage(new Image(bis));
                    } catch (Exception e) {
                        System.err.println("Failed to decode image: " + e.getMessage());
                    }
                }

                voyageList.add(voyage);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving voyages: " + e.getMessage());
        }

        return voyageList;
    }

    /**
     * Get a voyage by ID
     * 
     * @param voyageId ID of voyage to retrieve
     * @return Voyage object if found, null otherwise
     */
    public Voyage getVoyageById(int voyageId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM voyage WHERE voyage_id = ?")) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Voyage voyage = new Voyage(
                        rs.getInt("voyage_id"),
                        rs.getString("destination"),
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getDate("return_date").toLocalDate(),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("image"));

                // If image is stored as Base64 string, convert to Image
                String imageBase64 = rs.getString("image");
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] imageData = Base64.getDecoder().decode(imageBase64);
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                        voyage.setImage(new Image(bis));
                    } catch (Exception e) {
                        System.err.println("Failed to decode image: " + e.getMessage());
                    }
                }

                return voyage;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving voyage by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Create a new voyage
     * 
     * @param voyage Voyage object to create
     * @return true if creation successful, false otherwise
     */
    public boolean createVoyage(Voyage voyage) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO voyage (destination, departure_date, return_date, price, description, image) " +
                                "VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, java.sql.Date.valueOf(voyage.getDepartureDate()));
            pstmt.setDate(3, java.sql.Date.valueOf(voyage.getReturnDate()));
            pstmt.setDouble(4, voyage.getPrice());
            pstmt.setString(5, voyage.getDescription());

            // Handle image data
            if (voyage.getImagePath() != null && !voyage.getImagePath().isEmpty()) {
                try {
                    // Verify this is valid Base64 before attempting insert
                    Base64.getDecoder().decode(voyage.getImagePath());
                    pstmt.setString(6, voyage.getImagePath());
                } catch (IllegalArgumentException e) {
                    // Not valid Base64, log error and set to null
                    System.err.println("Invalid Base64 image data: " + e.getMessage());
                    pstmt.setNull(6, Types.VARCHAR);
                }
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    voyage.setVoyageId(generatedKeys.getInt(1));
                    voyageList.add(voyage);
                    return true;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Data too long") || e.getMessage().contains("truncation")) {
                System.err.println("Error creating voyage: Image data is too large for the database column.");
                System.err.println("You should run: ALTER TABLE voyage MODIFY COLUMN image MEDIUMTEXT;");
            } else {
                System.err.println("Error creating voyage: " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Update an existing voyage
     * 
     * @param voyage Voyage object to update
     * @return true if update successful, false otherwise
     */
    public boolean updateVoyage(Voyage voyage) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE voyage SET destination = ?, departure_date = ?, return_date = ?, " +
                                "price = ?, description = ?, image = ? WHERE voyage_id = ?")) {

            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, java.sql.Date.valueOf(voyage.getDepartureDate()));
            pstmt.setDate(3, java.sql.Date.valueOf(voyage.getReturnDate()));
            pstmt.setDouble(4, voyage.getPrice());
            pstmt.setString(5, voyage.getDescription());

            // Handle image data
            if (voyage.getImagePath() != null && !voyage.getImagePath().isEmpty()) {
                try {
                    // Verify this is valid Base64 before attempting insert
                    Base64.getDecoder().decode(voyage.getImagePath());
                    pstmt.setString(6, voyage.getImagePath());
                } catch (IllegalArgumentException e) {
                    // Not valid Base64, log error and set to null
                    System.err.println("Invalid Base64 image data: " + e.getMessage());
                    pstmt.setNull(6, Types.VARCHAR);
                }
            } else {
                // Use a special parameter to indicate we should keep the existing image
                PreparedStatement checkStmt = conn.prepareStatement("SELECT image FROM voyage WHERE voyage_id = ?");
                checkStmt.setInt(1, voyage.getVoyageId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getString("image") != null) {
                    pstmt.setString(6, rs.getString("image"));
                } else {
                    pstmt.setNull(6, Types.VARCHAR);
                }
                rs.close();
                checkStmt.close();
            }

            pstmt.setInt(7, voyage.getVoyageId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                // Update the voyage in our observable list
                for (int i = 0; i < voyageList.size(); i++) {
                    if (voyageList.get(i).getVoyageId() == voyage.getVoyageId()) {
                        voyageList.set(i, voyage);
                        break;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Data too long") || e.getMessage().contains("truncation")) {
                System.err.println("Error updating voyage: Image data is too large for the database column.");
                System.err.println("You should run: ALTER TABLE voyage MODIFY COLUMN image MEDIUMTEXT;");
            } else {
                System.err.println("Error updating voyage: " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Delete a voyage
     * 
     * @param voyageId ID of voyage to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteVoyage(int voyageId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM voyage WHERE voyage_id = ?")) {

            pstmt.setInt(1, voyageId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                // Remove the voyage from our observable list
                voyageList.removeIf(voyage -> voyage.getVoyageId() == voyageId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting voyage: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get the total count of voyages
     * 
     * @return Total number of voyages
     */
    public int getVoyageCount() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM voyage")) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting voyages: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Convert JavaFX Image to Base64 string
     * 
     * @param image JavaFX Image to convert
     * @return Base64 encoded string
     */
    private String imageToBase64(Image image) {
        try {
            // Get image dimensions
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // Convert JavaFX Image to byte array
            WritablePixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
            byte[] buffer = new byte[width * height * 4];
            image.getPixelReader().getPixels(0, 0, width, height, pixelFormat, buffer, 0, width * 4);

            // Encode to Base64
            return Base64.getEncoder().encodeToString(buffer);
        } catch (Exception e) {
            System.err.println("Failed to convert image to Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert file path to Base64 string
     * 
     * @param filePath Path to image file
     * @return Base64 encoded string
     */
    public String filePathToBase64(String filePath) {
        try {
            InputStream is = new FileInputStream(filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            is.close();

            // Encode to Base64
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            System.err.println("Failed to read image file: " + e.getMessage());
            return null;
        }
    }
}
