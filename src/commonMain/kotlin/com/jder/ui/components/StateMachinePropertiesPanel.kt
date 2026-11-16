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
import com.jder.strings.CommonStrings
import com.jder.strings.LogicStrings
import com.jder.strings.StateMachineStrings

fun finalStateActionLabel(isFinal: Boolean): String =
    if (isFinal) StateMachineStrings.REMOVE_FROM_FINAL_STATES
    else StateMachineStrings.SET_FINAL_STATE

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
    PropertyCard("${StateMachineStrings.STATE}: ${machineState.name}") {
        if (state.kind.hasStateOutputs) {
            PropertyDetail(
                "${LogicStrings.OUTPUT}: ${machineState.output.ifBlank { "—" }}",
                isEmphasized = true,
            )
        }
        PropertyDetail("${StateMachineStrings.INITIAL}: ${CommonStrings.yesNo(isInitial)}")
        PropertyDetail("${StateMachineStrings.FINAL}: ${CommonStrings.yesNo(machineState.isFinal)}")
        PropertyDetail(StateMachineStrings.transitionCounts(outgoing.size, incoming))
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(
        StateMachineStrings.SET_INITIAL_STATE,
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
        PropertyListTitle(StateMachineStrings.OUTGOING_TRANSITIONS)
        outgoing.forEach { transition ->
            PropertyItemCard(
                title = transition.label(state.kind),
                subtitle = StateMachineStrings.towards(stateName(state, transition.targetId)),
                deleteDescription = StateMachineStrings.DELETE_TRANSITION,
                onDelete = { state.deleteTransition(transition.id) },
                editDescription = StateMachineStrings.EDIT_TRANSITION,
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
    PropertyCard(StateMachineStrings.TRANSITION) {
        PropertyDetail("${CommonStrings.FROM}: ${stateName(state, transition.sourceId)}")
        PropertyDetail("${CommonStrings.TO}: ${stateName(state, transition.targetId)}")
        PropertyDetail(
            "${LogicStrings.INPUT}: ${transition.input.ifBlank { "—" }}",
            isEmphasized = true,
        )
        if (state.kind.hasTransitionOutputs) {
            PropertyDetail(
                "${LogicStrings.OUTPUT}: ${transition.output.ifBlank { "—" }}",
                isEmphasized = true,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit) { onEdit(transition.id) }
    PropertyOutlinedButton(
        CommonStrings.REVERSE_DIRECTION,
        Icons.Default.SwapHoriz,
        { state.reverseTransition(transition.id) },
        enabled = !transition.isLoop,
    )
    PropertyOutlinedButton(
        StateMachineStrings.RESET_SHAPE,
        Icons.Default.AutoFixHigh,
        { state.resetTransitionShape(transition.id) },
        enabled = transition.hasCustomShape,
    )
}

private fun stateName(state: StateMachineDiagramState, stateId: String): String =
    state.machineState(stateId)?.name ?: StateMachineStrings.UNKNOWN_STATE
