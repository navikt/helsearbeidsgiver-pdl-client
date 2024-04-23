package no.nav.helsearbeidsgiver.pdl

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import no.nav.helsearbeidsgiver.pdl.domene.FullPerson
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import java.time.LocalDate
import java.time.Month

class PdlClientTest : FunSpec({
    context("personNavn") {
        test("Gir personnavn ved gyldig respons") {
            val expected = PersonNavn(
                fornavn = "Ola",
                mellomnavn = "",
                etternavn = "Norrbagg",
            )

            val mockPdlClient = mockPdlClient(MockResponse.personNavn, HttpStatusCode.OK)

            val actual = mockPdlClient.personNavn(MOCK_FNR)

            actual shouldBe expected
        }

        test("Gir PdlException ved feilrespons") {
            val mockPdlClient = mockPdlClient(MockResponse.error, HttpStatusCode.OK)

            val e = shouldThrowExactly<PdlException> {
                mockPdlClient.personNavn(MOCK_FNR)
            }

            e shouldBe mockPdlException()
        }

        test("BadRequest gir ClientRequestException med status BadRequest") {
            val mockPdlClient = mockPdlClient("", HttpStatusCode.BadRequest)
            val e = shouldThrowExactly<ClientRequestException> {
                mockPdlClient.personNavn(MOCK_FNR)
            }

            e.response.status shouldBe HttpStatusCode.BadRequest
        }

        test("InternalServerError gir ServerResponseException med status InternalServerError") {
            val mockPdlClient = mockPdlClient("", HttpStatusCode.InternalServerError)

            val e = shouldThrowExactly<ServerResponseException> {
                mockPdlClient.personNavn(MOCK_FNR)
            }

            e.response.status shouldBe HttpStatusCode.InternalServerError
        }
    }

    context("fullPerson") {
        test("Gir full person ved gyldig respons") {
            val expected = FullPerson(
                navn = PersonNavn(
                    fornavn = "NILS",
                    mellomnavn = null,
                    etternavn = "FALSKESEN",
                ),
                foedselsdato = LocalDate.of(1984, Month.JANUARY, 31),
            )

            val mockPdlClient = mockPdlClient(MockResponse.fullPerson, HttpStatusCode.OK)

            val actual = mockPdlClient.fullPerson(MOCK_FNR)

            actual shouldBe expected
        }

        context("personBolk") {
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
                    ),
                )

                val mockPdlClient = mockPdlClient(MockResponse.personBolk, HttpStatusCode.OK)

                val actual = mockPdlClient.personBolk(listOf("12345678910", "12345678911", "test"))

                actual shouldBe expected
            }
        }

        test("Gir PdlException ved feilrespons") {
            val mockPdlClient = mockPdlClient(MockResponse.error, HttpStatusCode.OK)

            val e = shouldThrowExactly<PdlException> {
                mockPdlClient.fullPerson(MOCK_FNR)
            }

            e shouldBe mockPdlException()
        }

        test("BadRequest gir ClientRequestException med status BadRequest") {
            val mockPdlClient = mockPdlClient("", HttpStatusCode.BadRequest)

            val e = shouldThrowExactly<ClientRequestException> {
                mockPdlClient.fullPerson(MOCK_FNR)
            }

            e.response.status shouldBe HttpStatusCode.BadRequest
        }

        test("InternalServerError gir ServerResponseException med status InternalServerError") {
            val mockPdlClient = mockPdlClient("", HttpStatusCode.InternalServerError)

            val e = shouldThrowExactly<ServerResponseException> {
                mockPdlClient.fullPerson(MOCK_FNR)
            }

            e.response.status shouldBe HttpStatusCode.InternalServerError
        }
    }
    context("AktorID") {
        test("Gir aktorID ved gyldig respons") {

            val expected = "1234567890123"

            val mockPdlClient = mockPdlClient(MockResponse.aktorID, HttpStatusCode.OK)

            val actual = mockPdlClient.hentAktorID(MOCK_FNR)

            actual shouldBe expected
        }
    }
})
