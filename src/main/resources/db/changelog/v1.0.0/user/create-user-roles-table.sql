-- liquibase formatted sql

-- changeset system:004-create-user-roles-table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);
-- rollback DROP INDEX idx_user_roles_role_id;
-- rollback DROP INDEX idx_user_roles_user_id;
-- rollback DROP TABLE user_roles;
