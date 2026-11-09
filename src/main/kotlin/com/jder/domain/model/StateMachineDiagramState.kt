package com.jder.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jder.strings.CommonStrings
import com.jder.strings.StateMachineStrings
import java.util.UUID

private const val BINARY_OUTPUT = "0"
private val binarySymbols = listOf("0", "1")

class StateMachineDiagramState(val kind: StateMachineKind) :
    DiagramEditorState<StateMachineDiagram>({ StateMachineDiagram(name = "") }) {
    var selectedStateId by mutableStateOf<String?>(null)
        private set

    var selectedTransitionId by mutableStateOf<String?>(null)
        private set

    var selectedNoteId by mutableStateOf<String?>(null)
        private set

    var toolMode by mutableStateOf(StateMachineToolMode.SELECT)
    var pendingTransitionSourceId by mutableStateOf<String?>(null)
    var simulationInput by mutableStateOf("")

    override val diagramName: String
        get() = diagram.name.ifEmpty { kind.defaultName }

    override val isEmpty: Boolean
        get() = diagram.states.isEmpty() && diagram.transitions.isEmpty() && diagram.notes.isEmpty()

    override val hasSelection: Boolean
        get() = selectedStateId != null || selectedTransitionId != null || selectedNoteId != null

    override val isSelectTool: Boolean
        get() = toolMode == StateMachineToolMode.SELECT

    override fun withName(diagram: StateMachineDiagram, name: String) = diagram.copy(name = name)

    override fun pruneSelection() {
        if (selectedStateId != null && machineState(selectedStateId) == null) selectedStateId = null
        if (selectedTransitionId != null && transition(selectedTransitionId) == null)
            selectedTransitionId = null
        if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId })
            selectedNoteId = null
        if (pendingTransitionSourceId != null && machineState(pendingTransitionSourceId) == null)
            pendingTransitionSourceId = null
    }

    override fun resetToolMode() {
        toolMode = StateMachineToolMode.SELECT
        pendingTransitionSourceId = null
    }

    override fun deleteSelection(): String? {
        selectedStateId?.let {
            deleteState(it)
            return StateMachineStrings.STATE_DELETED
        }
        selectedTransitionId?.let {
            deleteTransition(it)
            return StateMachineStrings.TRANSITION_DELETED
        }
        selectedNoteId?.let {
            deleteNote(it)
            return CommonStrings.NOTE_DELETED
        }
        return null
    }

    fun machineState(id: String?): MachineState? = diagram.states.find { it.id == id }

    fun transition(id: String?): MachineTransition? = diagram.transitions.find { it.id == id }

    fun isInitial(stateId: String): Boolean = diagram.initialStateId == stateId

    fun addState(x: Float, y: Float) {
        val state =
            MachineState(
                id = UUID.randomUUID().toString(),
                name = nextStateName(),
                x = x,
                y = y,
                output = if (kind.hasStateOutputs) BINARY_OUTPUT else "",
            )
        edit { it.copy(states = it.states + state, initialStateId = it.initialStateId ?: state.id) }
        selectState(state.id)
    }

    fun addTransition(sourceId: String, targetId: String, input: String, output: String) {
        if (machineState(sourceId) == null || machineState(targetId) == null) return
        val arrow = diagram.transitions.find { it.sourceId == sourceId && it.targetId == targetId }
        val transition =
            MachineTransition(
                id = UUID.randomUUID().toString(),
                sourceId = sourceId,
                targetId = targetId,
                input = input.trim(),
                output = if (kind.hasTransitionOutputs) output.trim() else "",
                bend = arrow?.bend,
                loopAngle = arrow?.loopAngle,
            )
        edit { it.copy(transitions = it.transitions + transition) }
    }

    fun addNote(x: Float, y: Float, text: String) {
        val note = Note(id = UUID.randomUUID().toString(), text = text, x = x, y = y)
        edit { it.copy(notes = it.notes + note) }
        selectNote(note.id)
    }

    fun updateState(
        stateId: String,
        name: String,
        output: String,
        isInitial: Boolean,
        isFinal: Boolean,
    ) {
        edit { d ->
            d.copy(
                states =
                    d.states.map {
                        if (it.id == stateId) {
                            it.copy(name = name.trim(), output = output.trim(), isFinal = isFinal)
                        } else {
                            it
                        }
                    },
                initialStateId =
                    when {
                        isInitial -> stateId
                        d.initialStateId == stateId -> null
                        else -> d.initialStateId
                    },
            )
        }
    }

    fun updateStateWithoutSave(stateId: String, update: (MachineState) -> MachineState) {
        editWithoutSave { d ->
            d.copy(states = d.states.map { if (it.id == stateId) update(it) else it })
        }
    }

    fun updateTransition(transitionId: String, update: (MachineTransition) -> MachineTransition) {
        edit { d ->
            d.copy(
                transitions = d.transitions.map { if (it.id == transitionId) update(it) else it }
            )
        }
    }

    fun updateTransitionsWithoutSave(
        transitionIds: Set<String>,
        update: (MachineTransition) -> MachineTransition,
    ) {
        editWithoutSave { d ->
            d.copy(
                transitions = d.transitions.map { if (it.id in transitionIds) update(it) else it }
            )
        }
    }

    fun reverseTransition(transitionId: String) {
        val transition = transition(transitionId) ?: return
        if (transition.isLoop) return
        val reverseArrow =
            diagram.transitions.find {
                it.sourceId == transition.targetId && it.targetId == transition.sourceId
            }
        updateTransition(transitionId) {
            it.copy(
                sourceId = it.targetId,
                targetId = it.sourceId,
                bend = if (reverseArrow != null) reverseArrow.bend else it.bend?.unaryMinus(),
            )
        }
    }

    fun resetTransitionShape(transitionId: String) {
        val transition = transition(transitionId) ?: return
        if (!transition.hasCustomShape) return
        val arrowIds = diagram.arrowOf(transition).mapTo(mutableSetOf()) { it.id }
        edit { d ->
            d.copy(
                transitions =
                    d.transitions.map {
                        if (it.id in arrowIds) it.copy(bend = null, loopAngle = null) else it
                    }
            )
        }
    }

    fun setInitialState(stateId: String) {
        if (machineState(stateId) == null || isInitial(stateId)) return
        edit { it.copy(initialStateId = stateId) }
    }

    fun setFinalState(stateId: String, isFinal: Boolean) {
        val current = machineState(stateId) ?: return
        if (current.isFinal == isFinal) return
        edit { d ->
            d.copy(
                states = d.states.map { if (it.id == stateId) it.copy(isFinal = isFinal) else it }
            )
        }
    }

    fun updateNote(noteId: String, update: (Note) -> Note) {
        edit { d -> d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it }) }
    }

    fun updateNoteWithoutSave(noteId: String, update: (Note) -> Note) {
        editWithoutSave { d ->
            d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it })
        }
    }

    fun deleteState(stateId: String) {
        edit { d ->
            d.copy(
                states = d.states.filter { it.id != stateId },
                transitions =
                    d.transitions.filter { it.sourceId != stateId && it.targetId != stateId },
                initialStateId = d.initialStateId?.takeIf { it != stateId },
            )
        }
        pruneSelection()
    }

    fun deleteTransition(transitionId: String) {
        edit { d -> d.copy(transitions = d.transitions.filter { it.id != transitionId }) }
        if (selectedTransitionId == transitionId) selectedTransitionId = null
    }

    fun deleteNote(noteId: String) {
        edit { d -> d.copy(notes = d.notes.filter { it.id != noteId }) }
        if (selectedNoteId == noteId) selectedNoteId = null
    }

    fun selectState(stateId: String?) {
        clearSelection()
        selectedStateId = stateId
    }

    fun selectTransition(transitionId: String?) {
        clearSelection()
        selectedTransitionId = transitionId
    }

    fun selectNote(noteId: String?) {
        clearSelection()
        selectedNoteId = noteId
    }

    override fun clearSelection() {
        selectedStateId = null
        selectedTransitionId = null
        selectedNoteId = null
    }

    fun suggestedInput(sourceId: String): String {
        val alphabet = StateMachineAnalyzer.inputAlphabet(diagram)
        val candidates = if (binarySymbols.containsAll(alphabet)) binarySymbols else alphabet
        val used =
            diagram.transitions.filter { it.sourceId == sourceId }.flatMap { it.inputSymbols }
        return candidates.firstOrNull { it !in used } ?: ""
    }

    fun suggestedOutput(): String {
        if (!kind.hasTransitionOutputs) return ""
        val outputs = diagram.transitions.map { it.output.trim() }.filter { it.isNotEmpty() }
        return if (binarySymbols.containsAll(outputs)) BINARY_OUTPUT else ""
    }

    private fun nextStateName(): String {
        val used = diagram.states.map { it.name }.toSet()
        return generateSequence(0) { it + 1 }.map { "S$it" }.first { it !in used }
    }
}
