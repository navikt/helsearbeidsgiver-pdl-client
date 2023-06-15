package no.nav.helsearbeidsgiver.pdl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn

class PersonNavnTest : FunSpec({
    context("fulltNavn") {
        test("inkluderer mellomnavn som har verdi") {
            val navn = PersonNavn(
                fornavn = "Samwise",
                mellomnavn = "'The Brave'",
                etternavn = "Gamgee"
            )

            navn.fulltNavn() shouldBe "Samwise 'The Brave' Gamgee"
        }

        test("ekskluderer mellomnavn som er 'null'") {
            val navn = PersonNavn(
                fornavn = "Frodo",
                mellomnavn = null,
                etternavn = "Baggins"
            )

            navn.fulltNavn() shouldBe "Frodo Baggins"
        }

        test("ekskluderer navn som er tomt") {
            val navn = PersonNavn(
                fornavn = "Bilbo",
                mellomnavn = "",
                etternavn = "Baggins"
            )

            navn.fulltNavn() shouldBe "Bilbo Baggins"
        }

        test("ekskluderer navn som er whitespace") {
            val navn = PersonNavn(
                fornavn = "Gandalf",
                mellomnavn = null,
                etternavn = "  "
            )

            navn.fulltNavn() shouldBe "Gandalf"
        }
    }
})
