package com.jder.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SequenceLifelineType(val display: String) {
    ACTOR("Attore"),
    OBJECT("Oggetto"),
    BOUNDARY("Boundary"),
    CONTROL("Control"),
    ENTITY("Entity"),
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
enum class SequenceMessageType(val display: String) {
    SYNC("Sincrono"),
    ASYNC("Asincrono"),
    REPLY("Risposta"),
    CREATE("Creazione"),
    DESTROY("Distruzione"),
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
enum class SequenceFragmentType(val keyword: String, val display: String) {
    ALT("alt", "Alternativa"),
    OPT("opt", "Opzionale"),
    LOOP("loop", "Ciclo"),
    PAR("par", "Parallelo"),
    BREAK("break", "Interruzione"),
    CRITICAL("critical", "Regione critica"),
    REF("ref", "Riferimento");

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
    messages.forEach { message ->
        if (message.sourceId !in lifelineIds) {
            errors += "Il messaggio ${message.id} ha una sorgente inesistente: ${message.sourceId}"
        }
        if (message.targetId !in lifelineIds) {
            errors +=
                "Il messaggio ${message.id} ha una destinazione inesistente: ${message.targetId}"
        }
    }
    if (errors.isNotEmpty()) throw DiagramValidationException(errors)
}

data class SequenceAnchor(val lifelineId: String, val y: Float)
