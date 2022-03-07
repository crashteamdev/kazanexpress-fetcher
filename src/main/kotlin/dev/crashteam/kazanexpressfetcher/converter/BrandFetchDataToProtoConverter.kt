package dev.crashteam.kazanexpressfetcher.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import dev.crashteam.kazanexpressfetcher.job.model.BrandFetchData
import dev.crashteam.kz.fetcher.BrandFetch
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BrandFetchDataToProtoConverter(
    private val objectMapper: ObjectMapper
) : ProtoConverter<BrandFetchData, BrandFetch> {

    override fun convert(source: BrandFetchData): BrandFetch {
        val brandStructBuilder = Struct.newBuilder()
        val sellerJson = objectMapper.writeValueAsString(source.brandInfo)
        JsonFormat.parser().merge(sellerJson, brandStructBuilder)
        val now = Instant.now()

        return BrandFetch.newBuilder()
            .setCategoryId(source.categoryId)
            .addAllProductIds(source.productIds)
            .setBrand(brandStructBuilder.build())
            .setFetchTime(
                Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
            ).build()
    }

}