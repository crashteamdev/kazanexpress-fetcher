package dev.crashteam.kazanexpressfetcher.client.model

import java.math.BigDecimal

data class ProductResponse(
    val payload: ProductDataWrapper?,
)

data class ProductDataWrapper(
    val data: ProductData,
)

data class ProductData(
    val map: Map<String, Any>,
)
