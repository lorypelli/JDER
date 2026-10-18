package com.jder.domain.model

import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

const val MIN_GATE_INPUTS = 2
const val MAX_GATE_INPUTS = 8
const val LOGIC_GRID_SIZE = 10f

fun snapToLogicGrid(value: Float): Float = (value / LOGIC_GRID_SIZE).roundToInt() * LOGIC_GRID_SIZE

sealed interface LogicPin {
    data object Output : LogicPin

    data class Input(val index: Int) : LogicPin
}

data class LogicEndpoint(val componentId: String, val pin: LogicPin?)

@Serializable
enum class LogicComponentType(val display: String) {
    INPUT("Ingresso"),
    OUTPUT("Uscita"),
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    NAND("NAND"),
    NOR("NOR"),
    XOR("XOR"),
    XNOR("XNOR");

    val isGate: Boolean
        get() = this != INPUT && this != OUTPUT

    val hasOutput: Boolean
        get() = this != OUTPUT

    val isNegated: Boolean
        get() = this == NOT || this == NAND || this == NOR || this == XNOR

    val supportsMultipleInputs: Boolean
        get() = isGate && this != NOT

    companion object {
        val gates: List<LogicComponentType> = entries.filter { it.isGate }
    }
}

@Serializable
data class LogicComponent(
    val id: String,
    val type: LogicComponentType,
    val x: Float,
    val y: Float,
    val label: String = "",
    val inputCount: Int = MIN_GATE_INPUTS,
    val value: Boolean = false,
) {
    val inputPinCount: Int
        get() =
            when {
                type == LogicComponentType.INPUT -> 0
                type.supportsMultipleInputs -> inputCount.coerceIn(MIN_GATE_INPUTS, MAX_GATE_INPUTS)
                else -> 1
            }

    val displayName: String
        get() = if (label.isBlank()) type.display else "${type.display} $label"
}

@Serializable
data class LogicWire(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val targetPin: Int = 0,
)

@Serializable
data class LogicDiagram(
    val name: String,
    val components: List<LogicComponent> = emptyList(),
    val wires: List<LogicWire> = emptyList(),
    val notes: List<Note> = emptyList(),
)
