package model;

import java.time.LocalDateTime;

/**
 * Case - Represents an investigation case.
 *
 * OOP: Plain Java class with encapsulation.
 * A case can contain multiple evidence records (1:N relationship in DB).
 */
public class Case {

    // static constants for status and priority (final fields)
    public static final String STATUS_OPEN     = "OPEN";
    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_CLOSED   = "CLOSED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    public static final String PRIORITY_LOW      = "LOW";
    public static final String PRIORITY_MEDIUM   = "MEDIUM";
    public static final String PRIORITY_HIGH     = "HIGH";
    public static final String PRIORITY_CRITICAL = "CRITICAL";

    // Encapsulated fields
    private int           caseId;
    private String        caseNumber;
    private String        title;
    private String        description;
    private String        status;
    private String        priority;
    private int           createdBy;
    private String        createdByName;   // Joined from USER table
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    // ==============================================================
    // Constructors (method overloading)
    // ==============================================================

    public Case() {}

    /**
     * Constructor for creating a new case
     */
    public Case(String caseNumber, String title, String description,
                String status, String priority, int createdBy) {
        this.caseNumber  = caseNumber;
        this.title       = title;
        this.description = description;
        this.status      = status;
        this.priority    = priority;
        this.createdBy   = createdBy;
    }

    /**
     * Full constructor for loading from database
     */
    public Case(int caseId, String caseNumber, String title, String description,
                String status, String priority, int createdBy,
                LocalDateTime createdAt, LocalDateTime closedAt) {
        this.caseId      = caseId;
        this.caseNumber  = caseNumber;
        this.title       = title;
        this.description = description;
        this.status      = status;
        this.priority    = priority;
        this.createdBy   = createdBy;
        this.createdAt   = createdAt;
        this.closedAt    = closedAt;
    }

    // ==============================================================
    // Getters and Setters
    // ==============================================================

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    @Override
    public String toString() {
        return caseNumber + " - " + title + " [" + status + "]";
    }
}
