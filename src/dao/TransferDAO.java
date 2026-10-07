package dao;

import model.EvidenceTransfer;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransferDAO - Data Access Object for TRANSFER table.
 *
 * IMPORTANT: insertTransfer() accepts a Connection parameter
 * so it can participate in transactions managed by TransferService.
 */
public class TransferDAO {

    /**
     * Inserts a transfer record within an existing transaction.
     *
     * @param transfer The transfer to record
     * @param conn     The active transactional connection
     * @throws SQLException if insert fails
     */
    public int insertTransfer(EvidenceTransfer transfer, Connection conn) throws SQLException {
        String sql = "INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, " +
                     "reason, location, status, notes) VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transfer.getEvidenceId());
            ps.setInt(2, transfer.getFromUserId());
            ps.setInt(3, transfer.getToUserId());
            ps.setString(4, transfer.getReason());
            ps.setString(5, transfer.getLocation());
            ps.setString(6, EvidenceTransfer.STATUS_COMPLETED);
            ps.setString(7, transfer.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Returns all transfers for a specific evidence item (chain of custody).
     * Ordered chronologically.
     */
    public List<EvidenceTransfer> getTransfersByEvidence(int evidenceId) throws SQLException {
        List<EvidenceTransfer> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "e.evidence_number, " +
                     "uf.full_name AS from_user_name, " +
                     "ut.full_name AS to_user_name " +
                     "FROM TRANSFER t " +
                     "JOIN EVIDENCE e ON e.evidence_id = t.evidence_id " +
                     "JOIN USER uf ON uf.user_id = t.from_user_id " +
                     "JOIN USER ut ON ut.user_id = t.to_user_id " +
                     "WHERE t.evidence_id = ? " +
                     "ORDER BY t.transfer_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToTransfer(rs));
            }
        }
        return list;
    }

    /**
     * Returns all transfers in the system (Admin view).
     */
    public List<EvidenceTransfer> getAllTransfers() throws SQLException {
        List<EvidenceTransfer> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "e.evidence_number, " +
                     "uf.full_name AS from_user_name, " +
                     "ut.full_name AS to_user_name " +
                     "FROM TRANSFER t " +
                     "JOIN EVIDENCE e ON e.evidence_id = t.evidence_id " +
                     "JOIN USER uf ON uf.user_id = t.from_user_id " +
                     "JOIN USER ut ON ut.user_id = t.to_user_id " +
                     "ORDER BY t.transfer_date DESC LIMIT 200";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToTransfer(rs));
        }
        return list;
    }

    /**
     * Returns transfers involving a specific user (sender or receiver).
     */
    public List<EvidenceTransfer> getTransfersByUser(int userId) throws SQLException {
        List<EvidenceTransfer> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "e.evidence_number, " +
                     "uf.full_name AS from_user_name, " +
                     "ut.full_name AS to_user_name " +
                     "FROM TRANSFER t " +
                     "JOIN EVIDENCE e ON e.evidence_id = t.evidence_id " +
                     "JOIN USER uf ON uf.user_id = t.from_user_id " +
                     "JOIN USER ut ON ut.user_id = t.to_user_id " +
                     "WHERE t.from_user_id = ? OR t.to_user_id = ? " +
                     "ORDER BY t.transfer_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToTransfer(rs));
            }
        }
        return list;
    }

    private EvidenceTransfer mapResultSetToTransfer(ResultSet rs) throws SQLException {
        EvidenceTransfer t = new EvidenceTransfer();
        t.setTransferId(rs.getInt("transfer_id"));
        t.setEvidenceId(rs.getInt("evidence_id"));
        t.setEvidenceNumber(rs.getString("evidence_number"));
        t.setFromUserId(rs.getInt("from_user_id"));
        t.setFromUserName(rs.getString("from_user_name"));
        t.setToUserId(rs.getInt("to_user_id"));
        t.setToUserName(rs.getString("to_user_name"));
        Timestamp ts = rs.getTimestamp("transfer_date");
        if (ts != null) t.setTransferDate(ts.toLocalDateTime());
        t.setReason(rs.getString("reason"));
        t.setLocation(rs.getString("location"));
        t.setStatus(rs.getString("status"));
        t.setNotes(rs.getString("notes"));
        return t;
    }
}
