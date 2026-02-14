package lakho.ecommerce.webservices.user.repositories.entities.mapper

import lakho.ecommerce.webservices.user.repositories.entities.Role
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class RoleMapper : RowMapper<Role> {
    override fun mapRow(
        rs: ResultSet,
        rowNum: Int
    ): Role = Role(rs.getLong("id"), rs.getString("name"))
}