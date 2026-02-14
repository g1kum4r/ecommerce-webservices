package lakho.ecommerce.webservices.admin.api

import lakho.ecommerce.webservices.admin.api.models.UserSummary
import lakho.ecommerce.webservices.admin.services.AdminService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/users")
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserSummary>> {
        val pageable: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(adminService.getAllUsers(pageable))
    }

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserSummary> {
        val user = adminService.getUserById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @GetMapping("/consumers")
    fun listConsumers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserSummary>> {
        val pageable: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(adminService.getConsumers(pageable))
    }

    @GetMapping("/stores")
    fun listStores(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserSummary>> {
        val pageable: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(adminService.getStores(pageable))
    }
}
