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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
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
import com.jder.domain.model.MachineState
import com.jder.domain.model.MachineTransition
import com.jder.domain.model.StateMachineAnalyzer
import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.StateMachineRun
import com.jder.domain.model.TransitionTable
import com.jder.domain.model.parseInputSymbols
import com.jder.ui.strings.UiStrings

private val stateColumnWidth = 132.dp
private val valueColumnWidth = 88.dp
private val stepColumnWidth = 44.dp
private const val MISSING = "—"

@Composable
fun MachineStatePropertiesDialog(
    machineState: MachineState,
    kind: StateMachineKind,
    isInitial: Boolean,
    otherNames: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, output: String, isInitial: Boolean, isFinal: Boolean) -> Unit,
) {
    var name by remember(machineState.id) { mutableStateOf(machineState.name) }
    var output by remember(machineState.id) { mutableStateOf(machineState.output) }
    var initial by remember(machineState.id) { mutableStateOf(isInitial) }
    var final by remember(machineState.id) { mutableStateOf(machineState.isFinal) }
    val isDuplicate = name.trim() in otherNames
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.STATE_PROPERTIES) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(UiStrings.NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isDuplicate,
                )
                if (isDuplicate) ErrorText(UiStrings.DUPLICATE_STATE_NAME)
                if (kind.hasStateOutputs) {
                    OutlinedTextField(
                        value = output,
                        onValueChange = { output = it },
                        label = { Text(UiStrings.OUTPUT) },
                        placeholder = { Text(UiStrings.example("0")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText(UiStrings.MOORE_STATE_OUTPUT_HINT)
                }
                SwitchRow(UiStrings.INITIAL_STATE, initial) { initial = it }
                SwitchRow(UiStrings.FINAL_STATE, final) { final = it }
                HintText(UiStrings.FINAL_STATES_HINT)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim(), output.trim(), initial, final)
                    onDismiss()
                },
                enabled = name.isNotBlank() && !isDuplicate,
            ) {
                Text(UiStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CANCEL) } },
    )
}

@Composable
fun MachineTransitionPropertiesDialog(
    title: String,
    kind: StateMachineKind,
    sourceName: String,
    targetName: String,
    initialInput: String,
    initialOutput: String,
    usedSymbols: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (input: String, output: String) -> Unit,
) {
    var input by remember { mutableStateOf(initialInput) }
    var output by remember { mutableStateOf(initialOutput) }
    val repeated = parseInputSymbols(input).filter { it in usedSymbols }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (sourceName == targetName) UiStrings.selfTransition(sourceName)
                    else UiStrings.transitionBetween(sourceName, targetName),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(UiStrings.INPUT) },
                    placeholder = { Text(UiStrings.example("1")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (repeated.isEmpty()) {
                    HintText(UiStrings.MULTIPLE_INPUTS_HINT)
                } else {
                    ErrorText(UiStrings.repeatedInputs(sourceName, repeated.joinToString(", ")))
                }
                if (kind.hasTransitionOutputs) {
                    OutlinedTextField(
                        value = output,
                        onValueChange = { output = it },
                        label = { Text(UiStrings.OUTPUT) },
                        placeholder = { Text(UiStrings.example("0")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText(UiStrings.MEALY_TRANSITION_OUTPUT_HINT)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(input.trim(), output.trim())
                    onDismiss()
                },
                enabled = parseInputSymbols(input).isNotEmpty(),
            ) {
                Text(UiStrings.CONFIRM)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CANCEL) } },
    )
}

@Composable
fun TransitionTableDialog(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    onDismiss: () -> Unit,
) {
    val table = remember(diagram) { StateMachineAnalyzer.transitionTable(diagram) }
    val issues =
        remember(diagram, kind, UiStrings.language) { StateMachineAnalyzer.issues(diagram, kind) }
    val names = remember(diagram) { diagram.states.associate { it.id to it.name } }
    val clipboardManager = LocalClipboardManager.current
    val hasStates = diagram.states.isNotEmpty()
    fun cellText(cell: List<MachineTransition>): String =
        if (cell.isEmpty()) MISSING
        else
            cell
                .map { (_, _, targetId, _, output) ->
                    val target = names[targetId].orEmpty()
                    if (kind.hasTransitionOutputs) {
                        "$target/${output.trim().ifEmpty { "?" }}"
                    } else {
                        target
                    }
                }
                .distinct()
                .joinToString(", ")
    val hasFinalStates = diagram.states.any { it.isFinal }
    fun stateLabel(state: MachineState): String {
        val initialMark = if (state.id == diagram.initialStateId) "→" else ""
        val marks = initialMark + if (state.isFinal) "*" else ""
        return if (marks.isEmpty()) state.name else "$marks ${state.name}"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.TRANSITION_TABLE_TITLE) },
        text = {
            if (!hasStates) {
                Text(UiStrings.NO_STATES_FOR_TABLE)
            } else {
                val cellHint =
                    if (kind.hasTransitionOutputs) UiStrings.MEALY_TABLE_HINT
                    else UiStrings.MOORE_TABLE_HINT
                val markHint =
                    if (hasFinalStates) UiStrings.INITIAL_AND_FINAL_MARKS_HINT
                    else UiStrings.INITIAL_MARK_HINT
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HintText("$cellHint $markHint")
                    TransitionTableContent(table, kind, ::stateLabel, ::cellText)
                    if (issues.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${UiStrings.TO_CHECK}:",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            issues.forEach { ErrorText("• $it") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CLOSE) } },
        dismissButton =
            if (hasStates) {
                {
                    TextButton(
                        onClick = {
                            val header = buildList {
                                add(UiStrings.STATE)
                                addAll(table.symbols)
                                if (kind.hasStateOutputs) add(UiStrings.OUTPUT)
                            }
                            val rows =
                                table.rows.map { (state, cells) ->
                                    buildList {
                                        add(stateLabel(state))
                                        addAll(cells.map(::cellText))
                                        if (kind.hasStateOutputs) {
                                            add(state.output.trim().ifEmpty { MISSING })
                                        }
                                    }
                                }
                            val text =
                                (listOf(header) + rows).joinToString("\n") { it.joinToString("\t") }
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    ) {
                        Text(UiStrings.COPY)
                    }
                }
            } else null,
    )
}

@Composable
private fun TransitionTableContent(
    table: TransitionTable,
    kind: StateMachineKind,
    stateLabel: (MachineState) -> String,
    cellText: (List<MachineTransition>) -> String,
) {
    val outputColumns = if (kind.hasStateOutputs) 1 else 0
    val tableWidth =
        stateColumnWidth + valueColumnWidth * (table.symbols.size + outputColumns) + 9.dp
    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Column(modifier = Modifier.width(tableWidth)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TableCell(UiStrings.STATE, stateColumnWidth, isHeader = true)
                TableSeparator()
                table.symbols.forEach { TableCell(it, valueColumnWidth, isHeader = true) }
                if (kind.hasStateOutputs) {
                    TableCell(UiStrings.OUTPUT, valueColumnWidth, isHeader = true, isOutput = true)
                }
            }
            Divider()
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                itemsIndexed(table.rows) { index, row ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(stripeColor(index)),
                    ) {
                        TableCell(stateLabel(row.state), stateColumnWidth, isHeader = true)
                        TableSeparator()
                        row.cells.forEach {
                            TableCell(
                                cellText(it),
                                valueColumnWidth,
                                isError = StateMachineAnalyzer.isConflict(it, kind),
                                isOutput = kind.hasTransitionOutputs && it.isNotEmpty(),
                            )
                        }
                        if (kind.hasStateOutputs) {
                            TableCell(
                                row.state.output.trim().ifEmpty { MISSING },
                                valueColumnWidth,
                                isOutput = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateMachineSimulationDialog(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    sequence: String,
    onSequenceChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val inputs =
        remember(diagram, sequence) { StateMachineAnalyzer.parseInputSequence(diagram, sequence) }
    val run =
        remember(diagram, kind, inputs, UiStrings.language) {
            StateMachineAnalyzer.run(diagram, kind, inputs)
        }
    val alphabet = remember(diagram) { StateMachineAnalyzer.inputAlphabet(diagram) }
    val example = sequenceExample(alphabet)
    val clipboardManager = LocalClipboardManager.current
    val rows = remember(run) { simulationRows(run, kind) }
    val headers =
        if (kind.hasStateOutputs) {
            listOf("t", UiStrings.STATE, UiStrings.OUTPUT, UiStrings.INPUT, UiStrings.NEXT_STATE)
        } else {
            listOf("t", UiStrings.STATE, UiStrings.INPUT, UiStrings.OUTPUT, UiStrings.NEXT_STATE)
        }
    val outputIndex = headers.indexOf(UiStrings.OUTPUT)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiStrings.SIMULATE_SEQUENCE_TITLE) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = sequence,
                        onValueChange = onSequenceChange,
                        label = { Text(UiStrings.INPUT_SEQUENCE) },
                        placeholder = { Text(UiStrings.example(example)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText(
                        if (alphabet.isNotEmpty() && alphabet.all { it.length == 1 }) {
                            UiStrings.typeSymbolsHint(example)
                        } else {
                            UiStrings.SEPARATE_SYMBOLS_HINT
                        }
                    )
                }
                if (run.initialState != null && rows.isNotEmpty()) {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Column(
                            modifier =
                                Modifier.width(
                                    stepColumnWidth + valueColumnWidth * 2 + stateColumnWidth * 2
                                )
                        ) {
                            SimulationRow(headers, outputIndex, isHeader = true)
                            Divider()
                            LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                                itemsIndexed(rows) { index, row ->
                                    Box(modifier = Modifier.background(stripeColor(index))) {
                                        SimulationRow(row, outputIndex)
                                    }
                                }
                            }
                        }
                    }
                    if (inputs.isNotEmpty()) {
                        val outputs = run.outputs.joinToString(" ") { it.ifEmpty { "?" } }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "${UiStrings.OUTPUT_SEQUENCE}: $outputs",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            val reached = run.reachedState?.name.orEmpty()
                            Text(
                                "${UiStrings.REACHED_STATE}: $reached",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            run.isAccepted?.let { isAccepted ->
                                Text(
                                    when {
                                        isAccepted -> UiStrings.sequenceAccepted(reached)
                                        run.error != null -> UiStrings.SEQUENCE_STOPPED
                                        else -> UiStrings.sequenceRejected(reached)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color =
                                        if (isAccepted) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                run.error?.let { ErrorText(it) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiStrings.CLOSE) } },
        dismissButton =
            if (rows.isNotEmpty() && inputs.isNotEmpty()) {
                {
                    TextButton(
                        onClick = {
                            val text = buildString {
                                append(
                                    (listOf(headers) + rows).joinToString("\n") {
                                        it.joinToString("\t")
                                    }
                                )
                                append("\n\n${UiStrings.OUTPUT_SEQUENCE}\t")
                                append(run.outputs.joinToString(" "))
                                run.isAccepted?.let {
                                    append("\n${UiStrings.OUTCOME}\t")
                                    append(if (it) UiStrings.ACCEPTED else UiStrings.REJECTED)
                                }
                            }
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    ) {
                        Text(UiStrings.COPY)
                    }
                }
            } else null,
    )
}

private fun simulationRows(run: StateMachineRun, kind: StateMachineKind): List<List<String>> {
    val initial = run.initialState ?: return emptyList()
    val rows =
        run.steps.mapIndexed { index, step ->
            if (kind.hasStateOutputs) {
                listOf("$index", step.state.name, step.output, step.input, step.nextState.name)
            } else {
                listOf("$index", step.state.name, step.input, step.output, step.nextState.name)
            }
        }
    if (!kind.hasStateOutputs) return rows.map { row -> row.map { it.ifEmpty { "?" } } }
    val (_, name, _, _, output) = run.reachedState ?: initial
    val last = listOf("${run.steps.size}", name, output.trim(), MISSING, MISSING)
    return (rows + listOf(last)).map { row -> row.map { it.ifEmpty { "?" } } }
}

private fun sequenceExample(alphabet: List<String>): String =
    when {
        alphabet.isEmpty() || listOf("0", "1").containsAll(alphabet) -> "0110"
        alphabet.all { it.length == 1 } -> alphabet.take(4).joinToString("")
        else -> alphabet.take(3).joinToString(" ")
    }

@Composable
private fun SimulationRow(cells: List<String>, outputIndex: Int, isHeader: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        cells.forEachIndexed { index, text ->
            val width =
                when (index) {
                    0 -> stepColumnWidth
                    1,
                    4 -> stateColumnWidth
                    else -> valueColumnWidth
                }
            TableCell(text, width, isHeader = isHeader, isOutput = index == outputIndex)
        }
    }
}

@Composable
private fun stripeColor(index: Int): Color =
    if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    else Color.Transparent

@Composable
private fun TableSeparator() {
    Box(
        modifier =
            Modifier.padding(horizontal = 4.dp)
                .width(1.dp)
                .heightIn(min = 28.dp)
                .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false,
    isOutput: Boolean = false,
    isError: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 4.dp, vertical = 4.dp),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = if (isHeader || isOutput || isError) FontWeight.Bold else FontWeight.Normal,
        color =
            when {
                isError -> MaterialTheme.colorScheme.error
                isOutput -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.widthIn(max = 520.dp),
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.widthIn(max = 520.dp),
    )
}
