package dev.crashteam.kazanexpressfetcher.client.model

data class ProductReviewResponse(
    val payload: List<ProductReview>?,
    val error: String?
)

data class ProductReview(
    val id: Long,
    val reviewId: Long,
    val productId: Long,
    val date: Long,
    val edited: Boolean,
    val like: Boolean,
    val dislike: Boolean,
    val customer: String,
    val reply: ProductReviewReply,
    val rating: Short,
    val content: String,
    val photos: List<ProductReviewPhoto>,
    val status: String,
    val amountLike: Long,
    val amountDislike: Long,
    val isAnonymous: Boolean,
)

data class ProductReviewPhoto(
    val photoKey: String
)

data class ProductReviewReply(
    val id: Long,
    val date: Long,
    val edited: Boolean,
    val content: String,
    val shop: String,
)
