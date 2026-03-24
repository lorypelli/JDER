package com.jder.ui.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jder.data.DiagramRepository
import com.jder.data.UseCaseRepository
import com.jder.ui.components.DiagramMode
import com.jder.domain.model.DiagramState
import com.jder.domain.model.UseCaseState
import com.jder.ui.components.DiagramTabRow
import com.jder.ui.theme.ThemeState
@Composable
fun AppScreen(
    erState: DiagramState,
    useCaseState: UseCaseState,
    repository: DiagramRepository,
    useCaseRepository: UseCaseRepository,
    themeState: ThemeState
) {
    var currentMode by remember { mutableStateOf(DiagramMode.ER) }
    Column(modifier = Modifier.fillMaxSize()) {
        DiagramTabRow(
            currentMode = currentMode,
            onModeChange = { currentMode = it },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentMode) {
                DiagramMode.ER -> MainScreen(
                    state = erState,
                    repository = repository,
                    themeState = themeState
                )
                DiagramMode.USE_CASE -> UseCaseScreen(
                    state = useCaseState,
                    repository = useCaseRepository,
                    themeState = themeState
                )
            }
        }
    }
}
