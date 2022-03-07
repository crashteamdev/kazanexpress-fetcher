package dev.crashteam.kazanexpressfetcher.client.model

data class StyxResponse<T>(
    val code: Int,
    val originalStatus: Int,
    val message: String?,
    val url: String,
    val body: T?,
)