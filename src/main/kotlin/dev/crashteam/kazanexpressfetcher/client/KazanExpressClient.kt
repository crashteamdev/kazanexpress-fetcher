package dev.crashteam.kazanexpressfetcher.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.crashteam.kazanexpressfetcher.client.model.*
import dev.crashteam.kazanexpressfetcher.config.properties.ServiceProperties
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


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
        val queryParams = mapOf<String, Any>(
            "pageSize" to size,
            "page" to page,
            "sortBy" to "&order=ascending",
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=$ROOT_URL/category/v2/$categoryId?$queryParams"
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

    fun getProductInfoRaw(productId: Long): HashMap<String, Any>? {
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

    fun getProductInfo(productId: Long): ProductResponse? {
        val url = "${serviceProperties.proxy!!.url}/v2/proxy"
        val proxyRequestBody =
            ProxyRequestBody(
                url = "https://api.kazanexpress.ru/api/v2/product/$productId/",
                httpMethod = "GET",
                context = listOf(
                    ProxyRequestContext(
                        key = "headers",
                        value = mapOf("User-Agent" to USER_AGENT, "Authorization" to "Basic $AUTH_TOKEN")
                    )
                )
            )
        val responseType: ParameterizedTypeReference<StyxResponse<ProductResponse>> =
            object : ParameterizedTypeReference<StyxResponse<ProductResponse>>() {}
        val styxResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            HttpEntity<ProxyRequestBody>(proxyRequestBody),
            responseType
        ).body

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
        val queryParams = mapOf<String, Any>(
            "categoryId" to categoryId,
            "showAdult" to "true"
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=" +
                "$ROOT_URL/v2/main/search/filter?$queryParams"
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
        val queryParams = mapOf<String, Any>(
            "filter" to "1187:$brandId",
            "size" to size,
            "page" to page,
        ).map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }
            .joinToString(URLEncoder.encode("&", StandardCharsets.UTF_8))
        val url = "${serviceProperties.proxy!!.url}/proxy?url=" +
                "$ROOT_URL/v2/main/search/product?$queryParams"
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

    fun getCategoryProductsGraphQl(categoryId: Long, offset: Int, limit: Int): CategoryProductsGraphQlResponseWrapper? {
        val url = "${serviceProperties.proxy!!.url}/v2/proxy"
        val keGraphQlRequestBody = KeGraphQlRequestBody(
            operationName = "getMakeSearch",
            variables = KeGraphQlRequestVariables(
                queryInput = KeQueryInput(
                    categoryId = categoryId.toString(),
                    showAdultContent = "TRUE",
                    sort = "BY_RELEVANCE_DESC",
                    pagination = KeGraphQlPagination(
                        offset = offset,
                        limit = limit
                    )
                )
            ),
            query = GET_MAKE_SEARCH_QUERY
        )
        val requestBody = jacksonObjectMapper().writeValueAsBytes(keGraphQlRequestBody)
        val proxyRequestBody = ProxyRequestBody(
            url = "https://dshop.kznexpress.ru/",
            httpMethod = "POST",
            context = listOf(
                ProxyRequestContext(
                    "headers",
                    mapOf(
                        "User-Agent" to USER_AGENT,
                        "Authorization" to "Basic $AUTH_TOKEN",
                        "Content-Type" to MediaType.APPLICATION_JSON_VALUE
                    )
                ),
                ProxyRequestContext("content", Base64.getEncoder().encodeToString(requestBody))
            )
        )
        val responseType: ParameterizedTypeReference<StyxResponse<CategoryProductsGraphQlResponseWrapper>> =
            object : ParameterizedTypeReference<StyxResponse<CategoryProductsGraphQlResponseWrapper>>() {}
        val styxResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            HttpEntity<ProxyRequestBody>(proxyRequestBody),
            responseType
        ).body

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
        private const val GET_MAKE_SEARCH_QUERY =
            "query getMakeSearch(\$queryInput: MakeSearchQueryInput!) {\n  makeSearch(query: \$queryInput) {\n    id\n    queryId\n    queryText\n    category {\n      ...CategoryShortFragment\n      __typename\n    }\n    categoryTree {\n      category {\n        ...CategoryFragment\n        __typename\n      }\n      total\n      __typename\n    }\n    items {\n      catalogCard {\n        __typename\n        ...SkuGroupCardFragment\n      }\n      __typename\n    }\n    facets {\n      ...FacetFragment\n      __typename\n    }\n    total\n    mayHaveAdultContent\n    __typename\n  }\n}\n\nfragment FacetFragment on Facet {\n  filter {\n    id\n    title\n    type\n    measurementUnit\n    description\n    __typename\n  }\n  buckets {\n    filterValue {\n      id\n      description\n      image\n      name\n      __typename\n    }\n    total\n    __typename\n  }\n  range {\n    min\n    max\n    __typename\n  }\n  __typename\n}\n\nfragment CategoryFragment on Category {\n  id\n  icon\n  parent {\n    id\n    __typename\n  }\n  seo {\n    header\n    metaTag\n    __typename\n  }\n  title\n  adult\n  __typename\n}\n\nfragment CategoryShortFragment on Category {\n  id\n  parent {\n    id\n    title\n    __typename\n  }\n  title\n  __typename\n}\n\nfragment SkuGroupCardFragment on SkuGroupCard {\n  ...DefaultCardFragment\n  photos {\n    key\n    link(trans: PRODUCT_540) {\n      high\n      low\n      __typename\n    }\n    previewLink: link(trans: PRODUCT_240) {\n      high\n      low\n      __typename\n    }\n    __typename\n  }\n  badges {\n    ... on BottomTextBadge {\n      backgroundColor\n      description\n      id\n      link\n      text\n      textColor\n      __typename\n    }\n    __typename\n  }\n  characteristicValues {\n    id\n    value\n    title\n    characteristic {\n      values {\n        id\n        title\n        value\n        __typename\n      }\n      title\n      id\n      __typename\n    }\n    __typename\n  }\n  __typename\n}\n\nfragment DefaultCardFragment on CatalogCard {\n  adult\n  favorite\n  feedbackQuantity\n  id\n  minFullPrice\n  minSellPrice\n  offer {\n    due\n    icon\n    text\n    textColor\n    __typename\n  }\n  badges {\n    backgroundColor\n    text\n    textColor\n    __typename\n  }\n  ordersQuantity\n  productId\n  rating\n  title\n  __typename\n}"
    }
}
