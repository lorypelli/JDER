package com.jder.strings

object FileStrings {
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
}
