package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Enkel GraphQL-klient for PDL som kan enten hente navn fra aktør eller fnr (ident)
 * eller hente mer fullstendig data om en person via fnr eller aktørid (ident)
 *
 * Klienten bruker alltid PDL-Temaet 'SYK', så om du trenger et annet tema må du endre denne klienten.
 * Tema vil bli erstattet av behandlingsgrunnlag på sikt.
 * Behandlingsgrunnlag som er tilgjengelig i klienten kan utvides ved behov.
 */
class PdlClient(
    private val url: String,
    private val behandlingsgrunnlag: Behandlingsgrunnlag,
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
        val stsToken = getAccessToken()

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(userLoginToken ?: stsToken)
            header("behandlingsnummer", behandlingsgrunnlag.behandlingsnummer)
            // Erstattes av 'behandlingsnummer'-header, beholdes i overgangsfase
            header("Tema", "SYK")

            setBody(this@execute)
        }
            .body<PdlResponse<T>>()

        if (!response.errors.isNullOrEmpty()) {
            throw PdlException(response.errors)
        }

        return response.data
    }
}

class PdlException(val errors: List<PdlError>?) : RuntimeException()

private fun String.readQuery(): String =
    this.readResource().replace(Regex("[\r\n]"), "")
