package service;

import dao.EvidenceDAO;
import dao.TransferDAO;
import dao.AccessLogDAO;
import model.Evidence;
import model.EvidenceTransfer;
import model.AccessLog;
import util.DBConnection;
import exception.EvidenceNotFoundException;
import exception.UnauthorizedAccessException;
import exception.InvalidTransferException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * TransferService - Handles evidence chain-of-custody transfers.
 *
 * CRITICAL: Uses JDBC transactions (setAutoCommit, commit, rollback)
 * to ensure the transfer is atomic:
 *   - Both the transfer record INSERT and the evidence holder UPDATE
 *     succeed together, or both are rolled back.
 *
 * This prevents partial transfers where one step succeeds and the other fails.
 *
 * Concurrency consideration:
 *   - MySQL default REPEATABLE READ isolation prevents dirty reads.
 *   - The DB trigger (trg_before_transfer_insert) provides an additional
 *     DB-level check that the sender holds the evidence at insert time.
 *   - This double-check prevents two simultaneous transfers of the same evidence.
 */
public class TransferService {

    private final EvidenceDAO  evidenceDAO  = new EvidenceDAO();
    private final TransferDAO  transferDAO  = new TransferDAO();
    private final AccessLogDAO accessLogDAO = new AccessLogDAO();

    /**
     * Transfers evidence from one user to another.
     *
     * Transaction steps:
     * 1. Validate evidence exists
     * 2. Validate from_user is current holder
     * 3. Validate to_user != from_user
     * 4. INSERT transfer record
     * 5. UPDATE evidence current_holder and status
     * 6. COMMIT
     *
     * @throws EvidenceNotFoundException   if evidence doesn't exist
     * @throws UnauthorizedAccessException if sender doesn't hold the evidence
     * @throws InvalidTransferException    if transfer is otherwise invalid
     */
    public EvidenceTransfer transferEvidence(int evidenceId, int fromUserId, int toUserId,
                                             String reason, String location)
            throws EvidenceNotFoundException, UnauthorizedAccessException,
                   InvalidTransferException, Exception {

        // Basic validation
        if (fromUserId == toUserId) {
            throw new InvalidTransferException("Cannot transfer evidence to yourself.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidTransferException("Transfer reason cannot be empty.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // BEGIN TRANSACTION
            // Disable auto-commit so we control the transaction boundary
            conn.setAutoCommit(false);

            // Step 1: Load evidence
            Evidence evidence = evidenceDAO.findById(evidenceId);
            if (evidence == null) {
                throw new EvidenceNotFoundException("Evidence ID " + evidenceId + " not found.");
            }

            // Step 2: Verify sender holds the evidence
            if (evidence.getCurrentHolder() != fromUserId) {
                throw new UnauthorizedAccessException(
                        "User " + fromUserId,
                        "TRANSFER",
                        "User ID " + fromUserId + " does not currently hold evidence ID " + evidenceId +
                        ". Current holder: " + evidence.getCurrentHolder()
                );
            }

            // Step 3: Create transfer object
            EvidenceTransfer transfer = new EvidenceTransfer(evidenceId, fromUserId, toUserId,
                                                              reason, location);
            transfer.setNotes("Transfer location: " + (location != null ? location : "Not specified"));

            // Step 4: Insert transfer record (using the shared connection)
            // The DB trigger trg_before_transfer_insert will also validate
            int transferId = transferDAO.insertTransfer(transfer, conn);
            transfer.setTransferId(transferId);

            // Step 5: Update evidence holder and status
            evidenceDAO.updateEvidenceHolder(evidenceId, toUserId, Evidence.STATUS_TRANSFERRED, conn);

            // COMMIT - if we reach here, both steps succeeded
            conn.commit();

            System.out.println("[TransferService] Transfer committed: Evidence " +
                    evidenceId + " from user " + fromUserId + " to user " + toUserId);

            return transfer;

        } catch (EvidenceNotFoundException | UnauthorizedAccessException e) {
            // Application-level exceptions - rollback
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e;

        } catch (Exception e) {
            // Database or unexpected errors - rollback
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            System.err.println("[TransferService] Transaction rolled back due to: " + e.getMessage());
            throw new InvalidTransferException("Transfer failed: " + e.getMessage(), e);

        } finally {
            // Always restore auto-commit
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    /**
     * Returns all transfers for a specific evidence item (chain of custody).
     */
    public List<EvidenceTransfer> getChainOfCustody(int evidenceId) throws SQLException {
        return transferDAO.getTransfersByEvidence(evidenceId);
    }

    /**
     * Returns all transfers in the system.
     */
    public List<EvidenceTransfer> getAllTransfers() throws SQLException {
        return transferDAO.getAllTransfers();
    }

    /**
     * Returns transfers involving a specific user.
     */
    public List<EvidenceTransfer> getTransfersByUser(int userId) throws SQLException {
        return transferDAO.getTransfersByUser(userId);
    }
}
