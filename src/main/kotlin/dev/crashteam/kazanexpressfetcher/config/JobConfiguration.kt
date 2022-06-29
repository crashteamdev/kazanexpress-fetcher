package dev.crashteam.kazanexpressfetcher.config

import dev.crashteam.kazanexpressfetcher.config.properties.FetcherProperties
import dev.crashteam.kazanexpressfetcher.job.BrandFetchJob
import dev.crashteam.kazanexpressfetcher.job.CategoryFetchJob
import dev.crashteam.kazanexpressfetcher.job.ProductFetcherMasterJob
import org.quartz.*
import org.quartz.impl.JobDetailImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class JobConfiguration(
    private val fetcherProperties: FetcherProperties,
) {

    @Autowired
    private lateinit var schedulerFactoryBean: Scheduler

    @PostConstruct
    fun init() {
        schedulerFactoryBean.addJob(categoryProductMasterJob(), true, true)
        if (!schedulerFactoryBean.checkExists(TriggerKey(PRODUCT_FETCHER_MASTER_JOB, CATEGORY_PRODUCT_FETCHER_MASTER_JOB_GROUP))) {
            schedulerFactoryBean.scheduleJob(triggerGroupProductMasterJob())
        }
        schedulerFactoryBean.addJob(categoryFetchJob(), true, true)
        if (!schedulerFactoryBean.checkExists(TriggerKey(CATEGORY_FETCH_JOB, CATEGORY_FETCH_GROUP))) {
            schedulerFactoryBean.scheduleJob(triggerCategoryFetchJob())
        }
        schedulerFactoryBean.addJob(brandFetchJob(), true, true)
        if (!schedulerFactoryBean.checkExists(TriggerKey(BRAND_FETCH_JOB, BRAND_FETCH_GROUP))) {
            schedulerFactoryBean.scheduleJob(triggerBrandFetch())
        }
    }

    private fun brandFetchJob(): JobDetailImpl {
        val jobDetail = JobDetailImpl()
        jobDetail.key = JobKey(BRAND_FETCH_JOB, BRAND_FETCH_GROUP)
        jobDetail.jobClass = BrandFetchJob::class.java

        return jobDetail
    }

    private fun triggerBrandFetch(): CronTrigger {
        return TriggerBuilder.newTrigger()
            .forJob(brandFetchJob())
            .withIdentity(BRAND_FETCH_JOB, BRAND_FETCH_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(fetcherProperties.brandFetchCron))
            .build()
    }

    private fun categoryFetchJob(): JobDetailImpl {
        val jobDetail = JobDetailImpl()
        jobDetail.key = JobKey(CATEGORY_FETCH_JOB, CATEGORY_FETCH_GROUP)
        jobDetail.jobClass = CategoryFetchJob::class.java

        return jobDetail
    }

    private fun triggerCategoryFetchJob(): CronTrigger {
        return TriggerBuilder.newTrigger()
            .forJob(categoryFetchJob())
            .withIdentity(CATEGORY_FETCH_JOB, CATEGORY_FETCH_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(fetcherProperties.categoryFetchCron))
            .build()
    }

    private fun categoryProductMasterJob(): JobDetailImpl {
        val jobDetail = JobDetailImpl()
        jobDetail.key = JobKey(PRODUCT_FETCHER_MASTER_JOB, CATEGORY_PRODUCT_FETCHER_MASTER_JOB_GROUP)
        jobDetail.jobClass = ProductFetcherMasterJob::class.java

        return jobDetail
    }

    private fun triggerGroupProductMasterJob(): CronTrigger {
        return TriggerBuilder.newTrigger()
            .forJob(categoryProductMasterJob())
            .withIdentity(PRODUCT_FETCHER_MASTER_JOB, CATEGORY_PRODUCT_FETCHER_MASTER_JOB_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(fetcherProperties.productFetchCron))
            .build()
    }

    companion object {
        const val PRODUCT_FETCHER_MASTER_JOB = "productFetcherMasterJob"
        const val CATEGORY_PRODUCT_FETCHER_MASTER_JOB_GROUP = "categoryProductMasterJobGroup"
        const val CATEGORY_FETCH_JOB = "categoryFetchJob"
        const val CATEGORY_FETCH_GROUP = "categoryFetchGroup"
        const val BRAND_FETCH_JOB = "brandFetchJob"
        const val BRAND_FETCH_GROUP = "brandFetchGroup"
    }
}
