package com.jder.ui.strings

object UiStrings {
    const val ADD = "Aggiungi"
    const val CANCEL = "Annulla"
    const val CLOSE = "Chiudi"
    const val CONFIRM = "Conferma"
    const val DELETE = "Elimina"
    const val EDIT_PROPERTIES = "Modifica Proprietà"
    const val EDIT_TEXT = "Modifica Testo"
    const val NAME = "Nome"
    const val NOTE = "Nota"
    const val SAVE = "Salva"
    const val YES = "Sì"
    const val NO = "No"

    const val ADD_ATTRIBUTE = "Aggiungi Attributo"
    const val ADD_COLUMN = "Aggiungi Colonna"
    const val ADD_METHOD = "Aggiungi Metodo"
    const val CREATE_NOTE = "Crea Nota"
    const val RELATION = "Relazione"

    fun deleted(item: String): String = "$item eliminato"

    fun added(item: String): String = "$item aggiunto"

    fun modified(item: String): String = "$item modificato"

    fun created(item: String): String = "$item creato"
}
