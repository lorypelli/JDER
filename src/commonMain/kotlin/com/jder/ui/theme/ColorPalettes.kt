package com.jder.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.jder.strings.localized
import com.jder.ui.utils.sharedList
import com.jder.ui.utils.sharedValue

data class ColorPalette(
    val id: String,
    val name: () -> String,
    val primaryColor: Color,
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
)

object ColorPalettes {
    val allPalettes: List<ColorPalette> = sharedList("palettes").map(::createPalette)

    val default: ColorPalette =
        sharedValue("defaultPalette").let { defaultId ->
            allPalettes.first { it.id == defaultId }
        }

    private fun colors(row: String): List<Color> =
        row.split(' ').map { Color(0xFF000000 or it.toLong(16)) }

    private fun createPalette(row: String): ColorPalette {
        val (id, italian, english, light, dark) = row.split('|')
        val lightColors = colors(light)
        val darkColors = colors(dark)
        return ColorPalette(
            id = id,
            name = { localized(italian, english) },
            primaryColor = lightColors[0],
            lightScheme =
                lightColorScheme(
                    primary = lightColors[0],
                    onPrimary = lightColors[1],
                    primaryContainer = lightColors[2],
                    onPrimaryContainer = lightColors[3],
                    secondary = lightColors[4],
                    onSecondary = lightColors[5],
                    secondaryContainer = lightColors[6],
                    onSecondaryContainer = lightColors[7],
                    tertiary = lightColors[8],
                    onTertiary = lightColors[9],
                    tertiaryContainer = lightColors[10],
                    onTertiaryContainer = lightColors[11],
                    error = Color(0xFFB00020),
                    errorContainer = Color(0xFFFCD8DF),
                    onError = Color(0xFFFFFFFF),
                    onErrorContainer = Color(0xFF8C0009),
                    background = Color(0xFFFAFAFA),
                    onBackground = Color(0xFF1A1A1A),
                    surface = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF1A1A1A),
                    surfaceVariant = Color(0xFFE0E0E0),
                    onSurfaceVariant = Color(0xFF424242),
                    outline = Color(0xFF757575),
                    inverseOnSurface = Color(0xFFF5F5F5),
                    inverseSurface = Color(0xFF2F2F2F),
                    inversePrimary = darkColors[0],
                ),
            darkScheme =
                darkColorScheme(
                    primary = darkColors[0],
                    onPrimary = darkColors[1],
                    primaryContainer = darkColors[2],
                    onPrimaryContainer = darkColors[3],
                    secondary = darkColors[4],
                    onSecondary = darkColors[5],
                    secondaryContainer = darkColors[6],
                    onSecondaryContainer = darkColors[7],
                    tertiary = darkColors[8],
                    onTertiary = darkColors[9],
                    tertiaryContainer = darkColors[10],
                    onTertiaryContainer = darkColors[11],
                    error = Color(0xFFCF6679),
                    errorContainer = Color(0xFF93000A),
                    onError = Color(0xFF690005),
                    onErrorContainer = Color(0xFFFCD8DF),
                    background = Color(0xFF121212),
                    onBackground = Color(0xFFE0E0E0),
                    surface = Color(0xFF1E1E1E),
                    onSurface = Color(0xFFE0E0E0),
                    surfaceVariant = Color(0xFF424242),
                    onSurfaceVariant = Color(0xFFBDBDBD),
                    outline = Color(0xFF8D8D8D),
                    inverseOnSurface = Color(0xFF1A1A1A),
                    inverseSurface = Color(0xFFE0E0E0),
                    inversePrimary = lightColors[0],
                ),
        )
    }
}
