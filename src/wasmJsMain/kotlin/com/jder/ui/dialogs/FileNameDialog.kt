package com.jder.ui.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.strings.CommonStrings
import com.jder.strings.FileStrings
import com.jder.ui.components.baseFileName
import com.jder.ui.components.fileNameError

@Composable
fun FileNameDialog(
    title: String,
    initialName: String,
    extension: String,
    onDismiss: () -> Unit,
    onConfirm: (fileName: String) -> Unit,
) {
    var fileName by remember { mutableStateOf(initialName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    fun confirm() {
        val baseName = baseFileName(fileName, extension)
        errorMessage = fileNameError(baseName)
        if (errorMessage == null) onConfirm("$baseName$extension")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = {
                    fileName = it
                    errorMessage = null
                },
                label = { Text(FileStrings.FILE_NAME) },
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = extension,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = ::confirm, enabled = fileName.isNotBlank()) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(CommonStrings.SAVE)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(CommonStrings.CANCEL) } },
    )
}
