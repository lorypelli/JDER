package com.jder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jder.domain.model.Note

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
fun PropertyCard(title: String, details: (@Composable ColumnScope.() -> Unit)? = null) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
          title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
      )
      if (details != null) {
        Spacer(Modifier.height(4.dp))
        details()
      }
    }
  }
}

@Composable
fun PropertyDetail(text: String, isEmphasized: Boolean = false) {
  Text(
      text,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = if (isEmphasized) FontWeight.SemiBold else FontWeight.Normal,
  )
}

@Composable
fun PropertyButton(label: String, icon: ImageVector, onClick: () -> Unit) {
  Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(label)
  }
}

@Composable
fun PropertyOutlinedButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
) {
  OutlinedButton(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      colors =
          if (isDestructive) {
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
          } else {
            ButtonDefaults.outlinedButtonColors()
          },
  ) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(label)
  }
}

@Composable
fun PropertyListTitle(text: String) {
  Spacer(Modifier.height(8.dp))
  Text(text, style = MaterialTheme.typography.titleSmall)
}

@Composable
fun PropertyItemCard(
    title: String,
    subtitle: String,
    deleteDescription: String,
    onDelete: () -> Unit,
    isEmphasized: Boolean = false,
    editDescription: String? = null,
    onEdit: (() -> Unit)? = null,
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Normal,
        )
        if (subtitle.isNotEmpty()) {
          Text(
              subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          )
        }
      }
      Row {
        if (onEdit != null) {
          IconButton(onClick = onEdit) {
            Icon(
                Icons.Default.Edit,
                contentDescription = editDescription,
                tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
        IconButton(onClick = onDelete) {
          Icon(
              Icons.Default.Delete,
              contentDescription = deleteDescription,
              tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Composable
fun PropertyDetailCard(label: String, text: String) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Spacer(Modifier.height(4.dp))
      Text(text, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
fun DocumentationCard(documentation: String) {
  if (documentation.isBlank()) return
  Spacer(Modifier.height(8.dp))
  PropertyDetailCard("Documentazione:", documentation)
}

@Composable
fun NotePropertiesContent(note: Note, onEdit: () -> Unit) {
  val preview = if (note.text.length > 50) "${note.text.take(50)}..." else note.text
  PropertyCard("Nota") { PropertyDetail(preview) }
  Spacer(Modifier.height(8.dp))
  PropertyButton("Modifica Testo", Icons.Default.Edit, onEdit)
}
