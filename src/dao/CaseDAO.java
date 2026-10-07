package dao;

import model.Case;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CaseDAO - Data Access Object for CASE table operations.
 */
public class CaseDAO {

    /**
     * Inserts a new case into the database.
     * Returns the generated case_id.
     */
    public int insertCase(Case c) throws SQLException {
        String sql = "INSERT INTO `CASE` (case_number, title, description, status, priority, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getCaseNumber());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getStatus());
            ps.setString(5, c.getPriority());
            ps.setInt(6, c.getCreatedBy());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Returns all cases, joined with creator name.
     */
    public List<Case> getAllCases() throws SQLException {
        List<Case> cases = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS created_by_name " +
                     "FROM `CASE` c JOIN USER u ON u.user_id = c.created_by " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cases.add(mapResultSetToCase(rs));
            }
        }
        return cases;
    }

    /**
     * Returns cases created by or assigned to a specific user.
     */
    public List<Case> getCasesByUser(int userId) throws SQLException {
        List<Case> cases = new ArrayList<>();
        String sql = "SELECT DISTINCT c.*, u.full_name AS created_by_name " +
                     "FROM `CASE` c " +
                     "JOIN USER u ON u.user_id = c.created_by " +
                     "LEFT JOIN CASE_INVESTIGATOR ci ON ci.case_id = c.case_id " +
                     "WHERE c.created_by = ? OR ci.user_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cases.add(mapResultSetToCase(rs));
                }
            }
        }
        return cases;
    }

    /**
     * Finds a case by its case_id.
     */
    public Case findById(int caseId) throws SQLException {
        String sql = "SELECT c.*, u.full_name AS created_by_name " +
                     "FROM `CASE` c JOIN USER u ON u.user_id = c.created_by " +
                     "WHERE c.case_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToCase(rs);
            }
        }
        return null;
    }

    /**
     * Searches cases by title or case number (case-insensitive).
     */
    public List<Case> searchCases(String keyword) throws SQLException {
        List<Case> cases = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS created_by_name " +
                     "FROM `CASE` c JOIN USER u ON u.user_id = c.created_by " +
                     "WHERE c.case_number LIKE ? OR c.title LIKE ? OR c.description LIKE ? " +
                     "ORDER BY c.created_at DESC";

        String pattern = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cases.add(mapResultSetToCase(rs));
                }
            }
        }
        return cases;
    }

    /**
     * Updates case status and optionally sets closed_at.
     */
    public void updateCaseStatus(int caseId, String status) throws SQLException {
        String sql;
        if ("CLOSED".equals(status)) {
            sql = "UPDATE `CASE` SET status = ?, closed_at = NOW() WHERE case_id = ?";
        } else {
            sql = "UPDATE `CASE` SET status = ? WHERE case_id = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, caseId);
            ps.executeUpdate();
        }
    }

    /**
     * Updates case details.
     */
    public void updateCase(Case c) throws SQLException {
        String sql = "UPDATE `CASE` SET title=?, description=?, status=?, priority=? WHERE case_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getTitle());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getStatus());
            ps.setString(4, c.getPriority());
            ps.setInt(5, c.getCaseId());
            ps.executeUpdate();
        }
    }

    /**
     * Assigns an investigator to a case.
     */
    public void assignInvestigator(int caseId, int userId, int assignedBy) throws SQLException {
        String sql = "INSERT IGNORE INTO CASE_INVESTIGATOR (case_id, user_id, assigned_by) VALUES (?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ps.setInt(2, userId);
            ps.setInt(3, assignedBy);
            ps.executeUpdate();
        }
    }

    /**
     * Returns the next available case number (auto-generated).
     */
    public String generateCaseNumber() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM `CASE`";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("cnt") + 1;
                return String.format("CASE-%04d-%03d",
                        java.time.Year.now().getValue(), count);
            }
        }
        return "CASE-0001-001";
    }

    private Case mapResultSetToCase(ResultSet rs) throws SQLException {
        Case c = new Case();
        c.setCaseId(rs.getInt("case_id"));
        c.setCaseNumber(rs.getString("case_number"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setPriority(rs.getString("priority"));
        c.setCreatedBy(rs.getInt("created_by"));
        try { c.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException e) {}

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) c.setCreatedAt(created.toLocalDateTime());

        Timestamp closed = rs.getTimestamp("closed_at");
        if (closed != null) c.setClosedAt(closed.toLocalDateTime());

        return c;
    }
}
