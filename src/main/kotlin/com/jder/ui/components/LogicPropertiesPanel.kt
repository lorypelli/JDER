package com.jder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jder.domain.model.LogicComponent
import com.jder.domain.model.LogicComponentType
import com.jder.domain.model.LogicDiagramState
import com.jder.domain.model.LogicSimulator
import com.jder.domain.model.LogicWire
import com.jder.domain.model.MAX_GATE_INPUTS
import com.jder.domain.model.MIN_GATE_INPUTS
import com.jder.domain.model.Note

fun logicValueLabel(value: Boolean?): String =
    when (value) {
      true -> "1 (alto)"
      false -> "0 (basso)"
      null -> "indefinito"
    }

@Composable
fun LogicPropertiesPanel(
    state: LogicDiagramState,
    onEditComponent: () -> Unit,
    onEditNote: () -> Unit,
    onClose: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    PropertiesPanelHeader(onClose)
    state.component(state.selectedComponentId)?.let {
      LogicComponentContent(it, state, onEditComponent)
    }
    state.selectedWireId?.let { id ->
      state.diagram.wires.find { it.id == id }?.let { LogicWireContent(it, state) }
    }
    state.selectedNoteId?.let { id ->
      state.diagram.notes.find { it.id == id }?.let { LogicNoteContent(it, onEditNote) }
    }
  }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
      Spacer(Modifier.height(4.dp))
      content()
    }
  }
}

@Composable
private fun PanelButton(label: String, icon: ImageVector, onClick: () -> Unit) {
  Button(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
  ) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(label)
  }
}

@Composable
private fun LogicComponentContent(
    component: LogicComponent,
    state: LogicDiagramState,
    onEdit: () -> Unit,
) {
  val type = component.type
  InfoCard(component.displayName) {
    Text(
        "Tipo: " +
            when (type) {
              LogicComponentType.INPUT -> "ingresso"
              LogicComponentType.OUTPUT -> "uscita"
              else -> "porta logica ${type.display}"
            },
        style = MaterialTheme.typography.bodyMedium,
    )
    if (type.isGate) {
      Text("Ingressi: ${component.inputPinCount}", style = MaterialTheme.typography.bodyMedium)
    }
    val value =
        when (type) {
          LogicComponentType.INPUT -> component.value
          LogicComponentType.OUTPUT -> state.simulation.inputOf(component.id, 0)
          else -> state.simulation.outputOf(component.id)
        }
    Text(
        "Valore${if (type == LogicComponentType.INPUT) "" else " attuale"}: ${logicValueLabel(value)}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
    )
  }
  Spacer(Modifier.height(8.dp))
  PanelButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
  if (type == LogicComponentType.INPUT) {
    OutlinedButton(
        onClick = { state.toggleInput(component.id) },
        modifier = Modifier.fillMaxWidth(),
    ) {
      Icon(Icons.Default.ToggleOn, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Text("Commuta Valore")
    }
  }
  if (type.supportsMultipleInputs) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text("Numero di ingressi", style = MaterialTheme.typography.bodyMedium)
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { state.setInputCount(component.id, component.inputPinCount - 1) },
            enabled = component.inputPinCount > MIN_GATE_INPUTS,
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Rimuovi ingresso")
        }
        Text("${component.inputPinCount}", style = MaterialTheme.typography.titleMedium)
        IconButton(
            onClick = { state.setInputCount(component.id, component.inputPinCount + 1) },
            enabled = component.inputPinCount < MAX_GATE_INPUTS,
        ) {
          Icon(Icons.Default.Add, contentDescription = "Aggiungi ingresso")
        }
      }
    }
  }
  if (type != LogicComponentType.INPUT) {
    val expression =
        remember(state.diagram.components, state.diagram.wires, component.id) {
          LogicSimulator.expression(state.diagram, component.id)
        }
    expression?.let {
      Card(
          modifier = Modifier.fillMaxWidth(),
          colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
              "Espressione booleana:",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
          )
          Spacer(Modifier.height(4.dp))
          Text(
              if (type == LogicComponentType.OUTPUT) "${component.label.ifBlank { "Y" }} = $it"
              else it,
              style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun LogicWireContent(wire: LogicWire, state: LogicDiagramState) {
  val source = state.component(wire.sourceId)
  val target = state.component(wire.targetId)
  InfoCard("Collegamento") {
    Text("Da: ${source?.displayName ?: "?"}", style = MaterialTheme.typography.bodyMedium)
    Text(
        "A: ${target?.displayName ?: "?"}" +
            if ((target?.inputPinCount ?: 0) > 1) " (ingresso ${wire.targetPin + 1})" else "",
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        "Valore: ${logicValueLabel(state.simulation.outputOf(wire.sourceId))}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
    )
  }
  Spacer(Modifier.height(8.dp))
  OutlinedButton(onClick = { state.deleteWire(wire.id) }, modifier = Modifier.fillMaxWidth()) {
    Icon(
        Icons.Default.Delete,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.width(8.dp))
    Text("Elimina Collegamento", color = MaterialTheme.colorScheme.error)
  }
}

@Composable
private fun LogicNoteContent(note: Note, onEdit: () -> Unit) {
  InfoCard("Nota") {
    Text(
        note.text.take(50) + if (note.text.length > 50) "..." else "",
        style = MaterialTheme.typography.bodyMedium,
    )
  }
  Spacer(Modifier.height(8.dp))
  PanelButton("Modifica Testo", Icons.Default.Edit, onEdit)
}
