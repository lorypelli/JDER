package com.jder.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun UpdateAvailableDialog(downloadUrl: String, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiornamento disponibile") },
        text = {
            Text(
                "È disponibile una nuova versione di JDER. Scarica il nuovo JDER.jar e usalo al posto di quello attuale."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        uriHandler.openUri(downloadUrl)
                    } catch (_: Exception) {}
                    onDismiss()
                }
            ) {
                Text("Scarica")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Più tardi") } },
    )
}
