package dev.crashteam.kazanexpressfetcher.client.model

data class ProductRawResponse(
    val payload: ProductDataWrapper?,
)

data class ProductDataWrapper(
    val data: ProductDataRaw,
)

data class ProductDataRaw(
    val map: Map<String, Any>,
)
