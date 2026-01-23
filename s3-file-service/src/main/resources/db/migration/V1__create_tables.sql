CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL DEFAULT 'USER',
    upload_status VARCHAR(20)         NOT NULL DEFAULT 'NOT_UPLOADED'
);

COMMENT ON COLUMN users.role IS 'Роль пользователя: ADMIN или USER';
COMMENT ON COLUMN users.upload_status IS 'Статус загрузки файлов: NOT_UPLOADED, FILE_UPLOADED, UNLIMITED';

CREATE TABLE IF NOT EXISTS file_metadata
(
    id            BIGSERIAL PRIMARY KEY,
    unique_name   VARCHAR(64)   NOT NULL UNIQUE,
    original_name VARCHAR(1024) NOT NULL,
    type          VARCHAR(255),
    size          BIGINT        NOT NULL,
    file_hash     VARCHAR(64)   NOT NULL,
    user_id       BIGINT        NOT NULL,

    CONSTRAINT fk_file_metadata_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_file_hash_user
        UNIQUE (file_hash, user_id)
);