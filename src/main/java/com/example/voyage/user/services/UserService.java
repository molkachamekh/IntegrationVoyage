package com.example.voyage.user.services;

import com.example.voyage.user.models.User;
import com.example.voyage.utils.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    // Check if a username is already taken
    public boolean isUsernameTaken(String username) {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get user count
    public int getUserCount() {
        String query = "SELECT COUNT(*) FROM user";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get all users
    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT * FROM user";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password")); // This is the hashed password
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));

                Date dateOfBirth = rs.getDate("date_of_birth");
                if (dateOfBirth != null) {
                    user.setDateOfBirth(dateOfBirth.toLocalDate());
                }

                user.setPhoneNumber(rs.getString("phone_number"));
                user.setAdmin(rs.getBoolean("is_admin"));

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Get user by ID
    public User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password")); // This is the hashed password
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));

                Date dateOfBirth = rs.getDate("date_of_birth");
                if (dateOfBirth != null) {
                    user.setDateOfBirth(dateOfBirth.toLocalDate());
                }

                user.setPhoneNumber(rs.getString("phone_number"));
                user.setAdmin(rs.getBoolean("is_admin"));

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get user by username
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM user WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password")); // This is the hashed password
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));

                Date dateOfBirth = rs.getDate("date_of_birth");
                if (dateOfBirth != null) {
                    user.setDateOfBirth(dateOfBirth.toLocalDate());
                }

                user.setPhoneNumber(rs.getString("phone_number"));
                user.setAdmin(rs.getBoolean("is_admin"));

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Authenticate user - verify the password is correct
    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    /**
     * Authenticate user without location verification
     * This simplified authentication method checks only username and password
     * 
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUserWithoutLocation(String username, String password) {
        User user = getUserByUsername(username);

        if (user != null) {
            // For non-hashed password (legacy support)
            if (!user.getPassword().startsWith("$2a$") && user.getPassword().equals(password)) {
                return user;
            }
            // For BCrypt hashed password
            else if (user.getPassword().startsWith("$2a$") && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        }

        return null;
    }

    // Create a new user
    public boolean createUser(User user) {
        String query = "INSERT INTO user (username, password, email, full_name, date_of_birth, phone_number, is_admin) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Password should be already hashed
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());

            if (user.getDateOfBirth() != null) {
                stmt.setDate(5, Date.valueOf(user.getDateOfBirth()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setString(6, user.getPhoneNumber());
            stmt.setBoolean(7, user.isAdmin());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update existing user
    public boolean updateUser(User user) {
        // Determine if we need to update the password
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();

        String query = "UPDATE user SET " +
                "email = ?, full_name = ?, date_of_birth = ?, " +
                "phone_number = ?, is_admin = ? " +
                (updatePassword ? ", password = ? " : "") +
                "WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());

            if (user.getDateOfBirth() != null) {
                stmt.setDate(3, Date.valueOf(user.getDateOfBirth()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setString(4, user.getPhoneNumber());
            stmt.setBoolean(5, user.isAdmin());

            int paramIndex = 6;
            if (updatePassword) {
                stmt.setString(paramIndex++, user.getPassword());
            }

            stmt.setInt(paramIndex, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a user
    public boolean deleteUser(int userId) {
        // First check if user has bookings
        String checkBookingsQuery = "SELECT COUNT(*) FROM booking WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkBookingsQuery)) {

            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // User has bookings, can't delete
                return false;
            }

            // If no bookings, proceed with deletion
            String deleteQuery = "DELETE FROM user WHERE user_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, userId);
                int rowsAffected = deleteStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
