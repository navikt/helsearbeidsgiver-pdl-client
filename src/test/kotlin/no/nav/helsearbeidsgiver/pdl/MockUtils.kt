package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import no.nav.helsearbeidsgiver.pdl.domene.PdlError
import no.nav.helsearbeidsgiver.pdl.domene.PdlErrorExtension
import no.nav.helsearbeidsgiver.pdl.domene.PdlErrorLocation
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.helsearbeidsgiver.utils.test.mock.mockStatic
import no.nav.helsearbeidsgiver.utils.test.resource.readResource
import kotlin.time.Duration

const val MOCK_FNR = "test-ident"

object MockResponse {
    val personNavn = "hent-person-navn-response.json".readResource()
    val fullPerson = "hent-full-person-response.json".readResource()
    val personBolk = "hent-personbolk-response.json".readResource()
    val aktorID = "hent-aktoer-id-response.json".readResource()
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

    return mockStatic(::createHttpClient) {
        every { createHttpClient() } returns mockHttpClient
        PdlClient(
            "url",
            Behandlingsgrunnlag.INNTEKTSMELDING,
            LocalCache.Config(entryDuration = Duration.ZERO, maxEntries = 1),
        ) { "fake token" }
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
