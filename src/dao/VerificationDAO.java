package dao;

import model.Verification;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VerificationDAO - Data Access Object for VERIFICATION table.
 */
public class VerificationDAO {

    /**
     * Records a verification attempt.
     */
    public int insertVerification(Verification v) throws SQLException {
        String sql = "INSERT INTO VERIFICATION (evidence_id, verified_by, original_hash, " +
                     "current_hash, result, notes) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getEvidenceId());
            ps.setInt(2, v.getVerifiedBy());
            ps.setString(3, v.getOriginalHash());
            ps.setString(4, v.getCurrentHash());
            ps.setString(5, v.getResult());
            ps.setString(6, v.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Returns all verifications for a specific evidence item.
     */
    public List<Verification> getVerificationsByEvidence(int evidenceId) throws SQLException {
        List<Verification> list = new ArrayList<>();
        String sql = "SELECT v.*, e.evidence_number, u.full_name AS verified_by_name " +
                     "FROM VERIFICATION v " +
                     "JOIN EVIDENCE e ON e.evidence_id = v.evidence_id " +
                     "JOIN USER u     ON u.user_id     = v.verified_by " +
                     "WHERE v.evidence_id = ? " +
                     "ORDER BY v.verification_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToVerification(rs));
            }
        }
        return list;
    }

    /**
     * Returns recent verifications for all evidence.
     */
    public List<Verification> getAllVerifications() throws SQLException {
        List<Verification> list = new ArrayList<>();
        String sql = "SELECT v.*, e.evidence_number, u.full_name AS verified_by_name " +
                     "FROM VERIFICATION v " +
                     "JOIN EVIDENCE e ON e.evidence_id = v.evidence_id " +
                     "JOIN USER u     ON u.user_id     = v.verified_by " +
                     "ORDER BY v.verification_date DESC LIMIT 100";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToVerification(rs));
        }
        return list;
    }

    private Verification mapResultSetToVerification(ResultSet rs) throws SQLException {
        Verification v = new Verification();
        v.setVerificationId(rs.getInt("verification_id"));
        v.setEvidenceId(rs.getInt("evidence_id"));
        v.setEvidenceNumber(rs.getString("evidence_number"));
        v.setVerifiedBy(rs.getInt("verified_by"));
        v.setVerifiedByName(rs.getString("verified_by_name"));
        Timestamp ts = rs.getTimestamp("verification_date");
        if (ts != null) v.setVerificationDate(ts.toLocalDateTime());
        v.setOriginalHash(rs.getString("original_hash"));
        v.setCurrentHash(rs.getString("current_hash"));
        v.setResult(rs.getString("result"));
        v.setNotes(rs.getString("notes"));
        return v;
    }
}
