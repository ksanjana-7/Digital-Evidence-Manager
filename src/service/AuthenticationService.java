package service;

import dao.UserDAO;
import dao.AccessLogDAO;
import model.User;
import model.AccessLog;
import util.HashUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * AuthenticationService - Handles user login and logout.
 *
 * Demonstrates:
 *   - Service layer pattern (business logic separate from DAO and GUI)
 *   - SHA-256 password hashing
 *   - Session management (currentUser)
 */
public class AuthenticationService {

    private final UserDAO      userDAO      = new UserDAO();
    private final AccessLogDAO accessLogDAO = new AccessLogDAO();

    // Session: stores the currently logged-in user
    private static User currentUser = null;

    /**
     * Attempts to log in with the given credentials.
     *
     * 1. Hash the entered password with SHA-256
     * 2. Query the database using PreparedStatement (safe from SQL injection)
     * 3. If found, store the user in session
     * 4. Log the login event
     *
     * @param username  Plain-text username
     * @param password  Plain-text password (hashed before querying)
     * @return          The authenticated User object, or null if login fails
     */
    public User login(String username, String password) throws SQLException {
        try {
            // Step 1: Hash the password
            String passwordHash = HashUtil.computeHash(password, true);

            // Step 2: Query database safely
            User user = userDAO.findByUsernameAndPassword(username, passwordHash);

            if (user != null) {
                // Step 3: Set session
                currentUser = user;

                // Step 4: Log the login event
                try {
                    accessLogDAO.logAction(user.getUserId(), null, null,
                            AccessLog.ACTION_LOGIN,
                            "User '" + username + "' logged in successfully.");
                } catch (SQLException e) {
                    System.err.println("Warning: Could not log login event: " + e.getMessage());
                }

                System.out.println("[Auth] Login successful: " + user.getUserSummary());
            } else {
                System.out.println("[Auth] Login failed for username: " + username);
            }

            return user;

        } catch (NoSuchAlgorithmException e) {
            throw new SQLException("SHA-256 hashing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            try {
                accessLogDAO.logAction(currentUser.getUserId(), null, null,
                        AccessLog.ACTION_LOGOUT,
                        "User '" + currentUser.getUsername() + "' logged out.");
            } catch (SQLException e) {
                System.err.println("Warning: Could not log logout event.");
            }
            System.out.println("[Auth] User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Returns the currently logged-in user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the current user has admin role.
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    /**
     * Checks if the current user is an investigator.
     */
    public static boolean isInvestigator() {
        return currentUser != null && "INVESTIGATOR".equals(currentUser.getRole());
    }

    /**
     * Checks if the current user is a forensic officer.
     */
    public static boolean isForensicOfficer() {
        return currentUser != null && "FORENSIC_OFFICER".equals(currentUser.getRole());
    }
}
