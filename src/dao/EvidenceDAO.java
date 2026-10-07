package dao;

import model.Evidence;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EvidenceDAO - Data Access Object for EVIDENCE and EVIDENCE_FILE tables.
 *
 * Demonstrates JOIN queries, PreparedStatement, ResultSet processing.
 */
public class EvidenceDAO {

    /**
     * Inserts a new evidence record and its file metadata.
     * Returns the generated evidence_id.
     *
     * Note: Transaction management is handled by the service layer (EvidenceService).
     */
    public int insertEvidence(Evidence e, Connection conn) throws SQLException {
        String sql = "INSERT INTO EVIDENCE (evidence_number, case_id, evidence_type, description, " +
                     "collected_by, collection_location, collection_date, current_status, current_holder) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getEvidenceNumber());
            ps.setInt(2, e.getCaseId());
            ps.setString(3, e.getEvidenceType());
            ps.setString(4, e.getDescription());
            ps.setInt(5, e.getCollectedBy());
            ps.setString(6, e.getCollectionLocation());
            ps.setTimestamp(7, e.getCollectionDate() != null ?
                    Timestamp.valueOf(e.getCollectionDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, Evidence.STATUS_REGISTERED);
            ps.setInt(9, e.getCollectedBy());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Inserts file metadata into EVIDENCE_FILE.
     */
    public void insertEvidenceFile(int evidenceId, String fileName, String filePath,
                                   String fileType, long fileSize,
                                   String originalHash, Connection conn) throws SQLException {
        String sql = "INSERT INTO EVIDENCE_FILE (evidence_id, file_name, file_path, " +
                     "file_type, file_size, original_hash, current_hash) VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evidenceId);
            ps.setString(2, fileName);
            ps.setString(3, filePath);
            ps.setString(4, fileType);
            ps.setLong(5, fileSize);
            ps.setString(6, originalHash);
            ps.setString(7, originalHash);
            ps.executeUpdate();
        }
    }

    /**
     * Returns all evidence with joined case, user, and file information.
     */
    public List<Evidence> getAllEvidence() throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = buildEvidenceSelectSQL("ORDER BY e.registered_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToEvidence(rs));
        }
        return list;
    }

    /**
     * Returns evidence for a specific case.
     */
    public List<Evidence> getEvidenceByCase(int caseId) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = buildEvidenceSelectSQL("WHERE e.case_id = ? ORDER BY e.registered_at");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    /**
     * Returns evidence currently held by a specific user.
     */
    public List<Evidence> getEvidenceByHolder(int holderId) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = buildEvidenceSelectSQL("WHERE e.current_holder = ? ORDER BY e.registered_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    /**
     * Finds evidence by ID.
     */
    public Evidence findById(int evidenceId) throws SQLException {
        String sql = buildEvidenceSelectSQL("WHERE e.evidence_id = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToEvidence(rs);
            }
        }
        return null;
    }

    /**
     * Searches evidence by keyword (number, type, description, location).
     */
    public List<Evidence> searchEvidence(String keyword) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = buildEvidenceSelectSQL(
            "WHERE e.evidence_number LIKE ? OR e.description LIKE ? " +
            "OR e.collection_location LIKE ? OR e.evidence_type LIKE ? " +
            "ORDER BY e.registered_at DESC");
        String p = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p); ps.setString(2, p);
            ps.setString(3, p); ps.setString(4, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    /**
     * Updates the current holder and status of evidence.
     * Called during transfer (within a transaction).
     */
    public void updateEvidenceHolder(int evidenceId, int newHolder, String newStatus,
                                     Connection conn) throws SQLException {
        String sql = "UPDATE EVIDENCE SET current_holder = ?, current_status = ? WHERE evidence_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newHolder);
            ps.setString(2, newStatus);
            ps.setInt(3, evidenceId);
            ps.executeUpdate();
        }
    }

    /**
     * Updates evidence status.
     */
    public void updateEvidenceStatus(int evidenceId, String status) throws SQLException {
        String sql = "UPDATE EVIDENCE SET current_status = ? WHERE evidence_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, evidenceId);
            ps.executeUpdate();
        }
    }

    /**
     * Updates the current_hash in EVIDENCE_FILE after verification.
     */
    public void updateCurrentHash(int evidenceId, String newHash) throws SQLException {
        String sql = "UPDATE EVIDENCE_FILE SET current_hash = ? WHERE evidence_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, evidenceId);
            ps.executeUpdate();
        }
    }

    /**
     * Generates the next evidence number.
     */
    public String generateEvidenceNumber(String caseNumber) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM EVIDENCE WHERE case_id = " +
                     "(SELECT case_id FROM `CASE` WHERE case_number = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, caseNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("cnt") + 1;
                    return "EV-" + caseNumber.replace("CASE-","") + "-" + String.format("%02d", count);
                }
            }
        }
        return "EV-001-01";
    }

    // Builds the common SELECT SQL for evidence queries with proper JOINs
    private String buildEvidenceSelectSQL(String whereClause) {
        return "SELECT e.*, " +
               "c.case_number, " +
               "u_col.full_name AS collected_by_name, " +
               "u_hld.full_name AS current_holder_name, " +
               "ef.file_id, ef.file_name, ef.file_path, ef.file_type, ef.file_size, " +
               "ef.original_hash, ef.current_hash " +
               "FROM EVIDENCE e " +
               "JOIN `CASE` c    ON c.case_id   = e.case_id " +
               "JOIN USER u_col  ON u_col.user_id = e.collected_by " +
               "JOIN USER u_hld  ON u_hld.user_id = e.current_holder " +
               "LEFT JOIN EVIDENCE_FILE ef ON ef.evidence_id = e.evidence_id " +
               whereClause;
    }

    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence e = new Evidence();
        e.setEvidenceId(rs.getInt("evidence_id"));
        e.setEvidenceNumber(rs.getString("evidence_number"));
        e.setCaseId(rs.getInt("case_id"));
        e.setCaseNumber(rs.getString("case_number"));
        e.setEvidenceType(rs.getString("evidence_type"));
        e.setDescription(rs.getString("description"));
        e.setCollectedBy(rs.getInt("collected_by"));
        e.setCollectedByName(rs.getString("collected_by_name"));
        e.setCollectionLocation(rs.getString("collection_location"));
        Timestamp cd = rs.getTimestamp("collection_date");
        if (cd != null) e.setCollectionDate(cd.toLocalDateTime());
        e.setCurrentStatus(rs.getString("current_status"));
        e.setCurrentHolder(rs.getInt("current_holder"));
        e.setCurrentHolderName(rs.getString("current_holder_name"));
        Timestamp ra = rs.getTimestamp("registered_at");
        if (ra != null) e.setRegisteredAt(ra.toLocalDateTime());
        // File info (nullable - LEFT JOIN)
        try {
            e.setFileId(rs.getInt("file_id"));
            e.setFileName(rs.getString("file_name"));
            e.setFilePath(rs.getString("file_path"));
            e.setFileType(rs.getString("file_type"));
            e.setFileSize(rs.getLong("file_size"));
            e.setOriginalHash(rs.getString("original_hash"));
            e.setCurrentHash(rs.getString("current_hash"));
        } catch (SQLException ex) {
            // File info not present in this query
        }
        return e;
    }
}
