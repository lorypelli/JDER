package com.jder.strings

object AppStrings {
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
}
