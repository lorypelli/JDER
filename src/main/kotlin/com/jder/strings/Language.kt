package com.jder.strings

import com.jder.data.AppPreferences
import com.jder.ui.components.logError
import java.util.Locale

private const val LANGUAGE_KEY = "language"

enum class Language(val displayName: String, val locale: Locale) {
    ITALIAN("Italiano", Locale.ITALIAN),
    ENGLISH("English", Locale.ENGLISH);

    companion object {
        fun saved(): Language =
            runCatching { AppPreferences.node.get(LANGUAGE_KEY, null) }.getOrNull()?.let(::fromCode)
                ?: fromCode(Locale.getDefault().language)
                ?: ENGLISH

        fun save(language: Language) =
            AppPreferences.saveSafely({ logError(MenuStrings.LANGUAGE_SAVE_FAILED, it) }) {
                put(LANGUAGE_KEY, language.locale.language)
            }

        private fun fromCode(code: String): Language? = entries.find { it.locale.language == code }
    }
}
