package model;

import java.time.LocalDateTime;

/**
 * Admin - Represents a system administrator.
 *
 * OOP: Extends User (Inheritance), overrides abstract methods (Polymorphism)
 * Admins have full system access.
 */
public class Admin extends User {

    // Admin-specific fields
    private static final String ADMIN_ROLE = "ADMIN";
    private static int adminCount = 0;  // static field - shared across all Admin instances

    // ==============================================================
    // Constructors
    // ==============================================================

    public Admin() {
        super();
        adminCount++;
    }

    public Admin(String username, String passwordHash, String fullName,
                 String email, String phone, String badgeNumber, String department) {
        super(username, passwordHash, fullName, email, phone, badgeNumber, department);
        this.setRoleId(1);
        adminCount++;
    }

    public Admin(int userId, String username, String passwordHash, String fullName,
                 String email, String phone, String badgeNumber, String department,
                 boolean isActive, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, phone, badgeNumber,
              department, 1, isActive, createdAt);
        adminCount++;
    }

    // ==============================================================
    // Overriding abstract methods from User (Runtime Polymorphism)
    // ==============================================================

    @Override
    public String getRole() {
        return ADMIN_ROLE;
    }

    @Override
    public String getPermissionsDescription() {
        return "Full system access: manage users, view all cases, evidence, and audit logs.";
    }

    @Override
    public boolean canAccessEvidence(int evidenceId) {
        // Admins can access all evidence
        return isActive();
    }

    @Override
    public String getUserSummary() {
        return String.format("[ADMIN] %s (%s) - Full Access", getFullName(), getUsername());
    }

    // ==============================================================
    // Static method
    // ==============================================================
    public static int getAdminCount() {
        return adminCount;
    }
}
