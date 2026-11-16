package com.jder.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.jder.strings.MenuStrings

class DesktopWindow(private val windowState: WindowState) : AppWindow {
    override val isFullscreen: Boolean
        get() = windowState.placement == WindowPlacement.Fullscreen

    override fun toggleFullscreen() {
        windowState.placement =
            if (isFullscreen) WindowPlacement.Maximized else WindowPlacement.Fullscreen
    }

    fun windowMenu(): AppMenu =
        AppMenu(MenuStrings.WINDOW) {
            listOf(
                AppMenuEntry.Action(
                    MenuStrings.MINIMIZE,
                    commandShortcut(Key.M, "M"),
                    icon = Icons.Default.Minimize,
                ) {
                    windowState.isMinimized = true
                },
                AppMenuEntry.Action(MenuStrings.ZOOM_WINDOW, icon = Icons.Default.OpenInFull) {
                    windowState.placement =
                        if (windowState.placement == WindowPlacement.Maximized) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Maximized
                        }
                },
                AppMenuEntry.Separator,
                AppMenuEntry.Toggle(
                    MenuStrings.FULL_SCREEN,
                    isChecked = isFullscreen,
                    shortcut = MenuShortcut(Key.F, "F", isCommand = true, isControl = true),
                    onToggle = ::toggleFullscreen,
                ),
            )
        }
}
