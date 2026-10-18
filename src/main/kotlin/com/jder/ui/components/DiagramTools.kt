package com.jder.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

sealed class DiagramTool {
    class Mode(
        val icon: ImageVector,
        val label: String,
        val menuLabel: String,
        val isSelected: Boolean,
        val onSelect: () -> Unit,
    ) : DiagramTool()

    class Toggle(
        val icon: ImageVector,
        val label: String,
        val menuLabel: String,
        val isChecked: Boolean,
        val onToggle: () -> Unit,
    ) : DiagramTool()

    class Action(
        val icon: ImageVector,
        val label: String,
        val menuLabel: String,
        val onClick: () -> Unit,
    ) : DiagramTool()

    class Group(val menuLabel: String, val tools: List<DiagramTool>) : DiagramTool()

    data object Divider : DiagramTool()
}

class DiagramSelectionMenu(val title: String, val items: List<ContextMenuItem>)
