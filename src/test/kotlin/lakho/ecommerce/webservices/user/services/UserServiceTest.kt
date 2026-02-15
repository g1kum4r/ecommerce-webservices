package lakho.ecommerce.webservices.user.services

import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.RoleRepository
import lakho.ecommerce.webservices.user.repositories.UserRepository
import lakho.ecommerce.webservices.user.repositories.UserRoleRepository
import lakho.ecommerce.webservices.user.repositories.entities.Role
import lakho.ecommerce.webservices.user.repositories.entities.User
import lakho.ecommerce.webservices.user.repositories.entities.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import java.time.Instant
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userRoleRepository: UserRoleRepository
    private lateinit var roleRepository: RoleRepository
    private lateinit var userService: UserService

    private lateinit var jdbcAggregateTemplate: JdbcAggregateTemplate

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        userRoleRepository = mock(UserRoleRepository::class.java)
        roleRepository = mock(RoleRepository::class.java)
        jdbcAggregateTemplate = mock(JdbcAggregateTemplate::class.java)
        userService = UserService(userRepository, userRoleRepository, roleRepository, jdbcAggregateTemplate)
    }

    @Test
    fun `findById should return user when found`() {
        // Arrange
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "test@example.com",
            username = "testuser",
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val roles = setOf(Role(id = 1, name = "CONSUMER"))

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(roleRepository.findByUserId(userId)).thenReturn(roles)

        // Act
        val result = userService.findById(userId)

        // Assert
        assertNotNull(result)
        assertEquals(user.email, result?.email)
        assertEquals(user.username, result?.username)
        assertEquals(1, result?.roles?.size)
        verify(userRepository).findById(userId)
        verify(roleRepository).findByUserId(userId)
    }

    @Test
    fun `findById should return null when user not found`() {
        // Arrange
        val userId = UUID.randomUUID()
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act
        val result = userService.findById(userId)

        // Assert
        assertNull(result)
        verify(userRepository).findById(userId)
        verify(roleRepository, never()).findByUserId(userId)
    }

    @Test
    fun `findByEmailOrUsername should return user when found by email`() {
        // Arrange
        val email = "test@example.com"
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            username = "testuser",
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val roles = setOf(Role(id = 1, name = "CONSUMER"))

        `when`(userRepository.findByEmailOrUsername(email, email)).thenReturn(user)
        `when`(roleRepository.findByUserId(user.id!!)).thenReturn(roles)

        // Act
        val result = userService.findByEmailOrUsername(email)

        // Assert
        assertNotNull(result)
        assertEquals(email, result?.email)
        verify(userRepository).findByEmailOrUsername(email, email)
    }

    @Test
    fun `findByEmailOrUsername should return null when user not found`() {
        // Arrange
        val email = "nonexistent@example.com"
        `when`(userRepository.findByEmailOrUsername(email, email)).thenReturn(null)

        // Act
        val result = userService.findByEmailOrUsername(email)

        // Assert
        assertNull(result)
        verify(userRepository).findByEmailOrUsername(email, email)
    }

    @Test
    fun `findSecureUserByEmailOrUsername should return secure user when found`() {
        // Arrange
        val email = "test@example.com"
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            username = "testuser",
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val roles = setOf(Role(id = 1, name = "CONSUMER"))

        `when`(userRepository.findByEmailOrUsername(email, email)).thenReturn(user)
        `when`(roleRepository.findByUserId(user.id!!)).thenReturn(roles)

        // Act
        val result = userService.findSecureUserByEmailOrUsername(email)

        // Assert
        assertNotNull(result)
        assertEquals(email, result?.email)
        assertEquals("hashedPassword", result?.passwordHash)
        verify(userRepository).findByEmailOrUsername(email, email)
    }

    @Test
    fun `findAll should return all users with roles`() {
        // Arrange
        val user1 = User(
            id = UUID.randomUUID(),
            email = "user1@example.com",
            username = "user1",
            passwordHash = "hash1",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val user2 = User(
            id = UUID.randomUUID(),
            email = "user2@example.com",
            username = "user2",
            passwordHash = "hash2",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val roles = setOf(Role(id = 1, name = "CONSUMER"))

        `when`(userRepository.findAll()).thenReturn(listOf(user1, user2))
        `when`(roleRepository.findByUserId(user1.id!!)).thenReturn(roles)
        `when`(roleRepository.findByUserId(user2.id!!)).thenReturn(roles)

        // Act
        val result = userService.findAll()

        // Assert
        assertEquals(2, result.size)
        verify(userRepository).findAll()
        verify(roleRepository, times(1)).findByUserId(user1.id!!)
        verify(roleRepository, times(1)).findByUserId(user2.id!!)
    }

    @Test
    fun `findAllPaginated should return paginated users`() {
        // Arrange
        val user1 = User(
            id = UUID.randomUUID(),
            email = "user1@example.com",
            username = "user1",
            passwordHash = "hash1",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val user2 = User(
            id = UUID.randomUUID(),
            email = "user2@example.com",
            username = "user2",
            passwordHash = "hash2",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val roles = setOf(Role(id = 1, name = "CONSUMER"))
        val pageable = PageRequest.of(0, 10)

        `when`(userRepository.findAll()).thenReturn(listOf(user1, user2))
        `when`(roleRepository.findByUserId(user1.id!!)).thenReturn(roles)
        `when`(roleRepository.findByUserId(user2.id!!)).thenReturn(roles)

        // Act
        val result = userService.findAllPaginated(pageable)

        // Assert
        assertEquals(2, result.content.size)
        assertEquals(2, result.totalElements)
        verify(userRepository).findAll()
    }

    @Test
    fun `save should create user with roles`() {
        // Arrange
        val user = User(
            id = null,
            email = "new@example.com",
            username = "newuser",
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val savedUser = user.copy(id = UUID.randomUUID())
        val roles = setOf(Roles.CONSUMER)
        val roleEntities = setOf(Role(id = 1, name = "CONSUMER"))
        val userRoles = listOf(UserRole(savedUser.id!!, 1))

        `when`(userRepository.save(user)).thenReturn(savedUser)
        `when`(roleRepository.findByNameIn(listOf("CONSUMER"))).thenReturn(roleEntities.toList())
        `when`(jdbcAggregateTemplate.insertAll(anyList())).thenReturn(userRoles)
        `when`(roleRepository.findByUserId(savedUser.id!!)).thenReturn(roleEntities)

        // Act
        val result = userService.save(user, roles)

        // Assert
        assertNotNull(result)
        assertEquals(savedUser.email, result.email)
        assertEquals(1, result.roles.size)
        verify(userRepository).save(user)
        verify(roleRepository).findByNameIn(listOf("CONSUMER"))
        verify(jdbcAggregateTemplate).insertAll(anyList())
    }

    @Test
    fun `update should update user`() {
        // Arrange
        val user = User(
            id = UUID.randomUUID(),
            email = "updated@example.com",
            username = "updateduser",
            passwordHash = "hashedPassword",
            firstName = null,
            lastName = null,
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        `when`(userRepository.save(user)).thenReturn(user)

        // Act
        val result = userService.update(user)

        // Assert
        assertNotNull(result)
        assertEquals(user.email, result.email)
        verify(userRepository).save(user)
    }

    @Test
    fun `deleteById should delete user and user roles`() {
        // Arrange
        val userId = UUID.randomUUID()
        doNothing().`when`(userRoleRepository).deleteByUserId(userId)
        doNothing().`when`(userRepository).deleteById(userId)

        // Act
        userService.deleteById(userId)

        // Assert
        verify(userRoleRepository).deleteByUserId(userId)
        verify(userRepository).deleteById(userId)
    }

    @Test
    fun `existsByEmail should return true when email exists`() {
        // Arrange
        val email = "existing@example.com"
        `when`(userRepository.existsByEmail(email)).thenReturn(true)

        // Act
        val result = userService.existsByEmail(email)

        // Assert
        assertTrue(result)
        verify(userRepository).existsByEmail(email)
    }

    @Test
    fun `existsByEmail should return false when email does not exist`() {
        // Arrange
        val email = "nonexistent@example.com"
        `when`(userRepository.existsByEmail(email)).thenReturn(false)

        // Act
        val result = userService.existsByEmail(email)

        // Assert
        assertFalse(result)
        verify(userRepository).existsByEmail(email)
    }
}
