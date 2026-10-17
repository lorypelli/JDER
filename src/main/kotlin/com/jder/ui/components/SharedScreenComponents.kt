package com.jder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class SnackbarMessenger(val hostState: SnackbarHostState, private val scope: CoroutineScope) {
  fun show(message: String) {
    scope.launch {
      hostState.currentSnackbarData?.dismiss()
      hostState.showSnackbar(message)
    }
  }
}

@Composable
fun rememberSnackbarMessenger(): SnackbarMessenger {
  val hostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  return remember(hostState, scope) { SnackbarMessenger(hostState, scope) }
}

@Composable
fun DiagramSnackbarHost(hostState: SnackbarHostState) {
  SnackbarHost(hostState) {
    Snackbar(
        snackbarData = it,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionColor = MaterialTheme.colorScheme.primary,
    )
  }
}

@Composable
fun PropertiesPanelHeader(onClose: () -> Unit) {
  Box(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = "Proprietà",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.align(Alignment.CenterStart),
    )
    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd).size(32.dp)) {
      Icon(
          Icons.Default.Close,
          contentDescription = "Chiudi pannello proprietà",
          tint = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
  Divider()
}

@Composable
fun <T> RadioOptionList(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
) {
  Column {
    options.forEach { option ->
      val isSelected = option == selected
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
              Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                      if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                      else MaterialTheme.colorScheme.surface
                  )
                  .selectable(
                      selected = isSelected,
                      onClick = { onSelect(option) },
                      role = Role.RadioButton,
                  )
                  .padding(horizontal = 8.dp, vertical = 6.dp),
      ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(
            text = label(option),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
            color =
                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
fun ConfirmNewDiagramDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Conferma nuovo diagramma") },
      text = { Text("Ci sono modifiche non salvate. Vuoi creare un nuovo diagramma comunque?") },
      confirmButton = { TextButton(onClick = onConfirm) { Text("Sì") } },
      dismissButton = { TextButton(onClick = onDismiss) { Text("No") } },
  )
}

@Composable
fun ConfirmOpenDiagramDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Conferma apertura diagramma") },
      text = { Text("Ci sono modifiche non salvate. Vuoi aprire un altro diagramma comunque?") },
      confirmButton = { TextButton(onClick = onConfirm) { Text("Sì") } },
      dismissButton = { TextButton(onClick = onDismiss) { Text("No") } },
  )
}
