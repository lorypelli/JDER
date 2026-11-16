package com.jder.strings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadOnlyProperty

object Localization {
    var language by mutableStateOf(Language.saved())
        private set

    fun selectLanguage(selected: Language) {
        if (selected == language) return
        language = selected
        Language.save(selected)
    }
}

internal fun localized(italian: String, english: String): String =
    when (Localization.language) {
        Language.ITALIAN -> italian
        Language.ENGLISH -> english
    }

internal fun text(italian: String, english: String) =
    ReadOnlyProperty<Any?, String> { _, _ -> localized(italian, english) }
