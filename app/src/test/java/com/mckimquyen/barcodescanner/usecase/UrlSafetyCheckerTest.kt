package com.mckimquyen.barcodescanner.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSafetyCheckerTest {

    @Test
    fun `exact match on global whitelist is safe`() {
        assertTrue(UrlSafetyChecker.isDomainSafe("https://google.com"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://github.com/some/repo"))
    }

    @Test
    fun `subdomain of whitelisted domain is safe`() {
        assertTrue(UrlSafetyChecker.isDomainSafe("https://www.google.com"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://accounts.google.com/login"))
    }

    @Test
    fun `VN bank and e-wallet domains are safe`() {
        assertTrue(UrlSafetyChecker.isDomainSafe("https://vietcombank.com.vn/login"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://www.momo.vn/pay"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://zalopay.vn"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://vnpay.vn/qr"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://techcombank.com"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://mbbank.com.vn"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://vpbank.com.vn"))
    }

    @Test
    fun `path-based spoof is unsafe`() {
        // Naive substring check would match "google.com" inside the path
        assertFalse(UrlSafetyChecker.isDomainSafe("https://evil.com/google.com"))
        assertFalse(UrlSafetyChecker.isDomainSafe("https://phishing.net/momo.vn/pay"))
    }

    @Test
    fun `suffix spoof is unsafe`() {
        // Naive substring/endsWith(domain) check without a leading dot would match this
        assertFalse(UrlSafetyChecker.isDomainSafe("https://google.com.evil.net"))
        assertFalse(UrlSafetyChecker.isDomainSafe("https://momo.vn.attacker.com"))
    }

    @Test
    fun `lookalike domain without dot separator is unsafe`() {
        // "evilgoogle.com" ends with "google.com" as a raw string but is NOT a subdomain
        assertFalse(UrlSafetyChecker.isDomainSafe("https://evilgoogle.com"))
        assertFalse(UrlSafetyChecker.isDomainSafe("https://notmomo.vn"))
    }

    @Test
    fun `unrelated domain is unsafe`() {
        assertFalse(UrlSafetyChecker.isDomainSafe("https://random-suspicious-site.xyz"))
    }

    @Test
    fun `case insensitive host matching`() {
        assertTrue(UrlSafetyChecker.isDomainSafe("HTTPS://GOOGLE.COM"))
        assertTrue(UrlSafetyChecker.isDomainSafe("https://WWW.MOMO.VN"))
    }

    @Test
    fun `url without scheme is still parsed`() {
        assertTrue(UrlSafetyChecker.isDomainSafe("google.com/search?q=test"))
        assertFalse(UrlSafetyChecker.isDomainSafe("evil.com/google.com"))
    }

    @Test
    fun `malformed url does not crash and is unsafe`() {
        assertFalse(UrlSafetyChecker.isDomainSafe(""))
        assertFalse(UrlSafetyChecker.isDomainSafe("not a url at all !!!"))
        assertFalse(UrlSafetyChecker.isDomainSafe("://///"))
    }

    @Test
    fun `custom whitelist overrides default`() {
        val custom = listOf("example.com")
        assertTrue(UrlSafetyChecker.isDomainSafe("https://example.com", custom))
        assertFalse(UrlSafetyChecker.isDomainSafe("https://google.com", custom))
    }
}
