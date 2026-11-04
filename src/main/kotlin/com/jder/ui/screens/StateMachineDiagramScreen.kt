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
import com.jder.ui.dialogs.MachineStateDialog
import com.jder.ui.dialogs.MachineTransitionDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.StateMachineSimulationDialog
import com.jder.ui.dialogs.TransitionTableDialog
import com.jder.ui.strings.UiStrings
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
            StateMachineContextMenuType.STATE -> "Azioni Stato"
            StateMachineContextMenuType.TRANSITION -> "Azioni Transizione"
            StateMachineContextMenuType.NOTE -> "Azioni Nota"
        }
    fun actions(type: StateMachineContextMenuType): List<ContextMenuItem> = buildList {
        when (type) {
            StateMachineContextMenuType.STATE -> {
                add(ContextMenuItem("Modifica Proprietà", Icons.Default.Edit) { openEditor(type) })
                val stateId = state.selectedStateId
                add(
                    ContextMenuItem(
                        "Imposta come Stato Iniziale",
                        CustomIcons.InitialState,
                        enabled = stateId != null && !state.isInitial(stateId),
                    ) {
                        stateId?.let {
                            state.setInitialState(it)
                            Toasts.success("Stato iniziale impostato")
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
                                if (it.isFinal) "Stato rimosso dagli stati finali"
                                else "Stato finale impostato"
                            )
                        }
                    }
                )
            }
            StateMachineContextMenuType.TRANSITION -> {
                add(ContextMenuItem("Modifica Proprietà", Icons.Default.Edit) { openEditor(type) })
                val transition = state.transition(state.selectedTransitionId)
                add(
                    ContextMenuItem(
                        "Inverti Direzione",
                        Icons.Default.SwapHoriz,
                        enabled = transition != null && !transition.isLoop,
                    ) {
                        transition?.let { state.reverseTransition(it.id) }
                    }
                )
                add(
                    ContextMenuItem(
                        "Ripristina Forma",
                        Icons.Default.AutoFixHigh,
                        enabled = transition?.hasCustomShape == true,
                    ) {
                        transition?.let { state.resetTransitionShape(it.id) }
                    }
                )
            }
            StateMachineContextMenuType.NOTE ->
                add(ContextMenuItem("Modifica Testo", Icons.Default.Edit) { openEditor(type) })
        }
        add(
            ContextMenuItem("Elimina", Icons.Default.Delete, isDestructive = true) {
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
            "Scegli Stato dalla barra degli strumenti e clicca sulla lavagna.\n" +
                "Per creare una transizione trascina dal bordo di uno stato fino allo stato di " +
                "arrivo (anche lo stesso).\n" +
                if (kind.hasTransitionOutputs) {
                    "Nella macchina di Mealy l'uscita si scrive sulle transizioni, come " +
                        "ingresso/uscita."
                } else {
                    "Nella macchina di Moore l'uscita si scrive negli stati, sotto il nome."
                },
        tools =
            listOf(
                DiagramTool.Mode(
                    CustomIcons.MachineState,
                    "Crea Stato",
                    "Stato",
                    state.toolMode == StateMachineToolMode.STATE,
                ) {
                    state.toolMode = StateMachineToolMode.STATE
                },
                DiagramTool.Mode(
                    CustomIcons.Transition,
                    "Crea Transizione (clicca lo stato di partenza e poi quello di arrivo, " +
                        "oppure trascina dal bordo di uno stato)",
                    "Transizione",
                    state.toolMode == StateMachineToolMode.TRANSITION,
                ) {
                    state.toolMode = StateMachineToolMode.TRANSITION
                    state.pendingTransitionSourceId = null
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    "Crea Nota",
                    UiStrings.NOTE,
                    state.toolMode == StateMachineToolMode.NOTE,
                ) {
                    state.toolMode = StateMachineToolMode.NOTE
                },
                DiagramTool.Divider,
                DiagramTool.Action(
                    Icons.Default.TableChart,
                    "Tabella di transizione",
                    "Tabella di Transizione...",
                ) {
                    showTransitionTable = true
                },
                DiagramTool.Action(
                    Icons.Default.PlayArrow,
                    "Simula una sequenza di ingressi",
                    "Simula una Sequenza...",
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
            MachineStateDialog(
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
                    Toasts.success("Stato modificato")
                },
            )
        }
    }
    editedTransitionId?.let { transitionId ->
        state.transition(transitionId)?.let { transition ->
            MachineTransitionDialog(
                title = "Proprietà Transizione",
                kind = kind,
                sourceName = stateName(transition.sourceId),
                targetName = stateName(transition.targetId),
                initialInput = transition.input,
                initialOutput = transition.output,
                usedSymbols = usedSymbols(transition.sourceId, transition.id),
                onDismiss = { editedTransitionId = null },
                onConfirm = { input, output ->
                    state.updateTransition(transition.id) {
                        it.copy(input = input, output = output)
                    }
                    editedTransitionId = null
                    Toasts.success("Transizione modificata")
                },
            )
        }
    }
    pendingTransition?.let { (sourceId, targetId) ->
        MachineTransitionDialog(
            title = "Nuova Transizione",
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
                Toasts.success("Transizione creata")
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
                        Toasts.success("Nota modificata")
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
