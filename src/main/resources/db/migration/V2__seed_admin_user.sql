-- Password: admin123 (BCrypt encoded)
INSERT INTO users (email, password_hash, role, active)
VALUES ('admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE);
