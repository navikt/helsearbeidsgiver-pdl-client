package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory

/**
 * Enkel GraphQL-klient for PDL som kan enten hente navn fra aktør eller fnr (ident)
 * eller hente mer fullstendig data om en person via fnr eller aktørid (ident)
 *
 * Authorisasjon gjøres via den gitte Token prodvideren, og servicebrukeren som er angitt i token provideren må være i
 * i AD-gruppen `0000-GA-TEMA_SYK` som dokumentert [her](https://pdldocs-navno.msappproxy.net/intern/index.html#_konsumentroller_basert_p%C3%A5_tema).
 *
 * Klienten vil alltid gi PDL-Temaet 'SYK', så om du trenger et annet tema må du endre denne klienten.
 */
class PdlClient(
    private val url: String,
    private val getAccessToken: () -> String
) {
    private val httpClient = createHttpClient()

    private val personNavnQuery = "hentPersonNavn.graphql".readQuery()
    private val fullPersonQuery = "hentFullPerson.graphql".readQuery()

    suspend fun personNavn(ident: String, userLoginToken: String? = null): PdlHentPersonNavn.PdlPersonNavneliste? =
        PdlQuery(personNavnQuery, Variables(ident))
            .execute<PdlHentPersonNavn>(userLoginToken)
            ?.hentPerson

    suspend fun fullPerson(ident: String, userLoginToken: String? = null): PdlHentFullPerson? =
        PdlQuery(fullPersonQuery, Variables(ident))
            .execute(userLoginToken)

    // Funksjonen må være inline+reified for å kunne deserialisere T
    private suspend inline fun <reified T> PdlQuery.execute(userLoginToken: String?): T? {
        logger.info("Henter informasjon fra PDL")

        val accessToken = getAccessToken()

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(userLoginToken ?: accessToken)
            header("Nav-Consumer-Token", "Bearer $accessToken")
            header("Tema", "SYK")

            setBody(this@execute)
        }
            .body<PdlResponse<T>>()

        if (!response.errors.isNullOrEmpty()) {
            logger.error("Feilmelding ved spørring mot PDL: {}", response.errors)
            throw PdlException(response.errors)
        }

        return response.data
    }
    companion object {
        private val logger = LoggerFactory.getLogger(PdlClient::class.java)
    }
}

class PdlException(pdlErrors: List<PdlError>?) :
    RuntimeException("Feilmelding ved spørring mot PDL: $pdlErrors")

private fun String.readQuery(): String =
    this.readResource().replace(Regex("[\r\n]"), "")
