package com.jder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.MachineState
import com.jder.domain.model.MachineTransition
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.label
import com.jder.ui.strings.UiStrings

fun finalStateActionLabel(isFinal: Boolean): String =
    if (isFinal) UiStrings.REMOVE_FROM_FINAL_STATES else UiStrings.SET_FINAL_STATE

@Composable
fun StateMachinePropertiesPanel(
    state: StateMachineDiagramState,
    onEditState: () -> Unit,
    onEditTransition: (transitionId: String) -> Unit,
    onEditNote: () -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.machineState(state.selectedStateId)?.let {
            MachineStateContent(it, state, onEditState, onEditTransition)
        }
        state.transition(state.selectedTransitionId)?.let {
            MachineTransitionContent(it, state, onEditTransition)
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
    }
}

@Composable
private fun MachineStateContent(
    machineState: MachineState,
    state: StateMachineDiagramState,
    onEdit: () -> Unit,
    onEditTransition: (String) -> Unit,
) {
    val isInitial = state.isInitial(machineState.id)
    val outgoing = state.diagram.transitions.filter { it.sourceId == machineState.id }
    val incoming = state.diagram.transitions.count { it.targetId == machineState.id }
    PropertyCard("${UiStrings.STATE}: ${machineState.name}") {
        if (state.kind.hasStateOutputs) {
            PropertyDetail(
                "${UiStrings.OUTPUT}: ${machineState.output.ifBlank { "—" }}",
                isEmphasized = true,
            )
        }
        PropertyDetail("${UiStrings.INITIAL}: ${UiStrings.yesNo(isInitial)}")
        PropertyDetail("${UiStrings.FINAL}: ${UiStrings.yesNo(machineState.isFinal)}")
        PropertyDetail(UiStrings.transitionCounts(outgoing.size, incoming))
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(
        UiStrings.SET_INITIAL_STATE,
        CustomIcons.InitialState,
        { state.setInitialState(machineState.id) },
        enabled = !isInitial,
    )
    PropertyOutlinedButton(
        finalStateActionLabel(machineState.isFinal),
        CustomIcons.FinalState,
        { state.setFinalState(machineState.id, !machineState.isFinal) },
    )
    if (outgoing.isNotEmpty()) {
        PropertyListTitle(UiStrings.OUTGOING_TRANSITIONS)
        outgoing.forEach { transition ->
            PropertyItemCard(
                title = transition.label(state.kind),
                subtitle = UiStrings.towards(stateName(state, transition.targetId)),
                deleteDescription = UiStrings.DELETE_TRANSITION,
                onDelete = { state.deleteTransition(transition.id) },
                editDescription = UiStrings.EDIT_TRANSITION,
                onEdit = { onEditTransition(transition.id) },
            )
        }
    }
}

@Composable
private fun MachineTransitionContent(
    transition: MachineTransition,
    state: StateMachineDiagramState,
    onEdit: (String) -> Unit,
) {
    PropertyCard(UiStrings.TRANSITION) {
        PropertyDetail("${UiStrings.FROM}: ${stateName(state, transition.sourceId)}")
        PropertyDetail("${UiStrings.TO}: ${stateName(state, transition.targetId)}")
        PropertyDetail(
            "${UiStrings.INPUT}: ${transition.input.ifBlank { "—" }}",
            isEmphasized = true,
        )
        if (state.kind.hasTransitionOutputs) {
            PropertyDetail(
                "${UiStrings.OUTPUT}: ${transition.output.ifBlank { "—" }}",
                isEmphasized = true,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit) { onEdit(transition.id) }
    PropertyOutlinedButton(
        UiStrings.REVERSE_DIRECTION,
        Icons.Default.SwapHoriz,
        { state.reverseTransition(transition.id) },
        enabled = !transition.isLoop,
    )
    PropertyOutlinedButton(
        UiStrings.RESET_SHAPE,
        Icons.Default.AutoFixHigh,
        { state.resetTransitionShape(transition.id) },
        enabled = transition.hasCustomShape,
    )
}

private fun stateName(state: StateMachineDiagramState, stateId: String): String =
    state.machineState(stateId)?.name ?: UiStrings.UNKNOWN_STATE
