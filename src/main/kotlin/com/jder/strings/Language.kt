package com.jder.strings

import com.jder.ui.components.logError
import java.util.Locale
import java.util.prefs.Preferences

private const val PREFERENCES_NODE = "com/jder"
private const val LANGUAGE_KEY = "language"

enum class Language(val displayName: String, val locale: Locale) {
    ITALIAN("Italiano", Locale.ITALIAN),
    ENGLISH("English", Locale.ENGLISH);

    companion object {
        private val preferences by lazy { Preferences.userRoot().node(PREFERENCES_NODE) }

        fun saved(): Language =
            runCatching { preferences.get(LANGUAGE_KEY, null) }.getOrNull()?.let(::fromCode)
                ?: fromCode(Locale.getDefault().language)
                ?: ENGLISH

        fun save(language: Language) {
            try {
                preferences.put(LANGUAGE_KEY, language.locale.language)
                preferences.flush()
            } catch (e: Exception) {
                logError(MenuStrings.LANGUAGE_SAVE_FAILED, e)
            }
        }

        private fun fromCode(code: String): Language? = entries.find { it.locale.language == code }
    }
}
