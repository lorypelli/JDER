package com.jder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.jder.domain.model.SequenceFragment
import com.jder.domain.model.SequenceFragmentType
import com.jder.domain.model.SequenceLifelineType
import com.jder.domain.model.SequenceMessage
import com.jder.domain.model.SequenceMessageType
import com.jder.domain.model.SequenceOperand
import com.jder.ui.components.RadioOptionList
import com.jder.ui.strings.UiStrings
import java.util.UUID

@Composable
fun LifelinePropertiesDialog(
    lifelineId: String,
    lifelineName: String,
    lifelineType: SequenceLifelineType,
    lifelineDocumentation: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: SequenceLifelineType, documentation: String) -> Unit,
) {
    var name by remember(lifelineId) { mutableStateOf(lifelineName) }
    var type by remember(lifelineId) { mutableStateOf(lifelineType) }
    var documentation by remember(lifelineId) { mutableStateOf(lifelineDocumentation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.LIFELINE_PROPERTIES) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(UiStrings.NAME) },
                    placeholder = { Text(UiStrings.LIFELINE_NAME_EXAMPLE) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("${UiStrings.TYPE}:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(SequenceLifelineType.entries, type, { type = it }) { it.display }
                OutlinedTextField(
                    value = documentation,
                    onValueChange = { documentation = it },
                    label = { Text(UiStrings.DOCUMENTATION) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim(), type, documentation)
                    onDismiss()
                },
                enabled = name.isNotBlank(),
            ) {
                Text(UiStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CANCEL) } },
    )
}

@Composable
fun CreateSequenceMessageDialog(
    isSelfMessage: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (label: String, type: SequenceMessageType) -> Unit,
) {
    SequenceMessageDialog(
        title = UiStrings.NEW_MESSAGE,
        messageId = null,
        initialLabel = "",
        initialType = SequenceMessageType.SYNC,
        isSelfMessage = isSelfMessage,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun EditSequenceMessageDialog(
    message: SequenceMessage,
    onDismiss: () -> Unit,
    onConfirm: (label: String, type: SequenceMessageType) -> Unit,
) {
    SequenceMessageDialog(
        title = UiStrings.EDIT_MESSAGE,
        messageId = message.id,
        initialLabel = message.label,
        initialType = message.type,
        isSelfMessage = message.isSelfMessage,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun SequenceMessageDialog(
    title: String,
    messageId: String?,
    initialLabel: String,
    initialType: SequenceMessageType,
    isSelfMessage: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (label: String, type: SequenceMessageType) -> Unit,
) {
    var label by remember(messageId) { mutableStateOf(initialLabel) }
    var type by remember(messageId) { mutableStateOf(initialType) }
    val types =
        SequenceMessageType.entries.filter {
            !isSelfMessage ||
                (it != SequenceMessageType.CREATE && it != SequenceMessageType.DESTROY)
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(UiStrings.LABEL) },
                    placeholder = { Text(UiStrings.MESSAGE_LABEL_EXAMPLE) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("${UiStrings.TYPE}:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(types, type, { type = it }) { it.display }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(label.trim(), type)
                    onDismiss()
                }
            ) {
                Text(UiStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CANCEL) } },
    )
}

@Composable
fun SequenceFragmentPropertiesDialog(
    fragment: SequenceFragment,
    onDismiss: () -> Unit,
    onConfirm:
        (
            type: SequenceFragmentType,
            condition: String,
            operands: List<SequenceOperand>,
            width: Float,
            height: Float,
        ) -> Unit,
) {
    var type by remember(fragment.id) { mutableStateOf(fragment.type) }
    var condition by remember(fragment.id) { mutableStateOf(fragment.condition) }
    var operands by remember(fragment.id) { mutableStateOf(fragment.operands) }
    var width by remember(fragment.id) { mutableStateOf(fragment.width.toInt().toString()) }
    var height by remember(fragment.id) { mutableStateOf(fragment.height.toInt().toString()) }
    val isReference = type == SequenceFragmentType.REF
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.FRAGMENT_PROPERTIES) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("${UiStrings.TYPE}:", style = MaterialTheme.typography.labelMedium)
                RadioOptionList(SequenceFragmentType.entries, type, { type = it }) {
                    "${it.keyword} (${it.display})"
                }
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = {
                        Text(
                            if (isReference) UiStrings.REFERENCED_INTERACTION
                            else UiStrings.CONDITION
                        )
                    },
                    placeholder = {
                        Text(
                            if (isReference) UiStrings.REFERENCE_EXAMPLE
                            else UiStrings.CONDITION_EXAMPLE
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (type.hasOperands) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${UiStrings.ADDITIONAL_OPERANDS} (${operands.size})",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        IconButton(
                            onClick = {
                                val fragmentHeight = height.toFloatOrNull() ?: fragment.height
                                val lastOffset = operands.maxOfOrNull { it.offset } ?: 0f
                                operands =
                                    operands +
                                        SequenceOperand(
                                            id = UUID.randomUUID().toString(),
                                            offset = (lastOffset + fragmentHeight) / 2,
                                            condition = "else",
                                        )
                            }
                        ) {
                            Icon(Icons.Default.Add, UiStrings.ADD_OPERAND)
                        }
                    }
                    operands.forEach { (id, _, condition) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = condition,
                                onValueChange = { value ->
                                    operands =
                                        operands.map {
                                            if (it.id == id) it.copy(condition = value) else it
                                        }
                                },
                                label = { Text(UiStrings.CONDITION) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            IconButton(onClick = { operands = operands.filter { it.id != id } }) {
                                Icon(
                                    Icons.Default.Delete,
                                    UiStrings.REMOVE_OPERAND,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it.filter { it.isDigit() } },
                        label = { Text(UiStrings.WIDTH) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it.filter { it.isDigit() } },
                        label = { Text(UiStrings.HEIGHT) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        type,
                        condition.trim(),
                        if (type.hasOperands) {
                            operands.map { it.copy(condition = it.condition.trim()) }
                        } else {
                            emptyList()
                        },
                        width.toFloatOrNull()?.coerceAtLeast(120f) ?: fragment.width,
                        height.toFloatOrNull()?.coerceAtLeast(80f) ?: fragment.height,
                    )
                    onDismiss()
                }
            ) {
                Text(UiStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CANCEL) } },
    )
}
