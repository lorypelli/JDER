package com.jder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jder.data.ClassDiagramRepository
import com.jder.data.DiagramRepository
import com.jder.data.UseCaseRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.DiagramState
import com.jder.domain.model.UseCaseState
import com.jder.ui.screens.AppScreen
import com.jder.ui.theme.JDERTheme
import com.jder.ui.theme.ThemeState
import java.awt.Dimension
fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    val diagramState = remember { DiagramState() }
    val useCaseState = remember { UseCaseState() }
    val classState = remember { ClassDiagramState() }
    val themeState = remember { ThemeState() }
    val repository = remember { DiagramRepository() }
    val useCaseRepository = remember { UseCaseRepository() }
    val classRepository = remember { ClassDiagramRepository() }
    var showExitDialog by remember { mutableStateOf(false) }
    var shouldExit by remember { mutableStateOf(false) }
    if (shouldExit) {
        exitApplication()
    }
    Window(
        onCloseRequest = {
            if (diagramState.isModified || useCaseState.isModified || classState.isModified) {
                showExitDialog = true
            } else {
                exitApplication()
            }
        },
        title = "JDER - Java Diagrammi E/R",
        state = windowState,
        icon = painterResource("jder_icon.png")
    ) {
        window.minimumSize = Dimension(800, 600)
        JDERTheme(darkTheme = themeState.isDarkTheme, colorPalette = themeState.selectedPalette) {
            AppScreen(
                erState = diagramState,
                useCaseState = useCaseState,
                classState = classState,
                repository = repository,
                useCaseRepository = useCaseRepository,
                classRepository = classRepository,
                themeState = themeState
            )
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Conferma chiusura") },
                    text = { Text("Ci sono modifiche non salvate. Vuoi uscire comunque?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitDialog = false
                                shouldExit = true
                            }
                        ) {
                            Text("Sì")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showExitDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}
