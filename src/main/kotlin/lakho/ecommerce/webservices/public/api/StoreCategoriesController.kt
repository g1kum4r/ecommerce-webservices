package lakho.ecommerce.webservices.public.api

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.common.repositories.entities.StoreCategory
import lakho.ecommerce.webservices.common.services.CategoryService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/storecategories")
class StoreCategoriesController(private val categoryService: CategoryService) {

    @GetMapping
    fun getStoreCategories(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "false") rootOnly: Boolean
    ): ResponseEntity<CustomPage<StoreCategory>> {
        val pageable = PageRequest.of(page, size)

        val categories = when {
            !search.isNullOrBlank() -> categoryService.searchStoreCategories(search, pageable)
            rootOnly -> categoryService.getRootStoreCategories(pageable)
            else -> categoryService.getAllStoreCategories(pageable)
        }

        return ResponseEntity.ok(categories)
    }

    @GetMapping("/{id}")
    fun getStoreCategoryById(@PathVariable id: Long): ResponseEntity<StoreCategory> {
        val category = categoryService.getStoreCategoryById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(category)
    }

    @GetMapping("/slug/{slug}")
    fun getStoreCategoryBySlug(@PathVariable slug: String): ResponseEntity<StoreCategory> {
        val category = categoryService.getStoreCategoryBySlug(slug)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(category)
    }

    @GetMapping("/{id}/children")
    fun getStoreCategoryChildren(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CustomPage<StoreCategory>> {
        val pageable = PageRequest.of(page, size)
        val children = categoryService.getStoreCategoryChildren(id, pageable)
        return ResponseEntity.ok(children)
    }
}
