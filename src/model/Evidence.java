package model;

import java.time.LocalDateTime;

/**
 * Evidence - Represents a digital evidence record.
 *
 * OOP: Encapsulation, static final constants, constructors.
 * The actual file is stored on disk; metadata is stored in the database.
 */
public class Evidence {

    // Evidence type constants (static final)
    public static final String TYPE_LAPTOP    = "LAPTOP";
    public static final String TYPE_MOBILE    = "MOBILE";
    public static final String TYPE_USB       = "USB";
    public static final String TYPE_HARD_DISK = "HARD_DISK";
    public static final String TYPE_IMAGE     = "IMAGE";
    public static final String TYPE_VIDEO     = "VIDEO";
    public static final String TYPE_DOCUMENT  = "DOCUMENT";
    public static final String TYPE_LOG_FILE  = "LOG_FILE";
    public static final String TYPE_OTHER     = "OTHER";

    // Status constants
    public static final String STATUS_REGISTERED       = "REGISTERED";
    public static final String STATUS_IN_CUSTODY       = "IN_CUSTODY";
    public static final String STATUS_TRANSFERRED      = "TRANSFERRED";
    public static final String STATUS_UNDER_ANALYSIS   = "UNDER_ANALYSIS";
    public static final String STATUS_VERIFIED         = "VERIFIED";
    public static final String STATUS_RELEASED         = "RELEASED";
    public static final String STATUS_COMPROMISED      = "COMPROMISED";

    // Encapsulated fields
    private int           evidenceId;
    private String        evidenceNumber;
    private int           caseId;
    private String        caseNumber;      // Joined from CASE table
    private String        evidenceType;
    private String        description;
    private int           collectedBy;
    private String        collectedByName; // Joined from USER table
    private String        collectionLocation;
    private LocalDateTime collectionDate;
    private String        currentStatus;
    private int           currentHolder;
    private String        currentHolderName; // Joined from USER table
    private LocalDateTime registeredAt;

    // File metadata (from EVIDENCE_FILE table)
    private int    fileId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long   fileSize;
    private String originalHash;
    private String currentHash;

    // ==============================================================
    // Constructors
    // ==============================================================

    public Evidence() {}

    /**
     * Constructor for registering new evidence
     */
    public Evidence(String evidenceNumber, int caseId, String evidenceType,
                    String description, int collectedBy, String collectionLocation,
                    LocalDateTime collectionDate) {
        this.evidenceNumber     = evidenceNumber;
        this.caseId             = caseId;
        this.evidenceType       = evidenceType;
        this.description        = description;
        this.collectedBy        = collectedBy;
        this.collectionLocation = collectionLocation;
        this.collectionDate     = collectionDate;
        this.currentStatus      = STATUS_REGISTERED;
        this.currentHolder      = collectedBy;
    }

    // ==============================================================
    // Getters and Setters
    // ==============================================================

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public String getEvidenceNumber() { return evidenceNumber; }
    public void setEvidenceNumber(String evidenceNumber) { this.evidenceNumber = evidenceNumber; }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCollectedBy() { return collectedBy; }
    public void setCollectedBy(int collectedBy) { this.collectedBy = collectedBy; }

    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }

    public String getCollectionLocation() { return collectionLocation; }
    public void setCollectionLocation(String collectionLocation) { this.collectionLocation = collectionLocation; }

    public LocalDateTime getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDateTime collectionDate) { this.collectionDate = collectionDate; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public int getCurrentHolder() { return currentHolder; }
    public void setCurrentHolder(int currentHolder) { this.currentHolder = currentHolder; }

    public String getCurrentHolderName() { return currentHolderName; }
    public void setCurrentHolderName(String currentHolderName) { this.currentHolderName = currentHolderName; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public int getFileId() { return fileId; }
    public void setFileId(int fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getOriginalHash() { return originalHash; }
    public void setOriginalHash(String originalHash) { this.originalHash = originalHash; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    @Override
    public String toString() {
        return evidenceNumber + " - " + evidenceType + " [" + currentStatus + "]";
    }
}
