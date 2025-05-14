package com.example.voyage.user.services;

import com.example.voyage.user.models.Booking;
import com.example.voyage.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class BookingService {
    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    public BookingService() {
        // loadBookings(); // Can be called if needed at initialization
    }

    // Get all bookings
    public ObservableList<Booking> getAllBookings() {
        bookingList.clear();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT b.*, u.full_name AS user_name, v.destination AS voyage_name " +
                                "FROM booking b " +
                                "JOIN user u ON b.user_id = u.user_id " +
                                "JOIN voyage v ON b.voyage_id = v.voyage_id " +
                                "ORDER BY b.booking_date DESC")) {

            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setVoyageId(rs.getInt("voyage_id"));
                booking.setBookingDate(rs.getDate("booking_date").toLocalDate());
                booking.setStatus(rs.getString("status"));
                booking.setUserName(rs.getString("user_name"));
                booking.setVoyageName(rs.getString("voyage_name"));
                bookingList.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }

        return bookingList;
    }

    // Get user bookings by user ID
    public ObservableList<Booking> getUserBookings(int userId) {
        ObservableList<Booking> userBookings = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT b.*, u.full_name AS user_name, v.destination AS voyage_name " +
                                "FROM booking b " +
                                "JOIN user u ON b.user_id = u.user_id " +
                                "JOIN voyage v ON b.voyage_id = v.voyage_id " +
                                "WHERE b.user_id = ? " +
                                "ORDER BY b.booking_date DESC")) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setVoyageId(rs.getInt("voyage_id"));
                    booking.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    booking.setStatus(rs.getString("status"));
                    booking.setUserName(rs.getString("user_name"));
                    booking.setVoyageName(rs.getString("voyage_name"));
                    userBookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading user bookings: " + e.getMessage());
        }

        return userBookings;
    }

    // Get recent bookings (for dashboard)
    public ObservableList<Booking> getRecentBookings(int limit) {
        ObservableList<Booking> recentBookings = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT b.*, u.full_name AS user_name, v.destination AS voyage_name " +
                                "FROM booking b " +
                                "JOIN user u ON b.user_id = u.user_id " +
                                "JOIN voyage v ON b.voyage_id = v.voyage_id " +
                                "ORDER BY b.booking_date DESC " +
                                "LIMIT ?")) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setVoyageId(rs.getInt("voyage_id"));
                    booking.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    booking.setStatus(rs.getString("status"));
                    booking.setUserName(rs.getString("user_name"));
                    booking.setVoyageName(rs.getString("voyage_name"));
                    recentBookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading recent bookings: " + e.getMessage());
        }

        return recentBookings;
    }

    // Get booking by ID
    public Booking getBookingById(int bookingId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT b.*, u.full_name AS user_name, v.destination AS voyage_name, " +
                                "v.departure_date, v.return_date, v.price " +
                                "FROM booking b " +
                                "JOIN user u ON b.user_id = u.user_id " +
                                "JOIN voyage v ON b.voyage_id = v.voyage_id " +
                                "WHERE b.booking_id = ?")) {

            pstmt.setInt(1, bookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setVoyageId(rs.getInt("voyage_id"));
                    booking.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    booking.setStatus(rs.getString("status"));
                    booking.setUserName(rs.getString("user_name"));
                    booking.setVoyageName(rs.getString("voyage_name"));

                    // Add the voyage dates and price information
                    booking.setDepartureDate(rs.getDate("departure_date").toLocalDate());
                    booking.setReturnDate(rs.getDate("return_date").toLocalDate());
                    booking.setPrice(rs.getDouble("price"));

                    // Get isPaid if it exists in the database
                    try {
                        booking.setPaid(rs.getBoolean("is_paid"));
                    } catch (SQLException e) {
                        // If column doesn't exist, set default
                        booking.setPaid(false);
                    }

                    return booking;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting booking by ID: " + e.getMessage());
        }

        return null;
    }

    // Create a new booking
    public boolean createBooking(Booking booking) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO booking (user_id, voyage_id, booking_date, status) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, booking.getUserId());
            pstmt.setInt(2, booking.getVoyageId());
            pstmt.setDate(3, Date.valueOf(booking.getBookingDate()));

            // Set initial status to "En Attente" if not provided
            String status = booking.getStatus();
            if (status == null || status.isEmpty()) {
                status = "En Attente";
            }
            pstmt.setString(4, status);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setBookingId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }

        return false;
    }

    // Update booking status - KEEP THIS VERSION and remove duplicate below
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE booking SET status = ? WHERE booking_id = ?")) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, bookingId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
            return false;
        }
    }

    // Delete booking - ENHANCED with better validation
    public boolean deleteBooking(int bookingId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM booking WHERE booking_id = ?")) {

            pstmt.setInt(1, bookingId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a booking can be deleted by a specific user
     * 
     * @param bookingId The booking ID to check
     * @param userId    The user ID attempting to delete
     * @return true if the booking can be deleted by this user, false otherwise
     */
    public boolean canDeleteBooking(int bookingId, int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT user_id, status FROM booking WHERE booking_id = ?")) {

            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int bookingUserId = rs.getInt("user_id");
                String status = rs.getString("status");

                // Check if booking belongs to user and is in a deletable state
                return bookingUserId == userId &&
                        ("Pending".equals(status) || "Cancelled".equals(status));
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking if booking can be deleted: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete booking for a specific user with validation
     * 
     * @param bookingId The booking ID to delete
     * @param userId    The user ID attempting to delete
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteUserBooking(int bookingId, int userId) {
        // First check if the user can delete this booking
        if (!canDeleteBooking(bookingId, userId)) {
            return false;
        }

        // If allowed, proceed with deletion
        return deleteBooking(bookingId);
    }

    // Get booking count (for dashboard)
    public int getBookingCount() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM booking")) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting booking count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Update payment status for a booking
     * NOTE: This has been changed to update the booking status instead of a
     * non-existent is_paid column
     * 
     * @param bookingId Booking ID
     * @param isPaid    Whether the booking is paid
     * @return true if successful, false otherwise
     */
    public boolean updatePaymentStatus(int bookingId, boolean isPaid) {
        // Instead of trying to update a non-existent is_paid column,
        // we'll update the booking status to "Confirmed" when paid
        if (isPaid) {
            return updateBookingStatus(bookingId, "Confirmed");
        }
        return false;
    }
}
