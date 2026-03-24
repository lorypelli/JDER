package com.jder.ui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
fun ToolbarViewMenuBox(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showViewMenu by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { showViewMenu = true }) {
            Text("Vista")
        }
        DropdownMenu(
            expanded = showViewMenu,
            onDismissRequest = { showViewMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Zoom In") },
                onClick = {
                    coroutineScope.launch {
                        delay(150)
                        showViewMenu = false
                        onZoomIn()
                    }
                },
                leadingIcon = { Icon(Icons.Default.ZoomIn, null) }
            )
            DropdownMenuItem(
                text = { Text("Zoom Out") },
                onClick = {
                    coroutineScope.launch {
                        delay(150)
                        showViewMenu = false
                        onZoomOut()
                    }
                },
                leadingIcon = { Icon(Icons.Default.ZoomOut, null) }
            )
            DropdownMenuItem(
                text = { Text("Reimposta Zoom") },
                onClick = {
                    coroutineScope.launch {
                        delay(150)
                        showViewMenu = false
                        onResetZoom()
                    }
                },
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
    Text(
        text = zoomPercentage,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    IconButton(onClick = onZoomOut) {
        Icon(Icons.Default.ZoomOut, "Zoom Out")
    }
    IconButton(onClick = onResetZoom) {
        Icon(Icons.Default.CenterFocusWeak, "Reset Zoom")
    }
    IconButton(onClick = onZoomIn) {
        Icon(Icons.Default.ZoomIn, "Zoom In")
    }
}
