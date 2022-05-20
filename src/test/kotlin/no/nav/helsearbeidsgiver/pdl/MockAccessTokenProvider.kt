package no.nav.helsearbeidsgiver.pdl

import no.nav.helsearbeidsgiver.tokenprovider.AccessTokenProvider

class MockAccessTokenProvider : AccessTokenProvider {
    override fun getToken(): String {
        return "token"
    }
}
