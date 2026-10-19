package com.jder.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseRelationType
import com.jder.ui.components.RadioOptionList

@Composable
fun ActorPropertiesDialog(
    actorId: String,
    actorName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    var name by remember(actorId) { mutableStateOf(actorName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proprietà Attore") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name)
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
fun UseCasePropertiesDialog(
    useCaseId: String,
    useCaseName: String,
    useCaseDocumentation: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, documentation: String) -> Unit,
) {
    var name by remember(useCaseId) { mutableStateOf(useCaseName) }
    var documentation by remember(useCaseId) { mutableStateOf(useCaseDocumentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proprietà Caso d'Uso") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = documentation,
                    onValueChange = { documentation = it },
                    label = { Text("Documentazione") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp),
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name, documentation)
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
fun SelectRelationTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: UseCaseRelationType) -> Unit,
) {
    RelationTypeDialog(
        title = "Tipo di Relazione",
        relationId = null,
        initialType = UseCaseRelationType.ASSOCIATION,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun RelationEditDialog(
    relation: UseCaseRelation,
    onDismiss: () -> Unit,
    onConfirm: (type: UseCaseRelationType) -> Unit,
) {
    RelationTypeDialog(
        title = "Modifica Tipo Relazione",
        relationId = relation.id,
        initialType = relation.type,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun RelationTypeDialog(
    title: String,
    relationId: String?,
    initialType: UseCaseRelationType,
    onDismiss: () -> Unit,
    onConfirm: (type: UseCaseRelationType) -> Unit,
) {
    var selected by remember(relationId) { mutableStateOf(initialType) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            RadioOptionList(UseCaseRelationType.entries, selected, { selected = it }) { it.display }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selected)
                    onDismiss()
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun SystemBoundaryDialog(
    boundaryId: String,
    boundaryName: String,
    boundaryWidth: Float,
    boundaryHeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (name: String, width: Float, height: Float) -> Unit,
) {
    var name by remember(boundaryId) { mutableStateOf(boundaryName) }
    var width by remember(boundaryId) { mutableStateOf(boundaryWidth.toInt().toString()) }
    var height by remember(boundaryId) { mutableStateOf(boundaryHeight.toInt().toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proprietà Sistema") },
        text = {
            Column {
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
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it.filter { it.isDigit() } },
                    label = { Text("Altezza") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        width.toFloatOrNull()?.coerceAtLeast(100f) ?: boundaryWidth,
                        height.toFloatOrNull()?.coerceAtLeast(80f) ?: boundaryHeight,
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
