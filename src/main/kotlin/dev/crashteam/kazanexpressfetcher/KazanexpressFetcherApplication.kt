package dev.crashteam.kazanexpressfetcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class KazanexpressFetcherApplication

fun main(args: Array<String>) {
    runApplication<KazanexpressFetcherApplication>(*args)
}
