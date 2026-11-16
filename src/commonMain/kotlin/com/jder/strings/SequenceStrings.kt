package com.jder.strings

object SequenceStrings {
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
}
