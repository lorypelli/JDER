package com.jder

import com.jder.data.DiagramFileRepository
import com.jder.data.detectDiagramType
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

private const val JSON_OPTION = "--json"
private const val PNG_OPTION = "--png"

const val COMMAND_LINE_USAGE =
    """Uso: java -jar JDER.jar [--json <file.json>] [--png <file.png>]

Opzioni:
  --json <file>  apre il diagramma salvato nel file JSON, nella scheda del suo tipo
                 (se non esiste, crea un nuovo diagramma ER e lo salva alla prima modifica)
  --png <file>   salva il diagramma come immagine PNG (file con estensione .png) e la
                 aggiorna automaticamente a ogni modifica (richiede il file JSON)
  -h, --help     mostra questo messaggio ed esce

Esempio:
  java -jar JDER.jar --json diagramma.json --png diagramma.png"""

class CommandLineException(message: String) : Exception(message)

class CommandLineOptions(
    val jsonFile: File? = null,
    val pngFile: File? = null,
    val showHelp: Boolean = false,
)

fun parseCommandLine(args: Array<String>): CommandLineOptions {
    val files = mutableMapOf<String, File>()
    var index = 0
    while (index < args.size) {
        val arg = args[index++]
        val isOption = arg.startsWith("-")
        if (!isOption) throw CommandLineException("argomento non previsto: $arg")
        val option = arg.substringBefore('=')
        when {
            option == "-h" || option == "--help" -> return CommandLineOptions(showHelp = true)
            option != JSON_OPTION && option != PNG_OPTION ->
                throw CommandLineException("opzione sconosciuta: $option")
        }
        val path =
            when {
                '=' in arg -> arg.substringAfter('=')
                else -> args.getOrNull(index)?.takeUnless { it.startsWith("-") }?.also { index++ }
            }
        if (path.isNullOrEmpty()) throw CommandLineException("manca il file dopo $option")
        if (files.put(option, File(path).absoluteFile) != null) {
            throw CommandLineException("l'opzione $option è ripetuta")
        }
    }
    val jsonFile = files[JSON_OPTION]
    val pngFile = files[PNG_OPTION]
    if (jsonFile != null) {
        if (jsonFile.isDirectory) throw CommandLineException("${jsonFile.path} è una cartella")
        if (jsonFile.parentFile?.isDirectory != true) {
            throw CommandLineException("la cartella ${jsonFile.parent} non esiste")
        }
    }
    if (pngFile != null) {
        if (jsonFile == null) throw CommandLineException("$PNG_OPTION richiede anche il file JSON")
        if (!pngFile.extension.equals("png", ignoreCase = true)) {
            throw CommandLineException(
                "il file indicato con $PNG_OPTION deve avere estensione .png"
            )
        }
        if (pngFile.isDirectory) throw CommandLineException("${pngFile.path} è una cartella")
        if (pngFile.parentFile?.isDirectory != true) {
            throw CommandLineException("la cartella ${pngFile.parent} non esiste")
        }
        if (pngFile.exists() && Files.isSameFile(pngFile.toPath(), jsonFile.toPath())) {
            throw CommandLineException("$PNG_OPTION e $JSON_OPTION indicano lo stesso file")
        }
    }
    return CommandLineOptions(jsonFile, pngFile)
}

fun readDiagramType(file: File): DiagramType =
    if (!file.exists()) {
        DiagramType.ER
    } else
        try {
            detectDiagramType(file) ?: DiagramType.ER
        } catch (e: Exception) {
            exitWithError("impossibile aprire ${file.path}: ${e.message}")
        }

fun <T> openStartupDiagram(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    options: CommandLineOptions,
) {
    val file = options.jsonFile ?: return
    if (file.exists()) {
        val diagram =
            repository.loadDiagram(file).getOrElse {
                exitWithError("impossibile aprire ${file.path}: ${it.message}")
            }
        state.loadDiagram(diagram, file.absolutePath)
    } else {
        state.newDiagram()
        state.currentFile = file.absolutePath
        state.isAutoSaveEnabled = true
    }
    options.pngFile?.let {
        state.imageFile = it.absolutePath
        state.isAutoExportEnabled = true
    }
}

fun exitWithError(message: String?, showUsageHint: Boolean = false): Nothing {
    System.err.println("Errore: $message")
    if (showUsageHint) System.err.println("Usa --help per vedere le opzioni disponibili.")
    exitProcess(1)
}
