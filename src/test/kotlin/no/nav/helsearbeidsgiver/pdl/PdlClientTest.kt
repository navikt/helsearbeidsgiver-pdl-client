package no.nav.helsearbeidsgiver.pdl

import io.ktor.http.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PdlClientImplTest {

    @Test
    internal fun `Returnerer en person ved gyldig respons fra PDL`() {
        val response = buildClient(HttpStatusCode.OK, validPdlNavnResponse).personNavn(testFnr)
        val name = response
            ?.navn
            ?.firstOrNull()
        //assertThat(name?.fornavn).isEqualTo("Ola")
        assertEquals("Ola", name?.fornavn)
        //assertThat(name?.metadata?.master).isEqualTo("Freg")
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
        // TODO - assertThat(name).isEqualTo("NILS")
        //assertThat(response?.hentIdenter?.identer).hasSize(2)
        assertEquals(2, response?.hentIdenter?.identer?.size)
        // TODO - assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }).hasSize(1)
        assertEquals(1, response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.AKTORID }?.size)
        // TODO - assertThat(response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }).hasSize(1)
        assertEquals(1, response?.hentIdenter?.identer?.filter { it.gruppe == PdlIdent.PdlIdentGruppe.FOLKEREGISTERIDENT }?.size)

        //assertThat(response?.hentGeografiskTilknytning?.gtType).isEqualTo(PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE)
        assertEquals(PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.KOMMUNE, response?.hentGeografiskTilknytning?.gtType)

        //assertThat(response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato).isEqualTo(LocalDate.of(1984, 1, 31))
        assertEquals(LocalDate.of(1984, 1, 31), response?.hentPerson?.foedsel?.firstOrNull()?.foedselsdato)

        //assertThat(response?.hentPerson?.doedsfall).hasSize(0)
        assertEquals(0, response?.hentPerson?.doedsfall?.size)

        //assertThat(response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn).isEqualTo("MANN")
        assertEquals("MANN", response?.hentPerson?.kjoenn?.firstOrNull()?.kjoenn)
    }

    @Test
    internal fun `Kaster PdlException ved feilrespons fra PDL`() {
        assertThrows<PdlClient.PdlException> {
            buildClient(HttpStatusCode.OK, errorPdlResponse).personNavn(testFnr)
        }
    }
}
