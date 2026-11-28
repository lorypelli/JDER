package com.jder.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.UriHandler
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import com.jder.strings.CommonStrings
import com.jder.strings.Language
import com.jder.strings.Localization
import com.jder.strings.MenuStrings
import com.jder.ui.theme.ColorPalettes
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.isMacOS
import com.jder.ui.utils.openSafely
import com.jder.ui.utils.sharedValue
import com.jder.ui.utils.usesSystemMenuBar

private const val GUIDE_URL = "https://jder.it/guida.pdf"
private const val WEBSITE_URL = "https://jder.it/"
private val repositoryUrl = sharedValue("repository")
private val supportUrl = "mailto:${sharedValue("supportEmail")}"

val diagramTypeKeys =
    listOf(Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight)

class DiagramMenuCommands(
    val state: DiagramEditorState<*>,
    val onNewDiagram: () -> Unit,
    val onOpenDiagram: () -> Unit,
    val onSaveDiagram: () -> Unit,
    val onSaveAsDiagram: () -> Unit,
    val onToggleAutoSave: (() -> Unit)?,
    val onExportPNG: () -> Unit,
    val onToggleAutoExport: (() -> Unit)?,
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

interface AppWindow {
    val isFullscreen: Boolean

    fun toggleFullscreen()
}

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
    open val icon: ImageVector? = null

    class Action(
        val label: String,
        val shortcut: MenuShortcut? = null,
        val isEnabled: Boolean = true,
        override val icon: ImageVector? = null,
        val onClick: () -> Unit,
    ) : AppMenuEntry()

    class Toggle(
        val label: String,
        val isChecked: Boolean,
        val shortcut: MenuShortcut? = null,
        val isEnabled: Boolean = true,
        override val icon: ImageVector? = null,
        val onToggle: () -> Unit,
    ) : AppMenuEntry()

    class Choice(
        val label: String,
        val isSelected: Boolean,
        val shortcut: MenuShortcut? = null,
        override val icon: ImageVector? = null,
        val onSelect: () -> Unit,
    ) : AppMenuEntry()

    class Submenu(
        val label: String,
        val entries: List<AppMenuEntry>,
        override val icon: ImageVector? = null,
    ) : AppMenuEntry()

    data object Separator : AppMenuEntry()
}

fun appMenus(
    commands: DiagramMenuCommands?,
    themeState: ThemeState,
    window: AppWindow,
    currentType: DiagramType?,
    onTypeChange: (DiagramType) -> Unit,
    onAbout: () -> Unit,
    onExit: (() -> Unit)?,
    uriHandler: UriHandler,
): List<AppMenu> = buildList {
    if (commands != null) {
        add(AppMenu(MenuStrings.FILE) { fileEntries(commands, onExit) })
        add(AppMenu(MenuStrings.EDIT) { editEntries(commands) })
        add(
            AppMenu(MenuStrings.VIEW) {
                viewEntries(commands.state, themeState, window, currentType, onTypeChange)
            },
        )
        add(AppMenu(MenuStrings.TOOLS) { toolEntries(commands) })
        add(AppMenu(MenuStrings.ELEMENT) { selectionEntries(commands.selectionMenu()) })
    }
    add(AppMenu(MenuStrings.HELP) { helpEntries(uriHandler, onAbout) })
}

fun autoSaveToggle(state: DiagramEditorState<*>, onToggle: () -> Unit) =
    AppMenuEntry.Toggle(
        MenuStrings.AUTO_SAVE,
        isChecked = state.isAutoSaveEnabled,
        isEnabled = state.currentFile != null,
        onToggle = onToggle,
    )

fun autoExportToggle(state: DiagramEditorState<*>, onToggle: () -> Unit) =
    AppMenuEntry.Toggle(
        MenuStrings.AUTO_EXPORT,
        isChecked = state.isAutoExportEnabled,
        isEnabled = state.imageFile != null,
        onToggle = onToggle,
    )

internal fun commandShortcut(key: Key, keyLabel: String, isShift: Boolean = false) =
    MenuShortcut(key, keyLabel, isCommand = true, isShift = isShift)

private fun fileEntries(commands: DiagramMenuCommands, onExit: (() -> Unit)?): List<AppMenuEntry> =
    buildList {
        add(
            AppMenuEntry.Action(
                MenuStrings.NEW_DIAGRAM,
                commandShortcut(Key.N, "N"),
                icon = Icons.Default.Add,
                onClick = commands.onNewDiagram,
            ),
        )
        add(
            AppMenuEntry.Action(
                MenuStrings.OPEN_DIAGRAM_ELLIPSIS,
                commandShortcut(Key.O, "O"),
                icon = Icons.Default.FolderOpen,
                onClick = commands.onOpenDiagram,
            ),
        )
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Action(
                CommonStrings.SAVE,
                commandShortcut(Key.S, "S"),
                icon = Icons.Default.Save,
                onClick = commands.onSaveDiagram,
            ),
        )
        add(
            AppMenuEntry.Action(
                MenuStrings.SAVE_AS_ELLIPSIS,
                commandShortcut(Key.S, "S", isShift = true),
                icon = Icons.Default.Save,
                onClick = commands.onSaveAsDiagram,
            ),
        )
        commands.onToggleAutoSave?.let { add(autoSaveToggle(commands.state, it)) }
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Action(
                MenuStrings.EXPORT_PNG_ELLIPSIS,
                commandShortcut(Key.E, "E"),
                isEnabled = !commands.state.isEmpty,
                icon = Icons.Default.Image,
                onClick = commands.onExportPNG,
            ),
        )
        commands.onToggleAutoExport?.let { add(autoExportToggle(commands.state, it)) }
        if (onExit != null && !usesSystemMenuBar) {
            add(AppMenuEntry.Separator)
            add(
                AppMenuEntry.Action(
                    MenuStrings.EXIT,
                    MenuShortcut(Key.F4, "F4", isAlt = true),
                    icon = Icons.AutoMirrored.Default.ExitToApp,
                    onClick = onExit,
                ),
            )
        }
    }

private fun editEntries(commands: DiagramMenuCommands): List<AppMenuEntry> {
    val state = commands.state
    return listOf(
        AppMenuEntry.Action(
            MenuStrings.UNDO,
            commandShortcut(Key.Z, "Z"),
            isEnabled = state.canUndo(),
            icon = Icons.AutoMirrored.Default.Undo,
            onClick = commands.onUndo,
        ),
        AppMenuEntry.Action(
            MenuStrings.REDO,
            if (isMacOS) commandShortcut(Key.Z, "Z", isShift = true)
            else commandShortcut(Key.Y, "Y"),
            isEnabled = state.canRedo(),
            icon = Icons.AutoMirrored.Default.Redo,
            onClick = commands.onRedo,
        ),
        AppMenuEntry.Separator,
        AppMenuEntry.Action(
            MenuStrings.DELETE_SELECTED,
            if (isMacOS) MenuShortcut(Key.Backspace, "⌫")
            else MenuShortcut(Key.Delete, MenuStrings.DELETE_KEY),
            isEnabled = state.hasSelection,
            icon = Icons.Default.Delete,
            onClick = commands.onDeleteSelected,
        ),
    )
}

private fun viewEntries(
    state: DiagramEditorState<*>,
    themeState: ThemeState,
    window: AppWindow,
    currentType: DiagramType?,
    onTypeChange: (DiagramType) -> Unit,
): List<AppMenuEntry> = buildList {
    add(
        AppMenuEntry.Action(
            MenuStrings.ZOOM_IN,
            commandShortcut(Key.Plus, "+"),
            icon = Icons.Default.ZoomIn,
            onClick = state::zoomIn,
        ),
    )
    add(
        AppMenuEntry.Action(
            MenuStrings.ZOOM_OUT,
            commandShortcut(Key.Minus, "-"),
            icon = Icons.Default.ZoomOut,
            onClick = state::zoomOut,
        ),
    )
    add(
        AppMenuEntry.Action(
            MenuStrings.RESET_ZOOM,
            commandShortcut(Key.Zero, "0"),
            icon = Icons.Default.CenterFocusWeak,
            onClick = state::resetView,
        ),
    )
    add(AppMenuEntry.Separator)
    DiagramType.entries.forEachIndexed { index, type ->
        add(
            AppMenuEntry.Choice(
                type.tabLabel,
                isSelected = type == currentType,
                shortcut = commandShortcut(diagramTypeKeys[index], "${index + 1}"),
                onSelect = { onTypeChange(type) },
            ),
        )
    }
    add(AppMenuEntry.Separator)
    add(
        AppMenuEntry.Toggle(MenuStrings.DARK_THEME, isChecked = themeState.isDarkTheme) {
            themeState.toggleTheme()
        },
    )
    add(
        AppMenuEntry.Submenu(
            MenuStrings.COLOR_PALETTE,
            ColorPalettes.allPalettes.map { palette ->
                AppMenuEntry.Choice(
                    palette.name(),
                    isSelected = palette == themeState.selectedPalette,
                ) {
                    themeState.selectPalette(palette)
                }
            },
            Icons.Default.Palette,
        ),
    )
    add(
        AppMenuEntry.Submenu(
            MenuStrings.LANGUAGE,
            Language.entries.map {
                AppMenuEntry.Choice(it.displayName, isSelected = it == Localization.language) {
                    Localization.selectLanguage(it)
                }
            },
            Icons.Default.Language,
        ),
    )
    add(AppMenuEntry.Separator)
    add(
        AppMenuEntry.Toggle(
            MenuStrings.FULL_SCREEN,
            isChecked = window.isFullscreen,
            shortcut =
                if (usesSystemMenuBar) {
                    MenuShortcut(Key.F, "F", isCommand = true, isControl = true)
                } else {
                    MenuShortcut(Key.F11, "F11")
                },
            onToggle = window::toggleFullscreen,
        ),
    )
}

private fun toolEntries(commands: DiagramMenuCommands): List<AppMenuEntry> {
    val state = commands.state
    return listOf(
        AppMenuEntry.Choice(
            MenuStrings.SELECT_AND_MOVE,
            isSelected = state.isSelectTool,
            shortcut = MenuShortcut(Key.Escape, "Esc"),
            icon = Icons.Default.NearMe,
            onSelect = state::resetToolMode,
        ),
        AppMenuEntry.Separator,
    ) + commands.tools().map { it.toMenuEntry() }
}

private fun DiagramTool.toMenuEntry(): AppMenuEntry =
    when (this) {
        is DiagramTool.Mode ->
            AppMenuEntry.Choice(menuLabel, isSelected, icon = icon, onSelect = onSelect)
        is DiagramTool.Toggle ->
            AppMenuEntry.Toggle(menuLabel, isChecked, icon = icon, onToggle = onToggle)
        is DiagramTool.Action -> AppMenuEntry.Action(menuLabel, icon = icon, onClick = onClick)
        is DiagramTool.Group -> AppMenuEntry.Submenu(menuLabel, tools.map { it.toMenuEntry() })
        DiagramTool.Divider -> AppMenuEntry.Separator
    }

private fun selectionEntries(selection: DiagramSelectionMenu?): List<AppMenuEntry> =
    if (selection == null) {
        listOf(
            AppMenuEntry.Action(MenuStrings.NO_ELEMENT_SELECTED, isEnabled = false, onClick = {}),
        )
    } else {
        buildList {
            add(AppMenuEntry.Action(selection.title, isEnabled = false, onClick = {}))
            add(AppMenuEntry.Separator)
            selection.items.forEach { (label, icon, enabled, isDestructive, onClick) ->
                if (isDestructive) add(AppMenuEntry.Separator)
                add(AppMenuEntry.Action(label, isEnabled = enabled, icon = icon, onClick = onClick))
            }
        }
    }

private fun helpEntries(uriHandler: UriHandler, onAbout: () -> Unit): List<AppMenuEntry> =
    buildList {
        add(
            AppMenuEntry.Action(
                MenuStrings.USER_GUIDE,
                icon = Icons.AutoMirrored.Default.MenuBook,
            ) {
                uriHandler.openSafely(GUIDE_URL)
            },
        )
        add(
            AppMenuEntry.Action(MenuStrings.WEBSITE, icon = Icons.Default.Public) {
                uriHandler.openSafely(WEBSITE_URL)
            },
        )
        add(AppMenuEntry.Separator)
        add(
            AppMenuEntry.Action(MenuStrings.REPORT_ISSUE, icon = Icons.Default.BugReport) {
                uriHandler.openSafely("$repositoryUrl/issues")
            },
        )
        add(
            AppMenuEntry.Action(MenuStrings.CONTACT_SUPPORT, icon = Icons.Default.Email) {
                uriHandler.openSafely(supportUrl)
            },
        )
        add(
            AppMenuEntry.Action(MenuStrings.SOURCE_CODE, icon = Icons.Default.Code) {
                uriHandler.openSafely(repositoryUrl)
            },
        )
        if (!usesSystemMenuBar) {
            add(AppMenuEntry.Separator)
            add(
                AppMenuEntry.Action(
                    MenuStrings.ABOUT,
                    icon = Icons.Default.Info,
                    onClick = onAbout,
                ),
            )
        }
    }
