@file:UseSerializers(LocalDateSerializer::class)

package no.nav.helsearbeidsgiver.pdl.domene

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.helsearbeidsgiver.utils.json.serializer.LocalDateSerializer
import java.time.LocalDate

@Serializable
internal data class Response<T>(
    val errors: List<PdlError>? = null,
    val data: T? = null,
)

/** Tilsvarer graphql-spørringen hentPersonNavn.graphql */
@Serializable
internal data class PersonNavnResultat(
    val hentPerson: PersonNavnListe? = null,
)

@Serializable
internal data class PersonNavnListe(val navn: List<PdlPersonNavn>)

/** Tilsvarer graphql-spørringen hentFullPerson.graphql */
@Serializable
internal data class FullPersonResultat(
    val hentPerson: FullPersonListe? = null,
    val hentGeografiskTilknytning: PdlGeografiskTilknytning? = null,
)

@Serializable
internal data class FullPersonListe(
    val navn: List<PdlPersonNavn>,
    val foedselsdato: List<Foedselsdato>,
    val adressebeskyttelse: List<PdlAdresseBeskyttelse>,
)

@Serializable
internal data class PdlAdresseBeskyttelse(val gradering: String?)

@Serializable
internal data class PdlGeografiskTilknytning(
    val gtType: PdlGtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
) {
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
internal data class PersonBolkResultat(
    val hentPersonBolk: List<PdlBolkPerson>? = null,
)

@Serializable
internal data class PdlBolkPerson(
    val ident: String,
    val person: FullPersonListe?,
    val code: String,
)

@Serializable
internal data class PdlPersonNavn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)

@Serializable
internal data class Foedselsdato(
    val foedselsdato: LocalDate,
)

@Serializable
internal data class IdentResponse(
    val hentIdenter: HentIdenter? = null,
)

@Serializable
internal data class HentIdenter(
    val identer: List<Ident>? = null,
)

@Serializable
internal data class Ident(
    val ident: String? = null,
)
