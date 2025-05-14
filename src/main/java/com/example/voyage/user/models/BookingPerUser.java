package com.example.voyage.user.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.voyage.utils.DatabaseConnection;

/**
 * Data model for booking statistics per user
 */
public class BookingPerUser {
    private int userId;
    private String name;
    private int count;

    /**
     * Constructor for BookingPerUser
     * 
     * @param userId The user's ID
     * @param name   The user's name
     * @param count  Number of bookings made by the user
     */
    public BookingPerUser(int userId, String name, int count) {
        this.userId = userId;
        this.name = name;
        this.count = count;
    }

    /**
     * Get the user ID
     * 
     * @return User ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Get the user name
     * 
     * @return User name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the booking count
     * 
     * @return Number of bookings
     */
    public int getCount() {
        return count;
    }

    /**
     * Get count of bookings per user from database
     * 
     * @return List of BookingPerUser objects
     */
    public static List<BookingPerUser> getBookingsPerUserData() {
        List<BookingPerUser> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT u.user_id, u.full_name, COUNT(b.booking_id) as booking_count " +
                                "FROM user u LEFT JOIN booking b ON u.user_id = b.user_id " +
                                "GROUP BY u.user_id, u.full_name " +
                                "ORDER BY booking_count DESC")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                int count = rs.getInt("booking_count");

                result.add(new BookingPerUser(userId, fullName, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return empty list if query fails
        }

        return result;
    }
}