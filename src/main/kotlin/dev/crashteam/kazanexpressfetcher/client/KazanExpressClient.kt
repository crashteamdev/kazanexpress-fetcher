package dev.crashteam.kazanexpressfetcher.client

import dev.crashteam.kazanexpressfetcher.client.model.CategoryResponse
import dev.crashteam.kazanexpressfetcher.client.model.RootCategoriesResponse
import dev.crashteam.kazanexpressfetcher.client.model.StyxResponse
import dev.crashteam.kazanexpressfetcher.config.properties.ServiceProperties
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


private val log = KotlinLogging.logger {}

@Service
class KazanExpressClient(
    private val restTemplate: RestTemplate,
    private val serviceProperties: ServiceProperties,
) {

    fun getShopInfo(shopName: String): HashMap<String, Any>? {
        val queryParams = mapOf<String, Any>("order" to "ascending")
            .map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/shop/$shopName?$queryParams"
        log.debug { "Request shop info by name. Url=$url" }
        val headers = HttpHeaders().apply {
            set("X-User-Agent", USER_AGENT)
        }
        val httpEntity = HttpEntity<Nothing>(headers)
        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getShopProductsByName(shopName: String, size: Int = 500, page: Int = 0): HashMap<String, Any>? {
        val url = "$ROOT_URL/shop/$shopName/more?size=$size&page=$page"
        log.debug { "Request shop by name. Url=$url" }
        val headers = HttpHeaders().apply {
            set("X-User-Agent", USER_AGENT)
        }
        val httpEntity = HttpEntity<Nothing>(headers)
        val responseType: ParameterizedTypeReference<HashMap<String, Any>> =
            object : ParameterizedTypeReference<HashMap<String, Any>>() {}
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType).body
    }

    fun getCategoryProducts(categoryId: Long, size: Int = 24, page: Int = 0): CategoryResponse? {
        val queryParams = mapOf<String, Any>(
            "categoryId" to categoryId,
            "size" to size,
            "page" to page
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/v2/main/search/product?$queryParams"
        log.debug { "Request category by id. categoryId=$categoryId; size=$size; page=$page" }
        val httpHeaders = HttpHeaders().apply {
            set("X-User-Agent", USER_AGENT)
        }
        val responseType: ParameterizedTypeReference<StyxResponse<CategoryResponse>> =
            object : ParameterizedTypeReference<StyxResponse<CategoryResponse>>() {}
        val styxResponse =
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Nothing>(httpHeaders), responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getRootCategories(): RootCategoriesResponse? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/main/root-categories"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)
        val responseType: ParameterizedTypeReference<StyxResponse<RootCategoriesResponse>> =
            object : ParameterizedTypeReference<StyxResponse<RootCategoriesResponse>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getRootCategoriesRaw(): HashMap<String, Any>? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/main/root-categories"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)
        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getCategory(categoryId: Long, size: Int = 24, page: Int = 0): HashMap<String, Any>? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/category/v2/$categoryId?pageSize=$size&page=$page&sortBy=&order=ascending"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)
        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getProductInfo(productId: Long): HashMap<String, Any>? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/v2/product/$productId"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)
        log.debug { "Request product info. Url=$url" }

        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getProductReviews(productId: Long, amount: Long, page: Int = 0): HashMap<String, Any>? {
        val queryParams = mapOf<String, Any>(
            "amount" to amount,
            "page" to page,
            "hasPhoto" to "false"
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/product/$productId/reviews?$queryParams"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)
        log.debug { "Request product reviews. Url=$url" }

        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getSellerProducts(sellerId: Long, size: Int = 24, page: Int = 0): HashMap<String, Any>? {
        val queryParams = mapOf<String, Any>(
            "size" to size,
            "page" to page,
            "shopId" to sellerId,
            "showAdult" to "true"
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/v2/main/search/product?$queryParams"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)

        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getCategoryBrands(categoryId: Long): HashMap<String, Any>? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=" +
                "$ROOT_URL/v2/main/search/filter?&categoryId=$categoryId&showAdult=true"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)

        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    fun getProductsByBrand(brandId: Long, size: Int = 24, page: Int = 0): Map<String, Any>? {
        val url = "${serviceProperties.proxy!!.url}/proxy?url=" +
                "$ROOT_URL/v2/main/search/product?filter=1187:$brandId&size=$size&page=$page"
        val headers = HttpHeaders().apply {
            add("X-User-Agent", USER_AGENT)
            add("X-Authorization", "Basic $AUTH_TOKEN")
        }
        val entity = HttpEntity<Nothing>(headers)

        val responseType: ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>> =
            object : ParameterizedTypeReference<StyxResponse<HashMap<String, Any>>>() {}
        val styxResponse = restTemplate.exchange(url, HttpMethod.GET, entity, responseType).body

        return handleProxyResponse(styxResponse!!)
    }

    private fun <T> handleProxyResponse(styxResponse: StyxResponse<T>): T? {
        val originalStatus = styxResponse.originalStatus
        val statusCode = HttpStatus.resolve(originalStatus)
            ?: throw IllegalStateException("Unknown http status: $originalStatus")
        val isError = statusCode.series() == HttpStatus.Series.CLIENT_ERROR
                || statusCode.series() == HttpStatus.Series.SERVER_ERROR
        if (isError) {
            throw KazanExpressClientException(
                originalStatus,
                styxResponse.body.toString(),
                "Bad response from KazanExpress. Status=$originalStatus; Body=${styxResponse.body.toString()}"
            )
        }
        if (styxResponse.code != 0) {
            log.warn { "Bad proxy status - ${styxResponse.code}" }
        }
        return styxResponse.body
    }

    companion object {
        const val ROOT_URL = "https://api.kazanexpress.ru/api"
        private const val AUTH_TOKEN = "a2F6YW5leHByZXNzLWN1c3RvbWVyOmN1c3RvbWVyU2VjcmV0S2V5"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.115 Mobile Safari/537.36"
    }
}
