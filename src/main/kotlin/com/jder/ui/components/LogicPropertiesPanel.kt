package com.jder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.LogicComponent
import com.jder.domain.model.LogicComponentType
import com.jder.domain.model.LogicDiagramState
import com.jder.domain.model.LogicSimulator
import com.jder.domain.model.LogicWire
import com.jder.domain.model.MAX_GATE_INPUTS
import com.jder.domain.model.MIN_GATE_INPUTS

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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.component(state.selectedComponentId)?.let {
            LogicComponentContent(it, state, onEditComponent)
        }
        state.selectedWireId?.let { selId ->
            state.diagram.wires.find { it.id == selId }?.let { LogicWireContent(it, state) }
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
    }
}

@Composable
private fun LogicComponentContent(
    component: LogicComponent,
    state: LogicDiagramState,
    onEdit: () -> Unit,
) {
    val type = component.type
    val kind =
        when (type) {
            LogicComponentType.INPUT -> "ingresso"
            LogicComponentType.OUTPUT -> "uscita"
            else -> "porta logica ${type.display}"
        }
    val value =
        when (type) {
            LogicComponentType.INPUT -> component.value
            LogicComponentType.OUTPUT -> state.simulation.inputOf(component.id, 0)
            else -> state.simulation.outputOf(component.id)
        }
    val valueLabel = if (type == LogicComponentType.INPUT) "Valore" else "Valore attuale"
    PropertyCard("Componente: ${component.displayName}") {
        PropertyDetail("Tipo: $kind")
        if (type.isGate) PropertyDetail("Ingressi: ${component.inputPinCount}")
        PropertyDetail("$valueLabel: ${logicValueLabel(value)}", isEmphasized = true)
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
    if (type == LogicComponentType.INPUT) {
        PropertyOutlinedButton(
            "Commuta Valore",
            Icons.Default.ToggleOn,
            { state.toggleInput(component.id) },
        )
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
            PropertyDetailCard(
                "Espressione booleana:",
                if (type == LogicComponentType.OUTPUT) "${component.label.ifBlank { "Y" }} = $it"
                else it,
            )
        }
    }
}

@Composable
private fun LogicWireContent(wire: LogicWire, state: LogicDiagramState) {
    val source = state.component(wire.sourceId)
    val target = state.component(wire.targetId)
    val pin = if ((target?.inputPinCount ?: 0) > 1) " (ingresso ${wire.targetPin + 1})" else ""
    PropertyCard("Collegamento") {
        PropertyDetail("Da: ${source?.displayName ?: "?"}")
        PropertyDetail("A: ${target?.displayName ?: "?"}$pin")
        PropertyDetail(
            "Valore: ${logicValueLabel(state.simulation.outputOf(wire.sourceId))}",
            isEmphasized = true,
        )
    }
    Spacer(Modifier.height(8.dp))
    PropertyOutlinedButton(
        "Elimina Collegamento",
        Icons.Default.Delete,
        { state.deleteWire(wire.id) },
        isDestructive = true,
    )
}
