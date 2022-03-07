package dev.crashteam.kazanexpressfetcher.client.model

data class RootCategoriesResponse(
    val payload: List<SimpleCategory>
)

data class SimpleCategory(
    val id: Long,
    val productAmount: Long,
    val adult: Boolean,
    val eco: Boolean,
    val title: String,
    val path: List<String>?,
    val children: List<SimpleCategory>
)

object CategoryIdExtractor {
    fun extractCategoryIds(category: SimpleCategory, ids: MutableList<Long> = mutableListOf()): MutableList<Long> {
        for (childCategory in category.children) {
            ids.add(childCategory.id)
            extractCategoryIds(childCategory, ids)
        }
        return ids
    }
}
