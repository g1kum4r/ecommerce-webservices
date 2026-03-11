package lakho.ecommerce.webservices.consumer.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.consumer.repositories.WishlistRepository
import lakho.ecommerce.webservices.consumer.repositories.entities.WishlistItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WishlistService(
    private val wishlistRepository: WishlistRepository
) {
    @Transactional(readOnly = true)
    fun getWishlist(userId: UUID, page: Int, size: Int): CustomPage<WishlistItem> {
        val offset = page * size
        val items = wishlistRepository.findByUserId(userId, size, offset)
        val total = wishlistRepository.countByUserId(userId)
        return CustomPage.of(items, total, page, size)
    }

    @Transactional(readOnly = false)
    fun addToWishlist(userId: UUID, productId: UUID): WishlistItem {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw IllegalArgumentException("Product already in wishlist")
        }
        val item = WishlistItem(userId = userId, productId = productId)
        return wishlistRepository.save(item)
    }

    @Transactional(readOnly = false)
    fun removeFromWishlist(userId: UUID, productId: UUID) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId)
    }
}
