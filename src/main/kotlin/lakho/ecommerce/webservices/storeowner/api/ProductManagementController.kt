package lakho.ecommerce.webservices.storeowner.api

import jakarta.validation.Valid
import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.common.services.UserService
import lakho.ecommerce.webservices.storeowner.api.models.AddProductImageRequest
import lakho.ecommerce.webservices.storeowner.api.models.AddProductVariantRequest
import lakho.ecommerce.webservices.storeowner.api.models.CreateProductRequest
import lakho.ecommerce.webservices.storeowner.api.models.ProductResponse
import lakho.ecommerce.webservices.storeowner.api.models.UpdateProductRequest
import lakho.ecommerce.webservices.storeowner.api.models.UpdateProductVariantRequest
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductImage
import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductVariant
import lakho.ecommerce.webservices.storeowner.services.ProductManagementService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/storeowner/products")
class ProductManagementController(
    private val productManagementService: ProductManagementService,
    private val userService: UserService
) {

    @PostMapping
    fun createProduct(
        authentication: Authentication,
        @Valid @RequestBody request: CreateProductRequest
    ): ResponseEntity<ProductResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val product = productManagementService.createProduct(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @GetMapping
    fun getProducts(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<CustomPage<ProductResponse>> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val products = productManagementService.getProducts(user.id, page, size, status, search)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{id}")
    fun getProduct(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<ProductResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val product = productManagementService.getProductById(user.id, id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(product)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestBody request: UpdateProductRequest
    ): ResponseEntity<ProductResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val product = productManagementService.updateProduct(user.id, id, request)
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(
        authentication: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        productManagementService.deleteProduct(user.id, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/status")
    fun changeStatus(
        authentication: Authentication,
        @PathVariable id: UUID,
        @RequestParam status: String
    ): ResponseEntity<ProductResponse> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val product = productManagementService.changeProductStatus(user.id, id, status)
        return ResponseEntity.ok(product)
    }

    @PostMapping("/{id}/images")
    fun addImage(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddProductImageRequest
    ): ResponseEntity<ProductImage> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val image = productManagementService.addImage(user.id, id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(image)
    }

    @DeleteMapping("/{id}/images/{imageId}")
    fun removeImage(
        authentication: Authentication,
        @PathVariable id: UUID,
        @PathVariable imageId: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        productManagementService.removeImage(user.id, id, imageId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/variants")
    fun addVariant(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddProductVariantRequest
    ): ResponseEntity<ProductVariant> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val variant = productManagementService.addVariant(user.id, id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/{id}/variants/{variantId}")
    fun updateVariant(
        authentication: Authentication,
        @PathVariable id: UUID,
        @PathVariable variantId: UUID,
        @RequestBody request: UpdateProductVariantRequest
    ): ResponseEntity<ProductVariant> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val variant = productManagementService.updateVariant(user.id, id, variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    fun removeVariant(
        authentication: Authentication,
        @PathVariable id: UUID,
        @PathVariable variantId: UUID
    ): ResponseEntity<Void> {
        val user = userService.findByEmailOrUsername(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        productManagementService.removeVariant(user.id, id, variantId)
        return ResponseEntity.noContent().build()
    }
}
