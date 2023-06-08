package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

internal fun createHttpClient(): HttpClient =
    HttpClient(Apache5) { configureJsonHandler() }

@OptIn(ExperimentalSerializationApi::class)
internal fun HttpClientConfig<*>.configureJsonHandler() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
    }
}

internal fun String.readResource(): String =
    ClassLoader.getSystemResource(this).readText()
