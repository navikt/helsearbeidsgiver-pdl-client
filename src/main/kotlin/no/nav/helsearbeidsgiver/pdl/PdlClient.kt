package no.nav.helsearbeidsgiver.pdl

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Enkel GraphQL-klient for PDL som kan enten hente navn fra aktør eller fnr (ident)
 * eller hente mer fullstendig data om en person via fnr eller aktørid (ident)
 *
 * Authorisasjon gjøres via den gitte Token prodvideren, og servicebrukeren som er angitt i token provideren må være i
 * i AD-gruppen 0000-GA-TEMA_SYK som dokumentert her
 * https://navikt.github.io/pdl/index-intern.html#_konsumentroller_basert_p%C3%A5_tema
 *
 * Klienten vil alltid gi PDL-Temaet 'SYK', så om du trenger et annet tema må du endre denne klienten.
 */
class PdlClient(
    private val pdlUrl: String,
    private val getAccessToken: () -> String,
    private val httpClient: HttpClient
) {
    private val personNavnQuery = this::class.java.getResource("/pdl/hentPersonNavn.graphql")!!.readText().replace(Regex("[\n\r]"), "")
    private val fullPersonQuery = this::class.java.getResource("/pdl/hentFullPerson.graphql")!!.readText().replace(Regex("[\n\r]"), "")

    fun personNavn(ident: String): PdlHentPersonNavn.PdlPersonNavneliste? {
        val entity = PdlQueryObject(personNavnQuery, Variables(ident))
        val response = queryPdl<PdlHentPersonNavn?, PdlResponse<PdlHentPersonNavn?>>(entity)
        return response?.hentPerson
    }

    fun fullPerson(ident: String): PdlHentFullPerson? {
        val queryObject = PdlQueryObject(fullPersonQuery, Variables(ident))
        return queryPdl(queryObject)
    }

    fun personNavn(ident: String, userLoginToken: String): PdlHentPersonNavn.PdlPersonNavneliste? {
        val entity = PdlQueryObject(personNavnQuery, Variables(ident))
        val response = queryPdl<PdlHentPersonNavn?, PdlResponse<PdlHentPersonNavn?>>(entity, userLoginToken)
        return response?.hentPerson
    }

    fun fullPerson(ident: String, userLoginToken: String): PdlHentFullPerson? {
        val queryObject = PdlQueryObject(fullPersonQuery, Variables(ident))
        val response = queryPdl<PdlHentFullPerson?, PdlResponse<PdlHentFullPerson?>>(queryObject, userLoginToken)
        return response
    }

    private inline fun <K, reified T : PdlResponse<K>> queryPdl(graphqlQuery: PdlQueryObject, loggedInUserToken: String? = null): K? {
        val stsToken = getAccessToken()
        val pdlPersonReponse = runBlocking {
            httpClient.post<T> {
                url(pdlUrl)
                body = TextContent(Json.encodeToString(graphqlQuery), contentType = ContentType.Application.Json)
                header("Tema", "SYK")
                header("Authorization", "Bearer ${loggedInUserToken ?: stsToken}")
                header("Nav-Consumer-Token", "Bearer $stsToken")
            }
        }

        if (pdlPersonReponse.errors != null && pdlPersonReponse.errors!!.isNotEmpty()) {
            throw PdlException(pdlPersonReponse.errors)
        }

        return pdlPersonReponse.data
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PdlClient::class.java)
    }

    class PdlException(val pdlErrors: List<PdlError>?) : RuntimeException()
}
