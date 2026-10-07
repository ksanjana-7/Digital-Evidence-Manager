package service;

import dao.CaseDAO;
import dao.AccessLogDAO;
import model.Case;
import model.AccessLog;
import model.User;
import exception.UnauthorizedAccessException;

import java.sql.SQLException;
import java.util.List;

/**
 * CaseService - Business logic for investigation case management.
 */
public class CaseService {

    private final CaseDAO      caseDAO      = new CaseDAO();
    private final AccessLogDAO accessLogDAO = new AccessLogDAO();

    /**
     * Creates a new investigation case.
     *
     * @param currentUser The user creating the case
     * @param title       Case title
     * @param description Case description
     * @param priority    Priority level
     * @return            The created Case with ID assigned
     */
    public Case createCase(User currentUser, String title, String description,
                           String priority) throws SQLException, UnauthorizedAccessException {

        // Only Admin and Investigators can create cases
        if ("FORENSIC_OFFICER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException(currentUser.getUsername(),
                    "CREATE_CASE", "Forensic Officers cannot create cases.");
        }

        // Generate a unique case number
        String caseNumber = caseDAO.generateCaseNumber();

        Case c = new Case(caseNumber, title, description,
                          Case.STATUS_OPEN, priority, currentUser.getUserId());

        int caseId = caseDAO.insertCase(c);
        c.setCaseId(caseId);

        // Log the action
        accessLogDAO.logAction(currentUser.getUserId(), null, caseId,
                AccessLog.ACTION_CREATE_CASE, "Case created: " + caseNumber + " - " + title);

        return c;
    }

    /**
     * Returns cases visible to the current user.
     * Admins see all cases; others see their assigned cases.
     */
    public List<Case> getCasesForUser(User currentUser) throws SQLException {
        if ("ADMIN".equals(currentUser.getRole())) {
            return caseDAO.getAllCases();
        } else {
            return caseDAO.getCasesByUser(currentUser.getUserId());
        }
    }

    /**
     * Returns all cases (Admin only).
     */
    public List<Case> getAllCases() throws SQLException {
        return caseDAO.getAllCases();
    }

    /**
     * Searches cases by keyword.
     */
    public List<Case> searchCases(String keyword) throws SQLException {
        return caseDAO.searchCases(keyword);
    }

    /**
     * Closes a case.
     */
    public void closeCase(User currentUser, int caseId)
            throws SQLException, UnauthorizedAccessException {

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException(currentUser.getUsername(),
                    "CLOSE_CASE", "Only administrators can close cases.");
        }

        caseDAO.updateCaseStatus(caseId, Case.STATUS_CLOSED);
        accessLogDAO.logAction(currentUser.getUserId(), null, caseId,
                AccessLog.ACTION_CLOSE_CASE, "Case ID " + caseId + " closed by admin.");
    }

    /**
     * Updates case details.
     */
    public void updateCase(User currentUser, Case c) throws SQLException, UnauthorizedAccessException {
        if ("FORENSIC_OFFICER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException(currentUser.getUsername(),
                    "UPDATE_CASE", "Forensic Officers cannot update cases.");
        }
        caseDAO.updateCase(c);
        accessLogDAO.logAction(currentUser.getUserId(), null, c.getCaseId(),
                AccessLog.ACTION_UPDATE, "Case updated: " + c.getCaseNumber());
    }

    /**
     * Assigns an investigator to a case.
     */
    public void assignInvestigator(User admin, int caseId, int userId)
            throws SQLException, UnauthorizedAccessException {

        if (!"ADMIN".equals(admin.getRole())) {
            throw new UnauthorizedAccessException(admin.getUsername(),
                    "ASSIGN_INVESTIGATOR", "Only admins can assign investigators.");
        }
        caseDAO.assignInvestigator(caseId, userId, admin.getUserId());
        accessLogDAO.logAction(admin.getUserId(), null, caseId,
                AccessLog.ACTION_ASSIGN_INVESTIGATOR,
                "User ID " + userId + " assigned to case ID " + caseId);
    }

    /**
     * Finds a case by ID.
     */
    public Case findCaseById(int caseId) throws SQLException {
        return caseDAO.findById(caseId);
    }
}
