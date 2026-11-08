package com.jder

import com.jder.data.DiagramFileRepository
import com.jder.data.detectDiagramType
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import com.jder.ui.components.Toasts
import com.jder.ui.strings.UiStrings
import java.io.File

private const val JSON_OPTION = "--json"
private const val PNG_OPTION = "--png"

class CommandLineOptions(
    val jsonFile: File? = null,
    val pngFile: File? = null,
    val showHelp: Boolean = false,
    val warnings: List<String> = emptyList(),
)

class StartupDiagram(val type: DiagramType?, val pendingOptions: CommandLineOptions?)

fun parseCommandLine(args: Array<String>): CommandLineOptions {
    val files = mutableMapOf<String, File>()
    val warnings = mutableListOf<String>()
    var index = 0
    while (index < args.size) {
        val arg = args[index++]
        val isOption = arg.startsWith("-")
        if (!isOption) {
            warnings += UiStrings.unexpectedArgument(arg)
            continue
        }
        val option = arg.substringBefore('=')
        when {
            option == "-h" || option == "--help" -> return CommandLineOptions(showHelp = true)
            option != JSON_OPTION && option != PNG_OPTION -> {
                warnings += UiStrings.unknownOption(option)
                continue
            }
        }
        val path =
            when {
                '=' in arg -> arg.substringAfter('=')
                else -> args.getOrNull(index)?.takeUnless { it.startsWith("-") }?.also { index++ }
            }
        when {
            path.isNullOrEmpty() -> warnings += UiStrings.missingFileAfter(option)
            option in files -> warnings += UiStrings.repeatedOption(option)
            else -> files[option] = File(path).absoluteFile
        }
    }
    val jsonFile = files[JSON_OPTION]?.withExtension(".json")?.takeIfUsable(JSON_OPTION, warnings)
    val pngFile = files[PNG_OPTION]?.withExtension(".png")?.takeIfUsable(PNG_OPTION, warnings)
    return CommandLineOptions(jsonFile, pngFile, warnings = warnings)
}

private fun File.withExtension(extension: String): File =
    if (name.endsWith(extension, ignoreCase = true)) this else File(path + extension)

private fun File.takeIfUsable(option: String, warnings: MutableList<String>): File? {
    val problem =
        when {
            isDirectory -> UiStrings.isFolder(path)
            parentFile?.isDirectory != true -> UiStrings.missingFolder(parent)
            else -> return this
        }
    warnings += UiStrings.optionIgnored(option, problem)
    return null
}

fun prepareStartupDiagram(
    options: CommandLineOptions,
    openAs: (DiagramType, CommandLineOptions) -> Boolean,
): StartupDiagram {
    val jsonFile = options.jsonFile ?: return StartupDiagram(null, options)
    val type =
        readDiagramType(jsonFile).getOrElse {
            warnCannotOpen(jsonFile, it, options)
            return StartupDiagram(null, null)
        }
    return when {
        type == null -> StartupDiagram(null, options)
        openAs(type, options) -> StartupDiagram(type, null)
        else -> StartupDiagram(null, null)
    }
}

fun readDiagramType(file: File): Result<DiagramType?> =
    if (!file.exists()) Result.success(null) else runCatching { detectDiagramType(file) }

fun <T> openStartupDiagram(
    state: DiagramEditorState<T>,
    repository: DiagramFileRepository<T>,
    options: CommandLineOptions,
): Boolean {
    options.jsonFile?.let { file ->
        if (file.exists()) {
            val diagram =
                repository.loadDiagram(file).getOrElse {
                    warnCannotOpen(file, it, options)
                    return false
                }
            state.loadDiagram(diagram, file.absolutePath)
        } else {
            state.newDiagram()
            state.currentFile = file.absolutePath
            state.isAutoSaveEnabled = true
        }
    }
    options.pngFile?.let {
        state.imageFile = it.absolutePath
        state.isAutoExportEnabled = true
    }
    return true
}

private fun warnCannotOpen(file: File, cause: Throwable, options: CommandLineOptions) {
    Toasts.warning(UiStrings.cannotOpen(file.name), cause)
    if (options.pngFile != null) {
        Toasts.warning(UiStrings.optionIgnored(PNG_OPTION, UiStrings.JSON_CANNOT_BE_OPENED))
    }
}
