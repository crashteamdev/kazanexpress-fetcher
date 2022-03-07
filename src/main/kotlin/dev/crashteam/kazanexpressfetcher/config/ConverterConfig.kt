package dev.crashteam.kazanexpressfetcher.config

import dev.crashteam.kazanexpressfetcher.converter.ProtoConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.ConversionServiceFactoryBean

@Configuration
@ComponentScan(
    basePackages = [
        "dev.crashteam.kazanexpressfetcher.converter",
    ]
)
class ConverterConfig {

    @Bean
    @Primary
    fun conversionServiceFactoryBean(converters: Set<ProtoConverter<*, *>>): ConversionServiceFactoryBean {
        val conversionServiceFactoryBean = ConversionServiceFactoryBean()
        conversionServiceFactoryBean.setConverters(converters)

        return conversionServiceFactoryBean
    }
}
