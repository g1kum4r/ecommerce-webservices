-- liquibase formatted sql

-- changeset system:005-seed-roles
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('STORE_OWNER');
INSERT INTO roles (name) VALUES ('CONSUMER');
-- rollback DELETE FROM roles WHERE name IN ('ADMIN', 'STORE', 'CONSUMER');
