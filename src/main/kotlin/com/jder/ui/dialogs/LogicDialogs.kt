package com.jder.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jder.domain.model.LogicComponent
import com.jder.domain.model.LogicComponentType
import com.jder.domain.model.LogicDiagram
import com.jder.domain.model.LogicSimulator
import com.jder.domain.model.LogicTruthTable
import com.jder.domain.model.MAX_GATE_INPUTS
import com.jder.domain.model.MIN_GATE_INPUTS
import com.jder.ui.components.CustomIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogicComponentDialog(
    component: LogicComponent,
    onDismiss: () -> Unit,
    onConfirm: (label: String, type: LogicComponentType, inputCount: Int, value: Boolean) -> Unit,
) {
    var label by remember(component.id) { mutableStateOf(component.label) }
    var type by remember(component.id) { mutableStateOf(component.type) }
    var inputCount by
        remember(component.id) {
            mutableStateOf(component.inputCount.coerceIn(MIN_GATE_INPUTS, MAX_GATE_INPUTS))
        }
    var value by remember(component.id) { mutableStateOf(component.value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (component.type) {
                    LogicComponentType.INPUT -> "Proprietà Ingresso"
                    LogicComponentType.OUTPUT -> "Proprietà Uscita"
                    else -> "Proprietà Porta Logica"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etichetta") },
                    placeholder = { if (component.type.isGate) Text("Opzionale, es. U1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (component.type.isGate) {
                    Text("Tipo di porta:", style = MaterialTheme.typography.labelMedium)
                    LogicComponentType.gates.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { gate ->
                                FilterChip(
                                    selected = type == gate,
                                    onClick = { type = gate },
                                    label = { Text(gate.display) },
                                    leadingIcon = {
                                        Icon(
                                            CustomIcons.gate(gate),
                                            null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                )
                            }
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
                                    onClick = { inputCount-- },
                                    enabled = inputCount > MIN_GATE_INPUTS,
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Rimuovi ingresso",
                                    )
                                }
                                Text("$inputCount", style = MaterialTheme.typography.titleMedium)
                                IconButton(
                                    onClick = { inputCount++ },
                                    enabled = inputCount < MAX_GATE_INPUTS,
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Aggiungi ingresso",
                                    )
                                }
                            }
                        }
                    }
                }
                if (component.type == LogicComponentType.INPUT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Valore: ${if (value) "1" else "0"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(checked = value, onCheckedChange = { value = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(label.trim(), type, inputCount, value)
                    onDismiss()
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

private fun bit(value: Boolean?): String =
    when (value) {
        true -> "1"
        false -> "0"
        null -> "?"
    }

private fun columnName(component: LogicComponent): String =
    component.label.ifBlank { component.type.display }

private fun LogicTruthTable.toClipboardText(): String = buildString {
    append(
        buildList {
                addAll(inputs.map(::columnName))
                add("|")
                addAll(outputs.map(::columnName))
            }
            .joinToString("\t")
    )
    rows.forEach { row ->
        append('\n')
        append(
            buildList {
                    addAll(row.inputs.map(::bit))
                    add("|")
                    addAll(row.outputs.map(::bit))
                }
                .joinToString("\t")
        )
    }
}

@Composable
fun TruthTableDialog(diagram: LogicDiagram, onDismiss: () -> Unit) {
    val table = remember(diagram) { LogicSimulator.truthTable(diagram) }
    val inputCount = diagram.components.count { it.type == LogicComponentType.INPUT }
    val clipboardManager = LocalClipboardManager.current
    val hasRows = table != null && table.outputs.isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tabella di verità") },
        text = {
            when {
                table == null ->
                    Text(
                        "Il circuito ha $inputCount ingressi: la tabella di verità ne supporta al " +
                            "massimo ${LogicSimulator.MAX_TRUTH_TABLE_INPUTS}."
                    )
                table.outputs.isEmpty() ->
                    Text(
                        "Aggiungi almeno un'uscita al circuito per calcolare la tabella di verità."
                    )
                else -> TruthTableContent(table, diagram)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } },
        dismissButton =
            if (hasRows) {
                {
                    TextButton(
                        onClick = {
                            table?.let {
                                clipboardManager.setText(AnnotatedString(it.toClipboardText()))
                            }
                        }
                    ) {
                        Text("Copia")
                    }
                }
            } else null,
    )
}

@Composable
private fun TruthTableContent(table: LogicTruthTable, diagram: LogicDiagram) {
    val cellWidth = 52.dp
    val tableWidth = cellWidth * (table.inputs.size + table.outputs.size) + 9.dp
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Column(modifier = Modifier.width(tableWidth)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    table.inputs.forEach {
                        TruthTableCell(columnName(it), cellWidth, isHeader = true)
                    }
                    TruthTableSeparator()
                    table.outputs.forEach {
                        TruthTableCell(columnName(it), cellWidth, isHeader = true, isOutput = true)
                    }
                }
                Divider()
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    itemsIndexed(table.rows) { index, row ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.background(
                                    if (index % 2 == 1)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else Color.Transparent
                                ),
                        ) {
                            row.inputs.forEach { TruthTableCell(bit(it), cellWidth) }
                            TruthTableSeparator()
                            row.outputs.forEach {
                                TruthTableCell(bit(it), cellWidth, isOutput = true)
                            }
                        }
                    }
                }
            }
        }
        val expressions =
            remember(diagram) {
                table.outputs.mapNotNull { output ->
                    LogicSimulator.expression(diagram, output.id)?.let {
                        "${columnName(output)} = $it"
                    }
                }
            }
        if (expressions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Espressioni:", style = MaterialTheme.typography.labelMedium)
                expressions.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Composable
private fun TruthTableSeparator() {
    Box(
        modifier =
            Modifier.padding(horizontal = 4.dp)
                .width(1.dp)
                .heightIn(min = 28.dp)
                .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun TruthTableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false,
    isOutput: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(vertical = 4.dp),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = if (isHeader || isOutput) FontWeight.Bold else FontWeight.Normal,
        color =
            if (isOutput) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
    )
}
