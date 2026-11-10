package com.jder.ui.theme

import com.jder.data.AppPreferences
import com.jder.strings.MenuStrings
import com.jder.ui.components.logError

private const val DARK_THEME_KEY = "darkTheme"
private const val PALETTE_KEY = "palette"

object ThemePreferences {
    fun savedDarkTheme(): Boolean =
        runCatching { AppPreferences.node.getBoolean(DARK_THEME_KEY, false) }.getOrDefault(false)

    fun savedPalette(): ColorPalette =
        runCatching { AppPreferences.node.get(PALETTE_KEY, null) }
            .getOrNull()
            ?.let { id -> ColorPalettes.allPalettes.find { it.id == id } } ?: ColorPalettes.Blue

    fun saveDarkTheme(isDarkTheme: Boolean) =
        AppPreferences.saveSafely({ logError(MenuStrings.THEME_SAVE_FAILED, it) }) {
            putBoolean(DARK_THEME_KEY, isDarkTheme)
        }

    fun savePalette(palette: ColorPalette) =
        AppPreferences.saveSafely({ logError(MenuStrings.THEME_SAVE_FAILED, it) }) {
            put(PALETTE_KEY, palette.id)
        }
}
