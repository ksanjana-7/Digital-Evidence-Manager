package model;

import java.time.LocalDateTime;

/**
 * Verification - Represents a hash verification attempt.
 *
 * Stores whether the current hash matches the original stored hash.
 */
public class Verification {

    // Result constants
    public static final String RESULT_VERIFIED = "VERIFIED";
    public static final String RESULT_TAMPERED = "TAMPERED";
    public static final String RESULT_ERROR    = "ERROR";

    private int           verificationId;
    private int           evidenceId;
    private String        evidenceNumber;    // Joined
    private int           verifiedBy;
    private String        verifiedByName;    // Joined
    private LocalDateTime verificationDate;
    private String        originalHash;
    private String        currentHash;
    private String        result;
    private String        notes;

    // ==============================================================
    // Constructors
    // ==============================================================

    public Verification() {}

    public Verification(int evidenceId, int verifiedBy, String originalHash,
                        String currentHash, String result, String notes) {
        this.evidenceId   = evidenceId;
        this.verifiedBy   = verifiedBy;
        this.originalHash = originalHash;
        this.currentHash  = currentHash;
        this.result       = result;
        this.notes        = notes;
    }

    public Verification(int verificationId, int evidenceId, int verifiedBy,
                        LocalDateTime verificationDate, String originalHash,
                        String currentHash, String result, String notes) {
        this.verificationId   = verificationId;
        this.evidenceId       = evidenceId;
        this.verifiedBy       = verifiedBy;
        this.verificationDate = verificationDate;
        this.originalHash     = originalHash;
        this.currentHash      = currentHash;
        this.result           = result;
        this.notes            = notes;
    }

    // ==============================================================
    // Getters and Setters
    // ==============================================================

    public int getVerificationId() { return verificationId; }
    public void setVerificationId(int verificationId) { this.verificationId = verificationId; }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public String getEvidenceNumber() { return evidenceNumber; }
    public void setEvidenceNumber(String evidenceNumber) { this.evidenceNumber = evidenceNumber; }

    public int getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(int verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerifiedByName() { return verifiedByName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    public String getOriginalHash() { return originalHash; }
    public void setOriginalHash(String originalHash) { this.originalHash = originalHash; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isVerified() {
        return RESULT_VERIFIED.equals(result);
    }

    @Override
    public String toString() {
        return String.format("Verification #%d [%s] on %s by %s",
                verificationId, result, verificationDate, verifiedByName);
    }
}
