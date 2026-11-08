package com.jder.strings

object CommandLineStrings {
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
}
