package dev.crashteam.kazanexpressfetcher.client

class KazanExpressClientException(status: Int, rawResponseBody: String, message: String) : RuntimeException(message)