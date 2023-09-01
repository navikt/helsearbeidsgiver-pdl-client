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
)

@Serializable
internal data class FullPersonListe(
    val navn: List<PdlPersonNavn>,
    val foedsel: List<Foedsel>,
)

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
internal data class Foedsel(
    val foedselsdato: LocalDate,
)
