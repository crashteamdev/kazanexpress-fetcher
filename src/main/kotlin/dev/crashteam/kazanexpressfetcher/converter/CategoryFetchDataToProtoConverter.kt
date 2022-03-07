package dev.crashteam.kazanexpressfetcher.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ListValue
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import dev.crashteam.kazanexpressfetcher.job.model.CategoryFetchData
import dev.crashteam.kazanexpressfetcher.job.model.ProductFetchData
import dev.crashteam.kazanexpressfetcher.job.model.RootCategoriesFetchData
import dev.crashteam.kz.fetcher.CategoryFetch
import dev.crashteam.kz.fetcher.ProductFetch
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CategoryFetchDataToProtoConverter(
    private val objectMapper: ObjectMapper
) : ProtoConverter<RootCategoriesFetchData, CategoryFetch> {

    override fun convert(source: RootCategoriesFetchData): CategoryFetch {
        val categoriesListValBuilder = ListValue.newBuilder()
        val structList = source.payload.map { value ->
            val structBuilder = Struct.newBuilder()
            val json = objectMapper.writeValueAsString(value)
            JsonFormat.parser().merge(json, structBuilder)
            Value.newBuilder().setStructValue(structBuilder.build()).build()
        }
        val categories = categoriesListValBuilder.addAllValues(structList).build()
        val now = Instant.now()

        return CategoryFetch.newBuilder()
            .setCategories(categories)
            .setFetchTime(
                Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
            )
            .build()
    }

}