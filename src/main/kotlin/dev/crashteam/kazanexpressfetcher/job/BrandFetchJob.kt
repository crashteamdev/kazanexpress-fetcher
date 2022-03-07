package dev.crashteam.kazanexpressfetcher.job

import com.google.protobuf.Timestamp
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.client.model.CategoryIdExtractor
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import dev.crashteam.kazanexpressfetcher.job.model.BrandFetchData
import dev.crashteam.kazanexpressfetcher.service.StreamService
import dev.crashteam.kz.fetcher.BrandFetch
import dev.crashteam.kz.fetcher.FetchKazanExpressEvent
import mu.KotlinLogging
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.core.convert.ConversionService
import java.time.Instant

private val log = KotlinLogging.logger {}

@DisallowConcurrentExecution
class BrandFetchJob : Job {

    override fun execute(context: JobExecutionContext) {
        val applicationContext = context.getApplicationContext()
        val kazanExpressClient = applicationContext.getBean(KazanExpressClient::class.java)
        val conversionService = applicationContext.getBean(ConversionService::class.java)
        val streamService = applicationContext.getBean(StreamService::class.java)
        val rootCategories = kazanExpressClient.getRootCategories()
            ?: throw IllegalStateException("Can't get root categories")
        val categoryIds = rootCategories.payload.flatMap { CategoryIdExtractor.extractCategoryIds(it) }
        for (categoryId in categoryIds) {
            val categoryBrandResponse = kazanExpressClient.getCategoryBrands(categoryId)
                ?: throw IllegalStateException("Can't category brands")
            val categoryBrandResponsePayload = categoryBrandResponse["payload"] as? Map<*, *>
            val categoryBrandResponseFilters = categoryBrandResponsePayload?.get("filters") as? List<Map<*, *>>
            val brands = categoryBrandResponseFilters?.get(2)?.get("values") as? List<Map<*, *>>
            val brandFetchEvents = brands?.map { brand ->
                var brandProductsPage = 0
                val productIds = mutableListOf<Long>()
                while (true) {
                    val brandProductsResponse = kazanExpressClient.getProductsByBrand(
                        (brand["id"] as Int).toLong(),
                        page = brandProductsPage
                    ) ?: throw IllegalStateException("Can't get brand products")
                    val brandProductsResponsePayload = brandProductsResponse["payload"] as? Map<*, *>
                    val products = brandProductsResponsePayload?.get("products") as? List<Map<*, *>>
                    if (products == null || products.isEmpty()) break
                    productIds.addAll(products.map { (it["productId"] as Int).toLong() })
                    brandProductsPage++
                }
                val brandFetchData = BrandFetchData(brand, categoryId, productIds)
                val brandFetch = conversionService.convert(brandFetchData, BrandFetch::class.java)!!
                val now = Instant.now()
                FetchKazanExpressEvent.newBuilder()
                    .setCreatedAt(
                        Timestamp.newBuilder()
                            .setSeconds(now.epochSecond)
                            .setNanos(now.nano)
                            .build()
                    )
                    .setBrandFetch(brandFetch)
                    .build()
            }!!
            streamService.putFetchEvents(brandFetchEvents)
        }
    }

}