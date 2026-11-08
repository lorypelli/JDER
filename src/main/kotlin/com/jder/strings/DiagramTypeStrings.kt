package com.jder.strings

object DiagramTypeStrings {
    val ER_DIAGRAM by text("Diagramma E/R", "E/R Diagram")
    val RELATIONAL_SCHEMA by text("Schema Logico", "Relational Schema")
    val CLASS_DIAGRAM by text("Diagramma Classi", "Class Diagram")
    val USE_CASE_DIAGRAM by text("Casi d'Uso", "Use Cases")
    val SEQUENCE_DIAGRAM by text("Diagramma Sequenza", "Sequence Diagram")
    val LOGIC_DIAGRAM by text("Diagramma Logico", "Logic Diagram")
    val MEALY_MACHINE by text("Macchina di Mealy", "Mealy Machine")
    val MOORE_MACHINE by text("Macchina di Moore", "Moore Machine")
    val AN_ER_DIAGRAM by text("un diagramma E/R", "an E/R diagram")
    val A_RELATIONAL_SCHEMA by text("uno schema logico", "a relational schema")
    val A_CLASS_DIAGRAM by text("un diagramma delle classi", "a class diagram")
    val A_USE_CASE_DIAGRAM by text("un diagramma dei casi d'uso", "a use case diagram")
    val A_SEQUENCE_DIAGRAM by text("un diagramma di sequenza", "a sequence diagram")
    val A_LOGIC_DIAGRAM by text("un diagramma logico", "a logic diagram")
    val A_MEALY_MACHINE by text("una macchina di Mealy", "a Mealy machine")
    val A_MOORE_MACHINE by text("una macchina di Moore", "a Moore machine")
    val NEW_ER_DIAGRAM by text("Nuovo Diagramma E/R", "New E/R Diagram")
    val NEW_ER_DIAGRAM_NAME by text("Nuovo Diagramma E-R", "New E-R Diagram")
    val NEW_RELATIONAL_SCHEMA by text("Nuovo Schema Logico", "New Relational Schema")
    val NEW_CLASS_DIAGRAM by text("Nuovo Diagramma delle Classi", "New Class Diagram")
    val NEW_USE_CASE_DIAGRAM by text("Nuovo Diagramma dei Casi d'Uso", "New Use Case Diagram")
    val NEW_SEQUENCE_DIAGRAM by text("Nuovo Diagramma di Sequenza", "New Sequence Diagram")
    val NEW_LOGIC_DIAGRAM by text("Nuovo Diagramma Logico", "New Logic Diagram")
    val NEW_MEALY_MACHINE by text("Nuova Macchina di Mealy", "New Mealy Machine")
    val NEW_MOORE_MACHINE by text("Nuova Macchina di Moore", "New Moore Machine")
    val CHOOSE_DIAGRAM_TYPE by text("Scegli il tipo di diagramma", "Choose the diagram type")

    fun previewLoadFailed(path: String) =
        localized("Impossibile caricare l'anteprima $path", "Unable to load the preview $path")

    fun wrongDiagramType(found: String, foundTab: String, expectedTab: String) =
        localized(
            "il file contiene $found: aprilo dalla scheda \"$foundTab\" " +
                "invece che da \"$expectedTab\"",
            "the file contains $found: open it from the \"$foundTab\" tab " +
                "instead of \"$expectedTab\"",
        )
}
