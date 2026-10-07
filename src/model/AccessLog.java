package model;

import java.time.LocalDateTime;

/**
 * AccessLog - Represents an audit log entry.
 *
 * Records every important action taken in the system.
 * Used for accountability, investigation review, and audit reports.
 */
public class AccessLog {

    // Action constants
    public static final String ACTION_LOGIN              = "LOGIN";
    public static final String ACTION_LOGOUT             = "LOGOUT";
    public static final String ACTION_VIEW               = "VIEW";
    public static final String ACTION_REGISTER           = "REGISTER";
    public static final String ACTION_UPDATE             = "UPDATE";
    public static final String ACTION_TRANSFER           = "TRANSFER";
    public static final String ACTION_VERIFY             = "VERIFY";
    public static final String ACTION_DOWNLOAD           = "DOWNLOAD";
    public static final String ACTION_DELETE             = "DELETE";
    public static final String ACTION_CREATE_CASE        = "CREATE_CASE";
    public static final String ACTION_CLOSE_CASE         = "CLOSE_CASE";
    public static final String ACTION_ASSIGN_INVESTIGATOR = "ASSIGN_INVESTIGATOR";

    private int           logId;
    private int           userId;
    private String        userFullName;     // Joined
    private Integer       evidenceId;       // nullable
    private String        evidenceNumber;   // Joined, nullable
    private Integer       caseId;           // nullable
    private String        caseNumber;       // Joined, nullable
    private String        action;
    private String        description;
    private String        ipAddress;
    private LocalDateTime loggedAt;

    // ==============================================================
    // Constructors
    // ==============================================================

    public AccessLog() {}

    public AccessLog(int userId, Integer evidenceId, Integer caseId,
                     String action, String description) {
        this.userId      = userId;
        this.evidenceId  = evidenceId;
        this.caseId      = caseId;
        this.action      = action;
        this.description = description;
    }

    // ==============================================================
    // Getters and Setters
    // ==============================================================

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public Integer getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Integer evidenceId) { this.evidenceId = evidenceId; }

    public String getEvidenceNumber() { return evidenceNumber; }
    public void setEvidenceNumber(String evidenceNumber) { this.evidenceNumber = evidenceNumber; }

    public Integer getCaseId() { return caseId; }
    public void setCaseId(Integer caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s at %s",
                action, userFullName, description, loggedAt);
    }
}
