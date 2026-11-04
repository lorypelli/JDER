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

private val stateColumnWidth = 132.dp
private val valueColumnWidth = 88.dp
private val stepColumnWidth = 44.dp
private const val MISSING = "—"

@Composable
fun MachineStateDialog(
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
        title = { Text("Proprietà Stato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isDuplicate,
                )
                if (isDuplicate) ErrorText("Esiste già uno stato con questo nome")
                if (kind.hasStateOutputs) {
                    OutlinedTextField(
                        value = output,
                        onValueChange = { output = it },
                        label = { Text("Uscita") },
                        placeholder = { Text("es. 0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText("Nella macchina di Moore l'uscita dipende solo dallo stato.")
                }
                SwitchRow("Stato iniziale", initial) { initial = it }
                SwitchRow("Stato finale", final) { final = it }
                HintText(
                    "Facoltativo: nella definizione classica le macchine di Mealy e di Moore " +
                        "non hanno stati finali."
                )
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
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun MachineTransitionDialog(
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
                    if (sourceName == targetName) "Da $sourceName a sé stesso"
                    else "Da $sourceName a $targetName",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Ingresso") },
                    placeholder = { Text("es. 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (repeated.isEmpty()) {
                    HintText(
                        "Più ingressi con lo stesso effetto si separano con una virgola, es. 0,1"
                    )
                } else {
                    ErrorText(
                        "$sourceName ha già una transizione con ingresso " +
                            "${repeated.joinToString(", ")}: la macchina non sarà deterministica"
                    )
                }
                if (kind.hasTransitionOutputs) {
                    OutlinedTextField(
                        value = output,
                        onValueChange = { output = it },
                        label = { Text("Uscita") },
                        placeholder = { Text("es. 0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText(
                        "Nella macchina di Mealy l'uscita dipende dallo stato e dall'ingresso: " +
                            "si scrive come ingresso/uscita."
                    )
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
                Text("Conferma")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

@Composable
fun TransitionTableDialog(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    onDismiss: () -> Unit,
) {
    val table = remember(diagram) { StateMachineAnalyzer.transitionTable(diagram) }
    val issues = remember(diagram, kind) { StateMachineAnalyzer.issues(diagram, kind) }
    val names = remember(diagram) { diagram.states.associate { it.id to it.name } }
    val clipboardManager = LocalClipboardManager.current
    val hasStates = diagram.states.isNotEmpty()
    fun cellText(cell: List<MachineTransition>): String =
        if (cell.isEmpty()) MISSING
        else
            cell
                .map { transition ->
                    val target = names[transition.targetId].orEmpty()
                    if (kind.hasTransitionOutputs) {
                        "$target/${transition.output.trim().ifEmpty { "?" }}"
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
        title = { Text("Tabella di Transizione") },
        text = {
            if (!hasStates) {
                Text("Aggiungi almeno uno stato per costruire la tabella di transizione.")
            } else {
                val cellHint =
                    if (kind.hasTransitionOutputs) {
                        "Ogni cella indica lo stato successivo e l'uscita (stato/uscita) per lo " +
                            "stato della riga e l'ingresso della colonna."
                    } else {
                        "Ogni cella indica lo stato successivo per lo stato della riga e " +
                            "l'ingresso della colonna; l'uscita dipende solo dallo stato."
                    }
                val markHint =
                    if (hasFinalStates) "Lo stato iniziale è segnato con →, quelli finali con *."
                    else "Lo stato iniziale è segnato con →."
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HintText("$cellHint $markHint")
                    TransitionTableContent(table, kind, ::stateLabel, ::cellText)
                    if (issues.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Da controllare:", style = MaterialTheme.typography.labelMedium)
                            issues.forEach { ErrorText("• $it") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } },
        dismissButton =
            if (hasStates) {
                {
                    TextButton(
                        onClick = {
                            val header = buildList {
                                add("Stato")
                                addAll(table.symbols)
                                if (kind.hasStateOutputs) add("Uscita")
                            }
                            val rows =
                                table.rows.map { row ->
                                    buildList {
                                        add(stateLabel(row.state))
                                        addAll(row.cells.map(::cellText))
                                        if (kind.hasStateOutputs) {
                                            add(row.state.output.trim().ifEmpty { MISSING })
                                        }
                                    }
                                }
                            val text =
                                (listOf(header) + rows).joinToString("\n") { it.joinToString("\t") }
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    ) {
                        Text("Copia")
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
                TableCell("Stato", stateColumnWidth, isHeader = true)
                TableSeparator()
                table.symbols.forEach { TableCell(it, valueColumnWidth, isHeader = true) }
                if (kind.hasStateOutputs) {
                    TableCell("Uscita", valueColumnWidth, isHeader = true, isOutput = true)
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
                        row.cells.forEach { cell ->
                            TableCell(
                                cellText(cell),
                                valueColumnWidth,
                                isError = StateMachineAnalyzer.isConflict(cell, kind),
                                isOutput = kind.hasTransitionOutputs && cell.isNotEmpty(),
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
    val run = remember(diagram, kind, inputs) { StateMachineAnalyzer.run(diagram, kind, inputs) }
    val alphabet = remember(diagram) { StateMachineAnalyzer.inputAlphabet(diagram) }
    val example = sequenceExample(alphabet)
    val clipboardManager = LocalClipboardManager.current
    val rows = remember(run) { simulationRows(run, kind) }
    val headers =
        if (kind.hasStateOutputs) listOf("t", "Stato", "Uscita", "Ingresso", "Stato successivo")
        else listOf("t", "Stato", "Ingresso", "Uscita", "Stato successivo")
    val outputIndex = headers.indexOf("Uscita")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simula una Sequenza") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = sequence,
                        onValueChange = onSequenceChange,
                        label = { Text("Sequenza di ingressi") },
                        placeholder = { Text("es. $example") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    HintText(
                        if (alphabet.isNotEmpty() && alphabet.all { it.length == 1 }) {
                            "Scrivi i simboli uno dopo l'altro (es. $example); " +
                                "spazi e virgole vengono ignorati."
                        } else {
                            "Separa i simboli con spazi o virgole."
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
                                "Sequenza di uscita: $outputs",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            val reached = run.reachedState?.name.orEmpty()
                            Text(
                                "Stato raggiunto: $reached",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            run.isAccepted?.let { isAccepted ->
                                Text(
                                    when {
                                        isAccepted ->
                                            "Sequenza accettata: $reached è uno stato finale"
                                        run.error != null ->
                                            "Sequenza rifiutata: la macchina si ferma prima " +
                                                "della fine"
                                        else ->
                                            "Sequenza rifiutata: $reached non è uno stato finale"
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } },
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
                                append("\n\nSequenza di uscita\t")
                                append(run.outputs.joinToString(" "))
                                run.isAccepted?.let {
                                    append(if (it) "\nEsito\taccettata" else "\nEsito\trifiutata")
                                }
                            }
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    ) {
                        Text("Copia")
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
    val current = run.reachedState ?: initial
    val last = listOf("${run.steps.size}", current.name, current.output.trim(), MISSING, MISSING)
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
