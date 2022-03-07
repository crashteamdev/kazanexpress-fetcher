package dev.crashteam.kazanexpressfetcher.client

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class ProxyErrorHandler : ResponseErrorHandler {

    override fun hasError(response: ClientHttpResponse): Boolean {
        TODO("Not yet implemented")
    }

    override fun handleError(response: ClientHttpResponse) {
        TODO("Not yet implemented")
    }


}