package com.jder.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ThemeState(
    initialDarkTheme: Boolean = ThemePreferences.savedDarkTheme(),
    initialPalette: ColorPalette = ThemePreferences.savedPalette(),
) {
    var isDarkTheme by mutableStateOf(initialDarkTheme)
    var selectedPalette by mutableStateOf(initialPalette)

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        ThemePreferences.saveDarkTheme(isDarkTheme)
    }

    fun selectPalette(palette: ColorPalette) {
        if (palette == selectedPalette) return
        selectedPalette = palette
        ThemePreferences.savePalette(palette)
    }
}
