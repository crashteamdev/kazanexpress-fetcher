package dev.crashteam.kazanexpressfetcher.job

import com.google.protobuf.Timestamp
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import dev.crashteam.kazanexpressfetcher.job.model.CategoriesFetchData
import dev.crashteam.kazanexpressfetcher.service.StreamService
import dev.crashteam.kz.fetcher.CategoryFetch
import dev.crashteam.kz.fetcher.FetchKazanExpressEvent
import mu.KotlinLogging
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.core.convert.ConversionService
import java.time.Instant

private val log = KotlinLogging.logger {}

@DisallowConcurrentExecution
class CategoryFetchJob : Job {

    override fun execute(context: JobExecutionContext) {
        val applicationContext = context.getApplicationContext()
        val kazanExpressClient = applicationContext.getBean(KazanExpressClient::class.java)
        val rootCategories = kazanExpressClient.getRootCategories()
        val rootCategoriesPayload = rootCategories?.payload
        if (rootCategoriesPayload == null || rootCategoriesPayload.isEmpty()) {
            log.warn { "Empty root categories response" }
            return
        }
        val conversionService = applicationContext.getBean(ConversionService::class.java)
        for (rootCategory in rootCategoriesPayload) {
            val categoryRawResponse = kazanExpressClient.getCategory(rootCategory.id)
            val categoryRawPayload = categoryRawResponse?.get("payload") as? Map<*, *>
            val rawCategory = categoryRawPayload?.get("category") as? Map<String, Any>
            if (rawCategory == null) {
                log.warn { "Empty category response with root category ${rootCategory.id}" }
                continue
            }
            val categoriesFetchData = CategoriesFetchData(rawCategory)
            val categoryFetch = conversionService.convert(categoriesFetchData, CategoryFetch::class.java)
            val streamService = applicationContext.getBean(StreamService::class.java)
            val now = Instant.now()
            val fetchKazanExpressEvent = FetchKazanExpressEvent.newBuilder()
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setSeconds(now.epochSecond)
                        .setNanos(now.nano)
                        .build())
                .setCategoryFetch(categoryFetch)
                .build()
            streamService.putFetchEvent(fetchKazanExpressEvent)
        }
    }
}
