package com.jder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeViewport
import com.jder.domain.model.DiagramType
import com.jder.strings.AppStrings
import com.jder.ui.components.DiagramMenuHost
import com.jder.ui.components.LocalDiagramMenuHost
import com.jder.ui.components.ToastHost
import com.jder.ui.components.WindowMenuBar
import com.jder.ui.components.appMenus
import com.jder.ui.components.diagramTypeKeys
import com.jder.ui.components.logError
import com.jder.ui.dialogs.AboutDialog
import com.jder.ui.screens.AppScreen
import com.jder.ui.screens.DiagramWorkspace
import com.jder.ui.theme.AppFont
import com.jder.ui.theme.JDERTheme
import com.jder.ui.theme.ThemeState
import com.jder.web.BrowserWindow
import com.jder.web.warnBeforeUnload
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val workspace = DiagramWorkspace()
    warnBeforeUnload { workspace.hasUnsavedChanges }
    ComposeViewport("app") {
        var isFontLoaded by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            try {
                AppFont.load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError(AppStrings.FONT_LOAD_FAILED, e)
            }
            isFontLoaded = true
        }
        if (isFontLoaded) {
            WebApp(workspace)
        }
    }
}

@Composable
private fun WebApp(workspace: DiagramWorkspace) {
    val themeState = remember { ThemeState() }
    val menuHost = remember { DiagramMenuHost() }
    val browserWindow = remember { BrowserWindow() }
    var currentType by remember { mutableStateOf<DiagramType?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val menus =
        appMenus(
            commands = menuHost.commands,
            themeState = themeState,
            window = browserWindow,
            currentType = currentType,
            onTypeChange = { currentType = it },
            onAbout = { showAboutDialog = true },
            onExit = null,
            uriHandler = LocalUriHandler.current,
        )
    JDERTheme(darkTheme = themeState.isDarkTheme, colorPalette = themeState.selectedPalette) {
        Column(
            modifier =
                Modifier.fillMaxSize().onPreviewKeyEvent {
                    val typeIndex = diagramTypeKeys.indexOf(it.key)
                    val isShortcut =
                        it.type == KeyEventType.KeyDown &&
                            (it.isCtrlPressed || it.isMetaPressed) &&
                            typeIndex >= 0
                    if (isShortcut) currentType = DiagramType.entries[typeIndex]
                    isShortcut
                },
        ) {
            WindowMenuBar(menus)
            CompositionLocalProvider(LocalDiagramMenuHost provides menuHost) {
                AppScreen(
                    workspace = workspace,
                    themeState = themeState,
                    currentType = currentType,
                    onTypeChange = { currentType = it },
                )
            }
        }
        ToastHost(isDarkTheme = themeState.isDarkTheme)
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
    }
}
