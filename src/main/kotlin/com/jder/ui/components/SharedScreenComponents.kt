package com.jder.ui.components
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
@Composable
fun DiagramSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) {
        Snackbar(
            snackbarData = it,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            actionColor = MaterialTheme.colorScheme.primary
        )
    }
}
@Composable
fun ConfirmNewDiagramDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conferma nuovo diagramma") },
        text = { Text("Ci sono modifiche non salvate. Vuoi creare un nuovo diagramma comunque?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sì") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No") } }
    )
}
@Composable
fun ConfirmOpenDiagramDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conferma apertura diagramma") },
        text = { Text("Ci sono modifiche non salvate. Vuoi aprire un altro diagramma comunque?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sì") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No") } }
    )
}
