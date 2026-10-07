-- ============================================================
-- Digital Evidence Chain Manager
-- triggers.sql - Database Triggers
-- ============================================================

USE digital_evidence_manager;

DELIMITER $$

-- ============================================================
-- TRIGGER: trg_after_transfer_insert
-- PURPOSE: Automatically insert an ACCESS_LOG record
-- whenever a transfer is completed.
-- This ensures the audit trail is always updated atomically.
-- ============================================================
CREATE TRIGGER trg_after_transfer_insert
AFTER INSERT ON TRANSFER
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' THEN
        INSERT INTO ACCESS_LOG (user_id, evidence_id, action, description)
        VALUES (
            NEW.from_user_id,
            NEW.evidence_id,
            'TRANSFER',
            CONCAT('Evidence transferred from user_id=', NEW.from_user_id,
                   ' to user_id=', NEW.to_user_id,
                   '. Reason: ', NEW.reason)
        );
    END IF;
END$$

-- ============================================================
-- TRIGGER: trg_after_verification_insert
-- PURPOSE: Automatically insert an ACCESS_LOG record
-- when a verification is recorded.
-- ============================================================
CREATE TRIGGER trg_after_verification_insert
AFTER INSERT ON VERIFICATION
FOR EACH ROW
BEGIN
    INSERT INTO ACCESS_LOG (user_id, evidence_id, action, description)
    VALUES (
        NEW.verified_by,
        NEW.evidence_id,
        'VERIFY',
        CONCAT('Verification result: ', NEW.result,
               '. Original hash: ', LEFT(NEW.original_hash, 16), '...',
               ' Current hash: ', LEFT(NEW.current_hash, 16), '...')
    );
END$$

-- ============================================================
-- TRIGGER: trg_after_evidence_insert
-- PURPOSE: Automatically insert an ACCESS_LOG record
-- when new evidence is registered.
-- ============================================================
CREATE TRIGGER trg_after_evidence_insert
AFTER INSERT ON EVIDENCE
FOR EACH ROW
BEGIN
    INSERT INTO ACCESS_LOG (user_id, evidence_id, case_id, action, description)
    VALUES (
        NEW.collected_by,
        NEW.evidence_id,
        NEW.case_id,
        'REGISTER',
        CONCAT('Evidence registered: ', NEW.evidence_number,
               ', Type: ', NEW.evidence_type,
               ', Location: ', NEW.collection_location)
    );
END$$

-- ============================================================
-- TRIGGER: trg_before_transfer_insert
-- PURPOSE: Validate that the sender actually holds the evidence
-- before allowing the transfer record to be inserted.
-- Provides concurrency protection at the DB level.
-- ============================================================
CREATE TRIGGER trg_before_transfer_insert
BEFORE INSERT ON TRANSFER
FOR EACH ROW
BEGIN
    DECLARE v_holder INT;
    SELECT current_holder INTO v_holder
    FROM EVIDENCE
    WHERE evidence_id = NEW.evidence_id;

    IF v_holder != NEW.from_user_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transfer validation failed: Sender does not hold this evidence.';
    END IF;
END$$

DELIMITER ;
