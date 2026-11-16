package com.jder.strings

object ERStrings {
    val ENTITY by text("Entità", "Entity")
    val NEW_ENTITY by text("Nuova Entità", "New Entity")
    val NEW_RELATIONSHIP by text("Nuova Relazione", "New Relationship")
    val CREATE_ENTITY by text("Crea Entità", "Create Entity")
    val CREATE_RELATIONSHIP by text("Crea Relazione", "Create Relationship")
    val ENTITY_ACTIONS by text("Azioni Entità", "Entity Actions")
    val ENTITY_PROPERTIES by text("Proprietà Entità", "Entity Properties")
    val RELATIONSHIP_PROPERTIES by text("Proprietà Relazione", "Relationship Properties")
    val ENTITY_UPDATED by text("Entità modificata", "Entity updated")
    val ENTITY_DELETED by text("Entità eliminata", "Entity deleted")
    val UNKNOWN_ENTITY by text("Entità sconosciuta", "Unknown entity")
    val WEAK_ENTITY by text("Entità Debole", "Weak Entity")
    val WEAK by text("Debole", "Weak")
    val STRONG by text("Forte", "Strong")
    val CONNECTIONS by text("Connessioni", "Connections")
    val CARDINALITY by text("Cardinalità", "Cardinality")
    val ADD_CONNECTION by text("Aggiungi Connessione", "Add Connection")
    val CREATE_CONNECTION by text("Crea Connessione", "Create Connection")
    val EDIT_CONNECTION by text("Modifica Connessione", "Edit Connection")
    val DELETE_CONNECTION by text("Elimina connessione", "Delete connection")
    val CONNECTION_CREATED by text("Connessione creata", "Connection created")
    val CONNECTION_UPDATED by text("Connessione modificata", "Connection updated")
    val CONNECTION_DELETED by text("Connessione eliminata", "Connection deleted")
    val CONVERT_TO_ASSOCIATIVE_ENTITY by
        text("Converti in Entità Associativa", "Convert to Associative Entity")
    val RELATIONSHIP_CONVERTED by
        text(
            "Relazione convertita in entità associativa",
            "Relationship converted into an associative entity",
        )
    val NO_ENTITIES_YET by
        text(
            "Il diagramma non ha ancora entità: creane una prima di aggiungere connessioni.",
            "The diagram has no entities yet: create one before adding connections.",
        )
    val ALL_ENTITIES_CONNECTED by
        text(
            "Tutte le entità sono già connesse a questa relazione.",
            "All the entities are already connected to this relationship.",
        )
    val NO_ENTITY_AVAILABLE by text("Nessuna entità disponibile.", "No entity available.")
    val KEY_ATTRIBUTES_HINT by
        text(
            "Gli attributi di tipo Chiave sono sempre chiavi primarie",
            "Key attributes are always primary keys",
        )
    val MULTIPLICITY_EXAMPLE by
        text("Molteplicità (es: 1..N, 0..N)", "Multiplicity (e.g. 1..N, 0..N)")
    val COMPONENTS by text("Componenti", "Components")
    val ADD_COMPONENT by text("Aggiungi Componente", "Add Component")
    val REMOVE_COMPONENT by text("Rimuovi componente", "Remove component")
    val COMPONENT_NAME by text("Nome componente", "Component name")
    val NO_COMPONENTS by
        text(
            "Nessun componente. Aggiungi componenti per l'attributo composto.",
            "No components. Add components to the composite attribute.",
        )
    val NORMAL by text("Normale", "Normal")
    val KEY by text("Chiave", "Key")
    val MULTIVALUED by text("Multivalore", "Multivalued")
    val DERIVED by text("Derivato", "Derived")
    val COMPOSITE by text("Composto", "Composite")
    val ER_EMPTY_HINT by
        text(
            "Scegli Entità, Relazione o Nota dalla barra degli strumenti e clicca sulla " +
                "lavagna.\nPer collegare una relazione alle entità, selezionala e usa " +
                "Aggiungi Connessione nel pannello Proprietà o nel menu contestuale.",
            "Choose Entity, Relationship or Note from the toolbar and click on the " +
                "canvas.\nTo connect a relationship to the entities, select it and use " +
                "Add Connection in the Properties panel or in the context menu.",
        )
}
