package com.jder.ui.components

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
                AppMenuEntry.Action(MenuStrings.MINIMIZE, commandShortcut(Key.M, "M")) {
                    windowState.isMinimized = true
                },
                AppMenuEntry.Action(MenuStrings.ZOOM_WINDOW) {
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
