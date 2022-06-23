package dev.crashteam.kazanexpressfetcher.job.model

data class ProductFetchData(
    val productInfo: Map<String, Any>,
    val sellerInfo: Map<String, Any>,
    val productReviews: MutableList<Map<*, *>>
)
