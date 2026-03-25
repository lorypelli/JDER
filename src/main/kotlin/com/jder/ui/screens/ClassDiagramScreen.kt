package com.jder.ui.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.jder.data.ClassDiagramRepository
import com.jder.data.ImageExporter
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassDiagramToolMode
import com.jder.ui.components.ClassDiagramCanvas
import com.jder.ui.components.ClassDiagramContextMenu
import com.jder.ui.components.ClassDiagramContextMenuType
import com.jder.ui.components.ClassDiagramPropertiesPanel
import com.jder.ui.components.ClassDiagramToolbar
import com.jder.ui.components.ConfirmNewDiagramDialog
import com.jder.ui.components.ConfirmOpenDiagramDialog
import com.jder.ui.components.DiagramSnackbarHost
import com.jder.ui.components.FileManagerDialog
import com.jder.ui.components.FileManagerMode
import com.jder.ui.dialogs.ClassEntityDialog
import com.jder.ui.dialogs.ClassMemberDialog
import com.jder.ui.dialogs.ClassRelationDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.SelectClassRelationTypeDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderClassDiagramToBitmap
import java.io.File
@Composable
fun ClassDiagramScreen(
    state: ClassDiagramState,
    repository: ClassDiagramRepository,
    themeState: ThemeState
) {
    var showClassDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showAddAttributeDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var showSelectRelationTypeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showNewDiagramConfirmDialog by remember { mutableStateOf(false) }
    var showOpenDiagramConfirmDialog by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }
    var contextMenuType by remember { mutableStateOf(ClassDiagramContextMenuType.CLASS) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var pendingRelationSource by remember { mutableStateOf<String?>(null) }
    var pendingRelationTarget by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }
    fun saveToCurrentFile() {
        state.currentFile?.let { path ->
            val file = File(path)
            repository.saveDiagram(state.diagram.copy(name = file.nameWithoutExtension), file).fold(
                onSuccess = {
                    state.updateDiagramName(file.nameWithoutExtension)
                    state.markAsSaved(file.absolutePath)
                    snackbarMessage = "Diagramma salvato: ${file.name}"
                },
                onFailure = { snackbarMessage = "Errore nel salvataggio: ${it.message}" }
            )
        } ?: run { showSaveAsDialog = true }
    }
    fun openDiagram() {
        if (state.isModified) showOpenDiagramConfirmDialog = true
        else showOpenDialog = true
    }
    fun newDiagram() {
        if (state.isModified) showNewDiagramConfirmDialog = true
        else { state.newDiagram(); snackbarMessage = "Nuovo diagramma creato" }
    }
    Scaffold(
        snackbarHost = { DiagramSnackbarHost(snackbarHostState) },
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusTarget()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.isCtrlPressed && event.key == Key.Z -> {
                            if (state.canUndo()) { state.undo(); snackbarMessage = "Azione annullata" }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.Y -> {
                            if (state.canRedo()) { state.redo(); snackbarMessage = "Azione ripristinata" }
                            true
                        }
                        event.isCtrlPressed && event.key == Key.N -> { newDiagram(); true }
                        event.isCtrlPressed && event.key == Key.O -> { openDiagram(); true }
                        event.isCtrlPressed && event.key == Key.S -> { saveToCurrentFile(); true }
                        event.key == Key.Delete || event.key == Key.Backspace -> {
                            state.selectedClassId?.let { state.deleteClass(it); snackbarMessage = "Classe eliminata" }
                            state.selectedRelationId?.let { state.deleteRelation(it); snackbarMessage = "Relazione eliminata" }
                            state.selectedNoteId?.let { state.deleteNote(it); snackbarMessage = "Nota eliminata" }
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
                        event.isCtrlPressed && event.key == Key.Zero -> { state.resetView(); true }
                        event.key == Key.Escape -> {
                            state.clearSelection()
                            state.toolMode = ClassDiagramToolMode.SELECT
                            state.pendingRelationSourceId = null
                            true
                        }
                        else -> false
                    }
                } else false
            },
        topBar = {
            ClassDiagramToolbar(
                state = state,
                themeState = themeState,
                onNewDiagram = ::newDiagram,
                onOpenDiagram = ::openDiagram,
                onSaveDiagram = ::saveToCurrentFile,
                onSaveAsDiagram = { showSaveAsDialog = true },
                onExportPNG = { showExportDialog = true },
                onZoomIn = { state.zoom = (state.zoom * 1.2f).coerceAtMost(3f) },
                onZoomOut = { state.zoom = (state.zoom / 1.2f).coerceAtLeast(0.25f) },
                onResetZoom = { state.resetView() },
                onUndo = { if (state.canUndo()) { state.undo(); snackbarMessage = "Azione annullata" } },
                onRedo = { if (state.canRedo()) { state.redo(); snackbarMessage = "Azione ripristinata" } },
                onShowSnackbar = { snackbarMessage = it }
            )
        }
    ) { values ->
        Row(modifier = Modifier.fillMaxSize().padding(values)) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ClassDiagramCanvas(
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
                    ClassDiagramContextMenu(
                        position = contextMenuPosition,
                        type = contextMenuType,
                        onDismiss = { showContextMenu = false },
                        onEdit = {
                            showContextMenu = false
                            when (contextMenuType) {
                                ClassDiagramContextMenuType.CLASS -> showClassDialog = true
                                ClassDiagramContextMenuType.RELATION -> showRelationDialog = true
                                ClassDiagramContextMenuType.NOTE -> showNoteDialog = true
                            }
                        },
                        onDelete = {
                            showContextMenu = false
                            when (contextMenuType) {
                                ClassDiagramContextMenuType.CLASS -> state.selectedClassId?.let { state.deleteClass(it); snackbarMessage = "Classe eliminata" }
                                ClassDiagramContextMenuType.RELATION -> state.selectedRelationId?.let { state.deleteRelation(it); snackbarMessage = "Relazione eliminata" }
                                ClassDiagramContextMenuType.NOTE -> state.selectedNoteId?.let { state.deleteNote(it); snackbarMessage = "Nota eliminata" }
                            }
                        }
                    )
                }
            }
            if (state.selectedClassId != null || state.selectedRelationId != null || state.selectedNoteId != null) {
                Surface(modifier = Modifier.width(300.dp).fillMaxHeight(), tonalElevation = 2.dp) {
                    ClassDiagramPropertiesPanel(
                        state = state,
                        onEditClass = { showClassDialog = true },
                        onEditRelation = { showRelationDialog = true },
                        onEditNote = { showNoteDialog = true },
                        onAddAttribute = { showAddAttributeDialog = true },
                        onAddMethod = { showAddMethodDialog = true },
                        onDeleteMember = { memberId ->
                            state.selectedClassId?.let { state.deleteMemberFromClass(it, memberId); snackbarMessage = "Membro eliminato" }
                        },
                        onClose = { state.clearSelection() }
                    )
                }
            }
        }
    }
    if (showClassDialog) {
        state.diagram.classes.find { it.id == state.selectedClassId }?.let { entity ->
            ClassEntityDialog(
                name = entity.name,
                type = entity.type,
                isAbstract = entity.isAbstract,
                documentation = entity.documentation,
                onDismiss = { showClassDialog = false },
                onConfirm = { newName, newType, newAbstract, newDoc ->
                    state.updateClass(entity.id) { it.copy(name = newName, type = newType, isAbstract = newAbstract, documentation = newDoc) }
                    showClassDialog = false
                    snackbarMessage = "Classe modificata"
                }
            )
        }
    }
    if (showRelationDialog) {
        state.diagram.relations.find { it.id == state.selectedRelationId }?.let { relation ->
            ClassRelationDialog(
                relation = relation,
                onDismiss = { showRelationDialog = false },
                onConfirm = { newType, srcMulti, tgtMulti, lbl ->
                    state.updateRelation(relation.id) { it.copy(type = newType, sourceMultiplicity = srcMulti, targetMultiplicity = tgtMulti, label = lbl) }
                    showRelationDialog = false
                    snackbarMessage = "Relazione modificata"
                }
            )
        }
    }
    if (showNoteDialog) {
        state.diagram.notes.find { it.id == state.selectedNoteId }?.let { note ->
            NotePropertiesDialog(
                noteText = note.text,
                onDismiss = { showNoteDialog = false },
                onConfirm = { newText ->
                    state.updateNote(note.id) { it.copy(text = newText) }
                    showNoteDialog = false
                    snackbarMessage = "Nota modificata"
                }
            )
        }
    }
    if (showAddAttributeDialog) {
        state.selectedClassId?.let { classId ->
            ClassMemberDialog(
                isMethod = false,
                onDismiss = { showAddAttributeDialog = false },
                onConfirm = { member ->
                    state.addMemberToClass(classId, member, isMethod = false)
                    showAddAttributeDialog = false
                    snackbarMessage = "Attributo aggiunto"
                }
            )
        }
    }
    if (showAddMethodDialog) {
        state.selectedClassId?.let { classId ->
            ClassMemberDialog(
                isMethod = true,
                onDismiss = { showAddMethodDialog = false },
                onConfirm = { member ->
                    state.addMemberToClass(classId, member, isMethod = true)
                    showAddMethodDialog = false
                    snackbarMessage = "Metodo aggiunto"
                }
            )
        }
    }
    if (showSelectRelationTypeDialog) {
        SelectClassRelationTypeDialog(
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
        ConfirmNewDiagramDialog(
            onDismiss = { showNewDiagramConfirmDialog = false },
            onConfirm = {
                showNewDiagramConfirmDialog = false
                state.newDiagram()
                snackbarMessage = "Nuovo diagramma creato"
            }
        )
    }
    if (showOpenDiagramConfirmDialog) {
        ConfirmOpenDiagramDialog(
            onDismiss = { showOpenDiagramConfirmDialog = false },
            onConfirm = { showOpenDiagramConfirmDialog = false; showOpenDialog = true }
        )
    }
    if (showOpenDialog) {
        FileManagerDialog(
            mode = FileManagerMode.OPEN,
            initialDirectory = File(System.getProperty("user.home")),
            fileExtension = ".json",
            title = "Apri Diagramma",
            onDismiss = { showOpenDialog = false },
            onFileSelected = { file ->
                showOpenDialog = false
                repository.loadDiagram(file).fold(
                    onSuccess = { state.loadDiagram(it, file.absolutePath); snackbarMessage = "Diagramma caricato: ${file.name}" },
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
            title = "Salva Diagramma",
            defaultFileName = state.diagram.name,
            onDismiss = { showSaveAsDialog = false },
            onFileSelected = { file ->
                showSaveAsDialog = false
                val finalFile = if (file.extension != "json") File(file.parentFile, "${file.nameWithoutExtension}.json") else file
                repository.saveDiagram(state.diagram.copy(name = finalFile.nameWithoutExtension), finalFile).fold(
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
                    ImageExporter().exportToPNG(renderClassDiagramToBitmap(state.diagram), pngFile).fold(
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
