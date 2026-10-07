package service;

import model.Evidence;
import model.EvidenceTransfer;
import model.Verification;
import exception.EvidenceNotFoundException;
import exception.UnauthorizedAccessException;
import exception.InvalidTransferException;
import exception.HashMismatchException;

import java.util.List;

/**
 * EvidenceOperations - Interface defining core evidence operations.
 *
 * OOP: Interface used to enforce a contract on service classes.
 * Any class implementing this interface MUST provide these methods.
 *
 * This demonstrates Java Interfaces as a concept.
 */
public interface EvidenceOperations {

    /**
     * Registers a new piece of digital evidence.
     *
     * @param evidence   The evidence to register
     * @param filePath   Path to the evidence file (to compute hash)
     * @return           The registered evidence with ID and hash set
     * @throws Exception if registration fails
     */
    Evidence registerEvidence(Evidence evidence, String filePath) throws Exception;

    /**
     * Verifies the integrity of an evidence file by comparing SHA-256 hashes.
     *
     * @param evidenceId  The ID of the evidence to verify
     * @param filePath    Path to the current evidence file
     * @param verifiedBy  The user performing the verification
     * @return            Verification result record
     * @throws EvidenceNotFoundException if evidence does not exist
     * @throws HashMismatchException     if the hash does not match
     */
    Verification verifyEvidence(int evidenceId, String filePath, int verifiedBy)
        throws EvidenceNotFoundException, HashMismatchException, Exception;

    /**
     * Transfers evidence custody from one user to another.
     *
     * @param evidenceId  The evidence to transfer
     * @param fromUserId  The current holder (sender)
     * @param toUserId    The new holder (receiver)
     * @param reason      The reason for the transfer
     * @param location    Where the transfer takes place
     * @return            The completed transfer record
     * @throws EvidenceNotFoundException  if evidence does not exist
     * @throws UnauthorizedAccessException if sender does not hold the evidence
     * @throws InvalidTransferException   if the transfer is otherwise invalid
     */
    EvidenceTransfer transferEvidence(int evidenceId, int fromUserId, int toUserId,
                                      String reason, String location)
        throws EvidenceNotFoundException, UnauthorizedAccessException,
               InvalidTransferException, Exception;

    /**
     * Returns all evidence items. Admin use.
     */
    List<Evidence> getAllEvidence() throws Exception;

    /**
     * Returns evidence items for a specific case.
     */
    List<Evidence> getEvidenceByCase(int caseId) throws Exception;
}
