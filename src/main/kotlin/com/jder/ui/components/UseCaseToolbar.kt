package com.jder.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
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
import com.jder.domain.model.UseCaseState
import com.jder.domain.model.UseCaseToolMode
import com.jder.ui.theme.ThemeState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseCaseToolbar(
    state: UseCaseState,
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
    val hasSelection = state.selectedActorId != null || state.selectedUseCaseId != null ||
        state.selectedRelationId != null || state.selectedNoteId != null || state.selectedSystemBoundaryId != null
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
                        state.selectedActorId?.let { state.deleteActor(it); onShowSnackbar("Attore eliminato") }
                        state.selectedUseCaseId?.let { state.deleteUseCase(it); onShowSnackbar("Caso d'uso eliminato") }
                        state.selectedRelationId?.let { state.deleteRelation(it); onShowSnackbar("Relazione eliminata") }
                        state.selectedNoteId?.let { state.deleteNote(it); onShowSnackbar("Nota eliminata") }
                        state.selectedSystemBoundaryId?.let { state.deleteSystemBoundary(it); onShowSnackbar("Sistema eliminato") }
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
                    checked = state.toolMode == UseCaseToolMode.SELECT,
                    onCheckedChange = { state.toolMode = UseCaseToolMode.SELECT }
                ) { Icon(Icons.Default.NearMe, "Seleziona e Sposta") }
                ToolbarVerticalDivider()
                IconButton(onClick = onUndo, enabled = state.canUndo()) { Icon(Icons.Default.Undo, "Annulla (Ctrl+Z)") }
                IconButton(onClick = onRedo, enabled = state.canRedo()) { Icon(Icons.Default.Redo, "Ripristina (Ctrl+Y)") }
                ToolbarVerticalDivider()
                IconToggleButton(
                    checked = state.toolMode == UseCaseToolMode.ACTOR,
                    onCheckedChange = { state.toolMode = UseCaseToolMode.ACTOR }
                ) { Icon(Icons.Default.Person, "Crea Attore") }
                IconToggleButton(
                    checked = state.toolMode == UseCaseToolMode.USE_CASE,
                    onCheckedChange = { state.toolMode = UseCaseToolMode.USE_CASE }
                ) { Icon(CustomIcons.Ellipse, "Crea Caso d'Uso") }
                IconToggleButton(
                    checked = state.toolMode == UseCaseToolMode.RELATION,
                    onCheckedChange = {
                        state.toolMode = UseCaseToolMode.RELATION
                        state.pendingRelationSourceId = null
                    }
                ) { Icon(Icons.Default.ArrowRightAlt, "Crea Relazione") }
                IconToggleButton(
                    checked = state.toolMode == UseCaseToolMode.NOTE,
                    onCheckedChange = { state.toolMode = UseCaseToolMode.NOTE }
                ) { Icon(CustomIcons.StickyNote, "Crea Nota") }
                IconToggleButton(
                    checked = state.toolMode == UseCaseToolMode.SYSTEM,
                    onCheckedChange = { state.toolMode = UseCaseToolMode.SYSTEM }
                ) { Icon(Icons.Default.CropSquare, "Crea Sistema") }
                ToolbarVerticalDivider()
                IconButton(onClick = onSaveDiagram, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(Icons.Default.Save, "Salva")
                }
                IconButton(
                    onClick = {
                        state.selectedActorId?.let { state.deleteActor(it); onShowSnackbar("Attore eliminato") }
                        state.selectedUseCaseId?.let { state.deleteUseCase(it); onShowSnackbar("Caso d'uso eliminato") }
                        state.selectedRelationId?.let { state.deleteRelation(it); onShowSnackbar("Relazione eliminata") }
                        state.selectedNoteId?.let { state.deleteNote(it); onShowSnackbar("Nota eliminata") }
                        state.selectedSystemBoundaryId?.let { state.deleteSystemBoundary(it); onShowSnackbar("Sistema eliminato") }
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
