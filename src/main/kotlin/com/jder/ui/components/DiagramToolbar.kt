package com.jder.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.DiagramState
import com.jder.domain.model.ToolMode
import com.jder.ui.theme.ThemeState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramToolbar(
    state: DiagramState,
    themeState: ThemeState,
    onNewDiagram: () -> Unit,
    onOpenDiagram: () -> Unit,
    onSaveDiagram: () -> Unit,
    onSaveAsDiagram: () -> Unit,
    onExportPNG: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier
) {
    val title by remember {
        derivedStateOf {
            buildString {
                append("JDER - ")
                append(state.currentFile?.substringAfterLast("\\") ?: state.diagram.name)
                if (state.isModified) append(" *")
            }
        }
    }
    val zoomPercentage by remember { derivedStateOf { "${(state.zoom * 100).toInt()}%" } }
    val hasSelection = state.selectedEntityId != null || state.selectedRelationshipId != null || state.selectedNoteId != null
    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(text = title) },
            actions = {
                ToolbarFileMenu(
                    onNewDiagram = onNewDiagram,
                    onOpenDiagram = onOpenDiagram,
                    onSaveDiagram = onSaveDiagram,
                    onSaveAsDiagram = onSaveAsDiagram,
                    hasSelection = hasSelection,
                    onDeleteSelected = {
                        state.selectedEntityId?.let { state.deleteEntity(it); onShowSnackbar("Entità eliminata") }
                        state.selectedRelationshipId?.let { state.deleteRelationship(it); onShowSnackbar("Relazione eliminata") }
                        state.selectedNoteId?.let { state.deleteNote(it); onShowSnackbar("Nota eliminata") }
                    }
                )
                ToolbarViewMenuBox(onZoomIn = onZoomIn, onZoomOut = onZoomOut, onResetZoom = onResetZoom)
                ToolbarExportMenu(onExportPNG = onExportPNG)
                ToolbarPaletteTheme(themeState = themeState)
            }
        )
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconToggleButton(
                    checked = state.toolMode == ToolMode.SELECT,
                    onCheckedChange = { state.toolMode = ToolMode.SELECT }
                ) { Icon(Icons.Default.NearMe, "Seleziona e Sposta") }
                ToolbarVerticalDivider()
                IconButton(onClick = onUndo, enabled = state.canUndo()) { Icon(Icons.Default.Undo, "Annulla (Ctrl+Z)") }
                IconButton(onClick = onRedo, enabled = state.canRedo()) { Icon(Icons.Default.Redo, "Ripristina (Ctrl+Y)") }
                ToolbarVerticalDivider()
                IconToggleButton(
                    checked = state.toolMode == ToolMode.ENTITY,
                    onCheckedChange = { state.toolMode = ToolMode.ENTITY }
                ) { Icon(CustomIcons.Rectangle, "Crea Entità") }
                IconToggleButton(
                    checked = state.toolMode == ToolMode.RELATIONSHIP,
                    onCheckedChange = { state.toolMode = ToolMode.RELATIONSHIP }
                ) { Icon(CustomIcons.Diamond, "Crea Relazione") }
                IconToggleButton(
                    checked = state.toolMode == ToolMode.NOTE,
                    onCheckedChange = { state.toolMode = ToolMode.NOTE }
                ) { Icon(CustomIcons.StickyNote, "Crea Nota") }
                ToolbarVerticalDivider()
                IconButton(onClick = onSaveDiagram, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(Icons.Default.Save, "Salva")
                }
                IconButton(
                    onClick = {
                        state.selectedEntityId?.let { state.deleteEntity(it); onShowSnackbar("Entità eliminata") }
                        state.selectedRelationshipId?.let { state.deleteRelationship(it); onShowSnackbar("Relazione eliminata") }
                        state.selectedNoteId?.let { state.deleteNote(it); onShowSnackbar("Nota eliminata") }
                    },
                    enabled = hasSelection,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Elimina Elemento Selezionato",
                        tint = if (hasSelection) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                Spacer(Modifier.weight(1f))
                ToolbarZoomControls(
                    zoomPercentage = zoomPercentage,
                    onZoomIn = onZoomIn,
                    onZoomOut = onZoomOut,
                    onResetZoom = onResetZoom
                )
            }
        }
    }
}
