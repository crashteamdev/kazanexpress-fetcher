package dev.crashteam.kazanexpressfetcher.job.model

data class ProductFetchData(
    val productInfo: HashMap<String, Any>,
    val sellerInfo: HashMap<String, Any>,
    val productReviews: MutableList<Map<*, *>>
)
