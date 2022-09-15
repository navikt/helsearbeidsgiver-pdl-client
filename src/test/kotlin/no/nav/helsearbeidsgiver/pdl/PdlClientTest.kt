package no.nav.helsearbeidsgiver.pdl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class PdlClientTest {

    @Test
    fun `Returnerer en person ved gyldig respons fra PDL`() {
        val name = mockPdlClient(MockResponse.navn)
            .personNavn(MOCK_FNR)
            ?.navn
            ?.firstOrNull()

        assertEquals(
            "Ola" to name?.fornavn,
            "Freg" to name?.metadata?.master
        )
    }

    @Test
    fun `Full Person returnerer en person ved gyldig respons fra PDL`() {
        val response = mockPdlClient(MockResponse.fullPerson).fullPerson(MOCK_FNR)
        val name = response
            ?.hentPerson
            ?.navn
            ?.firstOrNull()
            ?.fornavn

        assertEquals(
            "NILS" to name,
            2 to response?.hentIdenter?.identer?.size,
            1 to response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }?.size,
            1 to response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }?.size,
            PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE to response?.hentGeografiskTilknytning?.gtType,
            LocalDate.of(1984, 1, 31) to response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato,
            0 to response?.hentPerson?.doedsfall?.size,
            "MANN" to response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn
        )
    }

    @Test
    fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlException> {
            mockPdlClient(MockResponse.error).personNavn(MOCK_FNR)
        }
    }
}

fun <T : Any> assertEquals(vararg expectedAndActuals: Pair<T, T?>) {
    expectedAndActuals.forEach { (expected, actual) ->
        Assertions.assertEquals(expected, actual)
    }
}
