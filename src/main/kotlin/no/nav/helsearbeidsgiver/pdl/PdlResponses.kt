package no.nav.helsearbeidsgiver.pdl

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tilsvarer graphql-spørringen hentPersonNavn.graphql
 */
@Serializable
data class PdlHentPersonNavn(val hentPerson: PdlPersonNavneliste?) {
    @Serializable
    data class PdlPersonNavneliste(val navn: List<PdlPersonNavn>) {
        @Serializable
        data class PdlPersonNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String, val metadata: PdlPersonNavnMetadata)
    }
}

/**
 * Tilsvarer graphql-spørringen hentFullPerson.graphql
 */
@Serializable
data class PdlHentFullPerson(val hentPerson: PdlFullPersonliste?, val hentIdenter: PdlIdentResponse?, val hentGeografiskTilknytning: PdlGeografiskTilknytning?) {

    @Serializable
    data class PdlIdentResponse(val identer: List<PdlIdent>) {
        fun trekkUtIdent(gruppe: PdlIdent.PdlIdentGruppe): String? = identer.filter { it.gruppe == gruppe }.firstOrNull()?.ident
    }

    @Serializable
    data class PdlGeografiskTilknytning(val gtType: PdlGtType, val gtKommune: String?, val gtBydel: String?, val gtLand: String?) {
        fun hentTilknytning(): String? {
            return when (gtType) {
                PdlGtType.KOMMUNE -> gtKommune
                PdlGtType.BYDEL -> gtBydel
                PdlGtType.UTLAND -> gtLand
                PdlGtType.UDEFINERT -> null
            }
        }
        enum class PdlGtType { KOMMUNE, BYDEL, UTLAND, UDEFINERT }
    }

    @Serializable
    data class PdlFullPersonliste(
        val navn: List<PdlNavn>,
        val foedsel: List<PdlFoedsel>,
        val doedsfall: List<PdlDoedsfall>,
        val adressebeskyttelse: List<PdlAdressebeskyttelse>,
        val statsborgerskap: List<PdlStatsborgerskap>,
        val bostedsadresse: List<PdlBostedsadresse>,
        val kjoenn: List<PdlKjoenn>
    ) {

        fun trekkUtFulltNavn() = navn.map { "${it.fornavn} ${it.mellomnavn ?: ""} ${it.etternavn}".replace("  ", " ") }.firstOrNull()
        fun trekkUtKjoenn() = kjoenn.firstOrNull()?.kjoenn
        fun trekkUtDoedsfalldato() = doedsfall.firstOrNull()?.doedsdato
        fun trekkUtFoedselsdato() = foedsel.firstOrNull()?.foedselsdato
        fun trekkUtDiskresjonskode() = adressebeskyttelse.firstOrNull()?.getKodeverkDiskresjonskode()

        @Serializable
        data class PdlNavn(
            val fornavn: String,
            val mellomnavn: String?,
            val etternavn: String,
            val metadata: PdlPersonNavnMetadata
        )

        @Serializable
        data class PdlKjoenn(val kjoenn: String)

        @Serializable
        data class PdlAdressebeskyttelse(val gradering: String) {
            fun getKodeverkDiskresjonskode(): String? {
                return when (gradering) {
                    "STRENGT_FORTROLIG" -> "SPSF"
                    "FORTROLIG" -> "SPFO"
                    else -> null
                }
            }
        }
        @Serializable
        data class PdlFoedsel(
            @Serializable(with = LocalDateSerializer::class)
            val foedselsdato: LocalDate
        )
        @Serializable
        data class PdlDoedsfall(
            @Serializable(with = LocalDateSerializer::class)
            val doedsdato: LocalDate
        )
        @Serializable
        data class PdlStatsborgerskap(val land: String)
        @Serializable
        data class PdlBostedsadresse(
            @Serializable(with = LocalDateTimeSerializer::class)
            val gyldigFraOgMed: LocalDateTime?,
            @Serializable(with = LocalDateTimeSerializer::class)
            val gyldigTilOgMed: LocalDateTime?,
            // For å hente ut om man er bosatt i norge hentes det ut om disse addressene finnes
            // dersom noden er null finnes ikke addressen
            // TODO - val vegadresse: JsonNode?,
            // TODO - val matrikkeladresse: JsonNode?,
            // TODO - val ukjentBosted: JsonNode?,
            @Serializable(with = LocalDateSerializer::class)
            val angittFlyttedato: LocalDate? = null
        )
    }
}

@Serializable
data class PdlIdent(val ident: String, val gruppe: PdlIdentGruppe) {
    enum class PdlIdentGruppe { AKTORID, FOLKEREGISTERIDENT, NPID }
}

@Serializable
data class PdlPersonNavnMetadata(
    /**
     * Inneholder "freg" dersom "eieren" av informasjonen er folkeregisteret
     */
    val master: String
)

@Serializable
open class PdlResponse<T>(
    open val errors: List<PdlError>?,
    open val data: T?
)

@Serializable
data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

@Serializable
data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

@Serializable
data class PdlErrorExtension(
    val code: String?,
    val classification: String
)
