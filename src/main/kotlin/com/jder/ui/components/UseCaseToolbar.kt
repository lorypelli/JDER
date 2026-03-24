package com.jder.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.UseCaseState
import com.jder.domain.model.UseCaseToolMode
import com.jder.ui.theme.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                val coroutineScope = rememberCoroutineScope()
                var showFileMenu by remember { mutableStateOf(false) }
                var showExportMenu by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showFileMenu = true }) { Text("File") }
                    DropdownMenu(expanded = showFileMenu, onDismissRequest = { showFileMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Nuovo Diagramma") },
                            onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onNewDiagram() } },
                            leadingIcon = { Icon(Icons.Default.Add, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Apri Diagramma...") },
                            onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onOpenDiagram() } },
                            leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Salva") },
                            onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onSaveDiagram() } },
                            leadingIcon = { Icon(Icons.Default.Save, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Salva con nome...") },
                            onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onSaveAsDiagram() } },
                            leadingIcon = { Icon(Icons.Default.Save, null) }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Elimina Elemento Selezionato") },
                            onClick = {
                                coroutineScope.launch {
                                    delay(150)
                                    showFileMenu = false
                                    state.selectedActorId?.let { state.deleteActor(it); onShowSnackbar("Attore eliminato") }
                                    state.selectedUseCaseId?.let { state.deleteUseCase(it); onShowSnackbar("Caso d'uso eliminato") }
                                    state.selectedRelationId?.let { state.deleteRelation(it); onShowSnackbar("Relazione eliminata") }
                                    state.selectedNoteId?.let { state.deleteNote(it); onShowSnackbar("Nota eliminata") }
                                    state.selectedSystemBoundaryId?.let { state.deleteSystemBoundary(it); onShowSnackbar("Sistema eliminato") }
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            enabled = hasSelection
                        )
                    }
                }
                ToolbarViewMenuBox(onZoomIn = onZoomIn, onZoomOut = onZoomOut, onResetZoom = onResetZoom)
                Box {
                    TextButton(onClick = { showExportMenu = true }) { Text("Esporta") }
                    DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Esporta come PNG...") },
                            onClick = { coroutineScope.launch { delay(150); showExportMenu = false; onExportPNG() } },
                            leadingIcon = { Icon(Icons.Default.Image, "Esporta diagramma come immagine PNG") }
                        )
                    }
                }
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
                Divider(modifier = Modifier.width(1.dp).height(40.dp))
                IconButton(onClick = onUndo, enabled = state.canUndo()) { Icon(Icons.Default.Undo, "Annulla (Ctrl+Z)") }
                IconButton(onClick = onRedo, enabled = state.canRedo()) { Icon(Icons.Default.Redo, "Ripristina (Ctrl+Y)") }
                Divider(modifier = Modifier.width(1.dp).height(40.dp))
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
                Divider(modifier = Modifier.width(1.dp).height(40.dp))
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
