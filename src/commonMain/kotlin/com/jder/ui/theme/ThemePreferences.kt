package com.jder.ui.theme

import com.jder.data.readPreference
import com.jder.data.savePreference
import com.jder.strings.MenuStrings
import com.jder.ui.components.Toasts

private const val DARK_THEME_KEY = "darkTheme"
private const val PALETTE_KEY = "palette"

object ThemePreferences {
    fun savedDarkTheme(): Boolean = runCatching {
        readPreference(DARK_THEME_KEY).toBoolean()
    }
        .getOrDefault(false)

    fun savedPalette(): ColorPalette =
        runCatching { readPreference(PALETTE_KEY) }
            .getOrNull()
            ?.let { id -> ColorPalettes.allPalettes.find { it.id == id } } ?: ColorPalettes.Blue

    fun saveDarkTheme(isDarkTheme: Boolean) = save(DARK_THEME_KEY, isDarkTheme.toString())

    fun savePalette(palette: ColorPalette) = save(PALETTE_KEY, palette.id)

    private fun save(key: String, value: String) =
        savePreference(key, value) { Toasts.error(MenuStrings.THEME_SAVE_FAILED, it) }
}
