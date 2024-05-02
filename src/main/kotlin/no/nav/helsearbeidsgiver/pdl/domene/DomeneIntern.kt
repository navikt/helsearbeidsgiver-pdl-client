@file:UseSerializers(LocalDateSerializer::class)

package no.nav.helsearbeidsgiver.pdl.domene

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.helsearbeidsgiver.utils.json.serializer.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class FullPerson(
    val navn: PersonNavn,
    val foedselsdato: LocalDate,
    val ident: String? = null,
    val diskresjonskode: String? = null,
    val geografiskTilknytning: String? = null,
)

@Serializable
data class PersonNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
) {
    fun fulltNavn(): String =
        listOfNotNull(
            fornavn,
            mellomnavn,
            etternavn,
        )
            .filter(String::isNotBlank)
            .joinToString(separator = " ")
}
