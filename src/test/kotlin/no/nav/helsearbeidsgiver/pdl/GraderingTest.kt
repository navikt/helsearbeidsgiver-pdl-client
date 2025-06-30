package no.nav.helsearbeidsgiver.pdl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GraderingTest : FunSpec({
    context("Oversett diskresjonskode fra kodeverk") {
        test("Håndterer gyldige verdier") {
            Gradering.STRENGT_FORTROLIG.tilKodeverkDiskresjonskode() shouldBe "SPSF"
            Gradering.FORTROLIG.tilKodeverkDiskresjonskode() shouldBe "SPFO"
        }

        test("Håndterer ugyldige verdier") {
            "".tilKodeverkDiskresjonskode() shouldBe null
            "Whatever".tilKodeverkDiskresjonskode() shouldBe null
        }
    }
})
