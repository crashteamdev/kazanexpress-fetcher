package dev.crashteam.kazanexpressfetcher.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ListValue
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import dev.crashteam.kazanexpressfetcher.job.model.ProductFetchData
import dev.crashteam.kz.fetcher.ProductFetch
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ProductFetchDataToProtoConverter(
    private val objectMapper: ObjectMapper
) : ProtoConverter<ProductFetchData, ProductFetch> {

    override fun convert(source: ProductFetchData): ProductFetch? {
        val productStructBuilder = Struct.newBuilder()
        val productJson = objectMapper.writeValueAsString(source.productInfo)
        JsonFormat.parser().merge(productJson, productStructBuilder)

        val sellerStructBuilder = Struct.newBuilder()
        val sellerJson = objectMapper.writeValueAsString(source.sellerInfo)
        JsonFormat.parser().merge(sellerJson, sellerStructBuilder)

        val reviewsListBuilder = ListValue.newBuilder()
        val structList = source.productReviews.map { value ->
            val structBuilder = Struct.newBuilder()
            val json = objectMapper.writeValueAsString(value)
            JsonFormat.parser().merge(json, structBuilder)
            Value.newBuilder().setStructValue(structBuilder.build()).build()
        }
        val reviews = reviewsListBuilder.addAllValues(structList).build()
        val now = Instant.now()

        return ProductFetch.newBuilder()
            .setProduct(productStructBuilder.build())
            .setSeller(sellerStructBuilder.build())
            .setReviews(reviews)
            .setFetchTime(
                Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
            )
            .build()
    }

}