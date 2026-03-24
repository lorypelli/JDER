package com.jder.ui.components
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
enum class DiagramMode {
    ER,
    USE_CASE
}
@Composable
fun DiagramTabRow(
    currentMode: DiagramMode,
    onModeChange: (DiagramMode) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = currentMode.ordinal,
        modifier = modifier
    ) {
        Tab(
            selected = currentMode == DiagramMode.ER,
            onClick = { onModeChange(DiagramMode.ER) },
            text = { Text("Diagramma E/R") }
        )
        Tab(
            selected = currentMode == DiagramMode.USE_CASE,
            onClick = { onModeChange(DiagramMode.USE_CASE) },
            text = { Text("Casi d'Uso") }
        )
    }
}
