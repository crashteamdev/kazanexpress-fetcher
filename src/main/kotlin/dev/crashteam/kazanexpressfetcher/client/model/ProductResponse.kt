package dev.crashteam.kazanexpressfetcher.client.model

data class ProductResponse(
    val payload: ProductPayload
)

data class ProductPayload(
    val data: ProductData
)

data class ProductData(
    val id: Long,
    val characteristics: List<ProductCharacteristic>,
    val skuList: List<ProductSku>
)

data class ProductCharacteristic(
    val id: Int,
    val title: String,
    val values: List<ProductCharacteristicValue>
)

data class ProductCharacteristicValue(
    val id: Int,
    val title: String,
    val value: String
)

data class ProductSku(
    val id: Long,
    val availableAmount: Int,
    val characteristics: List<ProductSkuCharacteristic>
)

data class ProductSkuCharacteristic(
    val charIndex: Short,
    val valueIndex: Short
)
