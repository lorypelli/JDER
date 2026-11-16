package com.jder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jder.data.DiagramFileRepository
import com.jder.domain.model.DiagramEditorState
import com.jder.strings.FileStrings
import com.jder.ui.components.ConfirmNewDiagramDialog
import com.jder.ui.components.ConfirmOpenDiagramDialog
import com.jder.ui.components.DiagramMenuCommands
import com.jder.ui.components.DiagramPalette
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.DiagramToolbar
import com.jder.ui.components.LocalDiagramMenuHost
import com.jder.ui.components.Toasts
import com.jder.ui.theme.ThemeState

private val zoomInKeys = setOf(Key.Plus, Key.Equals, Key.NumPadAdd)
private val zoomOutKeys = setOf(Key.Minus, Key.NumPadSubtract)
private val resetZoomKeys = setOf(Key.Zero, Key.NumPad0)

@Composable
fun <T> DiagramEditorScaffold(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    themeState: ThemeState,
    renderImage: (DiagramPalette) -> ImageBitmap,
    emptyHint: String,
    tools: List<DiagramTool>,
    selectionMenu: () -> DiagramSelectionMenu?,
    properties: @Composable () -> Unit,
    canvas: @Composable BoxScope.() -> Unit,
) {
    DiagramFileHost(state, repository, themeState, renderImage) { files ->
        DiagramEditorContent(
            state = state,
            themeState = themeState,
            files = files,
            emptyHint = emptyHint,
            tools = tools,
            selectionMenu = selectionMenu,
            properties = properties,
            canvas = canvas,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DiagramEditorContent(
    state: DiagramEditorState<*>,
    themeState: ThemeState,
    files: DiagramFileActions,
    emptyHint: String,
    tools: List<DiagramTool>,
    selectionMenu: () -> DiagramSelectionMenu?,
    properties: @Composable () -> Unit,
    canvas: @Composable BoxScope.() -> Unit,
) {
    var showNewDiagramConfirmDialog by remember { mutableStateOf(false) }
    var showOpenDiagramConfirmDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val currentFiles by rememberUpdatedState(files)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    fun requestOpenDiagram() {
        if (!currentFiles.flushAutoSave() || state.isModified) showOpenDiagramConfirmDialog = true
        else currentFiles.open()
    }
    fun requestNewDiagram() {
        if (!currentFiles.flushAutoSave() || state.isModified) showNewDiagramConfirmDialog = true
        else {
            state.newDiagram()
            Toasts.success(FileStrings.NEW_DIAGRAM_CREATED)
        }
    }
    fun requestExport() {
        if (state.isEmpty) Toasts.warning(FileStrings.NOTHING_TO_EXPORT) else currentFiles.export()
    }
    fun saveDiagram() {
        currentFiles.save()
    }
    fun saveDiagramAs() {
        currentFiles.saveAs()
    }
    fun undo() {
        if (state.canUndo()) {
            state.undo()
            Toasts.success(FileStrings.ACTION_UNDONE)
        }
    }
    fun redo() {
        if (state.canRedo()) {
            state.redo()
            Toasts.success(FileStrings.ACTION_REDONE)
        }
    }
    fun deleteSelected() {
        state.deleteSelection()?.let(Toasts::success)
    }
    val menuHost = LocalDiagramMenuHost.current
    val currentTools by rememberUpdatedState(tools)
    val currentSelectionMenu by rememberUpdatedState(selectionMenu)
    if (menuHost != null) {
        DisposableEffect(menuHost) {
            val commands =
                DiagramMenuCommands(
                    state = state,
                    onNewDiagram = { requestNewDiagram() },
                    onOpenDiagram = { requestOpenDiagram() },
                    onSaveDiagram = { saveDiagram() },
                    onSaveAsDiagram = { saveDiagramAs() },
                    onToggleAutoSave = files.toggleAutoSave,
                    onExportPNG = { requestExport() },
                    onToggleAutoExport = files.toggleAutoExport,
                    onUndo = { undo() },
                    onRedo = { redo() },
                    onDeleteSelected = { deleteSelected() },
                    flushAutoSave = { currentFiles.flushAutoSave() },
                    tools = { currentTools },
                    selectionMenu = { currentSelectionMenu() },
                )
            menuHost.commands = commands
            onDispose { if (menuHost.commands === commands) menuHost.commands = null }
        }
    }
    Scaffold(
        modifier =
            Modifier.focusRequester(focusRequester).focusTarget().onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val isCommand = it.isCtrlPressed || it.isMetaPressed
                when {
                    isCommand && it.key == Key.Z -> {
                        if (it.isShiftPressed) redo() else undo()
                        true
                    }
                    isCommand && it.key == Key.Y -> {
                        redo()
                        true
                    }
                    isCommand && it.key == Key.N -> {
                        requestNewDiagram()
                        true
                    }
                    isCommand && it.key == Key.O -> {
                        requestOpenDiagram()
                        true
                    }
                    isCommand && it.key == Key.S -> {
                        if (it.isShiftPressed) saveDiagramAs() else saveDiagram()
                        true
                    }
                    isCommand && it.key == Key.E -> {
                        requestExport()
                        true
                    }
                    isCommand && it.key in zoomInKeys -> {
                        state.zoomIn()
                        true
                    }
                    isCommand && it.key in zoomOutKeys -> {
                        state.zoomOut()
                        true
                    }
                    isCommand && it.key in resetZoomKeys -> {
                        state.resetView()
                        true
                    }
                    it.key == Key.Delete || it.key == Key.Backspace -> {
                        deleteSelected()
                        true
                    }
                    it.key == Key.Escape -> {
                        state.clearSelection()
                        state.resetToolMode()
                        true
                    }
                    else -> false
                }
            },
        topBar = {
            DiagramToolbar(
                state = state,
                themeState = themeState,
                onNewDiagram = ::requestNewDiagram,
                onOpenDiagram = ::requestOpenDiagram,
                onSaveDiagram = ::saveDiagram,
                onSaveAsDiagram = ::saveDiagramAs,
                onToggleAutoSave = files.toggleAutoSave,
                onExportPNG = ::requestExport,
                onToggleAutoExport = files.toggleAutoExport,
                onUndo = ::undo,
                onRedo = ::redo,
                onDeleteSelected = ::deleteSelected,
                tools = tools,
            )
        },
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(it)) {
            Box(
                modifier =
                    Modifier.weight(1f).fillMaxHeight().onPointerEvent(
                        PointerEventType.Press,
                        PointerEventPass.Initial,
                    ) {
                        focusRequester.requestFocus()
                    },
            ) {
                canvas()
                if (state.isEmpty) {
                    Text(
                        text = emptyHint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    )
                }
            }
            if (state.hasSelection) {
                Surface(modifier = Modifier.width(300.dp).fillMaxHeight(), tonalElevation = 2.dp) {
                    properties()
                }
            }
        }
    }
    if (showNewDiagramConfirmDialog) {
        ConfirmNewDiagramDialog(
            onDismiss = { showNewDiagramConfirmDialog = false },
            onConfirm = {
                showNewDiagramConfirmDialog = false
                state.newDiagram()
                Toasts.success(FileStrings.NEW_DIAGRAM_CREATED)
            },
        )
    }
    if (showOpenDiagramConfirmDialog) {
        ConfirmOpenDiagramDialog(
            onDismiss = { showOpenDiagramConfirmDialog = false },
            onConfirm = {
                showOpenDiagramConfirmDialog = false
                currentFiles.open()
            },
        )
    }
}
