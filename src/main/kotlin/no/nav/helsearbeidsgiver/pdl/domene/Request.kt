package no.nav.helsearbeidsgiver.pdl.domene

import kotlinx.serialization.Serializable

@Serializable
internal data class PdlQuery(
    val query: String,
    val variables: Variables,
)

@Serializable
internal data class Variables(
    val ident: String? = null,
    val identer: List<String>? = null
)
