-- ============================================================
-- Digital Evidence Chain Manager
-- procedures.sql - Stored Procedures and Functions
-- ============================================================

USE digital_evidence_manager;

DELIMITER $$

-- ============================================================
-- PROCEDURE: sp_transfer_evidence
-- Transfers evidence from one user to another as a transaction.
-- Inserts a transfer record and updates current_holder atomically.
-- ============================================================
CREATE PROCEDURE sp_transfer_evidence(
    IN  p_evidence_id   INT,
    IN  p_from_user_id  INT,
    IN  p_to_user_id    INT,
    IN  p_reason        TEXT,
    IN  p_location      VARCHAR(255),
    OUT p_result        VARCHAR(100)
)
BEGIN
    DECLARE v_current_holder INT;
    DECLARE v_evidence_count INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'ERROR: Transaction failed and rolled back.';
    END;

    START TRANSACTION;

    -- Validate evidence exists
    SELECT COUNT(*), current_holder
    INTO v_evidence_count, v_current_holder
    FROM EVIDENCE
    WHERE evidence_id = p_evidence_id;

    IF v_evidence_count = 0 THEN
        SET p_result = 'ERROR: Evidence not found.';
        ROLLBACK;
    ELSEIF v_current_holder != p_from_user_id THEN
        SET p_result = 'ERROR: Sender does not hold this evidence.';
        ROLLBACK;
    ELSE
        -- Insert transfer record
        INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, reason, location, status)
        VALUES (p_evidence_id, p_from_user_id, p_to_user_id, p_reason, p_location, 'COMPLETED');

        -- Update current holder
        UPDATE EVIDENCE
        SET current_holder = p_to_user_id,
            current_status = 'TRANSFERRED'
        WHERE evidence_id = p_evidence_id;

        COMMIT;
        SET p_result = 'SUCCESS: Evidence transferred successfully.';
    END IF;
END$$

-- ============================================================
-- PROCEDURE: sp_get_chain_of_custody
-- Returns the complete transfer history for an evidence item
-- in chronological order using a cursor.
-- ============================================================
CREATE PROCEDURE sp_get_chain_of_custody(IN p_evidence_id INT)
BEGIN
    DECLARE done         INT DEFAULT FALSE;
    DECLARE v_transfer_id INT;
    DECLARE v_from_name  VARCHAR(100);
    DECLARE v_to_name    VARCHAR(100);
    DECLARE v_date       DATETIME;
    DECLARE v_reason     TEXT;
    DECLARE v_location   VARCHAR(255);

    DECLARE cur CURSOR FOR
        SELECT t.transfer_id,
               uf.full_name  AS from_name,
               ut.full_name  AS to_name,
               t.transfer_date,
               t.reason,
               t.location
        FROM   TRANSFER t
        JOIN   USER uf ON uf.user_id = t.from_user_id
        JOIN   USER ut ON ut.user_id = t.to_user_id
        WHERE  t.evidence_id = p_evidence_id
        ORDER  BY t.transfer_date;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Temporary result table
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_chain (
        transfer_id   INT,
        from_name     VARCHAR(100),
        to_name       VARCHAR(100),
        transfer_date DATETIME,
        reason        TEXT,
        location      VARCHAR(255)
    );

    TRUNCATE TABLE tmp_chain;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_transfer_id, v_from_name, v_to_name, v_date, v_reason, v_location;
        IF done THEN
            LEAVE read_loop;
        END IF;
        INSERT INTO tmp_chain VALUES (v_transfer_id, v_from_name, v_to_name, v_date, v_reason, v_location);
    END LOOP;
    CLOSE cur;

    SELECT * FROM tmp_chain ORDER BY transfer_date;
END$$

-- ============================================================
-- PROCEDURE: sp_get_case_summary
-- Returns a summary of all cases with evidence counts
-- ============================================================
CREATE PROCEDURE sp_get_case_summary()
BEGIN
    SELECT
        c.case_id,
        c.case_number,
        c.title,
        c.status,
        c.priority,
        u.full_name    AS created_by,
        c.created_at,
        COUNT(e.evidence_id) AS total_evidence,
        SUM(CASE WHEN e.current_status = 'VERIFIED'   THEN 1 ELSE 0 END) AS verified_count,
        SUM(CASE WHEN e.current_status = 'TRANSFERRED' THEN 1 ELSE 0 END) AS transferred_count
    FROM   `CASE` c
    JOIN   USER u   ON u.user_id = c.created_by
    LEFT JOIN EVIDENCE e ON e.case_id = c.case_id
    GROUP  BY c.case_id, c.case_number, c.title, c.status, c.priority, u.full_name, c.created_at
    ORDER  BY c.created_at DESC;
END$$

-- ============================================================
-- FUNCTION: fn_count_transfers
-- Returns the number of transfers for a given evidence item
-- ============================================================
CREATE FUNCTION fn_count_transfers(p_evidence_id INT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM   TRANSFER
    WHERE  evidence_id = p_evidence_id;
    RETURN v_count;
END$$

-- ============================================================
-- FUNCTION: fn_get_evidence_status
-- Returns the current status of evidence as a string
-- ============================================================
CREATE FUNCTION fn_get_evidence_status(p_evidence_id INT)
RETURNS VARCHAR(50)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_status VARCHAR(50);
    SELECT current_status INTO v_status
    FROM   EVIDENCE
    WHERE  evidence_id = p_evidence_id;
    RETURN IFNULL(v_status, 'NOT_FOUND');
END$$

DELIMITER ;
