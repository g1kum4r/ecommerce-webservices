package lakho.ecommerce.webservices.user.repositories

import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRoleRepository(private val jdbcTemplate: JdbcTemplate) {

    fun findRolesByUserId(userId: UUID): Set<UserRole> {
        val sql = """
            SELECT r.name
            FROM roles r
            INNER JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            UserRole.valueOf(rs.getString("name"))
        }, userId).toSet()
    }

    fun saveUserRoles(userId: UUID, roles: Set<UserRole>) {
        // First delete existing roles
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId)

        // Then insert new roles
        val insertSql = """
            INSERT INTO user_roles (user_id, role_id)
            SELECT ?, id FROM roles WHERE name = ?
        """.trimIndent()

        roles.forEach { role ->
            jdbcTemplate.update(insertSql, userId, role.name)
        }
    }

    fun deleteUserRoles(userId: UUID) {
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId)
    }
}
