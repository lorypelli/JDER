package com.jder.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.ui.theme.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun ToolbarFileMenu(
    onNewDiagram: () -> Unit,
    onOpenDiagram: () -> Unit,
    onSaveDiagram: () -> Unit,
    onSaveAsDiagram: () -> Unit,
    hasSelection: Boolean,
    onDeleteSelected: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showFileMenu by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { showFileMenu = true }) { Text("File") }
        DropdownMenu(expanded = showFileMenu, onDismissRequest = { showFileMenu = false }) {
            DropdownMenuItem(
                text = { Text("Nuovo Diagramma") },
                onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onNewDiagram() } },
                leadingIcon = { Icon(Icons.Default.Add, null) }
            )
            DropdownMenuItem(
                text = { Text("Apri Diagramma...") },
                onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onOpenDiagram() } },
                leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
            )
            Divider()
            DropdownMenuItem(
                text = { Text("Salva") },
                onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onSaveDiagram() } },
                leadingIcon = { Icon(Icons.Default.Save, null) }
            )
            DropdownMenuItem(
                text = { Text("Salva con nome...") },
                onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onSaveAsDiagram() } },
                leadingIcon = { Icon(Icons.Default.Save, null) }
            )
            Divider()
            DropdownMenuItem(
                text = { Text("Elimina Elemento Selezionato") },
                onClick = { coroutineScope.launch { delay(150); showFileMenu = false; onDeleteSelected() } },
                leadingIcon = { Icon(Icons.Default.Delete, null) },
                enabled = hasSelection
            )
        }
    }
}
@Composable
fun ToolbarExportMenu(onExportPNG: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var showExportMenu by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { showExportMenu = true }) { Text("Esporta") }
        DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
            DropdownMenuItem(
                text = { Text("Esporta come PNG...") },
                onClick = { coroutineScope.launch { delay(150); showExportMenu = false; onExportPNG() } },
                leadingIcon = { Icon(Icons.Default.Image, "Esporta diagramma come immagine PNG") }
            )
        }
    }
}
@Composable
fun ToolbarViewMenuBox(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showViewMenu by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { showViewMenu = true }) { Text("Vista") }
        DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }) {
            DropdownMenuItem(
                text = { Text("Zoom In") },
                onClick = { coroutineScope.launch { delay(150); showViewMenu = false; onZoomIn() } },
                leadingIcon = { Icon(Icons.Default.ZoomIn, null) }
            )
            DropdownMenuItem(
                text = { Text("Zoom Out") },
                onClick = { coroutineScope.launch { delay(150); showViewMenu = false; onZoomOut() } },
                leadingIcon = { Icon(Icons.Default.ZoomOut, null) }
            )
            DropdownMenuItem(
                text = { Text("Reimposta Zoom") },
                onClick = { coroutineScope.launch { delay(150); showViewMenu = false; onResetZoom() } },
                leadingIcon = { Icon(Icons.Default.CenterFocusWeak, null) }
            )
        }
    }
}
@Composable
fun ToolbarPaletteTheme(themeState: ThemeState) {
    PaletteSelector(
        selectedPalette = themeState.selectedPalette,
        onPaletteSelected = themeState::selectPalette,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    ThemeToggleButton(
        isDarkTheme = themeState.isDarkTheme,
        onToggle = themeState::toggleTheme,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
@Composable
fun ToolbarZoomControls(
    zoomPercentage: String,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    Text(text = zoomPercentage, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
    IconButton(onClick = onZoomOut) { Icon(Icons.Default.ZoomOut, "Zoom Out") }
    IconButton(onClick = onResetZoom) { Icon(Icons.Default.CenterFocusWeak, "Reset Zoom") }
    IconButton(onClick = onZoomIn) { Icon(Icons.Default.ZoomIn, "Zoom In") }
}

@Composable
fun ToolbarVerticalDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
