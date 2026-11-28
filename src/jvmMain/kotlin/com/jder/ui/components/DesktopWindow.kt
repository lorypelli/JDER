package com.jder.ui.components

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

class DesktopWindow(private val windowState: WindowState) : AppWindow {
    override val isFullscreen: Boolean
        get() = windowState.placement == WindowPlacement.Fullscreen

    override fun toggleFullscreen() {
        windowState.placement =
            if (isFullscreen) WindowPlacement.Maximized else WindowPlacement.Fullscreen
    }
}
