package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json

internal fun buildClient(status: HttpStatusCode, content: String): PdlClient {
    val accessTokenProvider = MockAccessTokenProvider()
    return PdlClient(
        "url",
        { accessTokenProvider.getAccessToken() },
        mockHttpClient(status, content)
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
    val mockEngine = MockEngine {
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
