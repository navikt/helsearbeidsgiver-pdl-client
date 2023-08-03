package no.nav.helsearbeidsgiver.pdl.domene

import kotlinx.serialization.Serializable

@Serializable
data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>? = null,
    val extensions: PdlErrorExtension,
)

@Serializable
data class PdlErrorLocation(
    val line: Int? = null,
    val column: Int? = null,
)

@Serializable
data class PdlErrorExtension(
    val code: String? = null,
    val classification: String,
)
