package model;

import java.time.LocalDateTime;

/**
 * ForensicOfficer - Represents a digital forensics officer.
 *
 * OOP: Extends User (Inheritance)
 * Forensic officers focus on analysis, verification, and reporting.
 */
public class ForensicOfficer extends User {

    private static final String FORENSIC_ROLE = "FORENSIC_OFFICER";

    // Forensic-specific field
    private String specialization;  // e.g., "Mobile Forensics", "Network Forensics"

    // ==============================================================
    // Constructors
    // ==============================================================

    public ForensicOfficer() {
        super();
    }

    public ForensicOfficer(String username, String passwordHash, String fullName,
                           String email, String phone, String badgeNumber,
                           String department, String specialization) {
        super(username, passwordHash, fullName, email, phone, badgeNumber, department);
        this.setRoleId(3);
        this.specialization = specialization;
    }

    public ForensicOfficer(int userId, String username, String passwordHash, String fullName,
                           String email, String phone, String badgeNumber, String department,
                           boolean isActive, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, phone, badgeNumber,
              department, 3, isActive, createdAt);
    }

    // ==============================================================
    // Overriding abstract methods
    // ==============================================================

    @Override
    public String getRole() {
        return FORENSIC_ROLE;
    }

    @Override
    public String getPermissionsDescription() {
        return "Can view evidence, verify integrity, record forensic actions, perform transfers.";
    }

    @Override
    public boolean canAccessEvidence(int evidenceId) {
        return isActive();
    }

    @Override
    public String getUserSummary() {
        return String.format("[FORENSIC OFFICER] %s (%s) - Badge: %s%s",
                getFullName(), getUsername(), getBadgeNumber(),
                (specialization != null ? " - " + specialization : ""));
    }

    // ==============================================================
    // Forensic-specific methods
    // ==============================================================

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
