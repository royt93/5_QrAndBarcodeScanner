package com.mckimquyen.barcodescanner.usecase

import java.net.URI

object UrlSafetyChecker {

    // Global brand domains + VN banks/e-wallets (common Quishing targets)
    val SAFE_DOMAINS = listOf(
        "google.com",
        "facebook.com",
        "youtube.com",
        "vnexpress.net",
        "github.com",
        "amazon.com",
        "apple.com",
        "vietcombank.com.vn",
        "techcombank.com",
        "mbbank.com.vn",
        "vpbank.com.vn",
        "momo.vn",
        "zalopay.vn",
        "vnpay.vn",
        "shopee.vn",
        "lazada.vn",
        "tiktok.com",
    )

    fun isDomainSafe(url: String, safeDomains: List<String> = SAFE_DOMAINS): Boolean {
        val host = extractHost(url) ?: return false
        return safeDomains.any { domain -> host == domain || host.endsWith(".$domain") }
    }

    private fun extractHost(url: String): String? {
        val withScheme = if (url.contains("://")) url else "https://$url"
        return runCatching { URI(withScheme).host?.lowercase() }.getOrNull()
    }
}
