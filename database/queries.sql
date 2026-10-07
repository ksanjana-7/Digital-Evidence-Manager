-- ============================================================
-- Digital Evidence Chain Manager
-- queries.sql - Representative SQL Queries for DBMS Demonstration
-- ============================================================

USE digital_evidence_manager;

-- ============================================================
-- BASIC QUERIES
-- ============================================================

-- Q1: List all active cases with creator name
SELECT c.case_id, c.case_number, c.title, c.status, c.priority,
       u.full_name AS created_by, c.created_at
FROM   `CASE` c
JOIN   USER u ON u.user_id = c.created_by
WHERE  c.status != 'CLOSED'
ORDER  BY c.created_at DESC;

-- Q2: List all evidence in a specific case (CASE-2024-001)
SELECT e.evidence_number, e.evidence_type, e.description,
       u_col.full_name AS collected_by,
       u_hld.full_name AS current_holder,
       e.current_status, e.collection_date
FROM   EVIDENCE e
JOIN   USER u_col ON u_col.user_id = e.collected_by
JOIN   USER u_hld ON u_hld.user_id = e.current_holder
JOIN   `CASE` c   ON c.case_id = e.case_id
WHERE  c.case_number = 'CASE-2024-001'
ORDER  BY e.registered_at;

-- Q3: Complete transfer history for a specific evidence item
SELECT t.transfer_id,
       uf.full_name  AS from_user,
       ut.full_name  AS to_user,
       t.transfer_date, t.reason, t.location, t.status
FROM   TRANSFER t
JOIN   USER uf ON uf.user_id = t.from_user_id
JOIN   USER ut ON ut.user_id = t.to_user_id
WHERE  t.evidence_id = 1
ORDER  BY t.transfer_date;

-- ============================================================
-- AGGREGATE FUNCTIONS AND GROUP BY
-- ============================================================

-- Q4: Count evidence per case
SELECT c.case_number, c.title,
       COUNT(e.evidence_id) AS evidence_count
FROM   `CASE` c
LEFT JOIN EVIDENCE e ON e.case_id = c.case_id
GROUP  BY c.case_id, c.case_number, c.title
ORDER  BY evidence_count DESC;

-- Q5: Investigators handling the most evidence (current holders)
SELECT u.full_name, u.badge_number,
       COUNT(e.evidence_id) AS evidence_holding
FROM   USER u
JOIN   EVIDENCE e ON e.current_holder = u.user_id
GROUP  BY u.user_id, u.full_name, u.badge_number
ORDER  BY evidence_holding DESC;

-- Q6: Evidence count by status
SELECT current_status, COUNT(*) AS total
FROM   EVIDENCE
GROUP  BY current_status
ORDER  BY total DESC;

-- Q7: Cases with more than 2 evidence items
SELECT c.case_number, c.title, COUNT(e.evidence_id) AS evidence_count
FROM   `CASE` c
JOIN   EVIDENCE e ON e.case_id = c.case_id
GROUP  BY c.case_id, c.case_number, c.title
HAVING COUNT(e.evidence_id) > 2
ORDER  BY evidence_count DESC;

-- Q8: Most active users in transfers (combined sent + received)
SELECT u.full_name, u.badge_number,
       COUNT(DISTINCT t.transfer_id) AS transfer_count
FROM   USER u
LEFT JOIN TRANSFER t ON t.from_user_id = u.user_id OR t.to_user_id = u.user_id
GROUP  BY u.user_id, u.full_name, u.badge_number
ORDER  BY transfer_count DESC;

-- ============================================================
-- JOINS
-- ============================================================

-- Q9: Evidence with file information (INNER JOIN)
SELECT e.evidence_number, e.evidence_type, e.description,
       ef.file_name, ef.file_type, ef.file_size, ef.original_hash
FROM   EVIDENCE e
JOIN   EVIDENCE_FILE ef ON ef.evidence_id = e.evidence_id
ORDER  BY e.evidence_number;

-- Q10: All cases and their evidence (LEFT JOIN - includes cases with no evidence)
SELECT c.case_number, c.title, c.status,
       COALESCE(e.evidence_number, 'No Evidence') AS evidence,
       e.evidence_type, e.current_status
FROM   `CASE` c
LEFT JOIN EVIDENCE e ON e.case_id = c.case_id
ORDER  BY c.case_number, e.evidence_number;

-- Q11: Access log with user and evidence details
SELECT al.log_id, u.full_name, u.badge_number,
       al.action,
       COALESCE(e.evidence_number, 'N/A') AS evidence_number,
       COALESCE(c.case_number, 'N/A') AS case_number,
       al.description, al.logged_at
FROM   ACCESS_LOG al
JOIN   USER u        ON u.user_id = al.user_id
LEFT JOIN EVIDENCE e ON e.evidence_id = al.evidence_id
LEFT JOIN `CASE` c   ON c.case_id = al.case_id
ORDER  BY al.logged_at DESC
LIMIT 50;

-- ============================================================
-- SUBQUERIES
-- ============================================================

-- Q12: Evidence transferred more than 1 time
SELECT e.evidence_number, e.description, e.current_status,
       (SELECT COUNT(*) FROM TRANSFER t WHERE t.evidence_id = e.evidence_id) AS transfer_count
FROM   EVIDENCE e
WHERE  (SELECT COUNT(*) FROM TRANSFER t WHERE t.evidence_id = e.evidence_id) > 1
ORDER  BY transfer_count DESC;

-- Q13: Users who have never been assigned to any case
SELECT u.user_id, u.username, u.full_name
FROM   USER u
WHERE  u.user_id NOT IN (
    SELECT ci.user_id FROM CASE_INVESTIGATOR ci
)
AND u.role_id != 1;  -- exclude admin

-- Q14: Evidence with failed verifications
SELECT e.evidence_number, e.description, v.verification_date, v.result, v.notes
FROM   EVIDENCE e
JOIN   VERIFICATION v ON v.evidence_id = e.evidence_id
WHERE  v.result = 'TAMPERED';

-- ============================================================
-- CTEs (Common Table Expressions)
-- ============================================================

-- Q15: CTE to find high-priority cases with their evidence summary
WITH HighPriorityCases AS (
    SELECT case_id, case_number, title, status, priority
    FROM   `CASE`
    WHERE  priority IN ('HIGH', 'CRITICAL')
),
EvidenceSummary AS (
    SELECT case_id,
           COUNT(*) AS total_evidence,
           SUM(CASE WHEN current_status = 'VERIFIED' THEN 1 ELSE 0 END) AS verified
    FROM   EVIDENCE
    GROUP  BY case_id
)
SELECT hpc.case_number, hpc.title, hpc.status, hpc.priority,
       COALESCE(es.total_evidence, 0) AS total_evidence,
       COALESCE(es.verified, 0) AS verified_evidence
FROM   HighPriorityCases hpc
LEFT JOIN EvidenceSummary es ON es.case_id = hpc.case_id
ORDER  BY hpc.priority, hpc.case_number;

-- Q16: CTE for complete audit summary per user
WITH UserActivity AS (
    SELECT user_id,
           COUNT(*) AS total_actions,
           MAX(logged_at) AS last_action
    FROM   ACCESS_LOG
    GROUP  BY user_id
)
SELECT u.full_name, u.username, r.role_name,
       COALESCE(ua.total_actions, 0) AS total_actions,
       ua.last_action
FROM   USER u
JOIN   ROLE r ON r.role_id = u.role_id
LEFT JOIN UserActivity ua ON ua.user_id = u.user_id
ORDER  BY total_actions DESC;

-- ============================================================
-- WINDOW FUNCTIONS
-- ============================================================

-- Q17: Show previous and current holder of each evidence using LAG()
SELECT e.evidence_number,
       t.transfer_date,
       uf.full_name AS from_user,
       ut.full_name AS to_user,
       LAG(ut.full_name) OVER (PARTITION BY t.evidence_id ORDER BY t.transfer_date) AS previous_holder,
       ut.full_name AS current_holder_after_transfer
FROM   TRANSFER t
JOIN   EVIDENCE e ON e.evidence_id = t.evidence_id
JOIN   USER uf    ON uf.user_id    = t.from_user_id
JOIN   USER ut    ON ut.user_id    = t.to_user_id
ORDER  BY t.evidence_id, t.transfer_date;

-- Q18: Rank investigators by number of cases they are assigned to
SELECT u.full_name, u.badge_number,
       COUNT(ci.case_id) AS case_count,
       RANK() OVER (ORDER BY COUNT(ci.case_id) DESC) AS investigator_rank,
       DENSE_RANK() OVER (ORDER BY COUNT(ci.case_id) DESC) AS dense_rank_val
FROM   USER u
JOIN   CASE_INVESTIGATOR ci ON ci.user_id = u.user_id
GROUP  BY u.user_id, u.full_name, u.badge_number
ORDER  BY investigator_rank;

-- Q19: Running count of transfers over time (cumulative)
SELECT t.transfer_id, e.evidence_number, t.transfer_date,
       COUNT(*) OVER (ORDER BY t.transfer_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
           AS cumulative_transfers
FROM   TRANSFER t
JOIN   EVIDENCE e ON e.evidence_id = t.evidence_id
ORDER  BY t.transfer_date;

-- Q20: Row number for access logs per user
SELECT al.log_id, u.full_name, al.action, al.logged_at,
       ROW_NUMBER() OVER (PARTITION BY al.user_id ORDER BY al.logged_at DESC) AS user_log_rank
FROM   ACCESS_LOG al
JOIN   USER u ON u.user_id = al.user_id
ORDER  BY al.user_id, user_log_rank;

-- ============================================================
-- REPORT QUERIES
-- ============================================================

-- Q21: Full case report
SELECT c.case_number, c.title, c.status, c.priority,
       u.full_name    AS created_by,
       c.created_at,
       COUNT(DISTINCT e.evidence_id)    AS evidence_count,
       COUNT(DISTINCT ci.user_id)       AS investigator_count,
       COUNT(DISTINCT t.transfer_id)    AS transfer_count
FROM   `CASE` c
JOIN   USER u                ON u.user_id  = c.created_by
LEFT JOIN EVIDENCE e         ON e.case_id  = c.case_id
LEFT JOIN CASE_INVESTIGATOR ci ON ci.case_id = c.case_id
LEFT JOIN TRANSFER t         ON t.evidence_id = e.evidence_id
GROUP  BY c.case_id, c.case_number, c.title, c.status, c.priority, u.full_name, c.created_at
ORDER  BY c.created_at DESC;

-- Q22: Using stored function to show transfer count per evidence
SELECT e.evidence_number, e.evidence_type,
       fn_count_transfers(e.evidence_id) AS transfer_count,
       fn_get_evidence_status(e.evidence_id) AS current_status
FROM   EVIDENCE e
ORDER  BY transfer_count DESC;
