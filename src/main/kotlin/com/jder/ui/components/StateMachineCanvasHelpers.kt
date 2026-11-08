package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.MachineState
import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.StateMachineToolMode
import com.jder.ui.strings.UiStrings

private const val TRANSITION_HIT_DISTANCE = 6f
private const val LABEL_HIT_MARGIN = 2f

sealed interface StateMachineElement {
    val id: String

    data class StateRef(override val id: String) : StateMachineElement

    data class RingRef(override val id: String) : StateMachineElement

    data class TransitionRef(override val id: String) : StateMachineElement

    data class NoteRef(override val id: String) : StateMachineElement
}

internal val StateMachineElement.contextMenuType: StateMachineContextMenuType
    get() =
        when (this) {
            is StateMachineElement.StateRef,
            is StateMachineElement.RingRef -> StateMachineContextMenuType.STATE
            is StateMachineElement.TransitionRef -> StateMachineContextMenuType.TRANSITION
            is StateMachineElement.NoteRef -> StateMachineContextMenuType.NOTE
        }

internal fun machineStateAt(diagram: StateMachineDiagram, point: Offset): MachineState? =
    diagram.states.lastOrNull {
        (it.center - point).getDistance() <= MACHINE_STATE_RADIUS + MACHINE_RING_HIT
    }

internal fun hitTestStateMachine(
    diagram: StateMachineDiagram,
    layout: StateMachineLayout,
    point: Offset,
): StateMachineElement? {
    diagram.notes
        .lastOrNull { it.contains(point) }
        ?.let {
            return StateMachineElement.NoteRef(it.id)
        }
    layout.edges
        .flatMap { it.labels }
        .lastOrNull { it.rect.inflate(LABEL_HIT_MARGIN).contains(point) }
        ?.let {
            return StateMachineElement.TransitionRef(it.transitionId)
        }
    machineStateAt(diagram, point)?.let {
        val distance = (it.center - point).getDistance()
        return if (distance >= MACHINE_STATE_RADIUS - MACHINE_RING_HIT) {
            StateMachineElement.RingRef(it.id)
        } else {
            StateMachineElement.StateRef(it.id)
        }
    }
    layout.edges
        .lastOrNull {
            distanceToPolyline(point, it.points) <= TRANSITION_HIT_DISTANCE ||
                (it.tip - point).getDistance() <= TRANSITION_ARROW_SIZE
        }
        ?.let {
            return StateMachineElement.TransitionRef(it.transitionIds.first())
        }
    return null
}

internal fun selectStateMachineElement(
    state: StateMachineDiagramState,
    element: StateMachineElement,
) {
    when (element) {
        is StateMachineElement.StateRef,
        is StateMachineElement.RingRef -> state.selectState(element.id)
        is StateMachineElement.TransitionRef -> state.selectTransition(element.id)
        is StateMachineElement.NoteRef -> state.selectNote(element.id)
    }
}

internal fun handleStateMachineTap(
    state: StateMachineDiagramState,
    layout: StateMachineLayout,
    point: Offset,
    onTransitionCreateRequest: (sourceId: String, targetId: String) -> Unit,
) {
    when (state.toolMode) {
        StateMachineToolMode.SELECT ->
            hitTestStateMachine(state.diagram, layout, point)?.let {
                selectStateMachineElement(state, it)
            } ?: state.clearSelection()
        StateMachineToolMode.STATE -> {
            state.addState(point.x, point.y)
            state.toolMode = StateMachineToolMode.SELECT
        }
        StateMachineToolMode.TRANSITION -> {
            val clickedId = machineStateAt(state.diagram, point)?.id
            val pendingId = state.pendingTransitionSourceId
            when {
                clickedId == null -> state.pendingTransitionSourceId = null
                pendingId == null -> state.pendingTransitionSourceId = clickedId
                else -> {
                    state.pendingTransitionSourceId = null
                    onTransitionCreateRequest(pendingId, clickedId)
                }
            }
        }
        StateMachineToolMode.NOTE -> {
            state.addNote(point.x - 120f, point.y - 90f, UiStrings.NEW_NOTE)
            state.toolMode = StateMachineToolMode.SELECT
        }
    }
}

internal fun handleStateMachineDrag(
    state: StateMachineDiagramState,
    element: StateMachineElement,
    delta: Offset,
) {
    when (element) {
        is StateMachineElement.StateRef,
        is StateMachineElement.RingRef ->
            state.updateStateWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is StateMachineElement.NoteRef ->
            state.updateNoteWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is StateMachineElement.TransitionRef -> {}
    }
}

internal fun reshapeTransitionArrow(
    state: StateMachineDiagramState,
    edge: TransitionEdge,
    cursor: Offset,
) {
    val transitionIds = edge.transitionIds.toSet()
    if (edge.isLoop) {
        val angle = loopAngleTowards(edge, cursor)
        state.updateTransitionsWithoutSave(transitionIds) {
            it.copy(loopAngle = angle, bend = null)
        }
    } else {
        val bend = bendTowards(edge, cursor)
        state.updateTransitionsWithoutSave(transitionIds) { it.copy(bend = bend, loopAngle = null) }
    }
}
