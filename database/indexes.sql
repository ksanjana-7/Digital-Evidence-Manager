-- ============================================================
-- Digital Evidence Chain Manager
-- indexes.sql - Performance Indexes
-- ============================================================
-- WHY INDEXES?
-- Indexes speed up SELECT queries on frequently searched columns.
-- Without an index, MySQL performs a full table scan (slow).
-- With an index, MySQL uses a B-tree structure for fast lookups.
-- Trade-off: Indexes slow down INSERT/UPDATE slightly.
-- ============================================================

USE digital_evidence_manager;

-- Index on EVIDENCE: searching by case is very common
CREATE INDEX idx_evidence_case_id     ON EVIDENCE(case_id);

-- Index on EVIDENCE: filter by status frequently
CREATE INDEX idx_evidence_status      ON EVIDENCE(current_status);

-- Index on EVIDENCE: look up who holds evidence
CREATE INDEX idx_evidence_holder      ON EVIDENCE(current_holder);

-- Index on EVIDENCE: search by type
CREATE INDEX idx_evidence_type        ON EVIDENCE(evidence_type);

-- Index on EVIDENCE_FILE: join with EVIDENCE
CREATE INDEX idx_ef_evidence_id       ON EVIDENCE_FILE(evidence_id);

-- Index on EVIDENCE_FILE: hash lookups for verification
CREATE INDEX idx_ef_original_hash     ON EVIDENCE_FILE(original_hash);

-- Index on TRANSFER: look up all transfers for an evidence item
CREATE INDEX idx_transfer_evidence_id ON TRANSFER(evidence_id);

-- Index on TRANSFER: look up transfers by sender
CREATE INDEX idx_transfer_from_user   ON TRANSFER(from_user_id);

-- Index on TRANSFER: look up transfers by receiver
CREATE INDEX idx_transfer_to_user     ON TRANSFER(to_user_id);

-- Index on TRANSFER: chronological ordering
CREATE INDEX idx_transfer_date        ON TRANSFER(transfer_date);

-- Index on VERIFICATION: find verifications for evidence
CREATE INDEX idx_verif_evidence_id    ON VERIFICATION(evidence_id);

-- Index on VERIFICATION: results filtering
CREATE INDEX idx_verif_result         ON VERIFICATION(result);

-- Index on ACCESS_LOG: filter by user
CREATE INDEX idx_log_user_id          ON ACCESS_LOG(user_id);

-- Index on ACCESS_LOG: filter by evidence
CREATE INDEX idx_log_evidence_id      ON ACCESS_LOG(evidence_id);

-- Index on ACCESS_LOG: chronological queries
CREATE INDEX idx_log_logged_at        ON ACCESS_LOG(logged_at);

-- Index on ACCESS_LOG: filter by action type
CREATE INDEX idx_log_action           ON ACCESS_LOG(action);

-- Index on CASE_INVESTIGATOR: look up cases for a user
CREATE INDEX idx_ci_user_id           ON CASE_INVESTIGATOR(user_id);

-- Index on USER: filter active users
CREATE INDEX idx_user_is_active       ON USER(is_active);

-- Index on USER: filter by role
CREATE INDEX idx_user_role_id         ON USER(role_id);
