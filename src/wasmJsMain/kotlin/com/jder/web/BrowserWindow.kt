package com.jder.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jder.ui.components.AppWindow

class BrowserWindow : AppWindow {
    private var fullscreen by mutableStateOf(isDocumentFullscreen())

    init {
        onFullscreenChange { fullscreen = isDocumentFullscreen() }
    }

    override val isFullscreen: Boolean
        get() = fullscreen

    override fun toggleFullscreen() = toggleDocumentFullscreen()
}
