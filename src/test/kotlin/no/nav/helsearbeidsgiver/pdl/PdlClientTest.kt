package no.nav.helsearbeidsgiver.pdl

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import no.nav.helsearbeidsgiver.pdl.domene.FullPerson
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import java.time.LocalDate
import java.time.Month

class PdlClientTest : FunSpec({
    context(PdlClient::personNavn.name) {
        test("Gir personnavn ved gyldig respons") {
            val expected = PersonNavn(
                fornavn = "Ola",
                mellomnavn = "",
                etternavn = "Norrbagg",
            )

            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.personNavn)

            val actual = mockPdlClient.personNavn(MOCK_FNR)

            actual shouldBe expected
        }

        test("Gir PdlException ved feilrespons") {
            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.error)

            val e = shouldThrowExactly<PdlException> {
                mockPdlClient.personNavn(MOCK_FNR)
            }

            e shouldBe mockPdlException()
        }
    }

    context(PdlClient::fullPerson.name) {
        test("Gir full person ved gyldig respons") {
            val expected = FullPerson(
                navn = PersonNavn(
                    fornavn = "NILS",
                    mellomnavn = null,
                    etternavn = "FALSKESEN",
                ),
                foedselsdato = LocalDate.of(1984, Month.JANUARY, 31),
                diskresjonskode = Gradering.STRENGT_FORTROLIG.tilKodeverkDiskresjonskode(),
                geografiskTilknytning = "1851",
            )

            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.fullPerson)

            val actual = mockPdlClient.fullPerson(MOCK_FNR)

            actual shouldBe expected
        }

        test("Gir PdlException ved feilrespons") {
            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.error)

            val e = shouldThrowExactly<PdlException> {
                mockPdlClient.fullPerson(MOCK_FNR)
            }

            e shouldBe mockPdlException()
        }
    }

    context(PdlClient::personBolk.name) {
        test("Gir liste ved gyldig respons") {
            val expected = listOf(
                FullPerson(
                    navn = PersonNavn(
                        fornavn = "Ola",
                        mellomnavn = null,
                        etternavn = "Normann",
                    ),
                    foedselsdato = LocalDate.of(1984, Month.JANUARY, 31),
                    ident = "12345678910",
                    diskresjonskode = null,
                ),
            )

            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.personBolk)

            val actual = mockPdlClient.personBolk(listOf("12345678910", "12345678911", "test"))

            actual shouldBe expected
        }
    }

    context(PdlClient::hentAktoerID.name) {
        test("Gir aktorID ved gyldig respons") {

            val expected = "1234567890123"

            val mockPdlClient = mockPdlClient(HttpStatusCode.OK to MockResponse.aktorID)

            val actual = mockPdlClient.hentAktoerID(MOCK_FNR)

            actual shouldBe expected
        }
    }

    listOf<Triple<String, suspend PdlClient.() -> Unit, String>>(
        Triple(
            PdlClient::personNavn.name,
            { personNavn(Fnr.genererGyldig().verdi) },
            MockResponse.personNavn,
        ),
        Triple(
            PdlClient::fullPerson.name,
            { fullPerson(Fnr.genererGyldig().verdi) },
            MockResponse.fullPerson,
        ),
        Triple(
            PdlClient::personBolk.name,
            { personBolk(listOf(Fnr.genererGyldig().verdi, Fnr.genererGyldig().verdi)) },
            MockResponse.personBolk,
        ),
        Triple(
            PdlClient::hentAktoerID.name,
            { hentAktoerID(Fnr.genererGyldig().verdi) },
            MockResponse.aktorID,
        ),
    )
        .forEach { (testFnName, testFn, okResponse) ->
            context(testFnName) {
                test("feiler ved 4xx-feil") {
                    val mockPdlClient = mockPdlClient(HttpStatusCode.BadRequest to "")

                    val e = shouldThrowExactly<ClientRequestException> {
                        mockPdlClient.testFn()
                    }

                    e.response.status shouldBe HttpStatusCode.BadRequest
                }

                test("lykkes ved færre 5xx-feil enn max retries (5)") {
                    val mockPdlClient = mockPdlClient(
                        HttpStatusCode.InternalServerError to "",
                        HttpStatusCode.InternalServerError to "",
                        HttpStatusCode.InternalServerError to "",
                        HttpStatusCode.InternalServerError to "",
                        HttpStatusCode.InternalServerError to "",
                        HttpStatusCode.OK to okResponse,
                    )

                    runTest {
                        shouldNotThrowAny {
                            mockPdlClient.testFn()
                        }
                    }
                }

                test("feiler ved flere 5xx-feil enn max retries (5)") {
                    val mockPdlClient =
                        mockPdlClient(
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                        )

                    runTest {
                        val e = shouldThrowExactly<ServerResponseException> {
                            mockPdlClient.testFn()
                        }

                        e.response.status shouldBe HttpStatusCode.InternalServerError
                    }
                }

                test("kall feiler og prøver på nytt ved timeout") {
                    val mockPdlClient =
                        mockPdlClient(
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to okResponse,
                        )

                    runTest {
                        shouldNotThrowAny {
                            mockPdlClient.testFn()
                        }
                    }
                }
            }
        }
})
