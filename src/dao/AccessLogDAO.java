package dao;

import model.AccessLog;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessLogDAO - Data Access Object for ACCESS_LOG table.
 *
 * Inserts audit log records and retrieves them for display.
 * Most inserts happen via DB triggers, but the application
 * also inserts directly for login/logout and view events.
 */
public class AccessLogDAO {

    /**
     * Inserts an access log record.
     * This is used for actions not covered by DB triggers (e.g., LOGIN, VIEW).
     */
    public void insertLog(AccessLog log) throws SQLException {
        String sql = "INSERT INTO ACCESS_LOG (user_id, evidence_id, case_id, action, description) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getUserId());
            if (log.getEvidenceId() != null) ps.setInt(2, log.getEvidenceId());
            else ps.setNull(2, Types.INTEGER);
            if (log.getCaseId() != null) ps.setInt(3, log.getCaseId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, log.getAction());
            ps.setString(5, log.getDescription());
            ps.executeUpdate();
        }
    }

    /**
     * Convenience method to log an action.
     */
    public void logAction(int userId, Integer evidenceId, Integer caseId,
                          String action, String description) throws SQLException {
        AccessLog log = new AccessLog(userId, evidenceId, caseId, action, description);
        insertLog(log);
    }

    /**
     * Returns all access logs, most recent first.
     */
    public List<AccessLog> getAllLogs() throws SQLException {
        List<AccessLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name, " +
                     "e.evidence_number, c.case_number " +
                     "FROM ACCESS_LOG al " +
                     "JOIN USER u ON u.user_id = al.user_id " +
                     "LEFT JOIN EVIDENCE e ON e.evidence_id = al.evidence_id " +
                     "LEFT JOIN `CASE` c   ON c.case_id = al.case_id " +
                     "ORDER BY al.logged_at DESC LIMIT 500";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToLog(rs));
        }
        return list;
    }

    /**
     * Returns logs for a specific evidence item.
     */
    public List<AccessLog> getLogsByEvidence(int evidenceId) throws SQLException {
        List<AccessLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name, " +
                     "e.evidence_number, c.case_number " +
                     "FROM ACCESS_LOG al " +
                     "JOIN USER u ON u.user_id = al.user_id " +
                     "LEFT JOIN EVIDENCE e ON e.evidence_id = al.evidence_id " +
                     "LEFT JOIN `CASE` c ON c.case_id = al.case_id " +
                     "WHERE al.evidence_id = ? " +
                     "ORDER BY al.logged_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    /**
     * Returns logs for a specific user.
     */
    public List<AccessLog> getLogsByUser(int userId) throws SQLException {
        List<AccessLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_full_name, " +
                     "e.evidence_number, c.case_number " +
                     "FROM ACCESS_LOG al " +
                     "JOIN USER u ON u.user_id = al.user_id " +
                     "LEFT JOIN EVIDENCE e ON e.evidence_id = al.evidence_id " +
                     "LEFT JOIN `CASE` c ON c.case_id = al.case_id " +
                     "WHERE al.user_id = ? " +
                     "ORDER BY al.logged_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    private AccessLog mapResultSetToLog(ResultSet rs) throws SQLException {
        AccessLog log = new AccessLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUserFullName(rs.getString("user_full_name"));
        int eid = rs.getInt("evidence_id");
        if (!rs.wasNull()) {
            log.setEvidenceId(eid);
            log.setEvidenceNumber(rs.getString("evidence_number"));
        }
        int cid = rs.getInt("case_id");
        if (!rs.wasNull()) {
            log.setCaseId(cid);
            log.setCaseNumber(rs.getString("case_number"));
        }
        log.setAction(rs.getString("action"));
        log.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("logged_at");
        if (ts != null) log.setLoggedAt(ts.toLocalDateTime());
        return log;
    }
}
