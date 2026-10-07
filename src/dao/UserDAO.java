package dao;

import model.User;
import model.Admin;
import model.Investigator;
import model.ForensicOfficer;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object for USER table operations.
 *
 * JDBC Concepts Demonstrated:
 *   - PreparedStatement (SQL injection prevention)
 *   - ResultSet
 *   - Connection management
 *
 * Uses polymorphism to return the correct User subclass based on role_id.
 */
public class UserDAO {

    /**
     * Finds a user by username and password hash.
     * Uses PreparedStatement to prevent SQL injection.
     *
     * @param username     Login username
     * @param passwordHash SHA-256 hash of the entered password
     * @return             User object (Admin/Investigator/ForensicOfficer) or null
     */
    public User findByUsernameAndPassword(String username, String passwordHash) throws SQLException {
        // PreparedStatement - SAFE - no string concatenation
        String sql = "SELECT u.*, r.role_name FROM USER u " +
                     "JOIN ROLE r ON r.role_id = u.role_id " +
                     "WHERE u.username = ? AND u.password_hash = ? AND u.is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;  // Login failed
    }

    /**
     * Returns all users in the system.
     * Used by Admin to manage users.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.role_name FROM USER u " +
                     "JOIN ROLE r ON r.role_id = u.role_id ORDER BY u.full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Returns all active users (for assigning as receiver in transfers).
     */
    public List<User> getActiveUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.role_name FROM USER u " +
                     "JOIN ROLE r ON r.role_id = u.role_id " +
                     "WHERE u.is_active = 1 ORDER BY u.full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Finds a user by their user_id.
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT u.*, r.role_name FROM USER u " +
                     "JOIN ROLE r ON r.role_id = u.role_id WHERE u.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new user into the database.
     * Returns the generated user_id.
     */
    public int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO USER (username, password_hash, full_name, email, phone, " +
                     "badge_number, department, role_id, is_active) VALUES (?,?,?,?,?,?,?,?,1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getBadgeNumber());
            ps.setString(7, user.getDepartment());
            ps.setInt(8, user.getRoleId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Updates user active status.
     */
    public void setUserActiveStatus(int userId, boolean isActive) throws SQLException {
        String sql = "UPDATE USER SET is_active = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isActive ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to the correct User subclass.
     * Demonstrates Runtime Polymorphism - returns Admin, Investigator, or ForensicOfficer.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int       userId      = rs.getInt("user_id");
        String    username    = rs.getString("username");
        String    passHash    = rs.getString("password_hash");
        String    fullName    = rs.getString("full_name");
        String    email       = rs.getString("email");
        String    phone       = rs.getString("phone");
        String    badge       = rs.getString("badge_number");
        String    department  = rs.getString("department");
        int       roleId      = rs.getInt("role_id");
        boolean   isActive    = rs.getBoolean("is_active");
        Timestamp ts          = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

        // Runtime Polymorphism: create the right subclass based on role_id
        User user;
        switch (roleId) {
            case 1:
                user = new Admin(userId, username, passHash, fullName, email, phone,
                                 badge, department, isActive, createdAt);
                break;
            case 2:
                user = new Investigator(userId, username, passHash, fullName, email, phone,
                                        badge, department, isActive, createdAt);
                break;
            case 3:
                user = new ForensicOfficer(userId, username, passHash, fullName, email, phone,
                                           badge, department, isActive, createdAt);
                break;
            default:
                user = new Investigator(userId, username, passHash, fullName, email, phone,
                                        badge, department, isActive, createdAt);
        }
        user.setRoleId(roleId);
        return user;
    }
}
