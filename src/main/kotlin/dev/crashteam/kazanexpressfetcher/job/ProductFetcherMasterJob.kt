package dev.crashteam.kazanexpressfetcher.job

import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.client.model.CategoryIdExtractor
import dev.crashteam.kazanexpressfetcher.client.model.RootCategoriesResponse
import dev.crashteam.kazanexpressfetcher.extensions.getApplicationContext
import mu.KotlinLogging
import org.quartz.*
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import java.util.*

private val log = KotlinLogging.logger {}

@DisallowConcurrentExecution
class ProductFetcherMasterJob : Job {

    override fun execute(jobContext: JobExecutionContext) {
        val applicationContext = jobContext.getApplicationContext()
        val kazanExpressClient = applicationContext.getBean(KazanExpressClient::class.java)
        val rootCategories: RootCategoriesResponse = kazanExpressClient.getRootCategories()
            ?: throw IllegalStateException("Can't get root categories")
        val categoryIds = rootCategories.payload.flatMap { CategoryIdExtractor.extractCategoryIds(it) }
        for (categoryId in categoryIds) {
            val jobIdentity = "${categoryId}-category-product-fetch-job"
            val jobDetail =
                JobBuilder.newJob(ProductFetchJob::class.java).withIdentity(jobIdentity).build()
            val triggerFactoryBean = SimpleTriggerFactoryBean().apply {
                setName(jobIdentity)
                setStartTime(Date())
                setRepeatInterval(10000L)
                setRepeatCount(0)
                setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
                afterPropertiesSet()
            }.getObject()
            jobDetail.jobDataMap[JOB_CATEGORY_ID] = categoryId
            try {
                val schedulerFactoryBean = applicationContext.getBean(Scheduler::class.java)
                schedulerFactoryBean.scheduleJob(jobDetail, triggerFactoryBean)
            } catch (e: ObjectAlreadyExistsException) {
                log.warn { "Task still in progress: $jobIdentity" }
            } catch (e: Exception) {
                log.error(e) { "Failed to start scheduler job" }
            }
        }
    }

    companion object {
        const val JOB_CATEGORY_ID = "categoryId"
    }

}