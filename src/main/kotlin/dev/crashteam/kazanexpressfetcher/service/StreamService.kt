package dev.crashteam.kazanexpressfetcher.service

import dev.crashteam.kz.fetcher.FetchKazanExpressEvent
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.FailureCallback
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.util.concurrent.SuccessCallback
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class StreamService(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
) {

    @Value("\${fetcher.stream-topic-name}")
    private val streamTopicName: String? = null

    fun putFetchEvent(fetchEvent: FetchKazanExpressEvent): ListenableFuture<SendResult<String, ByteArray>> {
        val producerRecord = ProducerRecord(streamTopicName, UUID.randomUUID().toString(), fetchEvent.toByteArray())
        log.info { "Send producer record $producerRecord" }
        return kafkaTemplate.send(producerRecord)
    }

    fun putFetchEvents(fetchEvents: List<FetchKazanExpressEvent>) {
        val producerRecords =
            fetchEvents.map { ProducerRecord(streamTopicName, UUID.randomUUID().toString(), it.toByteArray()) }
        log.info { "Send producer records ${producerRecords.size}" }
        producerRecords.forEach { producerRecord ->
            kafkaTemplate.send(producerRecord).addCallback(
                { result -> log.debug {
                    "Successfully send event. Topic=" + result?.recordMetadata?.topic() + ";" +
                            " Offset=" + result?.recordMetadata?.offset() + ";" +
                            " Partition=" + result?.recordMetadata?.partition()
                }
                },
                { log.error(it.cause) { "Failed to send event. Topic=${producerRecord.topic()}; Offset=${producerRecord.partition()}" } })
        }
    }
}
