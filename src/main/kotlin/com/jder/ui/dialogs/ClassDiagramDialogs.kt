package com.jder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import com.jder.domain.model.ClassEntityType
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.ClassRelationType
import com.jder.domain.model.Visibility
import com.jder.ui.components.RadioOptionList
import java.util.UUID

@Composable
fun ClassEntityDialog(
    entityId: String,
    name: String,
    type: ClassEntityType,
    isAbstract: Boolean,
    documentation: String,
    onDismiss: () -> Unit,
    onConfirm:
        (name: String, type: ClassEntityType, isAbstract: Boolean, documentation: String) -> Unit,
) {
    var entityName by remember(entityId) { mutableStateOf(name) }
    var entityType by remember(entityId) { mutableStateOf(type) }
    var abstract by remember(entityId) { mutableStateOf(isAbstract) }
    var docs by remember(entityId) { mutableStateOf(documentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Proprietà Classe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entityName,
                    onValueChange = { entityName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Tipo:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(ClassEntityType.entries, entityType, { entityType = it }) { t ->
                    t.display
                }
                if (entityType == ClassEntityType.CLASS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Astratta", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = abstract, onCheckedChange = { abstract = it })
                    }
                }
                OutlinedTextField(
                    value = docs,
                    onValueChange = { docs = it },
                    label = { Text("Documentazione") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(entityName, entityType, abstract, docs)
                    onDismiss()
                },
                enabled = entityName.isNotBlank(),
            ) {
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun ClassMemberDialog(
    isMethod: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (member: ClassMember) -> Unit,
) {
    var memberName by remember { mutableStateOf("") }
    var memberType by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(Visibility.PUBLIC) }
    var isStatic by remember { mutableStateOf(false) }
    var isAbstract by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isMethod) "Aggiungi Metodo" else "Aggiungi Attributo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = memberType,
                        onValueChange = { memberType = it },
                        label = { Text("Tipo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                Text("Visibilità:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(Visibility.entries, visibility, { visibility = it }) { v ->
                    "${v.symbol}  ${v.display}"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Statico", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isStatic, onCheckedChange = { isStatic = it })
                }
                if (isMethod) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Astratto", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isAbstract, onCheckedChange = { isAbstract = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        ClassMember(
                            id = UUID.randomUUID().toString(),
                            name = memberName,
                            type = memberType,
                            visibility = visibility,
                            isStatic = isStatic,
                            isAbstract = isAbstract,
                        )
                    )
                    onDismiss()
                },
                enabled = memberName.isNotBlank(),
            ) {
                Text("Aggiungi")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun ClassRelationDialog(
    relation: ClassRelation,
    onDismiss: () -> Unit,
    onConfirm:
        (
            type: ClassRelationType,
            sourceMultiplicity: String,
            targetMultiplicity: String,
            label: String,
        ) -> Unit,
) {
    var relType by remember(relation.id) { mutableStateOf(relation.type) }
    var sourceMulti by remember(relation.id) { mutableStateOf(relation.sourceMultiplicity) }
    var targetMulti by remember(relation.id) { mutableStateOf(relation.targetMultiplicity) }
    var label by remember(relation.id) { mutableStateOf(relation.label) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifica Relazione") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(ClassRelationType.entries, relType, { relType = it }) { it.display }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sourceMulti,
                        onValueChange = { sourceMulti = it },
                        label = { Text("Moltep. sorgente") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = targetMulti,
                        onValueChange = { targetMulti = it },
                        label = { Text("Moltep. destinazione") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etichetta") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(relType, sourceMulti, targetMulti, label)
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
fun SelectClassRelationTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: ClassRelationType) -> Unit,
) {
    var selected by remember { mutableStateOf(ClassRelationType.ASSOCIATION) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo di Relazione") },
        text = {
            RadioOptionList(ClassRelationType.entries, selected, { selected = it }) { it.display }
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
