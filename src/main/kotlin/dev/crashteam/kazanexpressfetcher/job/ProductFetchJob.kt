package dev.crashteam.kazanexpressfetcher.job

import com.google.protobuf.Timestamp
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import dev.crashteam.kazanexpressfetcher.job.model.ProductFetchData
import dev.crashteam.kazanexpressfetcher.service.StreamService
import dev.crashteam.kz.fetcher.FetchKazanExpressEvent
import dev.crashteam.kz.fetcher.ProductCategoryPositionFetch
import dev.crashteam.kz.fetcher.ProductFetch
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.core.convert.ConversionService
import java.time.Instant

private val log = KotlinLogging.logger {}

class ProductFetchJob : Job {

    override fun execute(jobContext: JobExecutionContext) {
        val appContext = jobContext.getApplicationContext()
        val kazanExpressClient = appContext.getBean(KazanExpressClient::class.java)
        val conversionService = appContext.getBean(ConversionService::class.java)
        val streamService = appContext.getBean(StreamService::class.java)
        val categoryId = jobContext.jobDetail.jobDataMap[ProductFetcherMasterJob.JOB_CATEGORY_ID] as? Long
            ?: throw IllegalStateException("categoryId can't be null")
        var productCategoryPage = jobContext.jobDetail.jobDataMap["productCategoryPage"] as? Int ?: 0
        while (true) {
            val categoryResponse = kazanExpressClient.getCategoryProducts(categoryId, page = productCategoryPage)
            if (categoryResponse?.payload == null || categoryResponse.payload.products.isEmpty()) {
                log.debug { "Empty category products payload. Break the loop" }
                break
            }
            val fetchEventList = mutableListOf<FetchKazanExpressEvent>()
            categoryResponse.payload.products.forEachIndexed { index, product ->
                val productInfo = kazanExpressClient.getProductInfo(product.productId)
                val productInfoPayload = productInfo?.get("payload") as? Map<*, *>
                    ?: throw IllegalStateException("Can't get product info")
                val productData = productInfoPayload["data"] as? Map<String, Any>
                val productSeller = productData?.get("seller") as? Map<*, *>
                val sellerInfo = kazanExpressClient.getShopInfo(productSeller?.get("link") as String)!!
                var productReviewPage = jobContext.jobDetail.jobDataMap["productReviewPage"] as? Int ?: 0
                val productReviews = mutableListOf<Map<*, *>>()
                while (true) {
                    val productId = productData["id"]
                    try {
                        val productReviewsResponse = kazanExpressClient.getProductReviews(
                            (productId as Int).toLong(),
                            amount = 50,
                            page = productReviewPage
                        )
                        val productReviewPayload = productReviewsResponse?.get("payload") as? List<Map<*, *>>
                        if (productReviewPayload == null || productReviewPayload.isEmpty()) break
                        productReviews.addAll(productReviewPayload)
                        productReviewPage++
                        jobContext.jobDetail.jobDataMap["productReviewPage"] = productReviewPage
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        log.error(e) { "Can't get product review. productId=$productId" }
                    }
                }

                val now = Instant.now()
                val nowTimestamp = Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
                // Send product fetch event
                val sellerInfoPayload = sellerInfo["payload"] as? Map<String, Any>
                val productFetchData = ProductFetchData(productData, sellerInfoPayload!!, productReviews)
                val productFetch = conversionService.convert(productFetchData, ProductFetch::class.java)!!
                val fetchKazanExpressEvent = FetchKazanExpressEvent.newBuilder()
                    .setCreatedAt(nowTimestamp)
                    .setProductFetch(productFetch)
                    .build()
                fetchEventList.add(fetchKazanExpressEvent)

                // Send product position fetch
                val positionFetchEvent = FetchKazanExpressEvent.newBuilder()
                    .setCreatedAt(nowTimestamp)
                    .setProductPositionFetch(
                        ProductCategoryPositionFetch.newBuilder()
                            .setProductId((productData["id"] as Int).toLong())
                            .setPosition(index.toLong() + 1)
                            .setCategoryId(categoryId)
                    )
                    .build()
                fetchEventList.add(positionFetchEvent)
            }
            streamService.putFetchEvents(fetchEventList)
            productCategoryPage++
            jobContext.jobDetail.jobDataMap["productCategoryPage"] = productCategoryPage
        }

    }
}