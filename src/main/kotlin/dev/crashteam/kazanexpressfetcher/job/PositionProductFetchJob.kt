package dev.crashteam.kazanexpressfetcher.job

import com.google.protobuf.Timestamp
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import dev.crashteam.kazanexpressfetcher.service.StreamService
import dev.crashteam.kz.fetcher.FetchKazanExpressEvent
import dev.crashteam.kz.fetcher.ProductCategoryPositionFetch
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.Instant

private val log = KotlinLogging.logger {}

class PositionProductFetchJob : Job {

    override fun execute(jobContext: JobExecutionContext) {
        val applicationContext = jobContext.getApplicationContext()
        val kazanExpressClient = applicationContext.getBean(KazanExpressClient::class.java)
        val streamService = applicationContext.getBean(StreamService::class.java)
        val categoryId = jobContext.jobDetail.jobDataMap[PositionProductFetchMasterJob.JOB_CATEGORY_ID] as? Long
            ?: throw IllegalStateException("categoryId can't be null")
        var offset = jobContext.jobDetail.jobDataMap["offset"] as? Int ?: 0
        log.info { "Start position feetch job for categoryId=${categoryId}" }
        val limit = 48
        var position: Long = 0
        while (true) {
            val response = kazanExpressClient.getCategoryProductsGraphQl(categoryId, offset, limit)

            if (response?.data == null || response.data.makeSearch.items.isEmpty()) break

            val productItems = response.data.makeSearch.items
            val fetchEventList = mutableListOf<FetchKazanExpressEvent>()
            for (productItem in productItems) {
                position += 1
                val productItemCard = productItem.catalogCard
                val productItemCardCharacteristics = productItemCard.characteristicValues
                val productResponse = kazanExpressClient.getProductInfo(productItemCard.productId)
                if (productResponse == null) {
                    log.warn { "Empty product data for ${productItemCard.productId}" }
                    continue
                }
                if (productItemCardCharacteristics.isNotEmpty()) {
                    val productItemCardCharacteristic = productItemCardCharacteristics.first()
                    val characteristicId = productItemCardCharacteristic.id
                    val productCharacteristics = productResponse.payload.data.characteristics
                    var indexOfCharacteristic: Int? = null
                    productCharacteristics.forEach { productCharacteristic ->
                        productCharacteristic.values.forEachIndexed { index, productCharacteristicValue ->
                            if (productCharacteristicValue.id == characteristicId) {
                                indexOfCharacteristic = index
                            }
                        }
                    }
                    if (indexOfCharacteristic == null) {
                        log.warn { "Something goes wrong. Can't find index of characteristic. productId=${productItemCard.productId}; characteristicId=${characteristicId}" }
                        continue
                    }
                    val skuIds = productResponse.payload.data.skuList.filter { productSku ->
                        productSku.characteristics.find { it.valueIndex.toInt() == indexOfCharacteristic } != null
                                && productSku.availableAmount > 0
                    }.map { it.id }
                    val now = Instant.now()
                    val nowTimestamp = Timestamp.newBuilder()
                        .setSeconds(now.epochSecond)
                        .setNanos(now.nano)
                        .build()
                    skuIds.forEach { skuId ->
                        val productCategoryPositionFetch = ProductCategoryPositionFetch.newBuilder().apply {
                            this.productId = productItemCard.productId
                            this.skuId = skuId
                            this.categoryId = categoryId
                            this.position = position
                            this.fetchTime = nowTimestamp
                        }.build()
                        val fetchKazanExpressEvent = FetchKazanExpressEvent.newBuilder()
                            .setCreatedAt(nowTimestamp)
                            .setProductPositionFetch(productCategoryPositionFetch)
                            .build()
                        fetchEventList.add(fetchKazanExpressEvent)
                    }
                } else {
                    val skuIds = productResponse.payload.data.skuList.filter { productSku ->
                        productSku.availableAmount > 0
                    }.map { it.id }
                    val now = Instant.now()
                    val nowTimestamp = Timestamp.newBuilder()
                        .setSeconds(now.epochSecond)
                        .setNanos(now.nano)
                        .build()
                    skuIds.forEach { skuId ->
                        val productCategoryPositionFetch = ProductCategoryPositionFetch.newBuilder().apply {
                            this.productId = productItemCard.productId
                            this.skuId = skuId
                            this.categoryId = categoryId
                            this.position = position
                            this.fetchTime = nowTimestamp
                        }.build()
                        val fetchKazanExpressEvent = FetchKazanExpressEvent.newBuilder()
                            .setCreatedAt(nowTimestamp)
                            .setProductPositionFetch(productCategoryPositionFetch)
                            .build()
                        fetchEventList.add(fetchKazanExpressEvent)
                    }
                }
            }
            if (fetchEventList.isNotEmpty()) {
                streamService.putFetchEvents(fetchEventList)
            }

            offset += 48
            jobContext.jobDetail.jobDataMap["offset"] = offset
        }
    }
}
