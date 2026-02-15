package lakho.ecommerce.webservices.admin.services

import lakho.ecommerce.webservices.user.Roles
import lakho.ecommerce.webservices.user.repositories.entities.Role
import lakho.ecommerce.webservices.user.repositories.models.User
import lakho.ecommerce.webservices.user.services.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.*

class AdminServiceTest {

    private lateinit var userService: UserService
    private lateinit var adminService: AdminService

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        adminService = AdminService(userService)
    }

    @Test
    fun `getAllUsers should return paginated user summaries`() {
        // Arrange
        val user1 = User(
            id = UUID.randomUUID(),
            email = "user1@example.com",
            username = "user1",
            roles = setOf(Role(1, Roles.CONSUMER.name)),
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            firstName = "",
            lastName = ""
        )
        val user2 = User(
            id = UUID.randomUUID(),
            email = "user2@example.com",
            username = "user2",
            roles = setOf(Role(2, Roles.STORE.name)),
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true,
            firstName = "",
            lastName = "",
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl<User>(listOf(user1, user2), pageable, 2)

        `when`(userService.findAllPaginated(pageable)).thenReturn(page)

        // Act
        val result = adminService.getAllUsers(pageable)

        // Assert
        assertEquals(2, result.content.size)
        assertEquals(user1.email, result.content[0].email)
        assertEquals(user2.email, result.content[1].email)
        verify(userService).findAllPaginated(pageable)
    }

    @Test
    fun `getConsumers should return paginated consumer summaries`() {
        // Arrange
        val consumer = User(
            id = UUID.randomUUID(),
            email = "consumer@example.com",
            username = "consumer",
            roles = setOf(Role(1, Roles.CONSUMER.name)),
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(consumer), pageable, 1)

        `when`(userService.findByRoles(setOf(Roles.CONSUMER), pageable)).thenReturn(page)

        // Act
        val result = adminService.getConsumers(pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(consumer.email, result.content[0].email)
        assertTrue(result.content[0].roles.contains(Role(1, Roles.CONSUMER.name)))
        verify(userService).findByRoles(setOf(Roles.CONSUMER), pageable)
    }

    @Test
    fun `getStores should return paginated store summaries`() {
        // Arrange
        val store = User(
            id = UUID.randomUUID(),
            email = "store@example.com",
            username = "store",
            roles = setOf(Role(1, Roles.STORE.name)),
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(store), pageable, 1)

        `when`(userService.findByRoles(setOf(Roles.STORE), pageable)).thenReturn(page)

        // Act
        val result = adminService.getStores(pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(store.email, result.content[0].email)
        assertTrue(result.content[0].roles.contains(Role(2, Roles.STORE.name)))
        verify(userService).findByRoles(setOf(Roles.STORE), pageable)
    }

    @Test
    fun `getUserById should return user summary when user exists`() {
        // Arrange
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "user@example.com",
            username = "user",
            roles = setOf(Role(1, Roles.CONSUMER.name)),
            accountExpired = false,
            accountLocked = false,
            credentialsExpired = false,
            enabled = true
        )

        `when`(userService.findById(userId)).thenReturn(user)

        // Act
        val result = adminService.getUserById(userId)

        // Assert
        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals(user.email, result?.email)
        assertEquals(user.username, result?.username)
        verify(userService).findById(userId)
    }

    @Test
    fun `getUserById should return null when user does not exist`() {
        // Arrange
        val userId = UUID.randomUUID()
        `when`(userService.findById(userId)).thenReturn(null)

        // Act
        val result = adminService.getUserById(userId)

        // Assert
        assertNull(result)
        verify(userService).findById(userId)
    }

    @Test
    fun `getAllUsers should handle empty page`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<User>(emptyList(), pageable, 0)

        `when`(userService.findAllPaginated(pageable)).thenReturn(emptyPage)

        // Act
        val result = adminService.getAllUsers(pageable)

        // Assert
        assertTrue(result.content.isEmpty())
        assertEquals(0, result.totalElements)
        verify(userService).findAllPaginated(pageable)
    }

    @Test
    fun `getUserById should map user properties correctly`() {
        // Arrange
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "test@example.com",
            username = "testuser",
            roles = setOf(Role(1, Roles.ADMIN.name), Role(2, Roles.CONSUMER.name)),
            accountExpired = true,
            accountLocked = true,
            credentialsExpired = true,
            enabled = false
        )

        `when`(userService.findById(userId)).thenReturn(user)

        // Act
        val result = adminService.getUserById(userId)

        // Assert
        assertNotNull(result)
        assertEquals(2, result?.roles?.size)
        assertTrue(result?.roles?.contains(Role(1, Roles.ADMIN.name)) ?: false)
        assertTrue(result?.roles?.contains(Role(2, Roles.CONSUMER.name)) ?: false)
        assertEquals(true, result?.accountNonExpired)
        assertEquals(true, result?.accountNonLocked)
        assertEquals(true, result?.credentialsNonExpired)
        assertEquals(false, result?.enabled)
    }
}
