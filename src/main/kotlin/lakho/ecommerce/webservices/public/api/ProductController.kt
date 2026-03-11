package lakho.ecommerce.webservices.public.api

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.public.api.models.ProductDetailResponse
import lakho.ecommerce.webservices.public.api.models.ProductListResponse
import lakho.ecommerce.webservices.public.services.PublicProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/public/products")
class ProductController(
    private val publicProductService: PublicProductService
) {

    @GetMapping
    fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) storeId: UUID?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<CustomPage<ProductListResponse>> {
        val products = publicProductService.getProducts(page, size, categoryId, storeId, search)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{slug}")
    fun getProductBySlug(@PathVariable slug: String): ResponseEntity<ProductDetailResponse> {
        val product = publicProductService.getProductBySlug(slug)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(product)
    }

    @GetMapping("/store/{storeSlug}")
    fun getProductsByStore(
        @PathVariable storeSlug: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomPage<ProductListResponse>> {
        val products = publicProductService.getProductsByStore(storeSlug, page, size)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/featured")
    fun getFeaturedProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomPage<ProductListResponse>> {
        val products = publicProductService.getFeaturedProducts(page, size)
        return ResponseEntity.ok(products)
    }
}
