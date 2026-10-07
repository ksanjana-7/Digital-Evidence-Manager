-- ============================================================
-- Digital Evidence Chain Manager
-- schema.sql - Database Schema
-- ============================================================

DROP DATABASE IF EXISTS digital_evidence_manager;
CREATE DATABASE digital_evidence_manager
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE digital_evidence_manager;

-- ============================================================
-- TABLE: ROLE
-- Stores user roles (Admin, Investigator, Forensic Officer)
-- ============================================================
CREATE TABLE ROLE (
    role_id   INT          NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(50)  NOT NULL UNIQUE,
    PRIMARY KEY (role_id)
);

-- ============================================================
-- TABLE: USER
-- Stores all system users. Abstract concept mapped to table.
-- Inheritance: Admin / Investigator / ForensicOfficer all share this table
-- with role_id as discriminator.
-- ============================================================
CREATE TABLE USER (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    badge_number  VARCHAR(30),
    department    VARCHAR(100),
    role_id       INT          NOT NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES ROLE(role_id)
);

-- ============================================================
-- TABLE: CASE
-- Stores investigation cases
-- ============================================================
CREATE TABLE `CASE` (
    case_id      INT          NOT NULL AUTO_INCREMENT,
    case_number  VARCHAR(50)  NOT NULL UNIQUE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    status       ENUM('OPEN','ACTIVE','CLOSED','ARCHIVED') NOT NULL DEFAULT 'OPEN',
    priority     ENUM('LOW','MEDIUM','HIGH','CRITICAL')    NOT NULL DEFAULT 'MEDIUM',
    created_by   INT          NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at    DATETIME,
    PRIMARY KEY (case_id),
    CONSTRAINT fk_case_created_by FOREIGN KEY (created_by) REFERENCES USER(user_id)
);

-- ============================================================
-- TABLE: CASE_INVESTIGATOR
-- Many-to-many: A case can have multiple investigators
-- ============================================================
CREATE TABLE CASE_INVESTIGATOR (
    ci_id        INT      NOT NULL AUTO_INCREMENT,
    case_id      INT      NOT NULL,
    user_id      INT      NOT NULL,
    assigned_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by  INT      NOT NULL,
    PRIMARY KEY (ci_id),
    UNIQUE KEY uq_case_investigator (case_id, user_id),
    CONSTRAINT fk_ci_case    FOREIGN KEY (case_id)     REFERENCES `CASE`(case_id),
    CONSTRAINT fk_ci_user    FOREIGN KEY (user_id)     REFERENCES USER(user_id),
    CONSTRAINT fk_ci_assigned_by FOREIGN KEY (assigned_by) REFERENCES USER(user_id)
);

-- ============================================================
-- TABLE: EVIDENCE
-- Core evidence records
-- ============================================================
CREATE TABLE EVIDENCE (
    evidence_id         INT           NOT NULL AUTO_INCREMENT,
    evidence_number     VARCHAR(50)   NOT NULL UNIQUE,
    case_id             INT           NOT NULL,
    evidence_type       ENUM('LAPTOP','MOBILE','USB','HARD_DISK','IMAGE','VIDEO','DOCUMENT','LOG_FILE','OTHER') NOT NULL,
    description         TEXT,
    collected_by        INT           NOT NULL,
    collection_location VARCHAR(255)  NOT NULL,
    collection_date     DATETIME      NOT NULL,
    current_status      ENUM('REGISTERED','IN_CUSTODY','TRANSFERRED','UNDER_ANALYSIS','VERIFIED','RELEASED','COMPROMISED') NOT NULL DEFAULT 'REGISTERED',
    current_holder      INT           NOT NULL,
    registered_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (evidence_id),
    CONSTRAINT fk_evidence_case           FOREIGN KEY (case_id)        REFERENCES `CASE`(case_id),
    CONSTRAINT fk_evidence_collected_by   FOREIGN KEY (collected_by)   REFERENCES USER(user_id),
    CONSTRAINT fk_evidence_current_holder FOREIGN KEY (current_holder) REFERENCES USER(user_id)
);

-- ============================================================
-- TABLE: EVIDENCE_FILE
-- File metadata for evidence (normalized from EVIDENCE)
-- ============================================================
CREATE TABLE EVIDENCE_FILE (
    file_id       INT           NOT NULL AUTO_INCREMENT,
    evidence_id   INT           NOT NULL,
    file_name     VARCHAR(255)  NOT NULL,
    file_path     VARCHAR(500)  NOT NULL,
    file_type     VARCHAR(50),
    file_size     BIGINT,
    original_hash VARCHAR(64)   NOT NULL,
    current_hash  VARCHAR(64),
    uploaded_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (file_id),
    CONSTRAINT fk_ef_evidence FOREIGN KEY (evidence_id) REFERENCES EVIDENCE(evidence_id)
);

-- ============================================================
-- TABLE: TRANSFER
-- Records chain of custody transfers
-- ============================================================
CREATE TABLE TRANSFER (
    transfer_id   INT          NOT NULL AUTO_INCREMENT,
    evidence_id   INT          NOT NULL,
    from_user_id  INT          NOT NULL,
    to_user_id    INT          NOT NULL,
    transfer_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason        TEXT         NOT NULL,
    location      VARCHAR(255),
    status        ENUM('PENDING','COMPLETED','REJECTED') NOT NULL DEFAULT 'COMPLETED',
    notes         TEXT,
    PRIMARY KEY (transfer_id),
    CONSTRAINT fk_transfer_evidence  FOREIGN KEY (evidence_id)  REFERENCES EVIDENCE(evidence_id),
    CONSTRAINT fk_transfer_from_user FOREIGN KEY (from_user_id) REFERENCES USER(user_id),
    CONSTRAINT fk_transfer_to_user   FOREIGN KEY (to_user_id)   REFERENCES USER(user_id),
    CONSTRAINT chk_transfer_users    CHECK (from_user_id <> to_user_id)
);

-- ============================================================
-- TABLE: VERIFICATION
-- Hash verification attempts
-- ============================================================
CREATE TABLE VERIFICATION (
    verification_id   INT         NOT NULL AUTO_INCREMENT,
    evidence_id       INT         NOT NULL,
    verified_by       INT         NOT NULL,
    verification_date DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    original_hash     VARCHAR(64) NOT NULL,
    current_hash      VARCHAR(64) NOT NULL,
    result            ENUM('VERIFIED','TAMPERED','ERROR') NOT NULL,
    notes             TEXT,
    PRIMARY KEY (verification_id),
    CONSTRAINT fk_verif_evidence    FOREIGN KEY (evidence_id) REFERENCES EVIDENCE(evidence_id),
    CONSTRAINT fk_verif_verified_by FOREIGN KEY (verified_by) REFERENCES USER(user_id)
);

-- ============================================================
-- TABLE: ACCESS_LOG
-- Audit trail for every important action
-- ============================================================
CREATE TABLE ACCESS_LOG (
    log_id      INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    evidence_id INT,
    case_id     INT,
    action      ENUM('LOGIN','LOGOUT','VIEW','REGISTER','UPDATE','TRANSFER','VERIFY','DOWNLOAD','DELETE','CREATE_CASE','CLOSE_CASE','ASSIGN_INVESTIGATOR') NOT NULL,
    description TEXT,
    ip_address  VARCHAR(45),
    logged_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_log_user     FOREIGN KEY (user_id)     REFERENCES USER(user_id),
    CONSTRAINT fk_log_evidence FOREIGN KEY (evidence_id) REFERENCES EVIDENCE(evidence_id),
    CONSTRAINT fk_log_case     FOREIGN KEY (case_id)     REFERENCES `CASE`(case_id)
);
