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
import no.nav.helsearbeidsgiver.pdl.domene.IdentResponse
import no.nav.helsearbeidsgiver.pdl.domene.PdlError
import no.nav.helsearbeidsgiver.pdl.domene.PdlQuery
import no.nav.helsearbeidsgiver.pdl.domene.PersonBolkResultat
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavnResultat
import no.nav.helsearbeidsgiver.pdl.domene.Response
import no.nav.helsearbeidsgiver.pdl.domene.Variables
import no.nav.helsearbeidsgiver.utils.json.fromJson
import no.nav.helsearbeidsgiver.utils.json.toJson
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

/**
 * Enkel GraphQL-klient for PDL som kan enten hente navn fra aktør eller fnr (ident)
 * eller hente mer fullstendig data om en person via fnr eller aktørid (ident)
 *
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
    private val personBolkQuery = "hentPersonBolk.graphql".readQuery()
    private val aktorIdQuery = "hentAktorID.graphql".readQuery()

    private val logger = sikkerLogger()
    suspend fun personNavn(ident: String): PersonNavn? =
        PdlQuery(personNavnQuery, Variables(ident = ident))
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

    suspend fun fullPerson(ident: String): FullPerson? {
        val resultat = PdlQuery(fullPersonQuery, Variables(ident = ident))
            .execute(FullPersonResultat.serializer())
        val geografiskTilknytning = resultat?.hentGeografiskTilknytning?.hentTilknytning()
        return resultat?.hentPerson
            ?.let {
                val navn = it.navn.firstOrNull()
                val foedsel = it.foedsel.firstOrNull()
                val gradering = getKodeverkDiskresjonskode(it?.adressebeskyttelse?.firstOrNull()?.gradering)
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
                        gradering = gradering,
                        geografiskTilknytning = geografiskTilknytning,
                    )
                }
            }
    }

    /*
    OBS: PersonBolk-kallet henter ikke ut geografiskTilknytning!
    Så FullPerson fra dette kallet, vil aldri ha dette satt..
    TODO?: Lag to forskjellige Person-objekter, for å skille mellom disse
     */
    suspend fun personBolk(identer: List<String>): List<FullPerson>? =
        PdlQuery(personBolkQuery, Variables(identer = identer))
            .execute(PersonBolkResultat.serializer())
            ?.hentPersonBolk?.mapNotNull {
                if (it.code.equals("ok", ignoreCase = true)) {
                    val navn = it.person?.navn?.firstOrNull()
                    val foedsel = it.person?.foedsel?.firstOrNull()
                    val gradering = getKodeverkDiskresjonskode(it.person?.adressebeskyttelse?.firstOrNull()?.gradering)
                    if (navn == null || foedsel == null) {
                        null
                    } else {
                        FullPerson(
                            navn = PersonNavn(navn.fornavn, navn.mellomnavn, navn.etternavn),
                            foedselsdato = foedsel.foedselsdato,
                            ident = it.ident,
                            gradering = gradering,
                        )
                    }
                } else {
                    logger.warn("Fikk kode ${it.code}, kunne ikke finne ${it.ident}")
                    null
                }
            }

    suspend fun hentAktoerID(ident: String): String? =
        PdlQuery(aktorIdQuery, Variables(ident = ident))
            .execute(IdentResponse.serializer())
            ?.hentIdenter
            ?.identer
            ?.firstOrNull()
            ?.ident

    private suspend fun <T : Any> PdlQuery.execute(serializer: KSerializer<T>): T? {
        val request = toJson(PdlQuery.serializer())

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(getAccessToken())
            header("Behandlingsnummer", behandlingsgrunnlag.behandlingsnummer)

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

fun getKodeverkDiskresjonskode(gradering: String?): String? {
    return when (gradering) {
        KONSTANTER.STRENGT_FORTROLIG -> "SPSF"
        KONSTANTER.FORTROLIG -> "SPFO"
        else -> null
    }
}
class PdlException(val errors: List<PdlError>?) : RuntimeException()

private fun String.readQuery(): String =
    ClassLoader.getSystemResource(this)
        .readText()
        .replace(Regex("[\r\n]"), "")
