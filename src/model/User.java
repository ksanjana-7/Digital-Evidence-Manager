package model;

import java.time.LocalDateTime;

/**
 * User - Abstract base class for all system users.
 *
 * OOP Concepts Demonstrated:
 *   - Abstract class: Cannot be instantiated directly
 *   - Encapsulation: Private fields with getters/setters
 *   - Constructors: Parameterized constructors
 *   - Inheritance: Admin, Investigator, ForensicOfficer extend this class
 *   - Abstract method: getRole() must be implemented by subclasses
 */
public abstract class User {

    // Encapsulated fields (private with getters/setters)
    private int    userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private String badgeNumber;
    private String department;
    private int    roleId;
    private boolean isActive;
    private LocalDateTime createdAt;

    // ==============================================================
    // Constructors (Method Overloading)
    // ==============================================================

    /**
     * Default constructor
     */
    public User() {}

    /**
     * Constructor for creating new users (without ID, ID assigned by DB)
     */
    public User(String username, String passwordHash, String fullName,
                String email, String phone, String badgeNumber, String department) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.phone        = phone;
        this.badgeNumber  = badgeNumber;
        this.department   = department;
        this.isActive     = true;
    }

    /**
     * Full constructor for loading users from database
     */
    public User(int userId, String username, String passwordHash, String fullName,
                String email, String phone, String badgeNumber, String department,
                int roleId, boolean isActive, LocalDateTime createdAt) {
        this.userId       = userId;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.phone        = phone;
        this.badgeNumber  = badgeNumber;
        this.department   = department;
        this.roleId       = roleId;
        this.isActive     = isActive;
        this.createdAt    = createdAt;
    }

    // ==============================================================
    // Abstract method - subclasses MUST implement this
    // ==============================================================

    /**
     * Returns the role name of this user.
     * Each subclass defines its own role.
     */
    public abstract String getRole();

    /**
     * Returns a display-friendly string describing the user's permissions.
     * Demonstrates method overriding in subclasses.
     */
    public abstract String getPermissionsDescription();

    // ==============================================================
    // Concrete method - shared by all subclasses
    // ==============================================================

    /**
     * Returns a brief string summary of the user.
     * Can be overridden in subclasses (method overriding).
     */
    public String getUserSummary() {
        return String.format("[%s] %s (%s) - Badge: %s",
                getRole(), fullName, username, badgeNumber);
    }

    /**
     * Checks if this user can access the given evidence.
     * Overridden in subclasses for role-based logic.
     */
    public boolean canAccessEvidence(int evidenceId) {
        // Default: check if active
        return isActive;
    }

    // ==============================================================
    // Getters and Setters (Encapsulation)
    // ==============================================================

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getUserSummary();
    }
}
