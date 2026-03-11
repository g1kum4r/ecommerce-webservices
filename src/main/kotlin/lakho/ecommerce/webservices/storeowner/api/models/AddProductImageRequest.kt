package lakho.ecommerce.webservices.storeowner.api.models

import jakarta.validation.constraints.NotBlank

data class AddProductImageRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,
    val altText: String? = null,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false
)
