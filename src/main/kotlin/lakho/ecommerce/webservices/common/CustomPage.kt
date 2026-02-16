package lakho.ecommerce.webservices.common

/**
 * Custom implementation of pageable results matching Spring's Page interface properties.
 * This is needed because Spring Data JDBC's @Query methods don't support returning Page<T>.
 *
 * Property names match org.springframework.data.domain.Page for compatibility.
 */
data class CustomPage<T>(
    val content: List<T>,              // List of results
    val totalElements: Long,           // Total number of records
    val totalPages: Int,               // Total number of pages
    val size: Int,                     // Page size (number of items per page)
    val number: Int,                   // Current page number (0-indexed)
    val numberOfElements: Int,         // Number of elements in current page
    val first: Boolean,                // Is this the first page?
    val last: Boolean,                 // Is this the last page?
    val empty: Boolean                 // Is the page empty?
) {
    companion object {
        /**
         * Creates a CustomPage from repository results.
         *
         * @param content List of entities from repository
         * @param totalElements Total count from count query
         * @param pageNumber Current page number (0-indexed)
         * @param pageSize Number of items per page
         */
        fun <T> of(content: List<T>, totalElements: Long, pageNumber: Int, pageSize: Int): CustomPage<T> {
            val totalPages = if (pageSize > 0) ((totalElements + pageSize - 1) / pageSize).toInt() else 0
            val numberOfElements = content.size
            val first = pageNumber == 0
            val last = pageNumber >= totalPages - 1 || totalPages == 0
            val empty = content.isEmpty()

            return CustomPage(
                content = content,
                totalElements = totalElements,
                totalPages = totalPages,
                size = pageSize,
                number = pageNumber,
                numberOfElements = numberOfElements,
                first = first,
                last = last,
                empty = empty
            )
        }
    }
}
