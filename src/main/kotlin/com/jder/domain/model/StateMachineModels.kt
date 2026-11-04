package com.jder.domain.model

import kotlinx.serialization.Serializable

enum class StateMachineKind(val diagramType: DiagramType, val defaultName: String) {
    MEALY(DiagramType.MEALY, "Nuova Macchina di Mealy"),
    MOORE(DiagramType.MOORE, "Nuova Macchina di Moore");

    val hasStateOutputs: Boolean
        get() = this == MOORE

    val hasTransitionOutputs: Boolean
        get() = this == MEALY
}

@Serializable
data class MachineState(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val output: String = "",
    val isFinal: Boolean = false,
)

@Serializable
data class MachineTransition(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val input: String,
    val output: String = "",
    val bend: Float? = null,
    val loopAngle: Float? = null,
) {
    val inputSymbols: List<String>
        get() = parseInputSymbols(input)

    val isLoop: Boolean
        get() = sourceId == targetId

    val hasCustomShape: Boolean
        get() = bend != null || loopAngle != null

    fun sharesArrowWith(other: MachineTransition): Boolean =
        sourceId == other.sourceId &&
            targetId == other.targetId &&
            bend == other.bend &&
            loopAngle == other.loopAngle
}

@Serializable
data class StateMachineDiagram(
    val name: String,
    val initialStateId: String? = null,
    val states: List<MachineState> = emptyList(),
    val transitions: List<MachineTransition> = emptyList(),
    val notes: List<Note> = emptyList(),
) {
    fun arrowOf(transition: MachineTransition): List<MachineTransition> =
        transitions.filter { it.sharesArrowWith(transition) }
}

fun parseInputSymbols(input: String): List<String> =
    input.split(',').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

fun MachineTransition.label(kind: StateMachineKind): String {
    val symbols = inputSymbols.joinToString(",").ifEmpty { input.trim() }
    val output = output.trim()
    return if (kind.hasTransitionOutputs && output.isNotEmpty()) "$symbols/$output" else symbols
}

fun StateMachineDiagram.validateReferences() {
    val stateIds = states.mapTo(mutableSetOf()) { it.id }
    val errors = mutableListOf<String>()
    if (initialStateId != null && initialStateId !in stateIds) {
        errors += "Lo stato iniziale non esiste: $initialStateId"
    }
    transitions.forEach { (id, sourceId, targetId) ->
        if (sourceId !in stateIds) {
            errors += "La transizione ${id} parte da uno stato inesistente: " + sourceId
        }
        if (targetId !in stateIds) {
            errors += "La transizione ${id} arriva a uno stato inesistente: " + targetId
        }
    }
    if (errors.isNotEmpty()) throw DiagramValidationException(errors)
}
