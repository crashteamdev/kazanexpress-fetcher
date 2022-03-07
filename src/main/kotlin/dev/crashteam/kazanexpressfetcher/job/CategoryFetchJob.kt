package dev.crashteam.kazanexpressfetcher.job

import com.google.protobuf.Timestamp
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import dev.crashteam.kazanexpressfetcher.job.model.RootCategoriesFetchData
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
        val categories = kazanExpressClient.getRootCategoriesRaw()
        val payload = categories?.get("payload") as? List<Map<String, Any>>
        if (payload == null || payload.isEmpty()) {
            log.warn { "Empty root categories response" }
            return
        }
        val conversionService = applicationContext.getBean(ConversionService::class.java)
        val rootCategoriesFetchData = RootCategoriesFetchData(payload)
        val categoryFetch = conversionService.convert(rootCategoriesFetchData, CategoryFetch::class.java)
        val yandexDataStreamService = applicationContext.getBean(StreamService::class.java)
        val now = Instant.now()
        val fetchKazanExpressEvent = FetchKazanExpressEvent.newBuilder()
            .setCreatedAt(
                Timestamp.newBuilder()
                .setSeconds(now.epochSecond)
                .setNanos(now.nano)
                .build())
            .setCategoryFetch(categoryFetch)
            .build()
        yandexDataStreamService.putFetchEvent(fetchKazanExpressEvent)
    }
}