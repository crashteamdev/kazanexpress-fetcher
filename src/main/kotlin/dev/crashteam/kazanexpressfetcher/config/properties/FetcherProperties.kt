package dev.crashteam.kazanexpressfetcher.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "fetcher")
data class FetcherProperties(
    @field:NotEmpty
    val productFetchCron: String? = null,
    @field:NotEmpty
    val categoryFetchCron: String? = null,
    @field:NotEmpty
    val brandFetchCron: String? = null,
    @field:NotEmpty
    val streamTopicName: String? = null,
    @field:NotEmpty
    val productPositionFetchCron: String? = null,
)
