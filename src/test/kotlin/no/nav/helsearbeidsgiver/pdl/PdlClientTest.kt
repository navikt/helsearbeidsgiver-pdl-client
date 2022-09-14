package no.nav.helsearbeidsgiver.pdl

import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class PdlClientTest {

    @Test
    internal fun `Returnerer en person ved gyldig respons fra PDL`() {
        val response = buildClient(HttpStatusCode.OK, validPdlNavnResponse).personNavn(testFnr)
        val name = response
            ?.navn
            ?.firstOrNull()
        assertEquals("Ola", name?.fornavn)
        assertEquals("Freg", name?.metadata?.master)
    }

    @Test
    internal fun `Full Person returnerer en person ved gyldig respons fra PDL`() {
        val response = buildClient(HttpStatusCode.OK, validPdlFullPersonResponse).fullPerson(testFnr)
        val name = response
            ?.hentPerson
            ?.navn
            ?.firstOrNull()
            ?.fornavn
        assertEquals("NILS", name)
        assertEquals(2, response?.hentIdenter?.identer?.size)
        assertEquals(1, response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }?.size)
        assertEquals(1, response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }?.size)
        assertEquals(PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE, response?.hentGeografiskTilknytning?.gtType)
        assertEquals(LocalDate.of(1984, 1, 31), response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato)
        assertEquals(0, response?.hentPerson?.doedsfall?.size)
        assertEquals("MANN", response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn)
    }

    @Test
    internal fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlClient.PdlException> {
            buildClient(HttpStatusCode.OK, errorPdlResponse).personNavn(testFnr)
        }
    }
}
