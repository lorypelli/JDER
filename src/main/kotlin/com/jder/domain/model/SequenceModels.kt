package com.jder.domain.model

import com.jder.strings.SequenceStrings
import com.jder.strings.UseCaseStrings
import kotlinx.serialization.Serializable

@Serializable
enum class SequenceLifelineType {
    ACTOR,
    OBJECT,
    BOUNDARY,
    CONTROL,
    ENTITY;

    val display: String
        get() =
            when (this) {
                ACTOR -> UseCaseStrings.ACTOR
                OBJECT -> SequenceStrings.OBJECT
                BOUNDARY -> "Boundary"
                CONTROL -> "Control"
                ENTITY -> "Entity"
            }
}

@Serializable
data class SequenceLifeline(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val type: SequenceLifelineType = SequenceLifelineType.OBJECT,
    val width: Float = 120f,
    val documentation: String = "",
)

@Serializable
enum class SequenceMessageType {
    SYNC,
    ASYNC,
    REPLY,
    CREATE,
    DESTROY;

    val display: String
        get() =
            when (this) {
                SYNC -> SequenceStrings.SYNCHRONOUS
                ASYNC -> SequenceStrings.ASYNCHRONOUS
                REPLY -> SequenceStrings.REPLY
                CREATE -> SequenceStrings.CREATION
                DESTROY -> SequenceStrings.DESTRUCTION
            }
}

@Serializable
data class SequenceMessage(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val y: Float,
    val label: String = "",
    val type: SequenceMessageType = SequenceMessageType.SYNC,
) {
    val isSelfMessage: Boolean
        get() = sourceId == targetId
}

@Serializable
enum class SequenceFragmentType(val keyword: String) {
    ALT("alt"),
    OPT("opt"),
    LOOP("loop"),
    PAR("par"),
    BREAK("break"),
    CRITICAL("critical"),
    REF("ref");

    val display: String
        get() =
            when (this) {
                ALT -> SequenceStrings.ALTERNATIVE
                OPT -> SequenceStrings.OPTION
                LOOP -> SequenceStrings.LOOP
                PAR -> SequenceStrings.PARALLEL
                BREAK -> SequenceStrings.BREAK
                CRITICAL -> SequenceStrings.CRITICAL_REGION
                REF -> SequenceStrings.REFERENCE
            }

    val hasOperands: Boolean
        get() = this == ALT || this == PAR
}

@Serializable
data class SequenceOperand(val id: String, val offset: Float, val condition: String = "")

@Serializable
data class SequenceFragment(
    val id: String,
    val type: SequenceFragmentType,
    val x: Float,
    val y: Float,
    val width: Float = 320f,
    val height: Float = 160f,
    val condition: String = "",
    val operands: List<SequenceOperand> = emptyList(),
)

@Serializable
data class SequenceDiagram(
    val name: String,
    val lifelines: List<SequenceLifeline> = emptyList(),
    val messages: List<SequenceMessage> = emptyList(),
    val fragments: List<SequenceFragment> = emptyList(),
    val notes: List<Note> = emptyList(),
)

fun SequenceDiagram.validateMessageEndpoints() {
    val lifelineIds = lifelines.mapTo(mutableSetOf()) { it.id }
    val errors = mutableListOf<String>()
    messages.forEach { (id, sourceId, targetId) ->
        if (sourceId !in lifelineIds) {
            errors += SequenceStrings.messageMissingSource(id, sourceId)
        }
        if (targetId !in lifelineIds) {
            errors += SequenceStrings.messageMissingTarget(id, targetId)
        }
    }
    if (errors.isNotEmpty()) throw DiagramValidationException(errors)
}

data class SequenceAnchor(val lifelineId: String, val y: Float)
