-- liquibase formatted sql

-- changeset system:001-create-users-table
CREATE TABLE users (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    username   VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    credentials_expired BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users (email);
-- rollback DROP INDEX idx_users_email;
-- rollback DROP TABLE users;
