CREATE TABLE IF NOT EXISTS audit_logs
(
    id            BIGSERIAL PRIMARY KEY,
    request_id    VARCHAR(36),
    username      VARCHAR(255),
    ip_address    VARCHAR(45),
    operation     VARCHAR(50)         NOT NULL,
    resource_type VARCHAR(50),
    resource_id   VARCHAR(255),
    status        VARCHAR(20)         NOT NULL,
    details       TEXT,
    timestamp     TIMESTAMP           NOT NULL
);

COMMENT ON COLUMN audit_logs.operation IS 'Тип операции: LOGIN, LOGOUT, REGISTER, FILE_UPLOAD, FILE_DOWNLOAD, FILE_DELETE, FILE_SCAN';
COMMENT ON COLUMN audit_logs.status IS 'Статус выполнения: SUCCESS или ERROR';
COMMENT ON COLUMN audit_logs.request_id IS 'Уникальный идентификатор HTTP запроса для трейсинга';
COMMENT ON COLUMN audit_logs.resource_type IS 'Тип ресурса: FILE, USER, UNKNOWN';

CREATE INDEX idx_audit_logs_username ON audit_logs(username);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);