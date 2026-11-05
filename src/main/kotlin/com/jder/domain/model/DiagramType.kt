package com.jder.domain.model

enum class DiagramType(
    val key: String,
    val tabLabel: String,
    val displayName: String,
    val markerKeys: Set<String>,
) {
    ER("er", "Diagramma E/R", "un diagramma E/R", setOf("entities", "relationships")),
    RELATIONAL("relational", "Schema Logico", "uno schema logico", setOf("tables")),
    CLASS("class", "Diagramma Classi", "un diagramma delle classi", setOf("classes")),
    USE_CASE(
        "usecase",
        "Casi d'Uso",
        "un diagramma dei casi d'uso",
        setOf("actors", "useCases", "systemBoundaries"),
    ),
    SEQUENCE(
        "sequence",
        "Diagramma Sequenza",
        "un diagramma di sequenza",
        setOf("lifelines", "messages"),
    ),
    LOGIC("logic", "Diagramma Logico", "un diagramma logico", setOf("components", "wires")),
    MEALY("mealy", "Macchina di Mealy", "una macchina di Mealy", setOf("states", "transitions")),
    MOORE("moore", "Macchina di Moore", "una macchina di Moore", setOf("states", "transitions")),
}
