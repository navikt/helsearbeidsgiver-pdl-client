package no.nav.helsearbeidsgiver.pdl

/**
 * Oversikt over tilgjengelige behandlingsgrunnlag og mulige utvidelser finnes i
 * [behandlingskatalogen for sykepenger](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER).
 */
enum class Behandlingsgrunnlag(
    internal val behandlingsnummer: String,
) {
    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER/e1712d5c-f3e1-48c7-a830-a2da90482253). */
    INNTEKTSMELDING("B190"),
    FRITAKAGP("B329"),
    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER/250c33c1-183c-49cc-9f3b-fdfe9f05f486). */
    SYKMELDING("B229"),
}
