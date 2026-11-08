package com.jder.domain.model

import com.jder.ui.strings.UiStrings

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
                ER -> UiStrings.ER_DIAGRAM
                RELATIONAL -> UiStrings.RELATIONAL_SCHEMA
                CLASS -> UiStrings.CLASS_DIAGRAM
                USE_CASE -> UiStrings.USE_CASE_DIAGRAM
                SEQUENCE -> UiStrings.SEQUENCE_DIAGRAM
                LOGIC -> UiStrings.LOGIC_DIAGRAM
                MEALY -> UiStrings.MEALY_MACHINE
                MOORE -> UiStrings.MOORE_MACHINE
            }

    val displayName: String
        get() =
            when (this) {
                ER -> UiStrings.AN_ER_DIAGRAM
                RELATIONAL -> UiStrings.A_RELATIONAL_SCHEMA
                CLASS -> UiStrings.A_CLASS_DIAGRAM
                USE_CASE -> UiStrings.A_USE_CASE_DIAGRAM
                SEQUENCE -> UiStrings.A_SEQUENCE_DIAGRAM
                LOGIC -> UiStrings.A_LOGIC_DIAGRAM
                MEALY -> UiStrings.A_MEALY_MACHINE
                MOORE -> UiStrings.A_MOORE_MACHINE
            }
}
