package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import no.nav.helsearbeidsgiver.pdl.domene.PdlError
import no.nav.helsearbeidsgiver.pdl.domene.PdlErrorExtension
import no.nav.helsearbeidsgiver.pdl.domene.PdlErrorLocation
import kotlin.reflect.KFunction

const val MOCK_FNR = "test-ident"

object MockResponse {
    val personNavn = "hent-person-navn-response.json".readResource()
    val fullPerson = "hent-full-person-response.json".readResource()
    val error = "error-response.json".readResource()
}

fun mockPdlClient(content: String, status: HttpStatusCode): PdlClient {
    val mockEngine = MockEngine {
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    val mockHttpClient = HttpClient(mockEngine) { configure() }

    return mockFn(::createHttpClient) {
        every { createHttpClient() } returns mockHttpClient
        PdlClient("url", Behandlingsgrunnlag.INNTEKTSMELDING) { "fake token" }
    }
}

fun mockPdlException(): PdlException =
    PdlException(
        errors = listOf(
            PdlError(
                message = "PDL kunne ikke finne Chuck Norris. Han finner deg.",
                locations = listOf(
                    PdlErrorLocation(
                        line = null,
                        column = null,
                    ),
                ),
                path = null,
                extensions = PdlErrorExtension(
                    code = null,
                    classification = "Tullefeil",
                ),
            ),
        ),
    )

private fun <T> mockFn(fn: KFunction<*>, block: () -> T): T {
    mockkStatic(fn)
    return try {
        block()
    } finally {
        unmockkStatic(fn)
    }
}
