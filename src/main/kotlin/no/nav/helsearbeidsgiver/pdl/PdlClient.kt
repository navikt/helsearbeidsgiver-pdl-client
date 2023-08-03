package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.KSerializer
import no.nav.helsearbeidsgiver.pdl.domene.FullPerson
import no.nav.helsearbeidsgiver.pdl.domene.FullPersonResultat
import no.nav.helsearbeidsgiver.pdl.domene.PdlError
import no.nav.helsearbeidsgiver.pdl.domene.PdlQuery
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavnResultat
import no.nav.helsearbeidsgiver.pdl.domene.Response
import no.nav.helsearbeidsgiver.pdl.domene.Variables
import no.nav.helsearbeidsgiver.utils.json.fromJson
import no.nav.helsearbeidsgiver.utils.json.toJson

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
    private val getAccessToken: () -> String,
) {
    private val httpClient = createHttpClient()

    private val personNavnQuery = "hentPersonNavn.graphql".readQuery()
    private val fullPersonQuery = "hentFullPerson.graphql".readQuery()

    suspend fun personNavn(ident: String): PersonNavn? =
        PdlQuery(personNavnQuery, Variables(ident))
            .execute(PersonNavnResultat.serializer())
            ?.hentPerson
            ?.navn
            ?.firstOrNull()
            ?.let {
                PersonNavn(
                    fornavn = it.fornavn,
                    mellomnavn = it.mellomnavn,
                    etternavn = it.etternavn,
                )
            }

    suspend fun fullPerson(ident: String): FullPerson? =
        PdlQuery(fullPersonQuery, Variables(ident))
            .execute(FullPersonResultat.serializer())
            ?.hentPerson
            ?.let {
                val navn = it.navn.firstOrNull()
                val foedsel = it.foedsel.firstOrNull()

                if (navn == null || foedsel == null) {
                    null
                } else {
                    FullPerson(
                        navn = PersonNavn(
                            fornavn = navn.fornavn,
                            mellomnavn = navn.mellomnavn,
                            etternavn = navn.etternavn,
                        ),
                        foedselsdato = foedsel.foedselsdato,
                    )
                }
            }

    private suspend fun <T : Any> PdlQuery.execute(serializer: KSerializer<T>): T? {
        val request = toJson(PdlQuery.serializer())

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(getAccessToken())
            header("Behandlingsnummer", behandlingsgrunnlag.behandlingsnummer)
            // Erstattes av 'behandlingsnummer'-header, beholdes i overgangsfase
            header("Tema", "SYK")

            setBody(request)
        }
            .bodyAsText()
            .fromJson(Response.serializer(serializer))

        if (!response.errors.isNullOrEmpty()) {
            throw PdlException(response.errors)
        }

        return response.data
    }
}

class PdlException(val errors: List<PdlError>?) : RuntimeException()

private fun String.readQuery(): String =
    readResource().replace(Regex("[\r\n]"), "")
