package lakho.ecommerce.webservices.public.api

import lakho.ecommerce.webservices.storeowner.repositories.entities.ProductCategory
import lakho.ecommerce.webservices.storeowner.services.CategoryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/productcategories")
class ProductCategoriesController(private val categoryService: CategoryService) {

    @GetMapping
    fun getProductCategories(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "false") rootOnly: Boolean
    ): ResponseEntity<Page<ProductCategory>> {
        val pageable = PageRequest.of(page, size)

        val categories = when {
            !search.isNullOrBlank() -> categoryService.searchProductCategories(search, pageable)
            rootOnly -> categoryService.getRootProductCategories(pageable)
            else -> categoryService.getAllProductCategories(pageable)
        }

        return ResponseEntity.ok(categories)
    }

    @GetMapping("/{id}")
    fun getProductCategoryById(@PathVariable id: Long): ResponseEntity<ProductCategory> {
        val category = categoryService.getProductCategoryById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(category)
    }

    @GetMapping("/slug/{slug}")
    fun getProductCategoryBySlug(@PathVariable slug: String): ResponseEntity<ProductCategory> {
        val category = categoryService.getProductCategoryBySlug(slug)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(category)
    }

    @GetMapping("/{id}/children")
    fun getProductCategoryChildren(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProductCategory>> {
        val pageable = PageRequest.of(page, size)
        val children = categoryService.getProductCategoryChildren(id, pageable)
        return ResponseEntity.ok(children)
    }
}
