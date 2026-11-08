package com.jder.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import com.jder.ui.strings.Language
import com.jder.ui.strings.UiStrings
import com.jder.ui.theme.ColorPalettes
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.isMacOS
import com.jder.ui.utils.openSafely

private const val GUIDE_URL = "https://jder.it/guida.pdf"
private const val WEBSITE_URL = "https://jder.it/"
private const val ISSUES_URL = "https://github.com/LoryPelli/JDER/issues"
private const val SOURCE_URL = "https://github.com/LoryPelli/JDER"

val diagramTypeKeys =
    listOf(Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight)

class DiagramMenuCommands(
    val state: DiagramEditorState<*>,
    val onNewDiagram: () -> Unit,
    val onOpenDiagram: () -> Unit,
    val onSaveDiagram: () -> Unit,
    val onSaveAsDiagram: () -> Unit,
    val onToggleAutoSave: () -> Unit,
    val onExportPNG: () -> Unit,
    val onToggleAutoExport: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onDeleteSelected: () -> Unit,
    val flushAutoSave: () -> Boolean,
    val tools: () -> List<DiagramTool>,
    val selectionMenu: () -> DiagramSelectionMenu?,
)

class DiagramMenuHost {
    var commands by mutableStateOf<DiagramMenuCommands?>(null)
}

val LocalDiagramMenuHost = staticCompositionLocalOf<DiagramMenuHost?> { null }

class MenuShortcut(
    val key: Key,
    val keyLabel: String,
    val isCommand: Boolean = false,
    val isShift: Boolean = false,
    val isControl: Boolean = false,
    val isAlt: Boolean = false,
)

class AppMenu(val title: String, val entries: () -> List<AppMenuEntry>)

sealed class AppMenuEntry {
    class Action(
        val label: String,
        val shortcut: MenuShortcut? = null,
        val isEnabled: Boolean = true,
        val onClick: () -> Unit,
    ) : AppMenuEntry()

    class Toggle(
        val label: String,
        val isChecked: Boolean,
        val shortcut: MenuShortcut? = null,
        val isEnabled: Boolean = true,
        val onToggle: () -> Unit,
    ) : AppMenuEntry()

    class Choice(
        val label: String,
        val isSelected: Boolean,
        val shortcut: MenuShortcut? = null,
        val onSelect: () -> Unit,
    ) : AppMenuEntry()

    class Submenu(val label: String, val entries: List<AppMenuEntry>) : AppMenuEntry()

    data object Separator : AppMenuEntry()
}

fun appMenus(
    commands: DiagramMenuCommands?,
    themeState: ThemeState,
    windowState: WindowState,
    currentType: DiagramType?,
    onTypeChange: (DiagramType) -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    uriHandler: UriHandler,
): List<AppMenu> = buildList {
    if (commands != null) {
        add(AppMenu(UiStrings.FILE) { fileEntries(commands, onExit) })
        add(AppMenu(UiStrings.EDIT) { editEntries(commands) })
        add(
            AppMenu(UiStrings.VIEW) {
                viewEntries(commands.state, themeState, windowState, currentType, onTypeChange)
            }
        )
        add(AppMenu(UiStrings.TOOLS) { toolEntries(commands) })
        add(AppMenu(UiStrings.ELEMENT) { selectionEntries(commands.selectionMenu()) })
    }
    if (isMacOS) add(AppMenu(UiStrings.WINDOW) { windowEntries(windowState) })
    add(AppMenu(UiStrings.HELP) { helpEntries(uriHandler, onAbout) })
}

fun autoSaveToggle(state: DiagramEditorState<*>, onToggle: () -> Unit) =
    AppMenuEntry.Toggle(
        UiStrings.AUTO_SAVE,
        isChecked = state.isAutoSaveEnabled,
        isEnabled = state.currentFile != null,
        onToggle = onToggle,
    )

fun autoExportToggle(state: DiagramEditorState<*>, onToggle: () -> Unit) =
    AppMenuEntry.Toggle(
        UiStrings.AUTO_EXPORT,
        isChecked = state.isAutoExportEnabled,
        isEnabled = state.imageFile != null,
        onToggle = onToggle,
    )

fun toggleFullscreen(windowState: WindowState) {
    windowState.placement =
        if (windowState.placement == WindowPlacement.Fullscreen) WindowPlacement.Maximized
        else WindowPlacement.Fullscreen
}

private fun commandShortcut(key: Key, keyLabel: String, isShift: Boolean = false) =
    MenuShortcut(key, keyLabel, isCommand = true, isShift = isShift)

private fun fileEntries(commands: DiagramMenuCommands, onExit: () -> Unit): List<AppMenuEntry> =
    buildList {
        add(
            AppMenuEntry.Action(
                UiStrings.NEW_DIAGRAM,
                commandShortcut(Key.N, "N"),
                onClick = commands.onNewDiagram,
            )
        )
        add(
            AppMenuEntry.Action(
                UiStrings.OPEN_DIAGRAM_ELLIPSIS,
                commandShortcut(Key.O, "O"),
                onClick = commands.onOpenDiagram,
            )
        )
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Action(
                UiStrings.SAVE,
                commandShortcut(Key.S, "S"),
                onClick = commands.onSaveDiagram,
            )
        )
        add(
            AppMenuEntry.Action(
                UiStrings.SAVE_AS_ELLIPSIS,
                commandShortcut(Key.S, "S", isShift = true),
                onClick = commands.onSaveAsDiagram,
            )
        )
        add(autoSaveToggle(commands.state, commands.onToggleAutoSave))
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Action(
                UiStrings.EXPORT_PNG_ELLIPSIS,
                commandShortcut(Key.E, "E"),
                isEnabled = !commands.state.isEmpty,
                onClick = commands.onExportPNG,
            )
        )
        add(autoExportToggle(commands.state, commands.onToggleAutoExport))
        if (!isMacOS) {
            add(AppMenuEntry.Separator)
            add(
                AppMenuEntry.Action(
                    UiStrings.EXIT,
                    MenuShortcut(Key.F4, "F4", isAlt = true),
                    onClick = onExit,
                )
            )
        }
    }

private fun editEntries(commands: DiagramMenuCommands): List<AppMenuEntry> {
    val state = commands.state
    return listOf(
        AppMenuEntry.Action(
            UiStrings.UNDO,
            commandShortcut(Key.Z, "Z"),
            isEnabled = state.canUndo(),
            onClick = commands.onUndo,
        ),
        AppMenuEntry.Action(
            UiStrings.REDO,
            if (isMacOS) commandShortcut(Key.Z, "Z", isShift = true)
            else commandShortcut(Key.Y, "Y"),
            isEnabled = state.canRedo(),
            onClick = commands.onRedo,
        ),
        AppMenuEntry.Separator,
        AppMenuEntry.Action(
            UiStrings.DELETE_SELECTED,
            if (isMacOS) MenuShortcut(Key.Backspace, "⌫")
            else MenuShortcut(Key.Delete, UiStrings.DELETE_KEY),
            isEnabled = state.hasSelection,
            onClick = commands.onDeleteSelected,
        ),
    )
}

private fun viewEntries(
    state: DiagramEditorState<*>,
    themeState: ThemeState,
    windowState: WindowState,
    currentType: DiagramType?,
    onTypeChange: (DiagramType) -> Unit,
): List<AppMenuEntry> = buildList {
    add(
        AppMenuEntry.Action(
            UiStrings.ZOOM_IN,
            commandShortcut(Key.Plus, "+"),
            onClick = state::zoomIn,
        )
    )
    add(
        AppMenuEntry.Action(
            UiStrings.ZOOM_OUT,
            commandShortcut(Key.Minus, "-"),
            onClick = state::zoomOut,
        )
    )
    add(
        AppMenuEntry.Action(
            UiStrings.RESET_ZOOM,
            commandShortcut(Key.Zero, "0"),
            onClick = state::resetView,
        )
    )
    add(AppMenuEntry.Separator)
    DiagramType.entries.forEachIndexed { index, type ->
        add(
            AppMenuEntry.Choice(
                type.tabLabel,
                isSelected = type == currentType,
                shortcut = commandShortcut(diagramTypeKeys[index], "${index + 1}"),
                onSelect = { onTypeChange(type) },
            )
        )
    }
    add(AppMenuEntry.Separator)
    add(
        AppMenuEntry.Toggle(UiStrings.DARK_THEME, isChecked = themeState.isDarkTheme) {
            themeState.toggleTheme()
        }
    )
    add(
        AppMenuEntry.Submenu(
            UiStrings.COLOR_PALETTE,
            ColorPalettes.allPalettes.map { palette ->
                AppMenuEntry.Choice(
                    palette.name(),
                    isSelected = palette == themeState.selectedPalette,
                ) {
                    themeState.selectPalette(palette)
                }
            },
        )
    )
    add(
        AppMenuEntry.Submenu(
            UiStrings.LANGUAGE,
            Language.entries.map {
                AppMenuEntry.Choice(it.displayName, isSelected = it == UiStrings.language) {
                    UiStrings.selectLanguage(it)
                }
            },
        )
    )
    if (!isMacOS) {
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Toggle(
                UiStrings.FULL_SCREEN,
                isChecked = windowState.placement == WindowPlacement.Fullscreen,
                shortcut = MenuShortcut(Key.F11, "F11"),
            ) {
                toggleFullscreen(windowState)
            }
        )
    }
}

private fun toolEntries(commands: DiagramMenuCommands): List<AppMenuEntry> {
    val state = commands.state
    return listOf(
        AppMenuEntry.Choice(
            UiStrings.SELECT_AND_MOVE,
            isSelected = state.isSelectTool,
            shortcut = MenuShortcut(Key.Escape, "Esc"),
            onSelect = state::resetToolMode,
        ),
        AppMenuEntry.Separator,
    ) + commands.tools().map { it.toMenuEntry() }
}

private fun DiagramTool.toMenuEntry(): AppMenuEntry =
    when (this) {
        is DiagramTool.Mode -> AppMenuEntry.Choice(menuLabel, isSelected, onSelect = onSelect)
        is DiagramTool.Toggle -> AppMenuEntry.Toggle(menuLabel, isChecked, onToggle = onToggle)
        is DiagramTool.Action -> AppMenuEntry.Action(menuLabel, onClick = onClick)
        is DiagramTool.Group -> AppMenuEntry.Submenu(menuLabel, tools.map { it.toMenuEntry() })
        DiagramTool.Divider -> AppMenuEntry.Separator
    }

private fun selectionEntries(selection: DiagramSelectionMenu?): List<AppMenuEntry> =
    if (selection == null) {
        listOf(AppMenuEntry.Action(UiStrings.NO_ELEMENT_SELECTED, isEnabled = false, onClick = {}))
    } else {
        buildList {
            add(AppMenuEntry.Action(selection.title, isEnabled = false, onClick = {}))
            add(AppMenuEntry.Separator)
            selection.items.forEach { (label, _, enabled, isDestructive, onClick) ->
                if (isDestructive) add(AppMenuEntry.Separator)
                add(AppMenuEntry.Action(label, isEnabled = enabled, onClick = onClick))
            }
        }
    }

private fun windowEntries(windowState: WindowState): List<AppMenuEntry> =
    listOf(
        AppMenuEntry.Action(UiStrings.MINIMIZE, commandShortcut(Key.M, "M")) {
            windowState.isMinimized = true
        },
        AppMenuEntry.Action(UiStrings.ZOOM_WINDOW) {
            windowState.placement =
                if (windowState.placement == WindowPlacement.Maximized) WindowPlacement.Floating
                else WindowPlacement.Maximized
        },
        AppMenuEntry.Separator,
        AppMenuEntry.Toggle(
            UiStrings.FULL_SCREEN,
            isChecked = windowState.placement == WindowPlacement.Fullscreen,
            shortcut = MenuShortcut(Key.F, "F", isCommand = true, isControl = true),
        ) {
            toggleFullscreen(windowState)
        },
    )

private fun helpEntries(uriHandler: UriHandler, onAbout: () -> Unit): List<AppMenuEntry> =
    buildList {
        add(AppMenuEntry.Action(UiStrings.USER_GUIDE) { uriHandler.openSafely(GUIDE_URL) })
        add(AppMenuEntry.Action(UiStrings.WEBSITE) { uriHandler.openSafely(WEBSITE_URL) })
        add(AppMenuEntry.Separator)
        add(AppMenuEntry.Action(UiStrings.REPORT_ISSUE) { uriHandler.openSafely(ISSUES_URL) })
        add(AppMenuEntry.Action(UiStrings.SOURCE_CODE) { uriHandler.openSafely(SOURCE_URL) })
        if (!isMacOS) {
            add(AppMenuEntry.Separator)
            add(AppMenuEntry.Action(UiStrings.ABOUT, onClick = onAbout))
        }
    }
