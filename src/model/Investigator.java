package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Investigator - Represents an investigation officer.
 *
 * OOP: Extends User (Inheritance), uses ArrayList (Collections + Generics)
 * Investigators can create cases, register evidence, and transfer evidence.
 */
public class Investigator extends User {

    private static final String INVESTIGATOR_ROLE = "INVESTIGATOR";

    // Collections - tracks which cases this investigator is assigned to (in-memory)
    private List<Integer> assignedCaseIds;

    // ==============================================================
    // Constructors
    // ==============================================================

    public Investigator() {
        super();
        assignedCaseIds = new ArrayList<>();
    }

    public Investigator(String username, String passwordHash, String fullName,
                        String email, String phone, String badgeNumber, String department) {
        super(username, passwordHash, fullName, email, phone, badgeNumber, department);
        this.setRoleId(2);
        assignedCaseIds = new ArrayList<>();
    }

    public Investigator(int userId, String username, String passwordHash, String fullName,
                        String email, String phone, String badgeNumber, String department,
                        boolean isActive, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, phone, badgeNumber,
              department, 2, isActive, createdAt);
        assignedCaseIds = new ArrayList<>();
    }

    // ==============================================================
    // Overriding abstract methods
    // ==============================================================

    @Override
    public String getRole() {
        return INVESTIGATOR_ROLE;
    }

    @Override
    public String getPermissionsDescription() {
        return "Can create/manage cases, register evidence, transfer evidence, verify evidence.";
    }

    @Override
    public boolean canAccessEvidence(int evidenceId) {
        // Investigators can access evidence in their assigned cases
        // Full check done in service layer using DB
        return isActive();
    }

    @Override
    public String getUserSummary() {
        return String.format("[INVESTIGATOR] %s (%s) - Badge: %s",
                getFullName(), getUsername(), getBadgeNumber());
    }

    // ==============================================================
    // Investigator-specific methods
    // ==============================================================

    /**
     * Adds a case ID to the in-memory list of assigned cases.
     */
    public void assignCase(int caseId) {
        if (!assignedCaseIds.contains(caseId)) {
            assignedCaseIds.add(caseId);
        }
    }

    /**
     * Checks if this investigator is assigned to a specific case.
     * Uses Collections.contains() (Generics: List<Integer>)
     */
    public boolean isAssignedToCase(int caseId) {
        return assignedCaseIds.contains(caseId);
    }

    public List<Integer> getAssignedCaseIds() {
        return assignedCaseIds;
    }

    public void setAssignedCaseIds(List<Integer> assignedCaseIds) {
        this.assignedCaseIds = assignedCaseIds;
    }
}
