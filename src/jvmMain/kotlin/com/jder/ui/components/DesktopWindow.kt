package com.jder.ui.components

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.jder.ui.utils.isMacOS

class DesktopWindow(private val windowState: WindowState) : AppWindow {
    override val isFullscreen: Boolean
        get() = windowState.placement == WindowPlacement.Fullscreen

    override fun toggleFullscreen() {
        windowState.placement =
            when {
                !isFullscreen -> WindowPlacement.Fullscreen
                isMacOS -> WindowPlacement.Floating
                else -> WindowPlacement.Maximized
            }
    }
}
