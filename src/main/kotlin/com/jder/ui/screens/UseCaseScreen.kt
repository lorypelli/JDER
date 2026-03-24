package com.jder.ui.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.jder.data.ImageExporter
import com.jder.data.UseCaseRepository
import com.jder.ui.utils.renderUseCaseDiagramToBitmap
import com.jder.domain.model.UseCaseState
import com.jder.domain.model.UseCaseToolMode
import com.jder.ui.components.FileManagerDialog
import com.jder.ui.components.FileManagerMode
import com.jder.ui.components.UseCaseDiagramCanvas
import com.jder.ui.components.UseCaseContextMenu
import com.jder.ui.components.UseCaseContextMenuType
import com.jder.ui.components.UseCasePropertiesPanel
import com.jder.ui.components.UseCaseToolbar
import com.jder.ui.dialogs.ActorPropertiesDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.RelationEditDialog
import com.jder.ui.dialogs.SelectRelationTypeDialog
import com.jder.ui.dialogs.SystemBoundaryDialog
import com.jder.ui.dialogs.UseCasePropertiesDialog
import com.jder.ui.theme.ThemeState
import java.io.File
@Composable
fun UseCaseScreen(
    state: UseCaseState,
    repository: UseCaseRepository,
    themeState: ThemeState
) {
    var showActorDialog by remember { mutableStateOf(false) }
    var showUseCaseDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showSystemBoundaryDialog by remember { mutableStateOf(false) }
    var showSelectRelationTypeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var pendingRelationSource by remember { mutableStateOf<String?>(null) }
    var pendingRelationTarget by remember { mutableStateOf<String?>(null) }
    var showNewDiagramConfirmDialog by remember { mutableStateOf(false) }
    var showOpenDiagramConfirmDialog by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }
    var contextMenuType by remember { mutableStateOf(UseCaseContextMenuType.ACTOR) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusTarget()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.isCtrlPressed && event.key == Key.Z -> {
                            if (state.canUndo()) {
                                state.undo()
                                snackbarMessage = "Azione annullata"
                            }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.Y -> {
                            if (state.canRedo()) {
                                state.redo()
                                snackbarMessage = "Azione ripristinata"
                            }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.N -> {
                            if (state.isModified) {
                                showNewDiagramConfirmDialog = true
                            } else {
                                state.newDiagram()
                                snackbarMessage = "Nuovo diagramma creato"
                            }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.O -> {
                            if (state.isModified) {
                                showOpenDiagramConfirmDialog = true
                            } else {
                                showOpenDialog = true
                            }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.S -> {
                            state.currentFile?.let { text ->
                                val file = File(text)
                                val updated = state.diagram.copy(name = file.nameWithoutExtension)
                                repository.saveDiagram(updated, file).fold(
                                    onSuccess = {
                                        state.updateDiagramName(file.nameWithoutExtension)
                                        state.markAsSaved(file.absolutePath)
                                        snackbarMessage = "Diagramma salvato: ${file.name}"
                                    },
                                    onFailure = { snackbarMessage = "Errore nel salvataggio: ${it.message}" }
                                )
                            } ?: run { showSaveAsDialog = true }
                            true
                        }
                        event.key == Key.Delete || event.key == Key.Backspace -> {
                            state.selectedActorId?.let {
                                state.deleteActor(it)
                                snackbarMessage = "Attore eliminato"
                            }
                            state.selectedUseCaseId?.let {
                                state.deleteUseCase(it)
                                snackbarMessage = "Caso d'uso eliminato"
                            }
                            state.selectedRelationId?.let {
                                state.deleteRelation(it)
                                snackbarMessage = "Relazione eliminata"
                            }
                            state.selectedNoteId?.let {
                                state.deleteNote(it)
                                snackbarMessage = "Nota eliminata"
                            }
                            state.selectedSystemBoundaryId?.let {
                                state.deleteSystemBoundary(it)
                                snackbarMessage = "Sistema eliminato"
                            }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.Plus -> {
                            state.zoom = (state.zoom * 1.2f).coerceAtMost(3f)
                            true
                        }
                        event.isCtrlPressed && event.key == Key.Minus -> {
                            state.zoom = (state.zoom / 1.2f).coerceAtLeast(0.25f)
                            true
                        }
                        event.isCtrlPressed && event.key == Key.Zero -> {
                            state.resetView()
                            true
                        }
                        event.key == Key.Escape -> {
                            state.clearSelection()
                            state.toolMode = UseCaseToolMode.SELECT
                            state.pendingRelationSourceId = null
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        topBar = {
            UseCaseToolbar(
                state = state,
                themeState = themeState,
                onNewDiagram = {
                    if (state.isModified) showNewDiagramConfirmDialog = true
                    else {
                        state.newDiagram()
                        snackbarMessage = "Nuovo diagramma creato"
                    }
                },
                onOpenDiagram = {
                    if (state.isModified) showOpenDiagramConfirmDialog = true
                    else showOpenDialog = true
                },
                onSaveDiagram = {
                    state.currentFile?.let { text ->
                        val file = File(text)
                        val updated = state.diagram.copy(name = file.nameWithoutExtension)
                        repository.saveDiagram(updated, file).fold(
                            onSuccess = {
                                state.updateDiagramName(file.nameWithoutExtension)
                                state.markAsSaved(file.absolutePath)
                                snackbarMessage = "Diagramma salvato: ${file.name}"
                            },
                            onFailure = { snackbarMessage = "Errore nel salvataggio: ${it.message}" }
                        )
                    } ?: run { showSaveAsDialog = true }
                },
                onSaveAsDiagram = { showSaveAsDialog = true },
                onExportPNG = { showExportDialog = true },
                onZoomIn = { state.zoom = (state.zoom * 1.2f).coerceAtMost(3f) },
                onZoomOut = { state.zoom = (state.zoom / 1.2f).coerceAtLeast(0.25f) },
                onResetZoom = { state.resetView() },
                onUndo = {
                    if (state.canUndo()) {
                        state.undo()
                        snackbarMessage = "Azione annullata"
                    }
                },
                onRedo = {
                    if (state.canRedo()) {
                        state.redo()
                        snackbarMessage = "Azione ripristinata"
                    }
                },
                onShowSnackbar = { snackbarMessage = it },
                modifier = Modifier
            )
        }
    ) { values ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                UseCaseDiagramCanvas(
                    state = state,
                    onContextMenuRequest = { position, type ->
                        contextMenuPosition = position
                        contextMenuType = type
                        showContextMenu = true
                    },
                    onRelationCreateRequest = { sourceId, targetId ->
                        pendingRelationSource = sourceId
                        pendingRelationTarget = targetId
                        showSelectRelationTypeDialog = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
                if (showContextMenu) {
                    UseCaseContextMenu(
                        position = contextMenuPosition,
                        type = contextMenuType,
                        onDismiss = { showContextMenu = false },
                        onEdit = {
                            showContextMenu = false
                            when (contextMenuType) {
                                UseCaseContextMenuType.ACTOR -> showActorDialog = true
                                UseCaseContextMenuType.USE_CASE -> showUseCaseDialog = true
                                UseCaseContextMenuType.RELATION -> showRelationDialog = true
                                UseCaseContextMenuType.NOTE -> showNoteDialog = true
                                UseCaseContextMenuType.SYSTEM -> showSystemBoundaryDialog = true
                            }
                        },
                        onDelete = {
                            showContextMenu = false
                            when (contextMenuType) {
                                UseCaseContextMenuType.ACTOR -> {
                                    state.selectedActorId?.let {
                                        state.deleteActor(it)
                                        snackbarMessage = "Attore eliminato"
                                    }
                                }
                                UseCaseContextMenuType.USE_CASE -> {
                                    state.selectedUseCaseId?.let {
                                        state.deleteUseCase(it)
                                        snackbarMessage = "Caso d'uso eliminato"
                                    }
                                }
                                UseCaseContextMenuType.RELATION -> {
                                    state.selectedRelationId?.let {
                                        state.deleteRelation(it)
                                        snackbarMessage = "Relazione eliminata"
                                    }
                                }
                                UseCaseContextMenuType.NOTE -> {
                                    state.selectedNoteId?.let {
                                        state.deleteNote(it)
                                        snackbarMessage = "Nota eliminata"
                                    }
                                }
                                UseCaseContextMenuType.SYSTEM -> {
                                    state.selectedSystemBoundaryId?.let {
                                        state.deleteSystemBoundary(it)
                                        snackbarMessage = "Sistema eliminato"
                                    }
                                }
                            }
                        }
                    )
                }
            }
            if (state.selectedActorId != null || state.selectedUseCaseId != null || state.selectedRelationId != null || state.selectedNoteId != null || state.selectedSystemBoundaryId != null) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                    tonalElevation = 2.dp
                ) {
                    UseCasePropertiesPanel(
                        state = state,
                        onEditActor = { showActorDialog = true },
                        onEditUseCase = { showUseCaseDialog = true },
                        onEditRelation = { showRelationDialog = true },
                        onEditNote = { showNoteDialog = true },
                        onEditSystemBoundary = { showSystemBoundaryDialog = true },
                        onClose = { state.clearSelection() }
                    )
                }
            }
        }
    }
    if (showActorDialog) {
        val actor = state.diagram.actors.find { it.id == state.selectedActorId }
        actor?.let {
            ActorPropertiesDialog(
                actorName = it.name,
                onDismiss = { showActorDialog = false },
                onConfirm = { newName ->
                    state.updateActor(it.id) { a -> a.copy(name = newName) }
                    showActorDialog = false
                    snackbarMessage = "Attore modificato"
                }
            )
        }
    }
    if (showUseCaseDialog) {
        val useCase = state.diagram.useCases.find { it.id == state.selectedUseCaseId }
        useCase?.let {
            UseCasePropertiesDialog(
                useCaseName = it.name,
                useCaseDocumentation = it.documentation,
                onDismiss = { showUseCaseDialog = false },
                onConfirm = { newName, newDoc ->
                    state.updateUseCase(it.id) { uc -> uc.copy(name = newName, documentation = newDoc) }
                    showUseCaseDialog = false
                    snackbarMessage = "Caso d'uso modificato"
                }
            )
        }
    }
    if (showRelationDialog) {
        val relation = state.diagram.relations.find { it.id == state.selectedRelationId }
        relation?.let {
            RelationEditDialog(
                relation = it,
                onDismiss = { showRelationDialog = false },
                onConfirm = { newType ->
                    state.updateRelation(it.id) { r -> r.copy(type = newType) }
                    showRelationDialog = false
                    snackbarMessage = "Relazione modificata"
                }
            )
        }
    }
    if (showNoteDialog) {
        val note = state.diagram.notes.find { it.id == state.selectedNoteId }
        note?.let {
            NotePropertiesDialog(
                noteText = it.text,
                onDismiss = { showNoteDialog = false },
                onConfirm = { newText ->
                    state.updateNote(it.id) { n -> n.copy(text = newText) }
                    showNoteDialog = false
                    snackbarMessage = "Nota modificata"
                }
            )
        }
    }
    if (showSystemBoundaryDialog) {
        val boundary = state.diagram.systemBoundaries.find { it.id == state.selectedSystemBoundaryId }
        boundary?.let {
            SystemBoundaryDialog(
                boundaryName = it.name,
                boundaryWidth = it.width,
                boundaryHeight = it.height,
                onDismiss = { showSystemBoundaryDialog = false },
                onConfirm = { newName, newWidth, newHeight ->
                    state.updateSystemBoundary(it.id) { b -> b.copy(name = newName, width = newWidth, height = newHeight) }
                    showSystemBoundaryDialog = false
                    snackbarMessage = "Sistema modificato"
                }
            )
        }
    }
    if (showSelectRelationTypeDialog) {
        SelectRelationTypeDialog(
            onDismiss = {
                showSelectRelationTypeDialog = false
                pendingRelationSource = null
                pendingRelationTarget = null
            },
            onConfirm = { type ->
                val src = pendingRelationSource
                val tgt = pendingRelationTarget
                if (src != null && tgt != null) {
                    state.addRelation(src, tgt, type)
                    snackbarMessage = "Relazione creata"
                }
                showSelectRelationTypeDialog = false
                pendingRelationSource = null
                pendingRelationTarget = null
            }
        )
    }
    if (showNewDiagramConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showNewDiagramConfirmDialog = false },
            title = { Text("Conferma nuovo diagramma") },
            text = { Text("Ci sono modifiche non salvate. Vuoi creare un nuovo diagramma comunque?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNewDiagramConfirmDialog = false
                        state.newDiagram()
                        snackbarMessage = "Nuovo diagramma creato"
                    }
                ) { Text("Sì") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDiagramConfirmDialog = false }) { Text("No") }
            }
        )
    }
    if (showOpenDiagramConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showOpenDiagramConfirmDialog = false },
            title = { Text("Conferma apertura diagramma") },
            text = { Text("Ci sono modifiche non salvate. Vuoi aprire un altro diagramma comunque?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOpenDiagramConfirmDialog = false
                        showOpenDialog = true
                    }
                ) { Text("Sì") }
            },
            dismissButton = {
                TextButton(onClick = { showOpenDiagramConfirmDialog = false }) { Text("No") }
            }
        )
    }
    if (showOpenDialog) {
        FileManagerDialog(
            mode = FileManagerMode.OPEN,
            initialDirectory = File(System.getProperty("user.home")),
            fileExtension = ".json",
            title = "Apri Diagramma Casi d'Uso",
            onDismiss = { showOpenDialog = false },
            onFileSelected = { file ->
                showOpenDialog = false
                repository.loadDiagram(file).fold(
                    onSuccess = {
                        state.loadDiagram(it, file.absolutePath)
                        snackbarMessage = "Diagramma caricato: ${file.name}"
                    },
                    onFailure = { snackbarMessage = "Errore nel caricamento: ${it.message}" }
                )
            }
        )
    }
    if (showSaveAsDialog) {
        FileManagerDialog(
            mode = FileManagerMode.SAVE,
            initialDirectory = File(System.getProperty("user.home")),
            fileExtension = ".json",
            title = "Salva Diagramma Casi d'Uso",
            defaultFileName = state.diagram.name,
            onDismiss = { showSaveAsDialog = false },
            onFileSelected = { file ->
                showSaveAsDialog = false
                val finalFile = if (file.extension != "json") {
                    File(file.parentFile, "${file.nameWithoutExtension}.json")
                } else file
                val updated = state.diagram.copy(name = finalFile.nameWithoutExtension)
                repository.saveDiagram(updated, finalFile).fold(
                    onSuccess = {
                        state.updateDiagramName(finalFile.nameWithoutExtension)
                        state.markAsSaved(finalFile.absolutePath)
                        snackbarMessage = "Diagramma salvato: ${finalFile.name}"
                    },
                    onFailure = { snackbarMessage = "Errore nel salvataggio: ${it.message}" }
                )
            }
        )
    }
    if (showExportDialog) {
        FileManagerDialog(
            mode = FileManagerMode.SAVE,
            initialDirectory = File(System.getProperty("user.home")),
            fileExtension = ".png",
            title = "Esporta come PNG",
            defaultFileName = state.diagram.name,
            onDismiss = { showExportDialog = false },
            onFileSelected = { file ->
                showExportDialog = false
                val pngFile = if (file.extension != "png") File(file.parentFile, "${file.nameWithoutExtension}.png") else file
                try {
                    ImageExporter().exportToPNG(renderUseCaseDiagramToBitmap(state.diagram), pngFile).fold(
                        onSuccess = { snackbarMessage = "Immagine salvata: ${pngFile.name}" },
                        onFailure = { snackbarMessage = "Errore nell'esportazione: ${it.message}" }
                    )
                } catch (e: Exception) {
                    snackbarMessage = "Errore nell'esportazione: ${e.message}"
                }
            }
        )
    }
}
