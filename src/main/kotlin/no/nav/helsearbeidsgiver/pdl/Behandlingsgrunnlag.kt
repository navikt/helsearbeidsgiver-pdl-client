package no.nav.helsearbeidsgiver.pdl

/**
 * Oversikt over tilgjengelige behandlingsgrunnlag og mulige utvidelser finnes i
 * [behandlingskatalogen for sykepenger](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER).
 */
enum class Behandlingsgrunnlag(
    internal val behandlingsnummer: String,
) {
    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER/e1712d5c-f3e1-48c7-a830-a2da90482253). */
    @Deprecated("Bruk B139 SYKEPENGER.", ReplaceWith("SYKEPENGER"))
    INNTEKTSMELDING("B190"),

    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.ansatt.nav.no/process/purpose/SYKEPENGER/5b00b106-e066-4e98-91e0-51dc165adf04). */
    FRITAKAGP("B329"),

    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.intern.nav.no/process/purpose/SYKEPENGER/250c33c1-183c-49cc-9f3b-fdfe9f05f486). */
    SYKMELDING("B229"),

    /** Dokumentert i [behandlingskatalog](https://behandlingskatalog.ansatt.nav.no/process/purpose/SYKEPENGER/74664e24-51fc-4831-8167-8733bf9815e0). */
    SYKEPENGER("B139"),
}
