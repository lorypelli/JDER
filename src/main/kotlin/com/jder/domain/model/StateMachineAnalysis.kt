package com.jder.domain.model

private const val MAX_LISTED_ISSUES = 6
private val sequenceSeparators = Regex("[\\s,;]+")
private val symbolOrder =
    compareBy<String>(
        { it.toLongOrNull() == null },
        { it.toLongOrNull() },
        { it.lowercase() },
        { it },
    )

class TransitionTableRow(val state: MachineState, val cells: List<List<MachineTransition>>)

class TransitionTable(val symbols: List<String>, val rows: List<TransitionTableRow>)

class SimulationStep(
    val state: MachineState,
    val input: String,
    val output: String,
    val nextState: MachineState,
)

class StateMachineRun(
    val initialState: MachineState?,
    val steps: List<SimulationStep>,
    val outputs: List<String>,
    val error: String?,
    val isAccepted: Boolean? = null,
) {
    val reachedState: MachineState?
        get() = steps.lastOrNull()?.nextState ?: initialState
}

object StateMachineAnalyzer {
    fun inputAlphabet(diagram: StateMachineDiagram): List<String> =
        diagram.transitions.flatMap { it.inputSymbols }.distinct().sortedWith(symbolOrder)

    fun transitionTable(diagram: StateMachineDiagram): TransitionTable {
        val symbols = inputAlphabet(diagram)
        val outgoing = diagram.transitions.groupBy { it.sourceId }
        val rows =
            diagram.states.map { state ->
                val transitions = outgoing[state.id].orEmpty()
                TransitionTableRow(
                    state,
                    symbols.map { symbol -> transitions.filter { symbol in it.inputSymbols } },
                )
            }
        return TransitionTable(symbols, rows)
    }

    fun isConflict(cell: List<MachineTransition>, kind: StateMachineKind): Boolean =
        cell
            .distinctBy { it.targetId to it.output.trim().takeIf { kind.hasTransitionOutputs } }
            .size > 1

    fun issues(diagram: StateMachineDiagram, kind: StateMachineKind): List<String> = buildList {
        if (diagram.states.isEmpty()) return@buildList
        val names = diagram.states.associate { it.id to it.name }
        val initial = diagram.states.find { it.id == diagram.initialStateId }
        if (initial == null) {
            add("Manca lo stato iniziale: selezionane uno e usa «Imposta come Stato Iniziale».")
        }
        val table = transitionTable(diagram)
        val conflicts = mutableListOf<String>()
        val missing = mutableListOf<String>()
        table.rows.forEach { row ->
            row.cells.forEachIndexed { index, cell ->
                val case = "${row.state.name} con ingresso ${table.symbols[index]}"
                when {
                    cell.isEmpty() -> missing += case
                    isConflict(cell, kind) -> conflicts += case
                }
            }
        }
        if (conflicts.isNotEmpty()) {
            add(
                "Più transizioni con lo stesso ingresso (la macchina non è deterministica): " +
                    summarize(conflicts)
            )
        }
        if (missing.isNotEmpty()) add("Transizioni non definite: ${summarize(missing)}")
        val unlabeled =
            diagram.transitions
                .filter { it.inputSymbols.isEmpty() }
                .map { "${names[it.sourceId]} → ${names[it.targetId]}" }
        if (unlabeled.isNotEmpty()) add("Transizioni senza ingresso: ${summarize(unlabeled)}")
        val withoutOutput =
            if (kind.hasStateOutputs) {
                diagram.states.filter { it.output.isBlank() }.map { it.name }
            } else {
                diagram.transitions
                    .filter { it.output.isBlank() }
                    .map { "${names[it.sourceId]} → ${names[it.targetId]} (${it.input.trim()})" }
            }
        if (withoutOutput.isNotEmpty()) {
            val subject = if (kind.hasStateOutputs) "Stati" else "Transizioni"
            add("$subject senza uscita: ${summarize(withoutOutput)}")
        }
        if (initial != null) {
            val reachable = reachableStateIds(diagram, initial.id)
            val unreachable = diagram.states.filter { it.id !in reachable }.map { it.name }
            if (unreachable.isNotEmpty()) {
                add("Stati non raggiungibili dallo stato iniziale: ${summarize(unreachable)}")
            }
        }
    }

    fun parseInputSequence(diagram: StateMachineDiagram, text: String): List<String> {
        val alphabet = inputAlphabet(diagram)
        val isSingleCharacter = alphabet.isNotEmpty() && alphabet.all { it.length == 1 }
        return if (isSingleCharacter) {
            text.filterNot { it.isWhitespace() || it == ',' || it == ';' }.map { it.toString() }
        } else {
            text.split(sequenceSeparators).filter { it.isNotEmpty() }
        }
    }

    fun run(diagram: StateMachineDiagram, kind: StateMachineKind, inputs: List<String>) =
        Simulation(diagram, kind).run(inputs)

    private fun reachableStateIds(diagram: StateMachineDiagram, initialId: String): Set<String> {
        val outgoing = diagram.transitions.groupBy({ it.sourceId }, { it.targetId })
        val visited = mutableSetOf(initialId)
        val queue = ArrayDeque(listOf(initialId))
        while (queue.isNotEmpty()) {
            outgoing[queue.removeFirst()].orEmpty().forEach { if (visited.add(it)) queue += it }
        }
        return visited
    }

    private fun summarize(items: List<String>): String {
        val listed = items.take(MAX_LISTED_ISSUES).joinToString(", ")
        val others = items.size - MAX_LISTED_ISSUES
        return if (others > 0) "$listed e altri $others" else listed
    }

    private class Simulation(
        private val diagram: StateMachineDiagram,
        private val kind: StateMachineKind,
    ) {
        private val statesById = diagram.states.associateBy { it.id }
        private val outgoing = diagram.transitions.groupBy { it.sourceId }
        private val hasFinalStates = diagram.states.any { it.isFinal }

        fun run(inputs: List<String>): StateMachineRun {
            val initial =
                statesById[diagram.initialStateId]
                    ?: return StateMachineRun(
                        null,
                        emptyList(),
                        emptyList(),
                        "Imposta prima lo stato iniziale della macchina.",
                    )
            val steps = mutableListOf<SimulationStep>()
            val outputs = mutableListOf<String>()
            if (kind.hasStateOutputs) outputs += initial.output.trim()
            var current = initial
            inputs.forEachIndexed { index, input ->
                val candidates = outgoing[current.id].orEmpty().filter { input in it.inputSymbols }
                val error =
                    when {
                        candidates.isEmpty() ->
                            "t = $index: dallo stato ${current.name} non esce nessuna " +
                                "transizione con ingresso $input."
                        isConflict(candidates, kind) ->
                            "t = $index: dallo stato ${current.name} escono più transizioni " +
                                "con ingresso $input (la macchina non è deterministica)."
                        else -> null
                    }
                if (error != null) {
                    val isRejected = hasFinalStates && candidates.isEmpty()
                    return StateMachineRun(
                        initial,
                        steps,
                        outputs,
                        error,
                        isAccepted = false.takeIf { isRejected },
                    )
                }
                val transition = candidates.first()
                val next =
                    statesById[transition.targetId]
                        ?: return StateMachineRun(
                            initial,
                            steps,
                            outputs,
                            "t = $index: la transizione porta a uno stato inesistente.",
                        )
                val output =
                    if (kind.hasStateOutputs) current.output.trim() else transition.output.trim()
                steps += SimulationStep(current, input, output, next)
                outputs += if (kind.hasStateOutputs) next.output.trim() else output
                current = next
            }
            return StateMachineRun(
                initial,
                steps,
                outputs,
                null,
                isAccepted = current.isFinal.takeIf { hasFinalStates },
            )
        }
    }
}
