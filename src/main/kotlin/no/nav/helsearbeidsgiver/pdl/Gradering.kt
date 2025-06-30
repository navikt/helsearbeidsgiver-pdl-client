package no.nav.helsearbeidsgiver.pdl

internal object Gradering {
    val FORTROLIG = "FORTROLIG"
    val STRENGT_FORTROLIG = "STRENGT_FORTROLIG"
}

internal fun String.tilKodeverkDiskresjonskode(): String? =
    when (this) {
        Gradering.STRENGT_FORTROLIG -> "SPSF"
        Gradering.FORTROLIG -> "SPFO"
        else -> null
    }
