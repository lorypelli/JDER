package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jder.data.StateMachineDiagramRepository
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.StateMachineToolMode
import com.jder.strings.CommonStrings
import com.jder.strings.StateMachineStrings
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.PendingRelation
import com.jder.ui.components.StateMachineContextMenuType
import com.jder.ui.components.StateMachineDiagramCanvas
import com.jder.ui.components.StateMachinePropertiesPanel
import com.jder.ui.components.Toasts
import com.jder.ui.components.contextMenuType
import com.jder.ui.components.finalStateActionLabel
import com.jder.ui.dialogs.MachineStatePropertiesDialog
import com.jder.ui.dialogs.MachineTransitionPropertiesDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.StateMachineSimulationDialog
import com.jder.ui.dialogs.TransitionTableDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderStateMachineDiagramToBitmap

@Composable
fun StateMachineDiagramScreen(
    state: StateMachineDiagramState,
    repository: StateMachineDiagramRepository,
    themeState: ThemeState,
) {
    val kind = state.kind
    var showStateDialog by remember { mutableStateOf(false) }
    var editedTransitionId by remember { mutableStateOf<String?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showTransitionTable by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }
    var pendingTransition by remember { mutableStateOf<PendingRelation?>(null) }
    var contextMenu by remember {
        mutableStateOf<ContextMenuRequest<StateMachineContextMenuType>?>(null)
    }
    fun stateName(stateId: String): String = state.machineState(stateId)?.name.orEmpty()
    fun usedSymbols(sourceId: String, excludedId: String?): Set<String> =
        state.diagram.transitions
            .filter { it.sourceId == sourceId && it.id != excludedId }
            .flatMapTo(mutableSetOf()) { it.inputSymbols }
    fun editTransition(transitionId: String) {
        state.selectTransition(transitionId)
        editedTransitionId = transitionId
    }
    fun openEditor(type: StateMachineContextMenuType) {
        when (type) {
            StateMachineContextMenuType.STATE -> showStateDialog = true
            StateMachineContextMenuType.TRANSITION ->
                state.selectedTransitionId?.let(::editTransition)
            StateMachineContextMenuType.NOTE -> showNoteDialog = true
        }
    }
    fun selectedType(): StateMachineContextMenuType? =
        when {
            state.selectedStateId != null -> StateMachineContextMenuType.STATE
            state.selectedTransitionId != null -> StateMachineContextMenuType.TRANSITION
            state.selectedNoteId != null -> StateMachineContextMenuType.NOTE
            else -> null
        }
    fun actionsTitle(type: StateMachineContextMenuType): String =
        when (type) {
            StateMachineContextMenuType.STATE -> StateMachineStrings.STATE_ACTIONS
            StateMachineContextMenuType.TRANSITION -> StateMachineStrings.TRANSITION_ACTIONS
            StateMachineContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
        }
    fun actions(type: StateMachineContextMenuType): List<ContextMenuItem> = buildList {
        when (type) {
            StateMachineContextMenuType.STATE -> {
                add(
                    ContextMenuItem(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit) {
                        openEditor(type)
                    }
                )
                val stateId = state.selectedStateId
                add(
                    ContextMenuItem(
                        StateMachineStrings.SET_INITIAL_STATE,
                        CustomIcons.InitialState,
                        enabled = stateId != null && !state.isInitial(stateId),
                    ) {
                        stateId?.let {
                            state.setInitialState(it)
                            Toasts.success(StateMachineStrings.INITIAL_STATE_SET)
                        }
                    }
                )
                val machineState = state.machineState(stateId)
                add(
                    ContextMenuItem(
                        finalStateActionLabel(machineState?.isFinal == true),
                        CustomIcons.FinalState,
                        enabled = machineState != null,
                    ) {
                        machineState?.let {
                            state.setFinalState(it.id, !it.isFinal)
                            Toasts.success(
                                if (it.isFinal) StateMachineStrings.STATE_REMOVED_FROM_FINAL
                                else StateMachineStrings.FINAL_STATE_SET
                            )
                        }
                    }
                )
            }
            StateMachineContextMenuType.TRANSITION -> {
                add(
                    ContextMenuItem(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit) {
                        openEditor(type)
                    }
                )
                val transition = state.transition(state.selectedTransitionId)
                add(
                    ContextMenuItem(
                        CommonStrings.REVERSE_DIRECTION,
                        Icons.Default.SwapHoriz,
                        enabled = transition != null && !transition.isLoop,
                    ) {
                        transition?.let { state.reverseTransition(it.id) }
                    }
                )
                add(
                    ContextMenuItem(
                        StateMachineStrings.RESET_SHAPE,
                        Icons.Default.AutoFixHigh,
                        enabled = transition?.hasCustomShape == true,
                    ) {
                        transition?.let { state.resetTransitionShape(it.id) }
                    }
                )
            }
            StateMachineContextMenuType.NOTE ->
                add(
                    ContextMenuItem(CommonStrings.EDIT_TEXT, Icons.Default.Edit) {
                        openEditor(type)
                    }
                )
        }
        add(
            ContextMenuItem(CommonStrings.DELETE, Icons.Default.Delete, isDestructive = true) {
                state.deleteSelection()?.let(Toasts::success)
            }
        )
    }
    DiagramEditorScaffold(
        state = state,
        repository = repository,
        themeState = themeState,
        renderImage = { renderStateMachineDiagramToBitmap(state.diagram, kind, it) },
        emptyHint =
            "${StateMachineStrings.STATE_MACHINE_EMPTY_HINT}\n" +
                if (kind.hasTransitionOutputs) StateMachineStrings.MEALY_EMPTY_HINT
                else StateMachineStrings.MOORE_EMPTY_HINT,
        tools =
            listOf(
                DiagramTool.Mode(
                    CustomIcons.MachineState,
                    StateMachineStrings.CREATE_STATE,
                    StateMachineStrings.STATE,
                    state.toolMode == StateMachineToolMode.STATE,
                ) {
                    state.toolMode = StateMachineToolMode.STATE
                },
                DiagramTool.Mode(
                    CustomIcons.Transition,
                    StateMachineStrings.CREATE_TRANSITION,
                    StateMachineStrings.TRANSITION,
                    state.toolMode == StateMachineToolMode.TRANSITION,
                ) {
                    state.toolMode = StateMachineToolMode.TRANSITION
                    state.pendingTransitionSourceId = null
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == StateMachineToolMode.NOTE,
                ) {
                    state.toolMode = StateMachineToolMode.NOTE
                },
                DiagramTool.Divider,
                DiagramTool.Action(
                    Icons.Default.TableChart,
                    StateMachineStrings.TRANSITION_TABLE,
                    StateMachineStrings.TRANSITION_TABLE_ELLIPSIS,
                ) {
                    showTransitionTable = true
                },
                DiagramTool.Action(
                    Icons.Default.PlayArrow,
                    StateMachineStrings.SIMULATE_SEQUENCE,
                    StateMachineStrings.SIMULATE_SEQUENCE_ELLIPSIS,
                ) {
                    showSimulation = true
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            StateMachinePropertiesPanel(
                state = state,
                onEditState = { showStateDialog = true },
                onEditTransition = ::editTransition,
                onEditNote = { showNoteDialog = true },
                onClose = { state.clearSelection() },
            )
        },
    ) {
        StateMachineDiagramCanvas(
            state = state,
            onContextMenuRequest = { position, type ->
                contextMenu = ContextMenuRequest(position, type)
            },
            onTransitionCreateRequest = { sourceId, targetId ->
                pendingTransition = PendingRelation(sourceId, targetId)
            },
            onEditRequest = { openEditor(it.contextMenuType) },
        )
        contextMenu?.let {
            DiagramContextMenu(
                position = it.position,
                title = actionsTitle(it.type),
                items = actions(it.type),
                onDismiss = { contextMenu = null },
            )
        }
    }
    if (showStateDialog) {
        state.machineState(state.selectedStateId)?.let { machineState ->
            MachineStatePropertiesDialog(
                machineState = machineState,
                kind = kind,
                isInitial = state.isInitial(machineState.id),
                otherNames =
                    state.diagram.states
                        .filter { it.id != machineState.id }
                        .mapTo(mutableSetOf()) { it.name.trim() },
                onDismiss = { showStateDialog = false },
                onConfirm = { name, output, isInitial, isFinal ->
                    state.updateState(machineState.id, name, output, isInitial, isFinal)
                    showStateDialog = false
                    Toasts.success(StateMachineStrings.STATE_UPDATED)
                },
            )
        }
    }
    editedTransitionId?.let { transitionId ->
        state.transition(transitionId)?.let { (id, sourceId, targetId, input, output) ->
            MachineTransitionPropertiesDialog(
                title = StateMachineStrings.TRANSITION_PROPERTIES,
                kind = kind,
                sourceName = stateName(sourceId),
                targetName = stateName(targetId),
                initialInput = input,
                initialOutput = output,
                usedSymbols = usedSymbols(sourceId, id),
                onDismiss = { editedTransitionId = null },
                onConfirm = { newInput, newOutput ->
                    state.updateTransition(id) { it.copy(input = newInput, output = newOutput) }
                    editedTransitionId = null
                    Toasts.success(StateMachineStrings.TRANSITION_UPDATED)
                },
            )
        }
    }
    pendingTransition?.let { (sourceId, targetId) ->
        MachineTransitionPropertiesDialog(
            title = StateMachineStrings.NEW_TRANSITION,
            kind = kind,
            sourceName = stateName(sourceId),
            targetName = stateName(targetId),
            initialInput = state.suggestedInput(sourceId),
            initialOutput = state.suggestedOutput(),
            usedSymbols = usedSymbols(sourceId, null),
            onDismiss = { pendingTransition = null },
            onConfirm = { input, output ->
                state.addTransition(sourceId, targetId, input, output)
                pendingTransition = null
                Toasts.success(StateMachineStrings.TRANSITION_CREATED)
            },
        )
    }
    if (showNoteDialog) {
        state.diagram.notes
            .find { it.id == state.selectedNoteId }
            ?.let { (id, text) ->
                NotePropertiesDialog(
                    noteId = id,
                    noteText = text,
                    onDismiss = { showNoteDialog = false },
                    onConfirm = { newText ->
                        state.updateNote(id) { it.copy(text = newText) }
                        showNoteDialog = false
                        Toasts.success(CommonStrings.NOTE_UPDATED)
                    },
                )
            }
    }
    if (showTransitionTable) {
        TransitionTableDialog(
            diagram = state.diagram,
            kind = kind,
            onDismiss = { showTransitionTable = false },
        )
    }
    if (showSimulation) {
        StateMachineSimulationDialog(
            diagram = state.diagram,
            kind = kind,
            sequence = state.simulationInput,
            onSequenceChange = { state.simulationInput = it },
            onDismiss = { showSimulation = false },
        )
    }
}
