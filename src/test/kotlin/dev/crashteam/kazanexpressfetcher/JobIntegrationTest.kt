package dev.crashteam.kazanexpressfetcher

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crashteam.kazanexpressfetcher.client.KazanExpressClient
import dev.crashteam.kazanexpressfetcher.client.model.CategoryResponse
import dev.crashteam.kazanexpressfetcher.client.model.RootCategoriesResponse
import dev.crashteam.kazanexpressfetcher.client.model.SimpleCategory
import dev.crashteam.kazanexpressfetcher.job.BrandFetchJob
import dev.crashteam.kazanexpressfetcher.job.CategoryFetchJob
import dev.crashteam.kazanexpressfetcher.job.ProductFetchJob
import dev.crashteam.kazanexpressfetcher.service.StreamService
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mock
import org.mockito.Mockito.*
import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.UnorderedRequestExpectationManager
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.support.RestGatewaySupport
import java.io.BufferedReader

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest("spring.main.allow-bean-definition-overriding=true")
class JobIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @SpyBean
    lateinit var streamService: StreamService

    @MockBean
    lateinit var kazanExpressClient: KazanExpressClient

    @Mock
    lateinit var jobExecutionContext: JobExecutionContext

    var mockServer: MockRestServiceServer? = null

    @BeforeEach
    fun setUpEach() {
        val gateway = RestGatewaySupport()
        gateway.restTemplate = restTemplate
        mockServer = MockRestServiceServer.bindTo(restTemplate).build(UnorderedRequestExpectationManager())
        val mockScheduler = mock(Scheduler::class.java)
        val mockSchedulerContext = mock(SchedulerContext::class.java)
        `when`(mockSchedulerContext[anyString()]).thenReturn(applicationContext)
        `when`(mockScheduler.context).thenReturn(mockSchedulerContext)
        `when`(jobExecutionContext.scheduler).thenReturn(mockScheduler)
        val mockJobDetail = mock(JobDetail::class.java)
        val jobDataMap = JobDataMap().apply {
            put("categoryId", 3432542L)
        }
        `when`(mockJobDetail.jobDataMap).thenReturn(jobDataMap)
        `when`(jobExecutionContext.jobDetail).thenReturn(mockJobDetail)
        initFetcherTopic()
    }

    @Test
    fun `test product fetch`() {
        // Given
        val easyRandom = EasyRandom()
        val mockCategoryResponse = easyRandom.nextObject(CategoryResponse::class.java)
        val mockProductResponse: String =
            ClassPathResource("product-info-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val mockShopResponse: String =
            ClassPathResource("seller-info-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val mockProductReviewResponse: String =
            ClassPathResource("product-reviews-empty-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val responseType = object : TypeReference<HashMap<String, Any>>() {}
        val productResponseMap = objectMapper.readValue(mockProductResponse, responseType)

        // When
        `when`(kazanExpressClient.getCategoryProducts(anyLong(), anyInt(), anyInt()))
            .thenReturn(mockCategoryResponse)
            .thenReturn(CategoryResponse(null, null))
        `when`(kazanExpressClient.getProductInfoRaw(anyLong())).thenReturn(productResponseMap)
        `when`(kazanExpressClient.getShopInfo(anyString()))
            .thenReturn(
                objectMapper.readValue(mockShopResponse, responseType)
            )
        `when`(kazanExpressClient.getProductReviews(anyLong(), anyLong(), anyInt()))
            .thenReturn(
                objectMapper.readValue(mockProductReviewResponse)
            )
        val productFetchJob = ProductFetchJob()
        productFetchJob.execute(jobExecutionContext)

        // Then
        verify(streamService, atLeast(1)).putFetchEvents(org.mockito.kotlin.any())
    }

    @Test
    fun `test category fetch`() {
        // Given
        val mockCategoryResponse: String =
            ClassPathResource("root-categories-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val responseType = object : TypeReference<HashMap<String, Any>>() {}
        val productResponseMap = objectMapper.readValue(mockCategoryResponse, responseType)
        val childCategory = SimpleCategory(
            5678L, 15, true, true, "testChildCategory", null, emptyList()
        )
        val rootCategory = SimpleCategory(
            1234L, 10, true, true, "testCategory", null, listOf(childCategory)
        )
        val rootCategoriesResponse = RootCategoriesResponse(listOf(rootCategory))

        // When
        `when`(kazanExpressClient.getRootCategoriesRaw()).thenReturn(productResponseMap)
        `when`(kazanExpressClient.getRootCategories()).thenReturn(rootCategoriesResponse)
        val categoryFetchJob = CategoryFetchJob()
        categoryFetchJob.execute(jobExecutionContext)

        // Then
        //verify(streamService, atLeast(1)).putFetchEvent(org.mockito.kotlin.any()) // TODO: fix
    }

    @Test
    fun `test brand fetch`() {
        // Given
        val childCategory = SimpleCategory(
            5678L, 15, true, true, "testChildCategory", null, emptyList()
        )
        val rootCategory = SimpleCategory(
            1234L, 10, true, true, "testCategory", null, listOf(childCategory)
        )
        val rootCategoriesResponse = RootCategoriesResponse(listOf(rootCategory))
        val mockCategoryBrandResponse: String =
            ClassPathResource("category-brand-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val mockBrandProductsResponse: String =
            ClassPathResource("brand-products-response.json", javaClass).inputStream.bufferedReader()
                .use(BufferedReader::readText)
        val responseType = object : TypeReference<HashMap<String, Any>>() {}
        val categoryBrandResponseMap = objectMapper.readValue(mockCategoryBrandResponse, responseType)
        val brandProductsResponseMap = objectMapper.readValue(mockBrandProductsResponse, responseType)

        // When
        `when`(kazanExpressClient.getRootCategories()).thenReturn(rootCategoriesResponse)
        `when`(kazanExpressClient.getCategoryBrands(anyLong())).thenReturn(categoryBrandResponseMap)
        `when`(
            kazanExpressClient.getProductsByBrand(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any()
            )
        ).thenReturn(brandProductsResponseMap).thenReturn(mapOf<String, Any>("payload" to emptyList<Any>()))
        val brandFetchJob = BrandFetchJob()
        brandFetchJob.execute(jobExecutionContext)

        // Then
        verify(streamService, atLeast(1)).putFetchEvent(org.mockito.kotlin.any())
    }

}
