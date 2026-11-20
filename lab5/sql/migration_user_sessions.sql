USE java_app;

CREATE TABLE IF NOT EXISTS user_sessions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(150) NOT NULL,
    device_id VARCHAR(255) NULL,
    access_token TEXT CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    refresh_token TEXT CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    access_token_expiry TIMESTAMP(0) NOT NULL,
    refresh_token_expiry TIMESTAMP(0) NOT NULL,
    status VARCHAR(16) NOT NULL,

    CONSTRAINT chk_user_sessions_status
        CHECK (status IN ('ACTIVE', 'USED', 'REVOKED')),

    UNIQUE KEY uq_user_sessions_refresh_token (refresh_token(512)),
    INDEX idx_user_sessions_username_status (username, status),
    INDEX idx_user_sessions_refresh_expiry (refresh_token_expiry),

    CONSTRAINT fk_user_sessions_username
        FOREIGN KEY (username)
        REFERENCES users(email)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

GRANT SELECT, INSERT, UPDATE ON java_app.user_sessions TO 'admin'@'localhost';
