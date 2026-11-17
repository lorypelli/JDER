package com.jder.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jder.domain.model.DiagramEditorState
import com.jder.strings.CommonStrings
import com.jder.strings.MenuStrings
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.fileName
import com.jder.ui.utils.isCompactHeight
import com.jder.ui.utils.isCompactWidth
import com.jder.ui.utils.isMacOS
import com.jder.ui.utils.shortcutLabel
import com.jder.ui.utils.showsToolbarMenus
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramToolbar(
    state: DiagramEditorState<*>,
    themeState: ThemeState,
    onNewDiagram: () -> Unit,
    onOpenDiagram: () -> Unit,
    onSaveDiagram: () -> Unit,
    onSaveAsDiagram: () -> Unit,
    onToggleAutoSave: (() -> Unit)?,
    onExportPNG: () -> Unit,
    onToggleAutoExport: (() -> Unit)?,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDeleteSelected: () -> Unit,
    tools: List<DiagramTool>,
    modifier: Modifier = Modifier,
) {
    val isCompactWidth = isCompactWidth()
    val title = buildString {
        if (!isCompactWidth) append("JDER - ")
        append(state.currentFile?.let(::fileName) ?: state.diagramName)
        if (state.isModified) append(" *")
    }
    val hasSelection = state.hasSelection
    val zoomControls: @Composable () -> Unit = {
        ToolbarZoomControls(
            zoomPercentage = "${(state.zoom * 100).roundToInt()}%",
            onZoomIn = state::zoomIn,
            onZoomOut = state::zoomOut,
            onResetZoom = state::resetView,
        )
    }
    Column(modifier = modifier) {
        if (!isCompactHeight()) {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    if (showsToolbarMenus && !isCompactWidth) {
                        ToolbarFileMenu(
                            onNewDiagram = onNewDiagram,
                            onOpenDiagram = onOpenDiagram,
                            onSaveDiagram = onSaveDiagram,
                            onSaveAsDiagram = onSaveAsDiagram,
                            autoSave = onToggleAutoSave?.let { autoSaveToggle(state, it) },
                            onExportPNG = onExportPNG,
                            autoExport = onToggleAutoExport?.let { autoExportToggle(state, it) },
                            hasSelection = hasSelection,
                            onDeleteSelected = onDeleteSelected,
                        )
                        ToolbarViewMenuBox(
                            onZoomIn = state::zoomIn,
                            onZoomOut = state::zoomOut,
                            onResetZoom = state::resetView,
                        )
                        ToolbarExportMenu(
                            onExportPNG = onExportPNG,
                            autoExport = onToggleAutoExport?.let { autoExportToggle(state, it) },
                        )
                    }
                    ToolbarPaletteTheme(
                        themeState = themeState,
                        showsLanguageName = !isCompactWidth,
                    )
                },
            )
        }
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolbarToggleButton(
                        icon = Icons.Default.NearMe,
                        label = "${MenuStrings.SELECT_AND_MOVE} (Esc)",
                        checked = state.isSelectTool,
                        onClick = state::resetToolMode,
                    )
                    ToolbarVerticalDivider()
                    ToolbarIconButton(
                        icon = Icons.AutoMirrored.Default.Undo,
                        label = "${MenuStrings.UNDO} (${shortcutLabel("Z")})",
                        onClick = onUndo,
                        enabled = state.canUndo(),
                    )
                    ToolbarIconButton(
                        icon = Icons.AutoMirrored.Default.Redo,
                        label = "${MenuStrings.REDO} (${shortcutLabel("Y")})",
                        onClick = onRedo,
                        enabled = state.canRedo(),
                    )
                    ToolbarVerticalDivider()
                    tools.forEach { ToolbarTool(it) }
                    ToolbarVerticalDivider()
                    ToolbarIconButton(
                        icon = Icons.Default.Save,
                        label = "${CommonStrings.SAVE} (${shortcutLabel("S")})",
                        onClick = onSaveDiagram,
                    )
                    ToolbarIconButton(
                        icon = Icons.Default.Delete,
                        label =
                            "${MenuStrings.DELETE_SELECTED} (${if (isMacOS) "⌫" else MenuStrings.DELETE_KEY})",
                        onClick = onDeleteSelected,
                        enabled = hasSelection,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    if (isCompactWidth) {
                        ToolbarVerticalDivider()
                        zoomControls()
                    }
                }
                if (!isCompactWidth) zoomControls()
            }
        }
    }
}
