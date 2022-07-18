package dev.crashteam.kazanexpressfetcher.client.model

data class CategoryProductsGraphQlResponseWrapper(
    val data: CategoryProductsGraphQlResponse?
)

data class CategoryProductsGraphQlResponse(
    val makeSearch: CategoryProductsSearchResponse
)

data class CategoryProductsSearchResponse(
    val items: List<CategoryProductItem>
)

data class CategoryProductItem(
    val catalogCard: ProductItemCard
)

data class ProductItemCard(
    val title: String,
    val productId: Long,
    val characteristicValues: List<ProductItemCardCharacteristic>
)

data class ProductItemCardCharacteristic(
    val id: Int,
    val value: String,
    val title: String
)
