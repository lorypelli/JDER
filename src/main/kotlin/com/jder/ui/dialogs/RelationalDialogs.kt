package com.jder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import com.jder.domain.model.RelationalColumn
import com.jder.domain.model.RelationalReference
import com.jder.domain.model.RelationalTable
import java.util.UUID

private class ReferenceOption(val reference: RelationalReference, val label: String)

@Composable
fun TablePropertiesDialog(
    tableName: String,
    tableWidth: Float,
    tableDocumentation: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, width: Float, documentation: String) -> Unit,
) {
    var name by remember { mutableStateOf(tableName) }
    var width by remember { mutableStateOf(tableWidth.toInt().toString()) }
    var documentation by remember { mutableStateOf(tableDocumentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proprietà Tabella") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it.filter { it.isDigit() } },
                    label = { Text("Larghezza") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = documentation,
                    onValueChange = { documentation = it },
                    label = { Text("Documentazione") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name.trim(),
                        width.toFloatOrNull()?.coerceAtLeast(140f) ?: tableWidth,
                        documentation,
                    )
                    onDismiss()
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun AddColumnDialog(
    tables: List<RelationalTable>,
    onDismiss: () -> Unit,
    onConfirm: (column: RelationalColumn) -> Unit,
) {
    ColumnDialog(
        title = "Aggiungi Colonna",
        initialColumn = null,
        tables = tables,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun EditColumnDialog(
    column: RelationalColumn,
    tables: List<RelationalTable>,
    onDismiss: () -> Unit,
    onConfirm: (column: RelationalColumn) -> Unit,
) {
    ColumnDialog(
        title = "Modifica Colonna",
        initialColumn = column,
        tables = tables,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnDialog(
    title: String,
    initialColumn: RelationalColumn?,
    tables: List<RelationalTable>,
    onDismiss: () -> Unit,
    onConfirm: (column: RelationalColumn) -> Unit,
) {
    var name by remember { mutableStateOf(initialColumn?.name ?: "") }
    var type by remember { mutableStateOf(initialColumn?.type ?: "") }
    var isPrimaryKey by remember { mutableStateOf(initialColumn?.isPrimaryKey ?: false) }
    var isNullable by remember { mutableStateOf(initialColumn?.isNullable ?: false) }
    var isUnique by remember { mutableStateOf(initialColumn?.isUnique ?: false) }
    var reference by remember { mutableStateOf(initialColumn?.reference) }
    var expanded by remember { mutableStateOf(false) }
    val options =
        tables.flatMap { table ->
            table.columns
                .filter {
                    it.id != initialColumn?.id &&
                        (it.isPrimaryKey ||
                            it.isUnique ||
                            RelationalReference(table.id, it.id) == initialColumn?.reference)
                }
                .map {
                    ReferenceOption(
                        RelationalReference(table.id, it.id),
                        "${table.name}.${it.name}",
                    )
                }
        }
    val selectedOption = options.find { it.reference == reference }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Tipo") },
                        placeholder = { Text("es. INT") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                SwitchRow("Chiave Primaria", isPrimaryKey) { isPrimaryKey = it }
                SwitchRow("Opzionale (ammette NULL)", isNullable && !isPrimaryKey, !isPrimaryKey) {
                    isNullable = it
                }
                SwitchRow("Valore Unico", isUnique && !isPrimaryKey, !isPrimaryKey) {
                    isUnique = it
                }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedOption?.label ?: "Nessuna",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chiave esterna verso") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nessuna") },
                            onClick = {
                                reference = null
                                expanded = false
                            },
                        )
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    reference = option.reference
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                if (options.isEmpty()) {
                    Text(
                        "Per creare una chiave esterna serve una colonna chiave primaria o con " +
                            "valore unico in una tabella",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        RelationalColumn(
                            id = initialColumn?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = type.trim(),
                            isPrimaryKey = isPrimaryKey,
                            isNullable = isNullable && !isPrimaryKey,
                            isUnique = isUnique && !isPrimaryKey,
                            reference = selectedOption?.reference,
                        )
                    )
                    onDismiss()
                },
                enabled = name.isNotBlank(),
            ) {
                Text(if (initialColumn != null) "Conferma" else "Aggiungi")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForeignKeyDialog(
    sourceTable: RelationalTable,
    tables: List<RelationalTable>,
    initialTargetId: String?,
    onDismiss: () -> Unit,
    onConfirm: (columns: List<RelationalColumn>) -> Unit,
) {
    var targetId by remember { mutableStateOf(initialTargetId) }
    var isPartOfPrimaryKey by remember { mutableStateOf(false) }
    var isNullable by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val target = tables.find { it.id == targetId }
    val keyColumns = target?.primaryKey.orEmpty()
    var columnNames by
        remember(targetId) {
            mutableStateOf(
                target
                    ?.let { referenced ->
                        referenced.primaryKey.associate {
                            it.id to defaultForeignKeyName(sourceTable, referenced, it)
                        }
                    }
                    .orEmpty()
            )
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova Chiave Esterna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Da: ${sourceTable.name}", style = MaterialTheme.typography.bodyMedium)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = target?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tabella riferita") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        tables.forEach { table ->
                            DropdownMenuItem(
                                text = { Text(table.name) },
                                onClick = {
                                    targetId = table.id
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                target?.let { referenced ->
                    if (keyColumns.isEmpty()) {
                        Text(
                            "La tabella \"${referenced.name}\" non ha una chiave primaria: " +
                                "aggiungine una prima di creare la chiave esterna",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    keyColumns.forEach { keyColumn ->
                        OutlinedTextField(
                            value = columnNames[keyColumn.id].orEmpty(),
                            onValueChange = { columnNames = columnNames + (keyColumn.id to it) },
                            label = { Text("Colonna per ${referenced.name}.${keyColumn.name}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
                if (keyColumns.isNotEmpty()) {
                    SwitchRow("Parte della chiave primaria", isPartOfPrimaryKey) {
                        isPartOfPrimaryKey = it
                    }
                    SwitchRow(
                        "Opzionale (ammette NULL)",
                        isNullable && !isPartOfPrimaryKey,
                        !isPartOfPrimaryKey,
                    ) {
                        isNullable = it
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    target?.let { referenced ->
                        onConfirm(
                            keyColumns.map {
                                RelationalColumn(
                                    id = UUID.randomUUID().toString(),
                                    name = columnNames[it.id].orEmpty().trim(),
                                    type = it.type,
                                    isPrimaryKey = isPartOfPrimaryKey,
                                    isNullable = isNullable && !isPartOfPrimaryKey,
                                    reference = RelationalReference(referenced.id, it.id),
                                )
                            }
                        )
                    }
                    onDismiss()
                },
                enabled =
                    keyColumns.isNotEmpty() &&
                        keyColumns.all { columnNames[it.id].orEmpty().isNotBlank() },
            ) {
                Text("Aggiungi")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

private fun defaultForeignKeyName(
    source: RelationalTable,
    target: RelationalTable,
    keyColumn: RelationalColumn,
): String =
    if (source.columns.none { it.name.equals(keyColumn.name, ignoreCase = true) }) {
        keyColumn.name
    } else {
        "${keyColumn.name}_${target.name.lowercase()}"
    }
