package dev.crashteam.kazanexpressfetcher.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ListValue
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import dev.crashteam.kazanexpressfetcher.job.model.CategoriesFetchData
import dev.crashteam.kz.fetcher.CategoryFetch
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CategoryFetchDataToProtoConverter(
    private val objectMapper: ObjectMapper
) : ProtoConverter<CategoriesFetchData, CategoryFetch> {

    override fun convert(source: CategoriesFetchData): CategoryFetch {
        val categoryStructBuilder = Struct.newBuilder()
        val categoryJson = objectMapper.writeValueAsString(source.payload)
        JsonFormat.parser().merge(categoryJson, categoryStructBuilder)
        val now = Instant.now()
        return CategoryFetch.newBuilder()
            .setCategory(categoryStructBuilder.build())
            .setFetchTime(
                Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
            )
            .build()
    }

}
