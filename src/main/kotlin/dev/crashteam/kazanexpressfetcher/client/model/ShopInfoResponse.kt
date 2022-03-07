package dev.crashteam.kazanexpressfetcher.client.model

import java.math.BigDecimal

data class ShopInfoResponse(
    val payload: SellerInfo?,
    val error: String?
)

data class SellerInfo(
    val id: Long,
    val title: String,
    val link: String,
    val banner: String,
    val avatar: String,
    val description: String,
    val hasCharityProducts: Boolean,
    val registrationDate: Long,
    val rating: BigDecimal,
    val reviews: Long,
    val orders: Long,
    val official: Boolean,
    val accountId: Long,
    val info: SellerLegalInfo
)

data class SellerLegalInfo(
    val ogrnip: String,
    val accountName: String
)
