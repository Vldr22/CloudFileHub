ALTER TABLE file_metadata
    ADD COLUMN scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SCAN';

COMMENT ON COLUMN file_metadata.scan_status IS 'Статус антивирусного сканирования (PENDING_SCAN, CLEAN, INFECTED, ERROR)';