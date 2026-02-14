-- liquibase formatted sql

-- changeset system:006-seed-admin-user
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (email, username, password_hash, account_expired, account_locked, credentials_expired, enabled)
VALUES ('admin@ecommerce.com', 'admin', '$2a$10$DjrLYJIWpw9Hsb2IVOoTq.3qwd8cYyhh.SG87Y0jqgyfOrr1B3dD.', FALSE, FALSE, FALSE, TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@ecommerce.com' AND r.name = 'ADMIN';
-- rollback DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE email = 'admin@ecommerce.com');
-- rollback DELETE FROM users WHERE email = 'admin@ecommerce.com';
