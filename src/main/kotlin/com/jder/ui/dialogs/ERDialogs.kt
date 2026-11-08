package com.jder.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import com.jder.domain.model.Attribute
import com.jder.domain.model.AttributeType
import com.jder.domain.model.Cardinality
import com.jder.domain.model.Entity
import com.jder.domain.model.Relationship
import com.jder.strings.CommonStrings
import com.jder.strings.ERStrings
import java.util.UUID

@Composable
fun EntityPropertiesDialog(
    entity: Entity,
    onDismiss: () -> Unit,
    onConfirm: (entity: Entity) -> Unit,
) {
    var name by remember(entity.id) { mutableStateOf(entity.name) }
    var isWeak by remember(entity.id) { mutableStateOf(entity.isWeak) }
    var documentation by remember(entity.id) { mutableStateOf(entity.documentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ERStrings.ENTITY_PROPERTIES) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(CommonStrings.NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(ERStrings.WEAK_ENTITY)
                    Switch(checked = isWeak, onCheckedChange = { isWeak = it })
                }
                OutlinedTextField(
                    value = documentation,
                    onValueChange = { documentation = it },
                    label = { Text(CommonStrings.DOCUMENTATION) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        entity.copy(name = name, isWeak = isWeak, documentation = documentation)
                    )
                },
                enabled = name.isNotBlank(),
            ) {
                Text(CommonStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(CommonStrings.CANCEL) } },
    )
}

@Composable
fun RelationshipPropertiesDialog(
    relationship: Relationship,
    onDismiss: () -> Unit,
    onConfirm: (relationship: Relationship) -> Unit,
) {
    var name by remember(relationship.id) { mutableStateOf(relationship.name) }
    var documentation by remember(relationship.id) { mutableStateOf(relationship.documentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ERStrings.RELATIONSHIP_PROPERTIES) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(CommonStrings.NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = documentation,
                    onValueChange = { documentation = it },
                    label = { Text(CommonStrings.DOCUMENTATION) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(relationship.copy(name = name, documentation = documentation))
                },
                enabled = name.isNotBlank(),
            ) {
                Text(CommonStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(CommonStrings.CANCEL) } },
    )
}

@Composable
fun AddAttributeDialog(onDismiss: () -> Unit, onConfirm: (attribute: Attribute) -> Unit) {
    AttributeDialog(
        title = CommonStrings.ADD_ATTRIBUTE,
        initialAttribute = null,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun EditAttributeDialog(
    attribute: Attribute,
    onDismiss: () -> Unit,
    onConfirm: (attribute: Attribute) -> Unit,
) {
    AttributeDialog(
        title = CommonStrings.EDIT_ATTRIBUTE,
        initialAttribute = attribute,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeDialog(
    title: String,
    initialAttribute: Attribute?,
    onDismiss: () -> Unit,
    onConfirm: (attribute: Attribute) -> Unit,
) {
    val attributeId = initialAttribute?.id
    var name by remember(attributeId) { mutableStateOf(initialAttribute?.name ?: "") }
    var isPrimaryKey by
        remember(attributeId) { mutableStateOf(initialAttribute?.isPrimaryKey ?: false) }
    var attributeType by
        remember(attributeId) { mutableStateOf(initialAttribute?.type ?: AttributeType.NORMAL) }
    var multiplicity by
        remember(attributeId) { mutableStateOf(initialAttribute?.multiplicity ?: "") }
    var components by
        remember(attributeId) { mutableStateOf(initialAttribute?.components ?: emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var showAddComponentDialog by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(CommonStrings.NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(CommonStrings.PRIMARY_KEY)
                    Switch(
                        checked = isPrimaryKey,
                        onCheckedChange = { isPrimaryKey = it },
                        enabled = attributeType != AttributeType.KEY,
                    )
                }
                AnimatedVisibility(
                    visible = attributeType == AttributeType.KEY,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Text(
                        text = ERStrings.KEY_ATTRIBUTES_HINT,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = attributeType.display,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(CommonStrings.TYPE) },
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
                            text = { Text(AttributeType.NORMAL.display) },
                            onClick = {
                                attributeType = AttributeType.NORMAL
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(AttributeType.KEY.display) },
                            onClick = {
                                attributeType = AttributeType.KEY
                                isPrimaryKey = true
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(AttributeType.MULTIVALUED.display) },
                            onClick = {
                                attributeType = AttributeType.MULTIVALUED
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(AttributeType.DERIVED.display) },
                            onClick = {
                                attributeType = AttributeType.DERIVED
                                expanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(AttributeType.COMPOSITE.display) },
                            onClick = {
                                attributeType = AttributeType.COMPOSITE
                                isPrimaryKey = false
                                expanded = false
                            },
                        )
                    }
                }
                AnimatedVisibility(
                    visible = attributeType == AttributeType.MULTIVALUED,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    OutlinedTextField(
                        value = multiplicity,
                        onValueChange = { multiplicity = it },
                        label = { Text(ERStrings.MULTIPLICITY_EXAMPLE) },
                        placeholder = { Text("1..N") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                AnimatedVisibility(
                    visible = attributeType == AttributeType.COMPOSITE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${ERStrings.COMPONENTS} (${components.size})",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            IconButton(onClick = { showAddComponentDialog = true }) {
                                Icon(Icons.Default.Add, ERStrings.ADD_COMPONENT)
                            }
                        }
                        if (components.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                components.forEach { (id, name) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.secondaryContainer
                                            ),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(name, style = MaterialTheme.typography.bodyMedium)
                                            IconButton(
                                                onClick = {
                                                    components = components.filter { it.id != id }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    ERStrings.REMOVE_COMPONENT,
                                                    tint = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                ERStrings.NO_COMPONENTS,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalIsPrimaryKey = attributeType == AttributeType.KEY || isPrimaryKey
                        val base =
                            initialAttribute
                                ?: Attribute(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    type = attributeType,
                                )
                        val newAttribute =
                            base.copy(
                                name = name,
                                type = attributeType,
                                isPrimaryKey = finalIsPrimaryKey,
                                components =
                                    if (attributeType == AttributeType.COMPOSITE) components
                                    else emptyList(),
                                multiplicity =
                                    if (attributeType == AttributeType.MULTIVALUED) multiplicity
                                    else "",
                            )
                        onConfirm(newAttribute)
                    }
                },
                enabled =
                    name.isNotBlank() &&
                        (attributeType != AttributeType.COMPOSITE || components.isNotEmpty()),
            ) {
                Text(if (initialAttribute != null) CommonStrings.CONFIRM else CommonStrings.ADD)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(CommonStrings.CANCEL) } },
    )
    if (showAddComponentDialog) {
        var componentName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddComponentDialog = false },
            title = { Text(ERStrings.ADD_COMPONENT) },
            text = {
                OutlinedTextField(
                    value = componentName,
                    onValueChange = { componentName = it },
                    label = { Text(ERStrings.COMPONENT_NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (componentName.isNotBlank()) {
                            val newComponent =
                                Attribute(
                                    id = UUID.randomUUID().toString(),
                                    name = componentName,
                                    type = AttributeType.NORMAL,
                                    isPrimaryKey = false,
                                    components = emptyList(),
                                )
                            components = components + newComponent
                            showAddComponentDialog = false
                        }
                    },
                    enabled = componentName.isNotBlank(),
                ) {
                    Text(CommonStrings.ADD)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddComponentDialog = false }) {
                    Text(CommonStrings.CANCEL)
                }
            },
        )
    }
}

@Composable
fun CreateConnectionDialog(
    entities: List<Entity>,
    emptyMessage: String,
    onDismiss: () -> Unit,
    onConfirm: (entityId: String, cardinality: Cardinality) -> Unit,
) {
    ConnectionDialog(
        title = ERStrings.CREATE_CONNECTION,
        entities = entities,
        emptyMessage = emptyMessage,
        initialEntityId = null,
        initialCardinality = Cardinality.ONE,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun EditConnectionDialog(
    entities: List<Entity>,
    currentEntityId: String,
    currentCardinality: Cardinality,
    onDismiss: () -> Unit,
    onConfirm: (entityId: String, cardinality: Cardinality) -> Unit,
) {
    ConnectionDialog(
        title = ERStrings.EDIT_CONNECTION,
        entities = entities,
        emptyMessage = ERStrings.NO_ENTITY_AVAILABLE,
        initialEntityId = currentEntityId,
        initialCardinality = currentCardinality,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionDialog(
    title: String,
    entities: List<Entity>,
    emptyMessage: String,
    initialEntityId: String?,
    initialCardinality: Cardinality,
    onDismiss: () -> Unit,
    onConfirm: (entityId: String, cardinality: Cardinality) -> Unit,
) {
    var selectedEntity by remember { mutableStateOf(entities.find { it.id == initialEntityId }) }
    var selectedCardinality by remember { mutableStateOf(initialCardinality) }
    var expandedEntity by remember { mutableStateOf(false) }
    var expandedCardinality by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (entities.isEmpty()) {
                    Text(text = emptyMessage, modifier = Modifier.fillMaxWidth())
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expandedEntity,
                        onExpandedChange = { expandedEntity = it },
                    ) {
                        OutlinedTextField(
                            value = selectedEntity?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(ERStrings.ENTITY) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEntity)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEntity,
                            onDismissRequest = { expandedEntity = false },
                        ) {
                            entities.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.name) },
                                    onClick = {
                                        selectedEntity = it
                                        expandedEntity = false
                                    },
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedCardinality,
                        onExpandedChange = { expandedCardinality = it },
                    ) {
                        OutlinedTextField(
                            value = selectedCardinality.display,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(ERStrings.CARDINALITY) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedCardinality
                                )
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCardinality,
                            onDismissRequest = { expandedCardinality = false },
                        ) {
                            Cardinality.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.display) },
                                    onClick = {
                                        selectedCardinality = it
                                        expandedCardinality = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (entities.isNotEmpty()) {
                Button(
                    onClick = { selectedEntity?.let { onConfirm(it.id, selectedCardinality) } },
                    enabled = selectedEntity != null,
                ) {
                    Text(CommonStrings.CONFIRM)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (entities.isEmpty()) CommonStrings.CLOSE else CommonStrings.CANCEL)
            }
        },
    )
}
