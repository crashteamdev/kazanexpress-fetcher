package dev.crashteam.kazanexpressfetcher.job.model

data class BrandFetchData(
    val brandInfo: Map<*, *>,
    val categoryId: Long,
    val productIds: List<Long>
)
