package no.nav.helsearbeidsgiver.pdl

import kotlinx.serialization.Serializable

@Serializable
data class PdlQueryObject(
    val query: String,
    val variables: Variables
)

@Serializable
data class Variables(
    val ident: String
)
