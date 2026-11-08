package com.jder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.FrameWindowScope
import com.jder.domain.model.DiagramType
import com.jder.ui.strings.UiStrings
import java.awt.Desktop
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.Taskbar

@Composable
fun FrameWindowScope.DockMenu(onNewDiagram: (DiagramType) -> Unit) {
    val currentOnNewDiagram by rememberUpdatedState(onNewDiagram)
    DisposableEffect(UiStrings.language) {
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
            DiagramType.ER -> UiStrings.NEW_ER_DIAGRAM
            DiagramType.RELATIONAL -> UiStrings.NEW_RELATIONAL_SCHEMA
            DiagramType.CLASS -> UiStrings.NEW_CLASS_DIAGRAM
            DiagramType.USE_CASE -> UiStrings.NEW_USE_CASE_DIAGRAM
            DiagramType.SEQUENCE -> UiStrings.NEW_SEQUENCE_DIAGRAM
            DiagramType.LOGIC -> UiStrings.NEW_LOGIC_DIAGRAM
            DiagramType.MEALY -> UiStrings.NEW_MEALY_MACHINE
            DiagramType.MOORE -> UiStrings.NEW_MOORE_MACHINE
        }
