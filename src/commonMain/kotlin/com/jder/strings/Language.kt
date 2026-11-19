package com.jder.strings

import com.jder.data.readPreference
import com.jder.data.savePreference
import com.jder.ui.components.Toasts
import com.jder.ui.utils.systemLanguage

private const val LANGUAGE_KEY = "language"

enum class Language(val displayName: String, val code: String) {
    ITALIAN("Italiano", "it"),
    ENGLISH("English", "en");

    companion object {
        fun saved(): Language =
            runCatching { readPreference(LANGUAGE_KEY) }.getOrNull()?.let(::fromCode)
                ?: fromCode(systemLanguage)
                ?: ENGLISH

        fun save(language: Language) =
            savePreference(LANGUAGE_KEY, language.code) {
                Toasts.error(MenuStrings.LANGUAGE_SAVE_FAILED, it)
            }

        private fun fromCode(code: String): Language? = entries.find { it.code == code }
    }
}
