package service;

import dao.VerificationDAO;
import dao.AccessLogDAO;
import model.Verification;
import model.AccessLog;

import java.sql.SQLException;
import java.util.List;

/**
 * VerificationService - Retrieves verification records.
 * (Verification logic is in EvidenceService.verifyEvidence())
 */
public class VerificationService {

    private final VerificationDAO verificationDAO = new VerificationDAO();
    private final AccessLogDAO    accessLogDAO    = new AccessLogDAO();

    /**
     * Returns all verification attempts for a specific evidence item.
     */
    public List<Verification> getVerificationHistory(int evidenceId) throws SQLException {
        return verificationDAO.getVerificationsByEvidence(evidenceId);
    }

    /**
     * Returns all verifications across all evidence.
     */
    public List<Verification> getAllVerifications() throws SQLException {
        return verificationDAO.getAllVerifications();
    }
}
