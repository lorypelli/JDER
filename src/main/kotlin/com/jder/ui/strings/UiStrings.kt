package com.jder.ui.strings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadOnlyProperty

object UiStrings {
    var language by mutableStateOf(Language.saved())
        private set

    fun selectLanguage(selected: Language) {
        if (selected == language) return
        language = selected
        Language.save(selected)
    }

    private fun localized(italian: String, english: String): String =
        when (language) {
            Language.ITALIAN -> italian
            Language.ENGLISH -> english
        }

    private fun text(italian: String, english: String) =
        ReadOnlyProperty<Any?, String> { _, _ -> localized(italian, english) }

    val ADD by text("Aggiungi", "Add")
    val CANCEL by text("Annulla", "Cancel")
    val CLOSE by text("Chiudi", "Close")
    val CONFIRM by text("Conferma", "Confirm")
    val COPY by text("Copia", "Copy")
    val DELETE by text("Elimina", "Delete")
    val EDIT_PROPERTIES by text("Modifica Proprietà", "Edit Properties")
    val EDIT_TEXT by text("Modifica Testo", "Edit Text")
    val NAME by text("Nome", "Name")
    val NOTE by text("Nota", "Note")
    val OK by text("OK", "OK")
    val SAVE by text("Salva", "Save")
    val YES by text("Sì", "Yes")
    val NO by text("No", "No")

    val ADD_ATTRIBUTE by text("Aggiungi Attributo", "Add Attribute")
    val ADD_COLUMN by text("Aggiungi Colonna", "Add Column")
    val ADD_METHOD by text("Aggiungi Metodo", "Add Method")
    val CREATE_NOTE by text("Crea Nota", "Create Note")
    val RELATION by text("Relazione", "Relationship")

    val TYPE by text("Tipo", "Type")
    val FROM by text("Da", "From")
    val TO by text("A", "To")
    val LABEL by text("Etichetta", "Label")
    val DOCUMENTATION by text("Documentazione", "Documentation")
    val WIDTH by text("Larghezza", "Width")
    val HEIGHT by text("Altezza", "Height")
    val SIZE by text("Dimensioni", "Size")
    val PROPERTIES by text("Proprietà", "Properties")
    val CLOSE_PROPERTIES_PANEL by text("Chiudi pannello proprietà", "Close properties panel")
    val REVERSE_DIRECTION by text("Inverti Direzione", "Reverse Direction")
    val NEW_NOTE by text("Nuova Nota", "New Note")
    val NOTE_ACTIONS by text("Azioni Nota", "Note Actions")
    val NOTE_PROPERTIES by text("Proprietà Nota", "Note Properties")
    val NOTE_TEXT by text("Testo", "Text")
    val NOTE_UPDATED by text("Nota modificata", "Note updated")
    val NOTE_DELETED by text("Nota eliminata", "Note deleted")
    val RELATION_ACTIONS by text("Azioni Relazione", "Relationship Actions")
    val RELATION_TYPE by text("Tipo di Relazione", "Relationship Type")
    val EDIT_RELATION by text("Modifica Relazione", "Edit Relationship")
    val RELATION_CREATED by text("Relazione creata", "Relationship created")
    val RELATION_UPDATED by text("Relazione modificata", "Relationship updated")
    val RELATION_DELETED by text("Relazione eliminata", "Relationship deleted")
    val ASSOCIATION by text("Associazione", "Association")
    val PRIMARY_KEY by text("Chiave Primaria", "Primary Key")
    val ATTRIBUTES by text("Attributi", "Attributes")
    val EDIT_ATTRIBUTE by text("Modifica Attributo", "Edit Attribute")
    val DELETE_ATTRIBUTE by text("Elimina attributo", "Delete attribute")
    val ATTRIBUTE_ADDED by text("Attributo aggiunto", "Attribute added")
    val ATTRIBUTE_UPDATED by text("Attributo modificato", "Attribute updated")
    val ATTRIBUTE_DELETED by text("Attributo eliminato", "Attribute deleted")

    fun yesNo(value: Boolean) = if (value) localized("sì", "yes") else "no"

    fun example(value: String) = localized("es. $value", "e.g. $value")

    val LANGUAGE by text("Lingua", "Language")
    val LANGUAGE_SAVE_FAILED by
        text("Impossibile salvare la lingua scelta", "Unable to save the chosen language")
    val FILE by text("File", "File")
    val EDIT by text("Modifica", "Edit")
    val VIEW by text("Vista", "View")
    val TOOLS by text("Strumenti", "Tools")
    val ELEMENT by text("Elemento", "Element")
    val WINDOW by text("Finestra", "Window")
    val HELP by text("Aiuto", "Help")
    val EXPORT by text("Esporta", "Export")
    val NEW_DIAGRAM by text("Nuovo Diagramma", "New Diagram")
    val OPEN_DIAGRAM_ELLIPSIS by text("Apri Diagramma...", "Open Diagram...")
    val SAVE_AS_ELLIPSIS by text("Salva con nome...", "Save As...")
    val EXPORT_PNG_ELLIPSIS by text("Esporta come PNG...", "Export as PNG...")
    val AUTO_SAVE by text("Salvataggio Automatico", "Auto-Save")
    val AUTO_EXPORT by text("Esportazione Automatica", "Auto-Export")
    val EXIT by text("Esci", "Exit")
    val UNDO by text("Annulla", "Undo")
    val REDO by text("Ripristina", "Redo")
    val DELETE_SELECTED by text("Elimina Elemento Selezionato", "Delete Selected Element")
    val DELETE_KEY by text("Canc", "Del")
    val ZOOM_IN by text("Zoom In", "Zoom In")
    val ZOOM_OUT by text("Zoom Out", "Zoom Out")
    val RESET_ZOOM by text("Reimposta Zoom", "Reset Zoom")
    val DARK_THEME by text("Tema Scuro", "Dark Theme")
    val COLOR_PALETTE by text("Palette Colori", "Color Palette")
    val FULL_SCREEN by text("Schermo Intero", "Full Screen")
    val SELECT_AND_MOVE by text("Seleziona e Sposta", "Select and Move")
    val NO_ELEMENT_SELECTED by text("Nessun elemento selezionato", "No element selected")
    val MINIMIZE by text("Riduci a Icona", "Minimize")
    val ZOOM_WINDOW by text("Ingrandisci", "Zoom")
    val USER_GUIDE by text("Guida all'Uso", "User Guide")
    val WEBSITE by text("Sito Web di JDER", "JDER Website")
    val REPORT_ISSUE by text("Segnala un Problema", "Report an Issue")
    val SOURCE_CODE by text("Codice Sorgente", "Source Code")
    val ABOUT by text("Informazioni su JDER", "About JDER")

    val OPEN_DIAGRAM by text("Apri Diagramma", "Open Diagram")
    val SAVE_DIAGRAM by text("Salva Diagramma", "Save Diagram")
    val EXPORT_PNG by text("Esporta come PNG", "Export as PNG")
    val NEW_DIAGRAM_CREATED by text("Nuovo diagramma creato", "New diagram created")
    val NOTHING_TO_EXPORT by
        text(
            "Il diagramma è vuoto: non c'è nulla da esportare",
            "The diagram is empty: there is nothing to export",
        )
    val ACTION_UNDONE by text("Azione annullata", "Action undone")
    val ACTION_REDONE by text("Azione ripristinata", "Action redone")
    val SAVE_FAILED by text("Errore nel salvataggio", "Error while saving")
    val AUTO_SAVE_FAILED by text("Errore nel salvataggio automatico", "Error during auto-save")
    val EXPORT_FAILED by text("Errore nell'esportazione", "Error while exporting")
    val AUTO_EXPORT_FAILED by
        text("Errore nell'esportazione automatica", "Error during auto-export")
    val LOAD_FAILED by text("Errore nel caricamento", "Error while loading")
    val AUTO_SAVE_DISABLED by text("Salvataggio automatico disattivato", "Auto-save disabled")
    val AUTO_EXPORT_DISABLED by text("Esportazione automatica disattivata", "Auto-export disabled")

    fun diagramSaved(fileName: String) =
        localized("Diagramma salvato: $fileName", "Diagram saved: $fileName")

    fun diagramLoaded(fileName: String) =
        localized("Diagramma caricato: $fileName", "Diagram loaded: $fileName")

    fun imageSaved(fileName: String) =
        localized("Immagine salvata: $fileName", "Image saved: $fileName")

    fun autoSaveEnabled(fileName: String) =
        localized("Salvataggio automatico attivato: $fileName", "Auto-save enabled: $fileName")

    fun autoExportEnabled(fileName: String) =
        localized("Esportazione automatica attivata: $fileName", "Auto-export enabled: $fileName")

    val CONFIRM_EXIT_TITLE by text("Conferma chiusura", "Confirm exit")
    val CONFIRM_EXIT_MESSAGE by
        text(
            "Ci sono modifiche non salvate. Vuoi uscire comunque?",
            "There are unsaved changes. Do you want to exit anyway?",
        )
    val CONFIRM_NEW_DIAGRAM_TITLE by text("Conferma nuovo diagramma", "Confirm new diagram")
    val CONFIRM_NEW_DIAGRAM_MESSAGE by
        text(
            "Ci sono modifiche non salvate. Vuoi creare un nuovo diagramma comunque?",
            "There are unsaved changes. Do you want to create a new diagram anyway?",
        )
    val CONFIRM_OPEN_DIAGRAM_TITLE by text("Conferma apertura diagramma", "Confirm opening diagram")
    val CONFIRM_OPEN_DIAGRAM_MESSAGE by
        text(
            "Ci sono modifiche non salvate. Vuoi aprire un altro diagramma comunque?",
            "There are unsaved changes. Do you want to open another diagram anyway?",
        )

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

    val TABLE by text("Tabella", "Table")
    val NEW_TABLE by text("NuovaTabella", "NewTable")
    val CREATE_TABLE by text("Crea Tabella", "Create Table")
    val TABLE_ACTIONS by text("Azioni Tabella", "Table Actions")
    val TABLE_PROPERTIES by text("Proprietà Tabella", "Table Properties")
    val TABLE_UPDATED by text("Tabella modificata", "Table updated")
    val TABLE_DELETED by text("Tabella eliminata", "Table deleted")
    val COLUMNS by text("Colonne", "Columns")
    val EDIT_COLUMN by text("Modifica Colonna", "Edit Column")
    val DELETE_COLUMN by text("Elimina colonna", "Delete column")
    val COLUMN_ADDED by text("Colonna aggiunta", "Column added")
    val COLUMN_UPDATED by text("Colonna modificata", "Column updated")
    val COLUMN_DELETED by text("Colonna eliminata", "Column deleted")
    val FOREIGN_KEY by text("Chiave Esterna", "Foreign Key")
    val FOREIGN_KEYS by text("Chiavi esterne", "Foreign keys")
    val NEW_FOREIGN_KEY by text("Nuova Chiave Esterna", "New Foreign Key")
    val CREATE_FOREIGN_KEY by text("Crea Chiave Esterna", "Create Foreign Key")
    val ADD_FOREIGN_KEY by text("Aggiungi Chiave Esterna", "Add Foreign Key")
    val DELETE_FOREIGN_KEY by text("Elimina Chiave Esterna", "Delete Foreign Key")
    val FOREIGN_KEY_ACTIONS by text("Azioni Chiave Esterna", "Foreign Key Actions")
    val FOREIGN_KEY_CREATED by text("Chiave esterna creata", "Foreign key created")
    val FOREIGN_KEY_DELETED by text("Chiave esterna eliminata", "Foreign key deleted")
    val FOREIGN_KEY_TARGET by text("Chiave esterna verso", "Foreign key to")
    val FOREIGN_KEY_REQUIREMENT by
        text(
            "Per creare una chiave esterna serve una colonna chiave primaria o con " +
                "valore unico in una tabella",
            "To create a foreign key you need a primary key column or a column with " +
                "unique values in a table",
        )
    val PRIMARY_KEY_DETAIL by text("Chiave primaria", "Primary key")
    val PRIMARY_KEY_TAG by text("chiave primaria", "primary key")
    val NO_PRIMARY_KEY by text("nessuna", "none")
    val NO_REFERENCE by text("Nessuna", "None")
    val UNIQUE_TAG by text("unica", "unique")
    val OPTIONAL_TAG by text("opzionale", "optional")
    val OPTIONAL by text("Opzionale", "Optional")
    val NULLABLE by text("Opzionale (ammette NULL)", "Optional (allows NULL)")
    val UNIQUE_VALUE by text("Valore Unico", "Unique Value")
    val REFERENCED_TABLE by text("Tabella riferita", "Referenced table")
    val PART_OF_PRIMARY_KEY by text("Parte della chiave primaria", "Part of the primary key")
    val RELATIONAL_EMPTY_HINT by
        text(
            "Scegli Tabella o Nota dalla barra degli strumenti e clicca sulla lavagna.\n" +
                "Per collegare due tabelle, scegli Chiave Esterna e clicca prima sulla tabella " +
                "che riferisce e poi su quella riferita.",
            "Choose Table or Note from the toolbar and click on the canvas.\n" +
                "To connect two tables, choose Foreign Key and click first on the " +
                "referencing table and then on the referenced one.",
        )

    fun references(column: String) = localized("riferisce $column", "references $column")

    fun columnFor(column: String) = localized("Colonna per $column", "Column for $column")

    fun tableWithoutPrimaryKey(table: String) =
        localized(
            "La tabella \"$table\" non ha una chiave primaria: " +
                "aggiungine una prima di creare la chiave esterna",
            "The table \"$table\" has no primary key: add one before creating the foreign key",
        )

    fun columnReferencesMissingTable(column: String, table: String) =
        localized(
            "La colonna $column riferisce una tabella inesistente: $table",
            "The column $column references a nonexistent table: $table",
        )

    fun columnReferencesMissingColumn(column: String, target: String) =
        localized(
            "La colonna $column riferisce una colonna inesistente: $target",
            "The column $column references a nonexistent column: $target",
        )

    val CLASS by text("Classe", "Class")
    val INTERFACE by text("Interfaccia", "Interface")
    val ENUM by text("Enum", "Enum")
    val ENUMERATION by text("Enumerazione", "Enumeration")
    val ABSTRACT_CLASS by text("Classe astratta", "Abstract class")
    val NEW_CLASS by text("NuovaClasse", "NewClass")
    val NEW_INTERFACE by text("NuovaInterfaccia", "NewInterface")
    val NEW_ENUM by text("NuovoEnum", "NewEnum")
    val CREATE_CLASS by text("Crea Classe", "Create Class")
    val CREATE_INTERFACE by text("Crea Interfaccia", "Create Interface")
    val CREATE_ENUM by text("Crea Enum", "Create Enum")
    val CREATE_CLASS_RELATION by
        text(
            "Crea Relazione (clicca la classe sorgente e poi la destinazione)",
            "Create Relationship (click the source class and then the target)",
        )
    val CLASS_ACTIONS by text("Azioni Classe", "Class Actions")
    val CLASS_PROPERTIES by text("Proprietà Classe", "Class Properties")
    val CLASS_UPDATED by text("Classe modificata", "Class updated")
    val CLASS_DELETED by text("Classe eliminata", "Class deleted")
    val CLASS_ABSTRACT by text("Astratta", "Abstract")
    val METHOD_ABSTRACT by text("Astratto", "Abstract")
    val STATIC by text("Statico", "Static")
    val VISIBILITY by text("Visibilità", "Visibility")
    val PUBLIC by text("Pubblico", "Public")
    val PRIVATE by text("Privato", "Private")
    val PROTECTED by text("Protetto", "Protected")
    val METHODS by text("Metodi", "Methods")
    val EDIT_METHOD by text("Modifica Metodo", "Edit Method")
    val METHOD_ADDED by text("Metodo aggiunto", "Method added")
    val EDIT_MEMBER by text("Modifica membro", "Edit member")
    val DELETE_MEMBER by text("Elimina membro", "Delete member")
    val MEMBER_UPDATED by text("Membro modificato", "Member updated")
    val MEMBER_DELETED by text("Membro eliminato", "Member deleted")
    val SOURCE_MULTIPLICITY by text("Molteplicità sorgente", "Source multiplicity")
    val TARGET_MULTIPLICITY by text("Molteplicità destinazione", "Target multiplicity")
    val SOURCE_MULTIPLICITY_SHORT by text("Moltep. sorgente", "Source mult.")
    val TARGET_MULTIPLICITY_SHORT by text("Moltep. destinazione", "Target mult.")
    val AGGREGATION by text("Aggregazione", "Aggregation")
    val COMPOSITION by text("Composizione", "Composition")
    val INHERITANCE by text("Ereditarietà", "Inheritance")
    val REALIZATION by text("Realizzazione", "Realization")
    val DEPENDENCY by text("Dipendenza", "Dependency")
    val CLASS_EMPTY_HINT by
        text(
            "Scegli Classe, Interfaccia, Enum o Nota dalla barra degli strumenti e clicca " +
                "sulla lavagna",
            "Choose Class, Interface, Enum or Note from the toolbar and click on the canvas",
        )

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

    val OBJECT by text("Oggetto", "Object")
    val LIFELINE by text("Linea di Vita", "Lifeline")
    val MESSAGE by text("Messaggio", "Message")
    val FRAGMENT by text("Frammento", "Fragment")
    val STEREOTYPES by text("Stereotipi", "Stereotypes")
    val CREATE_OBJECT by text("Crea Oggetto", "Create Object")
    val CREATE_BOUNDARY by text("Crea Boundary", "Create Boundary")
    val CREATE_CONTROL by text("Crea Control", "Create Control")
    val CREATE_ENTITY_LIFELINE by text("Crea Entity", "Create Entity")
    val CREATE_MESSAGE by text("Crea Messaggio", "Create Message")
    val CREATE_FRAGMENT by
        text(
            "Crea Frammento Combinato (alt, opt, loop...)",
            "Create Combined Fragment (alt, opt, loop...)",
        )
    val LIFELINE_ACTIONS by text("Azioni Linea di Vita", "Lifeline Actions")
    val MESSAGE_ACTIONS by text("Azioni Messaggio", "Message Actions")
    val FRAGMENT_ACTIONS by text("Azioni Frammento", "Fragment Actions")
    val LIFELINE_PROPERTIES by text("Proprietà Linea di Vita", "Lifeline Properties")
    val FRAGMENT_PROPERTIES by text("Proprietà Frammento", "Fragment Properties")
    val NEW_MESSAGE by text("Nuovo Messaggio", "New Message")
    val EDIT_MESSAGE by text("Modifica Messaggio", "Edit Message")
    val LIFELINE_UPDATED by text("Linea di vita modificata", "Lifeline updated")
    val MESSAGE_CREATED by text("Messaggio creato", "Message created")
    val MESSAGE_UPDATED by text("Messaggio modificato", "Message updated")
    val FRAGMENT_UPDATED by text("Frammento modificato", "Fragment updated")
    val LIFELINE_DELETED by text("Linea di vita eliminata", "Lifeline deleted")
    val MESSAGE_DELETED by text("Messaggio eliminato", "Message deleted")
    val FRAGMENT_DELETED by text("Frammento eliminato", "Fragment deleted")
    val MESSAGES_SENT by text("Messaggi inviati", "Messages sent")
    val MESSAGES_RECEIVED by text("Messaggi ricevuti", "Messages received")
    val CONDITION by text("Condizione", "Condition")
    val DEFAULT_CONDITION by text("condizione", "condition")
    val INTERACTION by text("Interazione", "Interaction")
    val REFERENCED_INTERACTION by text("Interazione riferita", "Referenced interaction")
    val OPERANDS by text("Operandi", "Operands")
    val ADDITIONAL_OPERANDS by text("Operandi aggiuntivi", "Additional operands")
    val ADD_OPERAND by text("Aggiungi operando", "Add operand")
    val REMOVE_OPERAND by text("Rimuovi operando", "Remove operand")
    val LIFELINE_NAME_EXAMPLE by text("es. c : Carrello", "e.g. c : Cart")
    val MESSAGE_LABEL_EXAMPLE by text("es. login(utente, password)", "e.g. login(user, password)")
    val REFERENCE_EXAMPLE by text("es. Effettuare Login", "e.g. Log In")
    val CONDITION_EXAMPLE by text("es. utente registrato", "e.g. registered user")
    val SYNCHRONOUS by text("Sincrono", "Synchronous")
    val ASYNCHRONOUS by text("Asincrono", "Asynchronous")
    val REPLY by text("Risposta", "Reply")
    val CREATION by text("Creazione", "Creation")
    val DESTRUCTION by text("Distruzione", "Destruction")
    val ALTERNATIVE by text("Alternativa", "Alternative")
    val OPTION by text("Opzionale", "Option")
    val LOOP by text("Ciclo", "Loop")
    val PARALLEL by text("Parallelo", "Parallel")
    val BREAK by text("Interruzione", "Break")
    val CRITICAL_REGION by text("Regione critica", "Critical region")
    val REFERENCE by text("Riferimento", "Reference")
    val SEQUENCE_EMPTY_HINT by
        text(
            "Scegli Attore, Oggetto o un altro partecipante dalla barra degli strumenti e " +
                "clicca sulla lavagna.\nPer creare un messaggio, scegli Messaggio e clicca " +
                "prima sulla linea di vita mittente e poi sulla destinataria, oppure trascina " +
                "da una linea all'altra.",
            "Choose Actor, Object or another participant from the toolbar and click on the " +
                "canvas.\nTo create a message, choose Message and click first on the sender " +
                "lifeline and then on the receiver, or drag from one lifeline to the other.",
        )

    fun messageMissingSource(message: String, source: String) =
        localized(
            "Il messaggio $message ha una sorgente inesistente: $source",
            "The message $message has a nonexistent source: $source",
        )

    fun messageMissingTarget(message: String, target: String) =
        localized(
            "Il messaggio $message ha una destinazione inesistente: $target",
            "The message $message has a nonexistent target: $target",
        )

    val INPUT by text("Ingresso", "Input")
    val OUTPUT by text("Uscita", "Output")
    val INPUTS by text("Ingressi", "Inputs")
    val INPUT_KIND by text("ingresso", "input")
    val OUTPUT_KIND by text("uscita", "output")
    val LOGIC_GATES by text("Porte Logiche", "Logic Gates")
    val WIRE by text("Collegamento", "Wire")
    val COMPONENT by text("Componente", "Component")
    val INPUT_TOOL by
        text("Ingresso (clicca sul valore per commutarlo)", "Input (click the value to toggle it)")
    val WIRE_TOOL by
        text(
            "Collegamento (clicca la sorgente e poi la destinazione, oppure trascina da un pin)",
            "Wire (click the source and then the target, or drag from a pin)",
        )
    val INPUT_ACTIONS by text("Azioni Ingresso", "Input Actions")
    val OUTPUT_ACTIONS by text("Azioni Uscita", "Output Actions")
    val GATE_ACTIONS by text("Azioni Porta Logica", "Logic Gate Actions")
    val WIRE_ACTIONS by text("Azioni Collegamento", "Wire Actions")
    val INPUT_PROPERTIES by text("Proprietà Ingresso", "Input Properties")
    val OUTPUT_PROPERTIES by text("Proprietà Uscita", "Output Properties")
    val GATE_PROPERTIES by text("Proprietà Porta Logica", "Logic Gate Properties")
    val TOGGLE_VALUE by text("Commuta Valore", "Toggle Value")
    val ADD_INPUT by text("Aggiungi Ingresso", "Add Input")
    val REMOVE_INPUT by text("Rimuovi Ingresso", "Remove Input")
    val DELETE_WIRE by text("Elimina Collegamento", "Delete Wire")
    val TRUTH_TABLE by text("Tabella di verità", "Truth table")
    val TRUTH_TABLE_ELLIPSIS by text("Tabella di Verità...", "Truth Table...")
    val SHOW_SIMULATION by text("Mostra i valori della simulazione", "Show the simulation values")
    val HIDE_SIMULATION by text("Nascondi i valori della simulazione", "Hide the simulation values")
    val SHOW_SIMULATION_MENU by text("Mostra Valori della Simulazione", "Show Simulation Values")
    val CIRCUIT_OSCILLATES by
        text(
            "Il circuito oscilla: alcuni valori non sono determinabili",
            "The circuit oscillates: some values cannot be determined",
        )
    val COMPONENT_UPDATED by text("Componente modificato", "Component updated")
    val INPUT_DELETED by text("Ingresso eliminato", "Input deleted")
    val OUTPUT_DELETED by text("Uscita eliminata", "Output deleted")
    val GATE_DELETED by text("Porta eliminata", "Gate deleted")
    val WIRE_CREATED by text("Collegamento creato", "Wire created")
    val WIRE_DELETED by text("Collegamento eliminato", "Wire deleted")
    val INVALID_WIRE by text("Collegamento non valido", "Invalid wire")
    val VALUE by text("Valore", "Value")
    val CURRENT_VALUE by text("Valore attuale", "Current value")
    val HIGH_VALUE by text("1 (alto)", "1 (high)")
    val LOW_VALUE by text("0 (basso)", "0 (low)")
    val UNDEFINED_VALUE by text("indefinito", "undefined")
    val INPUT_COUNT by text("Numero di ingressi", "Number of inputs")
    val GATE_TYPE by text("Tipo di porta", "Gate type")
    val GATE_LABEL_EXAMPLE by text("Opzionale, es. U1", "Optional, e.g. U1")
    val BOOLEAN_EXPRESSION by text("Espressione booleana", "Boolean expression")
    val EXPRESSIONS by text("Espressioni", "Expressions")
    val TRUTH_TABLE_NEEDS_OUTPUT by
        text(
            "Aggiungi almeno un'uscita al circuito per calcolare la tabella di verità.",
            "Add at least one output to the circuit to compute the truth table.",
        )
    val LOGIC_EMPTY_HINT by
        text(
            "Scegli un ingresso, un'uscita o una porta logica dalla barra degli strumenti e " +
                "clicca sulla lavagna.\nPer collegare due elementi trascina da un pin all'altro.",
            "Choose an input, an output or a logic gate from the toolbar and click on the " +
                "canvas.\nTo connect two elements, drag from one pin to the other.",
        )

    fun gate(gate: String) = localized("Porta $gate", "$gate Gate")

    fun logicGateKind(gate: String) = localized("porta logica $gate", "$gate logic gate")

    fun inputNumber(number: Int) = localized("ingresso $number", "input $number")

    fun tooManyTruthTableInputs(count: Int, max: Int) =
        localized(
            "Il circuito ha $count ingressi: la tabella di verità ne supporta al massimo $max.",
            "The circuit has $count inputs: the truth table supports at most $max.",
        )

    val STATE by text("Stato", "State")
    val TRANSITION by text("Transizione", "Transition")
    val CREATE_STATE by text("Crea Stato", "Create State")
    val CREATE_TRANSITION by
        text(
            "Crea Transizione (clicca lo stato di partenza e poi quello di arrivo, " +
                "oppure trascina dal bordo di uno stato)",
            "Create Transition (click the start state and then the end state, " +
                "or drag from the edge of a state)",
        )
    val STATE_ACTIONS by text("Azioni Stato", "State Actions")
    val TRANSITION_ACTIONS by text("Azioni Transizione", "Transition Actions")
    val STATE_PROPERTIES by text("Proprietà Stato", "State Properties")
    val TRANSITION_PROPERTIES by text("Proprietà Transizione", "Transition Properties")
    val NEW_TRANSITION by text("Nuova Transizione", "New Transition")
    val STATE_UPDATED by text("Stato modificato", "State updated")
    val TRANSITION_CREATED by text("Transizione creata", "Transition created")
    val TRANSITION_UPDATED by text("Transizione modificata", "Transition updated")
    val STATE_DELETED by text("Stato eliminato", "State deleted")
    val TRANSITION_DELETED by text("Transizione eliminata", "Transition deleted")
    val EDIT_TRANSITION by text("Modifica transizione", "Edit transition")
    val DELETE_TRANSITION by text("Elimina transizione", "Delete transition")
    val SET_INITIAL_STATE by text("Imposta come Stato Iniziale", "Set as Initial State")
    val SET_FINAL_STATE by text("Imposta come Stato Finale", "Set as Final State")
    val REMOVE_FROM_FINAL_STATES by text("Rimuovi dagli Stati Finali", "Remove from Final States")
    val INITIAL_STATE_SET by text("Stato iniziale impostato", "Initial state set")
    val FINAL_STATE_SET by text("Stato finale impostato", "Final state set")
    val STATE_REMOVED_FROM_FINAL by
        text("Stato rimosso dagli stati finali", "State removed from the final states")
    val RESET_SHAPE by text("Ripristina Forma", "Reset Shape")
    val INITIAL by text("Iniziale", "Initial")
    val FINAL by text("Finale", "Final")
    val INITIAL_STATE by text("Stato iniziale", "Initial state")
    val FINAL_STATE by text("Stato finale", "Final state")
    val NEXT_STATE by text("Stato successivo", "Next state")
    val UNKNOWN_STATE by text("Stato sconosciuto", "Unknown state")
    val OUTGOING_TRANSITIONS by text("Transizioni uscenti", "Outgoing transitions")
    val DUPLICATE_STATE_NAME by
        text("Esiste già uno stato con questo nome", "A state with this name already exists")
    val MOORE_STATE_OUTPUT_HINT by
        text(
            "Nella macchina di Moore l'uscita dipende solo dallo stato.",
            "In a Moore machine the output depends only on the state.",
        )
    val MEALY_TRANSITION_OUTPUT_HINT by
        text(
            "Nella macchina di Mealy l'uscita dipende dallo stato e dall'ingresso: " +
                "si scrive come ingresso/uscita.",
            "In a Mealy machine the output depends on the state and on the input: " +
                "it is written as input/output.",
        )
    val FINAL_STATES_HINT by
        text(
            "Facoltativo: nella definizione classica le macchine di Mealy e di Moore " +
                "non hanno stati finali.",
            "Optional: in the classic definition, Mealy and Moore machines " +
                "have no final states.",
        )
    val MULTIPLE_INPUTS_HINT by
        text(
            "Più ingressi con lo stesso effetto si separano con una virgola, es. 0,1",
            "Separate multiple inputs with the same effect with a comma, e.g. 0,1",
        )
    val TRANSITION_TABLE by text("Tabella di transizione", "Transition table")
    val TRANSITION_TABLE_TITLE by text("Tabella di Transizione", "Transition Table")
    val TRANSITION_TABLE_ELLIPSIS by text("Tabella di Transizione...", "Transition Table...")
    val NO_STATES_FOR_TABLE by
        text(
            "Aggiungi almeno uno stato per costruire la tabella di transizione.",
            "Add at least one state to build the transition table.",
        )
    val MEALY_TABLE_HINT by
        text(
            "Ogni cella indica lo stato successivo e l'uscita (stato/uscita) per lo " +
                "stato della riga e l'ingresso della colonna.",
            "Each cell shows the next state and the output (state/output) for the " +
                "state of the row and the input of the column.",
        )
    val MOORE_TABLE_HINT by
        text(
            "Ogni cella indica lo stato successivo per lo stato della riga e " +
                "l'ingresso della colonna; l'uscita dipende solo dallo stato.",
            "Each cell shows the next state for the state of the row and the input " +
                "of the column; the output depends only on the state.",
        )
    val INITIAL_AND_FINAL_MARKS_HINT by
        text(
            "Lo stato iniziale è segnato con →, quelli finali con *.",
            "The initial state is marked with →, the final ones with *.",
        )
    val INITIAL_MARK_HINT by
        text("Lo stato iniziale è segnato con →.", "The initial state is marked with →.")
    val TO_CHECK by text("Da controllare", "To check")
    val SIMULATE_SEQUENCE by text("Simula una sequenza di ingressi", "Simulate an input sequence")
    val SIMULATE_SEQUENCE_TITLE by text("Simula una Sequenza", "Simulate a Sequence")
    val SIMULATE_SEQUENCE_ELLIPSIS by text("Simula una Sequenza...", "Simulate a Sequence...")
    val INPUT_SEQUENCE by text("Sequenza di ingressi", "Input sequence")
    val OUTPUT_SEQUENCE by text("Sequenza di uscita", "Output sequence")
    val REACHED_STATE by text("Stato raggiunto", "Reached state")
    val SEPARATE_SYMBOLS_HINT by
        text("Separa i simboli con spazi o virgole.", "Separate the symbols with spaces or commas.")
    val SEQUENCE_STOPPED by
        text(
            "Sequenza rifiutata: la macchina si ferma prima della fine",
            "Sequence rejected: the machine stops before the end",
        )
    val OUTCOME by text("Esito", "Outcome")
    val ACCEPTED by text("accettata", "accepted")
    val REJECTED by text("rifiutata", "rejected")
    val SET_INITIAL_STATE_FIRST by
        text(
            "Imposta prima lo stato iniziale della macchina.",
            "Set the initial state of the machine first.",
        )
    val MEALY_EMPTY_HINT by
        text(
            "Nella macchina di Mealy l'uscita si scrive sulle transizioni, come " +
                "ingresso/uscita.",
            "In a Mealy machine the output is written on the transitions, as input/output.",
        )
    val MOORE_EMPTY_HINT by
        text(
            "Nella macchina di Moore l'uscita si scrive negli stati, sotto il nome.",
            "In a Moore machine the output is written in the states, below the name.",
        )
    val STATE_MACHINE_EMPTY_HINT by
        text(
            "Scegli Stato dalla barra degli strumenti e clicca sulla lavagna.\n" +
                "Per creare una transizione trascina dal bordo di uno stato fino allo stato di " +
                "arrivo (anche lo stesso).",
            "Choose State from the toolbar and click on the canvas.\n" +
                "To create a transition, drag from the edge of a state to the end state " +
                "(even the same one).",
        )

    fun transitionCounts(outgoing: Int, incoming: Int) =
        localized(
            "Transizioni uscenti: $outgoing, entranti: $incoming",
            "Outgoing transitions: $outgoing, incoming: $incoming",
        )

    fun towards(state: String) = localized("verso $state", "to $state")

    fun transitionBetween(source: String, target: String) =
        localized("Da $source a $target", "From $source to $target")

    fun selfTransition(state: String) = localized("Da $state a sé stesso", "From $state to itself")

    fun repeatedInputs(state: String, inputs: String) =
        localized(
            "$state ha già una transizione con ingresso $inputs: " +
                "la macchina non sarà deterministica",
            "$state already has a transition with input $inputs: " +
                "the machine will not be deterministic",
        )

    fun typeSymbolsHint(example: String) =
        localized(
            "Scrivi i simboli uno dopo l'altro (es. $example); spazi e virgole vengono ignorati.",
            "Write the symbols one after the other (e.g. $example); spaces and commas are " +
                "ignored.",
        )

    fun sequenceAccepted(state: String) =
        localized(
            "Sequenza accettata: $state è uno stato finale",
            "Sequence accepted: $state is a final state",
        )

    fun sequenceRejected(state: String) =
        localized(
            "Sequenza rifiutata: $state non è uno stato finale",
            "Sequence rejected: $state is not a final state",
        )

    fun missingInitialState(action: String) =
        localized(
            "Manca lo stato iniziale: selezionane uno e usa «$action».",
            "The initial state is missing: select one and use “$action”.",
        )

    fun stateWithInput(state: String, input: String) =
        localized("$state con ingresso $input", "$state with input $input")

    fun nondeterministicTransitions(cases: String) =
        localized(
            "Più transizioni con lo stesso ingresso (la macchina non è deterministica): $cases",
            "Several transitions with the same input (the machine is not deterministic): $cases",
        )

    fun undefinedTransitions(cases: String) =
        localized("Transizioni non definite: $cases", "Undefined transitions: $cases")

    fun transitionsWithoutInput(transitions: String) =
        localized(
            "Transizioni senza ingresso: $transitions",
            "Transitions without input: $transitions",
        )

    fun statesWithoutOutput(states: String) =
        localized("Stati senza uscita: $states", "States without output: $states")

    fun transitionsWithoutOutput(transitions: String) =
        localized(
            "Transizioni senza uscita: $transitions",
            "Transitions without output: $transitions",
        )

    fun unreachableStates(states: String) =
        localized(
            "Stati non raggiungibili dallo stato iniziale: $states",
            "States not reachable from the initial state: $states",
        )

    fun andOthers(listed: String, others: Int) =
        localized("$listed e altri $others", "$listed and $others more")

    fun noTransitionForInput(step: Int, state: String, input: String) =
        localized(
            "t = $step: dallo stato $state non esce nessuna transizione con ingresso $input.",
            "t = $step: no transition with input $input leaves the state $state.",
        )

    fun severalTransitionsForInput(step: Int, state: String, input: String) =
        localized(
            "t = $step: dallo stato $state escono più transizioni con ingresso $input " +
                "(la macchina non è deterministica).",
            "t = $step: several transitions with input $input leave the state $state " +
                "(the machine is not deterministic).",
        )

    fun transitionToMissingState(step: Int) =
        localized(
            "t = $step: la transizione porta a uno stato inesistente.",
            "t = $step: the transition leads to a nonexistent state.",
        )

    fun missingInitialStateId(state: String) =
        localized(
            "Lo stato iniziale non esiste: $state",
            "The initial state does not exist: $state",
        )

    fun transitionMissingSource(transition: String, state: String) =
        localized(
            "La transizione $transition parte da uno stato inesistente: $state",
            "The transition $transition starts from a nonexistent state: $state",
        )

    fun transitionMissingTarget(transition: String, state: String) =
        localized(
            "La transizione $transition arriva a uno stato inesistente: $state",
            "The transition $transition ends in a nonexistent state: $state",
        )

    val OPEN by text("Apri", "Open")
    val OVERWRITE by text("Sovrascrivi", "Overwrite")
    val PATH by text("Percorso", "Path")
    val FILE_NAME by text("Nome file", "File name")
    val PARENT_FOLDER by text("Cartella superiore", "Parent folder")
    val RELOAD by text("Ricarica", "Reload")
    val SHOW_ONLY_JSON by text("Mostra solo file JSON", "Show only JSON files")
    val EMPTY_FOLDER by text("Cartella vuota", "Empty folder")
    val UNREADABLE_FOLDER by text("Impossibile leggere la cartella", "Unable to read the folder")
    val INVALID_PATH by text("Percorso non valido", "Invalid path")
    val PATH_NOT_FOUND by
        text("Il percorso specificato non esiste.", "The specified path does not exist.")
    val PATH_NOT_FOLDER by
        text("Il percorso specificato non è una cartella.", "The specified path is not a folder.")
    val FOLDER_ACCESS_DENIED by
        text(
            "Non hai i permessi per accedere a questa cartella.",
            "You do not have permission to access this folder.",
        )
    val FILE_NAME_REQUIRED by text("Inserisci un nome per il file", "Enter a name for the file")
    val FILE_NAME_INVALID by
        text(
            "Il nome del file contiene caratteri non validi",
            "The file name contains invalid characters",
        )
    val CONFIRM_OVERWRITE by text("Conferma sovrascrittura", "Confirm overwrite")

    fun fileExists(fileName: String) =
        localized(
            "Il file \"$fileName\" esiste già. Vuoi sovrascriverlo?",
            "The file \"$fileName\" already exists. Do you want to overwrite it?",
        )

    fun cannotRead(path: String) = localized("Impossibile leggere $path", "Unable to read $path")

    fun cannotOpen(target: String) =
        localized("Impossibile aprire $target", "Unable to open $target")

    val WARNING by text("Attenzione", "Warning")
    val ERROR by text("Errore", "Error")
    val DOCK_ICON_FAILED by
        text("Impossibile impostare l'icona nel Dock", "Unable to set the Dock icon")
    val READ_ONLY_FILE by text("il file è di sola lettura", "the file is read-only")
    val IMAGE_ENCODING_FAILED by
        text("Impossibile codificare l'immagine", "Unable to encode the image")
    val UPDATE_CHECK_FAILED by
        text("Controllo degli aggiornamenti non riuscito", "Update check failed")
    val UPDATE_AVAILABLE by text("Aggiornamento disponibile", "Update available")
    val UPDATE_AVAILABLE_MESSAGE by
        text(
            "È disponibile una nuova versione di JDER. Scarica il nuovo JDER.jar e usalo al " +
                "posto di quello attuale.",
            "A new version of JDER is available. Download the new JDER.jar and use it " +
                "instead of the current one.",
        )
    val DOWNLOAD by text("Scarica", "Download")
    val LATER by text("Più tardi", "Later")
    val ABOUT_TEXT by
        text(
            "Editor di diagrammi Entità-Relazione, schemi logici, diagrammi delle " +
                "Classi, dei Casi d'Uso, di Sequenza, Logici e macchine a stati finiti " +
                "di Mealy e di Moore.\n\n" +
                "Riscrittura completa in Kotlin e Compose del software originale creato da " +
                "Alessandro Ballini e successivamente mantenuto da Gianvito Pio.\n\n" +
                "Licenza MIT",
            "Editor of Entity-Relationship diagrams, relational schemas, Class, Use Case, " +
                "Sequence and Logic diagrams, and Mealy and Moore finite state machines.\n\n" +
                "Complete rewrite in Kotlin and Compose of the original software created by " +
                "Alessandro Ballini and later maintained by Gianvito Pio.\n\n" +
                "MIT License",
        )

    fun gitHubStatus(code: Int) =
        localized("GitHub ha risposto con il codice $code", "GitHub responded with code $code")

    val COMMAND_LINE_USAGE by
        text(
            """
            Uso: java -jar JDER.jar [--json <file.json>] [--png <file.png>]

            Opzioni:
              --json <file>  apre il diagramma salvato nel file JSON, nella scheda del suo tipo
                             (se il file non esiste o il tipo non si riconosce, il tipo si sceglie
                             all'avvio; un file nuovo viene salvato alla prima modifica)
              --png <file>   salva il diagramma come immagine PNG e la aggiorna automaticamente a
                             ogni modifica
              -h, --help     mostra questo messaggio ed esce

            Se manca, al nome del file viene aggiunta l'estensione .json o .png.

            Esempio:
              java -jar JDER.jar --json diagramma.json --png diagramma.png
            """
                .trimIndent(),
            """
            Usage: java -jar JDER.jar [--json <file.json>] [--png <file.png>]

            Options:
              --json <file>  opens the diagram saved in the JSON file, in the tab of its type
                             (if the file does not exist or its type is not recognized, the type
                             is chosen at startup; a new file is saved at the first change)
              --png <file>   saves the diagram as a PNG image and updates it automatically at
                             every change
              -h, --help     shows this message and exits

            If missing, the .json or .png extension is added to the file name.

            Example:
              java -jar JDER.jar --json diagram.json --png diagram.png
            """
                .trimIndent(),
        )
    val JSON_CANNOT_BE_OPENED by
        text("il file JSON non si può aprire", "the JSON file cannot be opened")

    fun unexpectedArgument(argument: String) =
        localized("Argomento non previsto: $argument", "Unexpected argument: $argument")

    fun unknownOption(option: String) =
        localized("Opzione sconosciuta: $option", "Unknown option: $option")

    fun missingFileAfter(option: String) =
        localized("Manca il file dopo $option", "Missing file after $option")

    fun repeatedOption(option: String) =
        localized(
            "Opzione $option ripetuta: viene usato il primo file",
            "Option $option repeated: the first file is used",
        )

    fun isFolder(path: String) = localized("$path è una cartella", "$path is a folder")

    fun missingFolder(path: String) =
        localized("la cartella $path non esiste", "the folder $path does not exist")

    fun optionIgnored(option: String, reason: String) =
        localized("Opzione $option ignorata: $reason", "Option $option ignored: $reason")

    val PALETTE_BLUE by text("Blu", "Blue")
    val PALETTE_RED by text("Rosso", "Red")
    val PALETTE_GREEN by text("Verde", "Green")
    val PALETTE_PURPLE by text("Viola", "Purple")
    val PALETTE_ORANGE by text("Arancione", "Orange")
    val PALETTE_TEAL by text("Verde Acqua", "Teal")
    val PALETTE_INDIGO by text("Indaco", "Indigo")
    val PALETTE_PINK by text("Rosa", "Pink")
    val PALETTE_CYAN by text("Ciano", "Cyan")
    val PALETTE_AMBER by text("Ambra", "Amber")
    val PALETTE_DEEP_PURPLE by text("Viola Scuro", "Deep Purple")
    val PALETTE_LIGHT_GREEN by text("Verde Chiaro", "Light Green")
    val PALETTE_LIME by text("Lime", "Lime")
    val PALETTE_YELLOW by text("Giallo", "Yellow")
    val PALETTE_DEEP_ORANGE by text("Arancione Scuro", "Deep Orange")
    val PALETTE_BROWN by text("Marrone", "Brown")
    val PALETTE_LIGHT_BLUE by text("Blu Chiaro", "Light Blue")
    val PALETTE_GRAY by text("Grigio", "Gray")
    val PALETTE_BLUE_GRAY by text("Grigio Blu", "Blue Gray")
    val PALETTE_BLACK_AND_WHITE by text("Bianco & Nero", "Black & White")
}
