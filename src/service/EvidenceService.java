package service;

import dao.EvidenceDAO;
import dao.VerificationDAO;
import dao.AccessLogDAO;
import model.Evidence;
import model.User;
import model.Verification;
import model.EvidenceTransfer;
import model.AccessLog;
import util.DBConnection;
import util.HashUtil;
import util.FileUtil;
import exception.EvidenceNotFoundException;
import exception.HashMismatchException;
import exception.InvalidTransferException;
import exception.UnauthorizedAccessException;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EvidenceService - Implements EvidenceOperations interface.
 *
 * OOP: Implements interface (EvidenceOperations), uses multiple DAOs.
 *
 * Key responsibilities:
 *   - Register evidence with file hash computation
 *   - Verify evidence integrity
 *   - Delegate transfer to TransferService
 *   - Log all actions
 */
public class EvidenceService implements EvidenceOperations {

    private final EvidenceDAO     evidenceDAO     = new EvidenceDAO();
    private final VerificationDAO verificationDAO = new VerificationDAO();
    private final AccessLogDAO    accessLogDAO    = new AccessLogDAO();

    /**
     * Registers a new evidence item.
     *
     * Process:
     * 1. Copy the file to evidence directory
     * 2. Compute SHA-256 hash of the file
     * 3. Store evidence + file metadata in DB (within a transaction)
     * 4. Log the action
     *
     * @param evidence  Evidence metadata
     * @param filePath  Path to the original file selected by user
     * @return          The registered evidence with ID, hash, etc.
     */
    @Override
    public Evidence registerEvidence(Evidence evidence, String filePath) throws Exception {
        File sourceFile = new File(filePath);

        if (!sourceFile.exists()) {
            throw new EvidenceNotFoundException("Evidence file not found at: " + filePath);
        }

        // Compute SHA-256 hash of the evidence file
        String hash = HashUtil.computeHash(filePath);

        // Copy the file to the evidence directory
        String storedPath = FileUtil.copyEvidenceFile(sourceFile,
                evidence.getCaseNumber(), evidence.getEvidenceNumber());

        // Use a transaction to insert EVIDENCE + EVIDENCE_FILE atomically
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);  // BEGIN TRANSACTION

            // Insert evidence record
            int evidenceId = evidenceDAO.insertEvidence(evidence, conn);
            evidence.setEvidenceId(evidenceId);

            // Insert file metadata
            evidenceDAO.insertEvidenceFile(
                    evidenceId,
                    sourceFile.getName(),
                    storedPath,
                    FileUtil.getFileExtension(sourceFile),
                    sourceFile.length(),
                    hash,
                    conn
            );

            conn.commit();  // COMMIT

            // Set computed values on the returned object
            evidence.setOriginalHash(hash);
            evidence.setCurrentHash(hash);
            evidence.setFileName(sourceFile.getName());
            evidence.setFilePath(storedPath);
            evidence.setFileSize(sourceFile.length());
            evidence.setFileType(FileUtil.getFileExtension(sourceFile));
            evidence.setRegisteredAt(LocalDateTime.now());

            System.out.println("[EvidenceService] Registered evidence: " + evidence.getEvidenceNumber());
            return evidence;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    /**
     * Verifies the integrity of an evidence file by comparing SHA-256 hashes.
     *
     * @param evidenceId  DB ID of the evidence
     * @param filePath    Path to the current evidence file
     * @param verifiedBy  User performing verification
     * @return            Verification record
     */
    @Override
    public Verification verifyEvidence(int evidenceId, String filePath, int verifiedBy)
            throws EvidenceNotFoundException, HashMismatchException, Exception {

        // Retrieve evidence from DB
        Evidence evidence = evidenceDAO.findById(evidenceId);
        if (evidence == null) {
            throw new EvidenceNotFoundException("Evidence ID " + evidenceId + " not found.");
        }

        String originalHash = evidence.getOriginalHash();
        String currentHash;

        try {
            currentHash = HashUtil.computeHash(filePath);
        } catch (Exception e) {
            // Record error verification
            Verification v = new Verification(evidenceId, verifiedBy, originalHash,
                    "ERROR", Verification.RESULT_ERROR, "Could not read file: " + e.getMessage());
            verificationDAO.insertVerification(v);
            throw e;
        }

        String result = currentHash.equalsIgnoreCase(originalHash)
                ? Verification.RESULT_VERIFIED
                : Verification.RESULT_TAMPERED;

        // Record verification in DB
        Verification v = new Verification(evidenceId, verifiedBy, originalHash,
                currentHash, result, null);
        int verificationId = verificationDAO.insertVerification(v);
        v.setVerificationId(verificationId);

        // Update current hash in EVIDENCE_FILE
        evidenceDAO.updateCurrentHash(evidenceId, currentHash);

        // Update evidence status
        if (Verification.RESULT_VERIFIED.equals(result)) {
            evidenceDAO.updateEvidenceStatus(evidenceId, Evidence.STATUS_VERIFIED);
        } else {
            evidenceDAO.updateEvidenceStatus(evidenceId, Evidence.STATUS_COMPROMISED);
        }

        // If tampered, throw exception so caller can handle it
        if (Verification.RESULT_TAMPERED.equals(result)) {
            throw new HashMismatchException(evidence.getEvidenceNumber(),
                    originalHash, currentHash,
                    "TAMPERING DETECTED: Hash mismatch for evidence " + evidence.getEvidenceNumber());
        }

        return v;
    }

    /**
     * Delegates to TransferService (implemented there).
     * This method stub satisfies the interface requirement.
     */
    @Override
    public EvidenceTransfer transferEvidence(int evidenceId, int fromUserId, int toUserId,
                                             String reason, String location)
            throws EvidenceNotFoundException, UnauthorizedAccessException,
                   InvalidTransferException, Exception {
        TransferService ts = new TransferService();
        return ts.transferEvidence(evidenceId, fromUserId, toUserId, reason, location);
    }

    @Override
    public List<Evidence> getAllEvidence() throws Exception {
        return evidenceDAO.getAllEvidence();
    }

    @Override
    public List<Evidence> getEvidenceByCase(int caseId) throws Exception {
        return evidenceDAO.getEvidenceByCase(caseId);
    }

    /**
     * Returns evidence by the user who currently holds it.
     */
    public List<Evidence> getEvidenceByHolder(int holderId) throws SQLException {
        return evidenceDAO.getEvidenceByHolder(holderId);
    }

    /**
     * Searches evidence by keyword.
     */
    public List<Evidence> searchEvidence(String keyword) throws SQLException {
        return evidenceDAO.searchEvidence(keyword);
    }

    /**
     * Returns a specific evidence record.
     */
    public Evidence getEvidenceById(int evidenceId) throws SQLException, EvidenceNotFoundException {
        Evidence e = evidenceDAO.findById(evidenceId);
        if (e == null) throw new EvidenceNotFoundException("Evidence ID " + evidenceId + " not found.");
        return e;
    }

    /**
     * Logs a VIEW action when evidence details are opened.
     */
    public void logView(int userId, int evidenceId, String description) {
        try {
            accessLogDAO.logAction(userId, evidenceId, null, AccessLog.ACTION_VIEW, description);
        } catch (SQLException e) {
            System.err.println("Warning: Could not log view action.");
        }
    }

    /**
     * Generates the next evidence number for a case.
     */
    public String generateEvidenceNumber(String caseNumber) throws SQLException {
        return evidenceDAO.generateEvidenceNumber(caseNumber);
    }
}
