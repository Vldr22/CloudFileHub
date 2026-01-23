ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

COMMENT ON COLUMN users.status IS 'Статус аккаунта пользователя (ACTIVE, BLOCKED)';