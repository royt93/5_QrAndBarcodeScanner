package com.mckimquyen.barcodescanner.usecase

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        // If system, don't change locale
        if (languageCode == "system") {
            return context
        }

        val locale = when (languageCode) {
            "vi" -> Locale("vi")
            "en" -> Locale("en")
            "es" -> Locale("es")
            "fr" -> Locale("fr")
            "de" -> Locale("de")
            "ja" -> Locale("ja")
            "it" -> Locale("it")
            "pt" -> Locale("pt", "BR")
            "ru" -> Locale("ru")
            "zh" -> Locale("zh")
            "zh-TW" -> Locale("zh", "TW")
            "pl" -> Locale("pl")
            "tr" -> Locale("tr")
            "ca" -> Locale("ca")
            "eu" -> Locale("eu")
            "iw" -> Locale("iw")
            "el" -> Locale("el")
            "fa" -> Locale("fa")
            else -> Locale("en")
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "system" -> "System Default"
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "ja" -> "日本語"
            "it" -> "Italiano"
            "pt" -> "Português (Brasil)"
            "ru" -> "Русский"
            "zh" -> "中文"
            "zh-TW" -> "中文 (台灣)"
            "pl" -> "Polski"
            "tr" -> "Türkçe"
            "ca" -> "Català"
            "eu" -> "Euskara"
            "iw" -> "עברית"
            "el" -> "Ελληνικά"
            "fa" -> "فارسی"
            else -> "English"
        }
    }

    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "system" to "System Default",
            "vi" to "Tiếng Việt",
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "de" to "Deutsch",
            "ja" to "日本語",
            "it" to "Italiano",
            "pt" to "Português (Brasil)",
            "ru" to "Русский",
            "zh" to "中文",
            "zh-TW" to "中文 (台灣)",
            "pl" to "Polski",
            "tr" to "Türkçe",
            "ca" to "Català",
            "eu" to "Euskara",
            "iw" to "עברית",
            "el" to "Ελληνικά",
            "fa" to "فارسی"
        )
    }
}
