package com.jder.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Immutable
data class DiagramPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val text: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val outline: Color,
    val grid: Color,
    val textBackground: Color,
    val signalHigh: Color,
    val signalLow: Color,
    val signalUnknown: Color,
) {
  val selected: Color
    get() = tertiary
}

fun ColorScheme.toDiagramPalette(): DiagramPalette {
  val isDark = background.luminance() < 0.5f
  return DiagramPalette(
      background = background,
      surface = surface,
      surfaceVariant = surfaceVariant,
      text = onSurface,
      primary = primary,
      secondary = secondary,
      tertiary = tertiary,
      error = error,
      outline = outline,
      grid = if (isDark) surfaceVariant.copy(alpha = 0.55f) else surfaceVariant,
      textBackground = surface.copy(alpha = 0.9f),
      signalHigh = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32),
      signalLow = lerp(background, onSurface, 0.55f),
      signalUnknown = outline,
  )
}

fun ColorScheme.toExportPalette(): DiagramPalette =
    toDiagramPalette()
        .copy(background = Color.White, surface = Color.White, textBackground = Color.White)
