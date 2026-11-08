package com.jder.domain.model

import com.jder.strings.DiagramTypeStrings

enum class DiagramType(val key: String, val markerKeys: Set<String>) {
    ER("er", setOf("entities", "relationships")),
    RELATIONAL("relational", setOf("tables")),
    CLASS("class", setOf("classes")),
    USE_CASE("usecase", setOf("actors", "useCases", "systemBoundaries")),
    SEQUENCE("sequence", setOf("lifelines", "messages")),
    LOGIC("logic", setOf("components", "wires")),
    MEALY("mealy", setOf("states", "transitions")),
    MOORE("moore", setOf("states", "transitions"));

    val tabLabel: String
        get() =
            when (this) {
                ER -> DiagramTypeStrings.ER_DIAGRAM
                RELATIONAL -> DiagramTypeStrings.RELATIONAL_SCHEMA
                CLASS -> DiagramTypeStrings.CLASS_DIAGRAM
                USE_CASE -> DiagramTypeStrings.USE_CASE_DIAGRAM
                SEQUENCE -> DiagramTypeStrings.SEQUENCE_DIAGRAM
                LOGIC -> DiagramTypeStrings.LOGIC_DIAGRAM
                MEALY -> DiagramTypeStrings.MEALY_MACHINE
                MOORE -> DiagramTypeStrings.MOORE_MACHINE
            }

    val displayName: String
        get() =
            when (this) {
                ER -> DiagramTypeStrings.AN_ER_DIAGRAM
                RELATIONAL -> DiagramTypeStrings.A_RELATIONAL_SCHEMA
                CLASS -> DiagramTypeStrings.A_CLASS_DIAGRAM
                USE_CASE -> DiagramTypeStrings.A_USE_CASE_DIAGRAM
                SEQUENCE -> DiagramTypeStrings.A_SEQUENCE_DIAGRAM
                LOGIC -> DiagramTypeStrings.A_LOGIC_DIAGRAM
                MEALY -> DiagramTypeStrings.A_MEALY_MACHINE
                MOORE -> DiagramTypeStrings.A_MOORE_MACHINE
            }
}
