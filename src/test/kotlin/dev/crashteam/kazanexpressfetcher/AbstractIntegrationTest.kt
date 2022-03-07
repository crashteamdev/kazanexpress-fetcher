package dev.crashteam.kazanexpressfetcher

import dev.crashteam.kazanexpressfetcher.config.properties.FetcherProperties
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = [KazanexpressFetcherApplication::class],
    initializers = [AbstractIntegrationTest.Initializer::class]
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AbstractIntegrationTest {

    @Autowired
    lateinit var fetcherProperties: FetcherProperties

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            kafka.start()
            postgresql.start()
            TestPropertyValues.of(
                "spring.kafka.bootstrap-servers=${kafka.bootstrapServers}",
                "spring.datasource.url=${postgresql.jdbcUrl}",
                "spring.datasource.username=${postgresql.username}",
                "spring.datasource.password=${postgresql.password}",
                "spring.flyway.url=${postgresql.jdbcUrl}",
                "spring.flyway.user=${postgresql.username}",
                "spring.flyway.password=${postgresql.password}",
            ).applyTo(applicationContext.environment)
        }
    }

    protected fun initFetcherTopic() {
        val adminClient = AdminClient.create(mapOf(BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers))
        adminClient.use {
            adminClient.createTopics(listOf(NewTopic(fetcherProperties.streamTopicName, 1, 1)))
        }
    }

    companion object {
        private val kafka: KafkaContainer by lazy {
            val tag = DockerImageName.parse("confluentinc/cp-kafka").withTag("7.0.1")
            KafkaContainer(tag).withEmbeddedZookeeper()
        }
        const val POSTGRESQL_DATABASE = "taskmaster"
        const val POSTGRESQL_USERNAME = "root"
        const val POSTGRESQL_PASSWORD = "root"
        private val postgresql: PostgreSQLContainer<Nothing> by lazy {
            PostgreSQLContainer<Nothing>("postgres:11").apply {
                withDatabaseName(POSTGRESQL_DATABASE)
                withUsername(POSTGRESQL_USERNAME)
                withPassword(POSTGRESQL_PASSWORD)
            }
        }
    }

}