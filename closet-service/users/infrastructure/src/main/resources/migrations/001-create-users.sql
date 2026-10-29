CREATE TABLE users (
                       id            UUID PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL,
                       username      VARCHAR(30)  NOT NULL,
                       name          VARCHAR(100) NOT NULL,
                       bio           VARCHAR(500),
                       password_hash VARCHAR(100) NOT NULL,
                       created_at    TIMESTAMPTZ  NOT NULL
);
CREATE UNIQUE INDEX uq_users_email ON users (email);
CREATE UNIQUE INDEX uq_users_username ON users (lower(username));

CREATE TABLE user_logs (
                           id         UUID PRIMARY KEY,
                           user_id    UUID        NOT NULL,
                           type       VARCHAR(50) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_user_logs_user_created ON user_logs (user_id, created_at DESC);
