package com.jder.strings

object UseCaseStrings {
    val ACTOR by text("Attore", "Actor")
    val USE_CASE by text("Caso d'Uso", "Use Case")
    val SYSTEM by text("Sistema", "System")
    val NEW_ACTOR by text("Nuovo Attore", "New Actor")
    val NEW_USE_CASE by text("Nuovo Caso d'Uso", "New Use Case")
    val CREATE_ACTOR by text("Crea Attore", "Create Actor")
    val CREATE_USE_CASE by text("Crea Caso d'Uso", "Create Use Case")
    val CREATE_SYSTEM by text("Crea Sistema", "Create System")
    val CREATE_USE_CASE_RELATION by
        text(
            "Crea Relazione (clicca l'elemento sorgente e poi la destinazione)",
            "Create Relationship (click the source element and then the target)",
        )
    val ACTOR_ACTIONS by text("Azioni Attore", "Actor Actions")
    val USE_CASE_ACTIONS by text("Azioni Caso d'Uso", "Use Case Actions")
    val SYSTEM_ACTIONS by text("Azioni Sistema", "System Actions")
    val ACTOR_PROPERTIES by text("Proprietà Attore", "Actor Properties")
    val USE_CASE_PROPERTIES by text("Proprietà Caso d'Uso", "Use Case Properties")
    val SYSTEM_PROPERTIES by text("Proprietà Sistema", "System Properties")
    val ACTOR_UPDATED by text("Attore modificato", "Actor updated")
    val USE_CASE_UPDATED by text("Caso d'uso modificato", "Use case updated")
    val SYSTEM_UPDATED by text("Sistema modificato", "System updated")
    val ACTOR_DELETED by text("Attore eliminato", "Actor deleted")
    val USE_CASE_DELETED by text("Caso d'uso eliminato", "Use case deleted")
    val SYSTEM_DELETED by text("Sistema eliminato", "System deleted")
    val EDIT_NAME by text("Modifica Nome", "Edit Name")
    val EDIT_TYPE by text("Modifica Tipo", "Edit Type")
    val EDIT_RELATION_TYPE by text("Modifica Tipo Relazione", "Edit Relationship Type")
    val GENERALIZATION by text("Generalizzazione", "Generalization")
    val UNKNOWN_ELEMENT by text("Elemento sconosciuto", "Unknown element")
    val USE_CASE_EMPTY_HINT by
        text(
            "Scegli Attore, Caso d'Uso, Sistema o Nota dalla barra degli strumenti e clicca " +
                "sulla lavagna",
            "Choose Actor, Use Case, System or Note from the toolbar and click on the canvas",
        )
}
