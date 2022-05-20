package no.nav.helsearbeidsgiver.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.*
import io.mockk.mockk
import kotlinx.serialization.json.Json
import no.nav.helsearbeidsgiver.tokenprovider.AccessTokenProvider

internal fun buildClient(status: HttpStatusCode, content: String): PdlClient {
    return PdlClient(
        "url",
        mockk<AccessTokenProvider>(relaxed = true),
        mockHttpClient(status, content),
        ObjectMapper()
    )
}

val testFnr = "test-ident"
val validPdlNavnResponse = "pdl-mock-data/pdl-person-response.json".loadFromResources()
val validPdlFullPersonResponse = "pdl-mock-data/pdl-hentFullPerson-response.json".loadFromResources()
val errorPdlResponse = "pdl-mock-data/pdl-error-response.json".loadFromResources()

fun String.loadFromResources(): String {
    return ClassLoader.getSystemResource(this).readText()
}

fun mockHttpClient(status: HttpStatusCode, content: String): HttpClient {
    val mockEngine = MockEngine { request ->
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    return HttpClient(mockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
    }
}
