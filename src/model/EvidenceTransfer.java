package model;

import java.time.LocalDateTime;

/**
 * EvidenceTransfer - Represents a chain-of-custody transfer record.
 *
 * OOP: Encapsulation, constructors.
 * Every transfer is permanently recorded for audit purposes.
 */
public class EvidenceTransfer {

    // Status constants
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REJECTED  = "REJECTED";

    // Encapsulated fields
    private int           transferId;
    private int           evidenceId;
    private String        evidenceNumber;   // Joined
    private int           fromUserId;
    private String        fromUserName;     // Joined
    private int           toUserId;
    private String        toUserName;       // Joined
    private LocalDateTime transferDate;
    private String        reason;
    private String        location;
    private String        status;
    private String        notes;

    // ==============================================================
    // Constructors
    // ==============================================================

    public EvidenceTransfer() {}

    public EvidenceTransfer(int evidenceId, int fromUserId, int toUserId,
                            String reason, String location) {
        this.evidenceId  = evidenceId;
        this.fromUserId  = fromUserId;
        this.toUserId    = toUserId;
        this.reason      = reason;
        this.location    = location;
        this.status      = STATUS_COMPLETED;
    }

    public EvidenceTransfer(int transferId, int evidenceId, int fromUserId, int toUserId,
                            LocalDateTime transferDate, String reason, String location,
                            String status, String notes) {
        this.transferId   = transferId;
        this.evidenceId   = evidenceId;
        this.fromUserId   = fromUserId;
        this.toUserId     = toUserId;
        this.transferDate = transferDate;
        this.reason       = reason;
        this.location     = location;
        this.status       = status;
        this.notes        = notes;
    }

    // ==============================================================
    // Getters and Setters
    // ==============================================================

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public String getEvidenceNumber() { return evidenceNumber; }
    public void setEvidenceNumber(String evidenceNumber) { this.evidenceNumber = evidenceNumber; }

    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public int getToUserId() { return toUserId; }
    public void setToUserId(int toUserId) { this.toUserId = toUserId; }

    public String getToUserName() { return toUserName; }
    public void setToUserName(String toUserName) { this.toUserName = toUserName; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return String.format("Transfer #%d: %s -> %s on %s",
                transferId, fromUserName, toUserName, transferDate);
    }
}
