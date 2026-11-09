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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jder.data.ClassDiagramRepository
import com.jder.data.ERDiagramRepository
import com.jder.data.LogicDiagramRepository
import com.jder.data.RelationalDiagramRepository
import com.jder.data.SequenceDiagramRepository
import com.jder.data.StateMachineDiagramRepository
import com.jder.data.UpdateChecker
import com.jder.data.UseCaseDiagramRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import com.jder.domain.model.ERDiagramState
import com.jder.domain.model.LogicDiagramState
import com.jder.domain.model.RelationalDiagramState
import com.jder.domain.model.SequenceDiagramState
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.UseCaseDiagramState
import com.jder.strings.AppStrings
import com.jder.strings.CommandLineStrings
import com.jder.strings.CommonStrings
import com.jder.strings.FileStrings
import com.jder.ui.components.AppMenuBar
import com.jder.ui.components.DiagramMenuHost
import com.jder.ui.components.DockMenu
import com.jder.ui.components.LocalDiagramMenuHost
import com.jder.ui.components.ToastHost
import com.jder.ui.components.Toasts
import com.jder.ui.components.WindowMenuBar
import com.jder.ui.components.appMenus
import com.jder.ui.components.diagramTypeKeys
import com.jder.ui.components.lastUsedDirectory
import com.jder.ui.components.logError
import com.jder.ui.components.toggleFullscreen
import com.jder.ui.dialogs.AboutDialog
import com.jder.ui.dialogs.UpdateAvailableDialog
import com.jder.ui.screens.AppScreen
import com.jder.ui.theme.JDERTheme
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.bringToFront
import com.jder.ui.utils.isMacOS
import com.jder.ui.utils.setDockIcon
import java.awt.Dimension

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    if (options.showHelp) {
        println(CommandLineStrings.COMMAND_LINE_USAGE)
        return
    }
    options.warnings.forEach(Toasts::warning)
    val erState = ERDiagramState()
    val relationalState = RelationalDiagramState()
    val useCaseState = UseCaseDiagramState()
    val sequenceState = SequenceDiagramState()
    val classState = ClassDiagramState()
    val logicState = LogicDiagramState()
    val mealyState = StateMachineDiagramState(StateMachineKind.MEALY)
    val mooreState = StateMachineDiagramState(StateMachineKind.MOORE)
    val erRepository = ERDiagramRepository()
    val relationalRepository = RelationalDiagramRepository()
    val useCaseRepository = UseCaseDiagramRepository()
    val sequenceRepository = SequenceDiagramRepository()
    val classRepository = ClassDiagramRepository()
    val logicRepository = LogicDiagramRepository()
    val mealyRepository = StateMachineDiagramRepository(StateMachineKind.MEALY)
    val mooreRepository = StateMachineDiagramRepository(StateMachineKind.MOORE)
    fun openStartupDiagramAs(type: DiagramType, startupOptions: CommandLineOptions) =
        when (type) {
            DiagramType.ER -> openStartupDiagram(erState, erRepository, startupOptions)
            DiagramType.RELATIONAL ->
                openStartupDiagram(relationalState, relationalRepository, startupOptions)
            DiagramType.CLASS -> openStartupDiagram(classState, classRepository, startupOptions)
            DiagramType.USE_CASE ->
                openStartupDiagram(useCaseState, useCaseRepository, startupOptions)
            DiagramType.SEQUENCE ->
                openStartupDiagram(sequenceState, sequenceRepository, startupOptions)
            DiagramType.LOGIC -> openStartupDiagram(logicState, logicRepository, startupOptions)
            DiagramType.MEALY -> openStartupDiagram(mealyState, mealyRepository, startupOptions)
            DiagramType.MOORE -> openStartupDiagram(mooreState, mooreRepository, startupOptions)
        }
    val startup = prepareStartupDiagram(options, ::openStartupDiagramAs)
    (options.jsonFile ?: options.pngFile)?.parentFile?.let { lastUsedDirectory = it }
    System.setProperty("apple.awt.application.name", "JDER")
    setDockIcon("jder_icon.png")
    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
        val themeState = remember { ThemeState() }
        val menuHost = remember { DiagramMenuHost() }
        var currentType by remember { mutableStateOf(startup.type) }
        var pendingNewDiagram by remember { mutableStateOf<DiagramType?>(null) }
        var showExitDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var shouldExit by remember { mutableStateOf(false) }
        var updateUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            updateUrl =
                UpdateChecker.findUpdate()
                    .onFailure { logError(AppStrings.UPDATE_CHECK_FAILED, it) }
                    .getOrNull()
        }
        fun hasUnsavedChanges() =
            erState.isModified ||
                relationalState.isModified ||
                useCaseState.isModified ||
                sequenceState.isModified ||
                classState.isModified ||
                logicState.isModified ||
                mealyState.isModified ||
                mooreState.isModified
        fun diagramState(type: DiagramType): DiagramEditorState<*> =
            when (type) {
                DiagramType.ER -> erState
                DiagramType.RELATIONAL -> relationalState
                DiagramType.CLASS -> classState
                DiagramType.USE_CASE -> useCaseState
                DiagramType.SEQUENCE -> sequenceState
                DiagramType.LOGIC -> logicState
                DiagramType.MEALY -> mealyState
                DiagramType.MOORE -> mooreState
            }
        fun selectType(type: DiagramType) {
            if (currentType == null) startup.pendingOptions?.let { openStartupDiagramAs(type, it) }
            currentType = type
        }
        fun requestExit() {
            val isFlushed = menuHost.commands?.flushAutoSave?.invoke() != false
            if (!isFlushed || hasUnsavedChanges()) {
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
            icon = painterResource("jder_icon.png"),
        ) {
            window.minimumSize = Dimension(800, 600)
            val isModified = hasUnsavedChanges()
            LaunchedEffect(isModified) {
                window.rootPane.putClientProperty("Window.documentModified", isModified)
            }
            LaunchedEffect(showExitDialog) { if (showExitDialog) window.bringToFront() }
            LaunchedEffect(pendingNewDiagram, menuHost.commands) {
                val type = pendingNewDiagram
                val commands = menuHost.commands
                if (type != null && commands != null && commands.state === diagramState(type)) {
                    pendingNewDiagram = null
                    commands.onNewDiagram()
                }
            }
            val menus =
                appMenus(
                    commands = menuHost.commands,
                    themeState = themeState,
                    windowState = windowState,
                    currentType = currentType,
                    onTypeChange = { selectType(it) },
                    onAbout = { showAboutDialog = true },
                    onExit = { requestExit() },
                    uriHandler = LocalUriHandler.current,
                )
            if (isMacOS) {
                AppMenuBar(menus = menus, onAbout = { showAboutDialog = true })
                DockMenu(
                    onNewDiagram = {
                        if (currentType != null) pendingNewDiagram = it
                        selectType(it)
                    }
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
                                    toggleFullscreen(windowState)
                                    true
                                }
                                else -> false
                            }
                        }
                ) {
                    if (!isMacOS) {
                        WindowMenuBar(menus)
                    }
                    CompositionLocalProvider(LocalDiagramMenuHost provides menuHost) {
                        AppScreen(
                            erState = erState,
                            relationalState = relationalState,
                            useCaseState = useCaseState,
                            sequenceState = sequenceState,
                            classState = classState,
                            logicState = logicState,
                            mealyState = mealyState,
                            mooreState = mooreState,
                            erRepository = erRepository,
                            relationalRepository = relationalRepository,
                            useCaseRepository = useCaseRepository,
                            sequenceRepository = sequenceRepository,
                            classRepository = classRepository,
                            logicRepository = logicRepository,
                            mealyRepository = mealyRepository,
                            mooreRepository = mooreRepository,
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
                                }
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
