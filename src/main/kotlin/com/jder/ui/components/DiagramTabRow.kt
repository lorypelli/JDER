package com.jder.ui.components

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jder.domain.model.DiagramType

@Composable
fun DiagramTabRow(
    currentType: DiagramType,
    onTypeChange: (DiagramType) -> Unit,
    isModified: (DiagramType) -> Boolean,
    modifier: Modifier = Modifier,
) {
    TabRow(selectedTabIndex = currentType.ordinal, modifier = modifier) {
        DiagramType.entries.forEach { type ->
            Tab(
                selected = currentType == type,
                onClick = { onTypeChange(type) },
                text = { Text(if (isModified(type)) "${type.tabLabel} •" else type.tabLabel) },
            )
        }
    }
}
