package com.jder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.FrameWindowScope
import com.jder.domain.model.DiagramType
import java.awt.Desktop
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.Taskbar

@Composable
fun FrameWindowScope.DockMenu(onNewDiagram: (DiagramType) -> Unit) {
    val currentOnNewDiagram by rememberUpdatedState(onNewDiagram)
    DisposableEffect(Unit) {
        val isSupported =
            Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.MENU)
        val desktop = Desktop.getDesktop()
        val canRequestForeground = desktop.isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)
        fun showWindow() {
            window.isMinimized = false
            if (canRequestForeground) desktop.requestForeground(false)
        }
        if (isSupported) {
            Taskbar.getTaskbar().menu =
                PopupMenu().apply {
                    DiagramType.entries.forEach { type ->
                        add(
                            MenuItem(type.newDiagramLabel).apply {
                                addActionListener {
                                    showWindow()
                                    currentOnNewDiagram(type)
                                }
                            }
                        )
                    }
                }
        }
        onDispose { if (isSupported) Taskbar.getTaskbar().menu = null }
    }
}

private val DiagramType.newDiagramLabel: String
    get() =
        when (this) {
            DiagramType.ER -> "Nuovo Diagramma E/R"
            DiagramType.CLASS_DIAGRAM -> "Nuovo Diagramma delle Classi"
            DiagramType.USE_CASE -> "Nuovo Diagramma dei Casi d'Uso"
            DiagramType.LOGIC -> "Nuovo Diagramma Logico"
        }
