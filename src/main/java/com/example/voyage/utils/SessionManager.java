package com.example.voyage.utils;

import com.example.voyage.user.models.User;

/**
 * Singleton class to manage user session throughout the application
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    public void logout() {
        currentUser = null;
    }
}
