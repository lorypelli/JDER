package com.jder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jder.data.UpdateChecker
import com.jder.domain.model.DiagramType
import com.jder.strings.CommandLineStrings
import com.jder.strings.CommonStrings
import com.jder.strings.FileStrings
import com.jder.ui.components.AppMenuBar
import com.jder.ui.components.DesktopWindow
import com.jder.ui.components.DiagramMenuHost
import com.jder.ui.components.DockMenu
import com.jder.ui.components.LocalDiagramMenuHost
import com.jder.ui.components.ToastHost
import com.jder.ui.components.Toasts
import com.jder.ui.components.WindowMenuBar
import com.jder.ui.components.appMenus
import com.jder.ui.components.diagramTypeKeys
import com.jder.ui.components.lastUsedDirectory
import com.jder.ui.dialogs.AboutDialog
import com.jder.ui.dialogs.UpdateAvailableDialog
import com.jder.ui.screens.AppScreen
import com.jder.ui.screens.DiagramWorkspace
import com.jder.ui.theme.AppFont
import com.jder.ui.theme.JDERTheme
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.bringToFront
import com.jder.ui.utils.isMacOS
import com.jder.ui.utils.resourcePainter
import com.jder.ui.utils.setDockIcon
import com.jder.ui.utils.usesSystemMenuBar
import java.awt.Dimension
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    if (options.showHelp) {
        println(CommandLineStrings.COMMAND_LINE_USAGE)
        return
    }
    runBlocking { AppFont.load() }
    options.warnings.forEach(Toasts::warning)
    val workspace = DiagramWorkspace()
    fun openStartupDiagramAs(type: DiagramType, startupOptions: CommandLineOptions) =
        when (type) {
            DiagramType.ER ->
                openStartupDiagram(workspace.erState, workspace.erRepository, startupOptions)
            DiagramType.RELATIONAL ->
                openStartupDiagram(
                    workspace.relationalState,
                    workspace.relationalRepository,
                    startupOptions,
                )
            DiagramType.CLASS ->
                openStartupDiagram(workspace.classState, workspace.classRepository, startupOptions)
            DiagramType.USE_CASE ->
                openStartupDiagram(
                    workspace.useCaseState,
                    workspace.useCaseRepository,
                    startupOptions,
                )
            DiagramType.SEQUENCE ->
                openStartupDiagram(
                    workspace.sequenceState,
                    workspace.sequenceRepository,
                    startupOptions,
                )
            DiagramType.LOGIC ->
                openStartupDiagram(workspace.logicState, workspace.logicRepository, startupOptions)
            DiagramType.MEALY ->
                openStartupDiagram(workspace.mealyState, workspace.mealyRepository, startupOptions)
            DiagramType.MOORE ->
                openStartupDiagram(workspace.mooreState, workspace.mooreRepository, startupOptions)
        }
    val startup = prepareStartupDiagram(options, ::openStartupDiagramAs)
    (options.jsonFile ?: options.pngFile)?.parentFile?.let { lastUsedDirectory = it }
    System.setProperty("apple.awt.application.name", "JDER")
    setDockIcon("jder_icon.png")
    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
        val desktopWindow = remember(windowState) { DesktopWindow(windowState) }
        val themeState = remember { ThemeState() }
        val menuHost = remember { DiagramMenuHost() }
        var currentType by remember { mutableStateOf(startup.type) }
        var pendingNewDiagram by remember { mutableStateOf<DiagramType?>(null) }
        var showExitDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var shouldExit by remember { mutableStateOf(false) }
        var updateUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            updateUrl = UpdateChecker.findUpdate().getOrNull()
        }
        fun selectType(type: DiagramType) {
            if (currentType == null) startup.pendingOptions?.let { openStartupDiagramAs(type, it) }
            currentType = type
        }
        fun requestExit() {
            val isFlushed = menuHost.commands?.flushAutoSave?.invoke() != false
            if (!isFlushed || workspace.hasUnsavedChanges) {
                showExitDialog = true
            } else {
                exitApplication()
            }
        }
        if (shouldExit) {
            exitApplication()
        }
        Window(
            onCloseRequest = { requestExit() },
            title = "JDER - Java Diagrammi E/R",
            state = windowState,
            icon = remember { resourcePainter("jder_icon.png") },
        ) {
            window.minimumSize = Dimension(800, 600)
            val isModified = workspace.hasUnsavedChanges
            LaunchedEffect(isModified) {
                window.rootPane.putClientProperty("Window.documentModified", isModified)
            }
            LaunchedEffect(showExitDialog) { if (showExitDialog) window.bringToFront() }
            LaunchedEffect(pendingNewDiagram, menuHost.commands) {
                val type = pendingNewDiagram
                val commands = menuHost.commands
                if (type != null && commands != null && commands.state === workspace.state(type)) {
                    pendingNewDiagram = null
                    commands.onNewDiagram()
                }
            }
            val menus =
                appMenus(
                    commands = menuHost.commands,
                    themeState = themeState,
                    window = desktopWindow,
                    currentType = currentType,
                    onTypeChange = { selectType(it) },
                    onAbout = { showAboutDialog = true },
                    onExit = { requestExit() },
                    uriHandler = LocalUriHandler.current,
                    windowMenu = if (isMacOS) desktopWindow.windowMenu() else null,
                )
            if (usesSystemMenuBar) {
                AppMenuBar(menus = menus, onAbout = { showAboutDialog = true })
            }
            if (isMacOS) {
                DockMenu(
                    onNewDiagram = {
                        if (currentType != null) pendingNewDiagram = it
                        selectType(it)
                    },
                )
            }
            JDERTheme(
                darkTheme = themeState.isDarkTheme,
                colorPalette = themeState.selectedPalette,
            ) {
                Column(
                    modifier =
                        Modifier.fillMaxSize().onPreviewKeyEvent {
                            val typeIndex = diagramTypeKeys.indexOf(it.key)
                            when {
                                it.type != KeyEventType.KeyDown -> false
                                (it.isCtrlPressed || it.isMetaPressed) && typeIndex >= 0 -> {
                                    selectType(DiagramType.entries[typeIndex])
                                    true
                                }
                                !isMacOS && it.key == Key.F11 -> {
                                    desktopWindow.toggleFullscreen()
                                    true
                                }
                                else -> false
                            }
                        },
                ) {
                    if (!usesSystemMenuBar) {
                        WindowMenuBar(menus)
                    }
                    CompositionLocalProvider(LocalDiagramMenuHost provides menuHost) {
                        AppScreen(
                            workspace = workspace,
                            themeState = themeState,
                            currentType = currentType,
                            onTypeChange = { selectType(it) },
                        )
                    }
                }
                ToastHost(isDarkTheme = themeState.isDarkTheme)
                updateUrl?.let {
                    UpdateAvailableDialog(downloadUrl = it, onDismiss = { updateUrl = null })
                }
                if (showAboutDialog) {
                    AboutDialog(onDismiss = { showAboutDialog = false })
                }
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text(FileStrings.CONFIRM_EXIT_TITLE) },
                        text = { Text(FileStrings.CONFIRM_EXIT_MESSAGE) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showExitDialog = false
                                    shouldExit = true
                                },
                            ) {
                                Text(CommonStrings.YES)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) {
                                Text(CommonStrings.NO)
                            }
                        },
                    )
                }
            }
        }
    }
}
