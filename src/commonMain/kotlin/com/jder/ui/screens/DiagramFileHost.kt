package com.jder.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.jder.data.DiagramFileRepository
import com.jder.domain.model.DiagramEditorState
import com.jder.ui.components.DiagramPalette
import com.jder.ui.components.toExportPalette
import com.jder.ui.theme.ThemeState

class DiagramFileActions(
    val open: () -> Unit,
    val save: () -> Unit,
    val saveAs: () -> Unit,
    val export: () -> Unit,
    val toggleAutoSave: (() -> Unit)?,
    val toggleAutoExport: (() -> Unit)?,
    val flushAutoSave: () -> Boolean,
)

@Composable
expect fun <T> DiagramFileHost(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    themeState: ThemeState,
    renderImage: (DiagramPalette) -> ImageBitmap,
    content: @Composable (DiagramFileActions) -> Unit,
)

fun renderExportImage(
    themeState: ThemeState,
    renderImage: (DiagramPalette) -> ImageBitmap,
): ImageBitmap = renderImage(themeState.selectedPalette.lightScheme.toExportPalette())
