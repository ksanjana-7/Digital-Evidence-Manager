-- ============================================================
-- Digital Evidence Chain Manager
-- sample_data.sql - Realistic Sample Data
-- ============================================================

USE digital_evidence_manager;

-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO ROLE (role_name) VALUES
('ADMIN'),
('INVESTIGATOR'),
('FORENSIC_OFFICER');

-- ============================================================
-- USERS (passwords are SHA-256 of: admin123, inv123, etc.)
-- Plain-text passwords for demo:
--   admin      -> admin123
--   inv001     -> inv123
--   inv002     -> inv456
--   forensic01 -> for123
--   forensic02 -> for456
-- The Java app stores SHA-256 of the password.
-- Precomputed SHA-256 values below:
--   admin123  -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
--   inv123    -> 3b14f16d0e4b1f62ea23a8e6d02d4c1a5fb2fc37c2d57fbef4b74a00b0a7c93
-- For simplicity in sample_data, store SHA-256('password') for all
-- SHA-256 of 'admin123'   = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- SHA-256 of 'inv123'     = a6b7c6b42c8f11b66af3e6f28ae1d42fcf3ecb1d0d45b84a20a17ee8a5cc9e6f (approx)
-- We will use a fixed known hash for demo:
-- All demo user passwords = 'password123'
-- SHA-256('password123')  = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- ============================================================
INSERT INTO USER (username, password_hash, full_name, email, phone, badge_number, department, role_id, is_active) VALUES
('admin',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'System Administrator', 'admin@dem.gov',       '9800000001', 'ADM-001', 'Administration',    1, 1),
('inv001',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Investigator Arjun',   'arjun@dem.gov',       '9800000002', 'INV-101', 'Cybercrime Unit',   2, 1),
('inv002',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Investigator Priya',   'priya@dem.gov',       '9800000003', 'INV-102', 'Digital Forensics', 2, 1),
('forensic01', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Dr. Rahul Sharma',     'rahul.s@dem.gov',     '9800000004', 'FOR-201', 'Forensic Lab',      3, 1),
('forensic02', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Dr. Meena Patel',      'meena.p@dem.gov',     '9800000005', 'FOR-202', 'Forensic Lab',      3, 1),
('inv003',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Investigator Vikram',  'vikram@dem.gov',      '9800000006', 'INV-103', 'Cybercrime Unit',   2, 1);

-- ============================================================
-- CASES
-- ============================================================
INSERT INTO `CASE` (case_number, title, description, status, priority, created_by, created_at) VALUES
('CASE-2024-001', 'Corporate Data Breach Investigation',   'Investigation into unauthorized access to corporate servers resulting in data exfiltration.',           'ACTIVE',  'CRITICAL', 2, '2024-01-15 09:00:00'),
('CASE-2024-002', 'Online Fraud Investigation',            'Multiple complaints received about phishing emails targeting bank account holders.',                    'ACTIVE',  'HIGH',     3, '2024-02-10 10:30:00'),
('CASE-2024-003', 'Insider Threat Analysis',               'Suspected insider leaking confidential product blueprints to competitors via removable media.',          'OPEN',    'HIGH',     2, '2024-03-05 11:00:00'),
('CASE-2024-004', 'Ransomware Attack Response',            'Critical infrastructure hit by ransomware. Recovery and forensic investigation underway.',              'ACTIVE',  'CRITICAL', 1, '2024-03-20 08:00:00'),
('CASE-2024-005', 'Child Safety Cybercrime Case',          'Investigation into online exploitation activities involving digital communications.',                    'ACTIVE',  'CRITICAL', 2, '2024-04-01 09:00:00'),
('CASE-2023-099', 'Legacy Fraud Case',                     'Old case from 2023 now closed after successful prosecution.',                                           'CLOSED',  'MEDIUM',   3, '2023-11-01 09:00:00');

-- ============================================================
-- CASE INVESTIGATORS
-- ============================================================
INSERT INTO CASE_INVESTIGATOR (case_id, user_id, assigned_by) VALUES
(1, 2, 1), (1, 4, 1),
(2, 3, 1), (2, 5, 1),
(3, 2, 1), (3, 6, 1),
(4, 6, 1), (4, 4, 1),
(5, 2, 1), (5, 3, 1),
(6, 3, 1);

-- ============================================================
-- EVIDENCE (Note: file data inserted after in EVIDENCE_FILE)
-- ============================================================
INSERT INTO EVIDENCE (evidence_number, case_id, evidence_type, description, collected_by, collection_location, collection_date, current_status, current_holder, registered_at) VALUES
('EV-2024-001-01', 1, 'LAPTOP',    'Dell Latitude laptop seized from suspect office, BitLocker encrypted',           2, 'TechCorp HQ, Floor 3, Desk 12',    '2024-01-16 10:00:00', 'VERIFIED',    4, '2024-01-16 11:00:00'),
('EV-2024-001-02', 1, 'USB',       'USB drive found in suspect personal bag, 64GB Kingston',                        2, 'TechCorp HQ, Parking Lot',          '2024-01-16 10:30:00', 'TRANSFERRED', 5, '2024-01-16 11:30:00'),
('EV-2024-001-03', 1, 'LOG_FILE',  'Server access logs from Jan 1-15 extracted from SIEM system',                   4, 'TechCorp Server Room',              '2024-01-17 09:00:00', 'IN_CUSTODY',  4, '2024-01-17 10:00:00'),
('EV-2024-002-01', 2, 'DOCUMENT',  'Phishing email samples collected from victim inboxes - PDF exports',            3, 'Victim Residence, Mumbai',          '2024-02-11 11:00:00', 'VERIFIED',    5, '2024-02-11 12:00:00'),
('EV-2024-002-02', 2, 'MOBILE',    'Samsung Galaxy phone of primary suspect seized during raid',                    3, 'Suspect Apartment, Pune',           '2024-02-15 08:00:00', 'UNDER_ANALYSIS', 4, '2024-02-15 09:00:00'),
('EV-2024-003-01', 3, 'USB',       '32GB USB drive found at insider suspect workstation',                           2, 'DesignCorp Office, Bangalore',      '2024-03-06 14:00:00', 'REGISTERED',  2, '2024-03-06 15:00:00'),
('EV-2024-004-01', 4, 'HARD_DISK', 'Encrypted hard disk from infected workstation',                                 6, 'City Hospital Server Room',         '2024-03-21 07:00:00', 'UNDER_ANALYSIS', 4, '2024-03-21 08:00:00'),
('EV-2024-004-02', 4, 'LOG_FILE',  'Firewall and IDS logs from ransomware attack window',                           6, 'City Hospital NOC',                 '2024-03-21 09:00:00', 'IN_CUSTODY',  6, '2024-03-21 10:00:00');

-- ============================================================
-- EVIDENCE FILES (file metadata only; actual file paths are local)
-- original_hash = SHA-256 of a sample known string (for demo)
-- ============================================================
INSERT INTO EVIDENCE_FILE (evidence_id, file_name, file_path, file_type, file_size, original_hash, current_hash) VALUES
(1, 'dell_laptop_image.E01', 'Evidence/CASE-2024-001/EV-2024-001-01/dell_laptop_image.E01', 'E01', 2147483648, 'a3f5c8d2e1b4970341a2c5d9e8f7b3c1a2d5e6f7890a1b2c3d4e5f6a7b8c9d0e', 'a3f5c8d2e1b4970341a2c5d9e8f7b3c1a2d5e6f7890a1b2c3d4e5f6a7b8c9d0e'),
(2, 'kingston_usb_image.img', 'Evidence/CASE-2024-001/EV-2024-001-02/kingston_usb_image.img', 'IMG', 68719476736, 'b4c6d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8', 'b4c6d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8'),
(3, 'server_access_logs.txt', 'Evidence/CASE-2024-001/EV-2024-001-03/server_access_logs.txt', 'TXT', 524288, 'c5d7e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9', 'c5d7e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9'),
(4, 'phishing_emails.pdf',    'Evidence/CASE-2024-002/EV-2024-002-01/phishing_emails.pdf',    'PDF', 10485760, 'd6e8f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0', 'd6e8f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0'),
(5, 'samsung_phone_dump.tar', 'Evidence/CASE-2024-002/EV-2024-002-02/samsung_phone_dump.tar', 'TAR', 4294967296, 'e7f9a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1', 'e7f9a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1'),
(6, 'usb_32gb_contents.zip',  'Evidence/CASE-2024-003/EV-2024-003-01/usb_32gb_contents.zip',  'ZIP', 1073741824, 'f8a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2', 'f8a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2'),
(7, 'hdd_encrypted_image.E01','Evidence/CASE-2024-004/EV-2024-004-01/hdd_encrypted_image.E01','E01', 107374182400,'a9b1c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2a3', 'a9b1c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2a3'),
(8, 'firewall_ids_logs.zip',  'Evidence/CASE-2024-004/EV-2024-004-02/firewall_ids_logs.zip',  'ZIP', 209715200, 'b0c2d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2a3b4', 'b0c2d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0e1f2a3b4');

-- ============================================================
-- TRANSFERS (chain of custody events)
-- Note: trg_before_transfer_insert will validate current_holder.
-- We insert in order to maintain validity.
-- ============================================================

-- EV-2024-001-01: Collected by inv001(2), then transferred to forensic01(4)
INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, reason, location, status)
VALUES (1, 2, 4, 'Sent to forensic lab for disk imaging and analysis', 'Forensic Lab, HQ', 'COMPLETED');

-- EV-2024-001-02: Collected by inv001(2) -> forensic02(5)
INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, reason, location, status)
VALUES (2, 2, 5, 'Transferred to forensic lab for USB content analysis', 'Forensic Lab, HQ', 'COMPLETED');

-- EV-2024-002-01: Collected by inv002(3) -> forensic02(5)
INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, reason, location, status)
VALUES (4, 3, 5, 'Transferred for email header analysis', 'Forensic Lab, HQ', 'COMPLETED');

-- EV-2024-002-02: Collected by inv002(3) -> forensic01(4)
INSERT INTO TRANSFER (evidence_id, from_user_id, to_user_id, reason, location, status)
VALUES (5, 3, 4, 'Phone transferred for data extraction', 'Forensic Lab, HQ', 'COMPLETED');

-- ============================================================
-- VERIFICATIONS
-- ============================================================
INSERT INTO VERIFICATION (evidence_id, verified_by, original_hash, current_hash, result, notes) VALUES
(1, 4, 'a3f5c8d2e1b4970341a2c5d9e8f7b3c1a2d5e6f7890a1b2c3d4e5f6a7b8c9d0e', 'a3f5c8d2e1b4970341a2c5d9e8f7b3c1a2d5e6f7890a1b2c3d4e5f6a7b8c9d0e', 'VERIFIED', 'Laptop image integrity confirmed before analysis'),
(4, 5, 'd6e8f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0', 'd6e8f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5f6a7b8c9d0', 'VERIFIED', 'PDF evidence verified intact');
