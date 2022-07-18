package dev.crashteam.kazanexpressfetcher.client.model

data class KeGraphQlRequestBody(
    val operationName: String,
    val variables: KeGraphQlRequestVariables,
    val query: String
)

data class KeGraphQlRequestVariables(
    val queryInput: KeQueryInput
)

data class KeQueryInput(
    val categoryId: String,
    val showAdultContent: String,
    val filters: List<String> = emptyList(),
    val sort: String,
    val pagination: KeGraphQlPagination
)

data class KeGraphQlPagination(
    val offset: Int,
    val limit: Int
)
