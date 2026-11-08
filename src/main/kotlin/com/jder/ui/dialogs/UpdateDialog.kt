package com.jder.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.jder.ui.strings.UiStrings
import com.jder.ui.utils.openSafely

@Composable
fun UpdateAvailableDialog(downloadUrl: String, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.UPDATE_AVAILABLE) },
        text = { Text(UiStrings.UPDATE_AVAILABLE_MESSAGE) },
        confirmButton = {
            TextButton(
                onClick = {
                    uriHandler.openSafely(downloadUrl)
                    onDismiss()
                }
            ) {
                Text(UiStrings.DOWNLOAD)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.LATER) } },
    )
}
