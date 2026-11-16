package com.jder.strings

object LogicStrings {
    val INPUT by text("Ingresso", "Input")
    val OUTPUT by text("Uscita", "Output")
    val INPUTS by text("Ingressi", "Inputs")
    val INPUT_KIND by text("ingresso", "input")
    val OUTPUT_KIND by text("uscita", "output")
    val LOGIC_GATES by text("Porte Logiche", "Logic Gates")
    val WIRE by text("Collegamento", "Wire")
    val COMPONENT by text("Componente", "Component")
    val INPUT_TOOL by
        text("Ingresso (clicca sul valore per commutarlo)", "Input (click the value to toggle it)")
    val WIRE_TOOL by
        text(
            "Collegamento (clicca la sorgente e poi la destinazione, oppure trascina da un pin)",
            "Wire (click the source and then the target, or drag from a pin)",
        )
    val INPUT_ACTIONS by text("Azioni Ingresso", "Input Actions")
    val OUTPUT_ACTIONS by text("Azioni Uscita", "Output Actions")
    val GATE_ACTIONS by text("Azioni Porta Logica", "Logic Gate Actions")
    val WIRE_ACTIONS by text("Azioni Collegamento", "Wire Actions")
    val INPUT_PROPERTIES by text("Proprietà Ingresso", "Input Properties")
    val OUTPUT_PROPERTIES by text("Proprietà Uscita", "Output Properties")
    val GATE_PROPERTIES by text("Proprietà Porta Logica", "Logic Gate Properties")
    val TOGGLE_VALUE by text("Commuta Valore", "Toggle Value")
    val ADD_INPUT by text("Aggiungi Ingresso", "Add Input")
    val REMOVE_INPUT by text("Rimuovi Ingresso", "Remove Input")
    val DELETE_WIRE by text("Elimina Collegamento", "Delete Wire")
    val TRUTH_TABLE by text("Tabella di verità", "Truth table")
    val TRUTH_TABLE_ELLIPSIS by text("Tabella di Verità...", "Truth Table...")
    val SHOW_SIMULATION by text("Mostra i valori della simulazione", "Show the simulation values")
    val HIDE_SIMULATION by text("Nascondi i valori della simulazione", "Hide the simulation values")
    val SHOW_SIMULATION_MENU by text("Mostra Valori della Simulazione", "Show Simulation Values")
    val CIRCUIT_OSCILLATES by
        text(
            "Il circuito oscilla: alcuni valori non sono determinabili",
            "The circuit oscillates: some values cannot be determined",
        )
    val COMPONENT_UPDATED by text("Componente modificato", "Component updated")
    val INPUT_DELETED by text("Ingresso eliminato", "Input deleted")
    val OUTPUT_DELETED by text("Uscita eliminata", "Output deleted")
    val GATE_DELETED by text("Porta eliminata", "Gate deleted")
    val WIRE_CREATED by text("Collegamento creato", "Wire created")
    val WIRE_DELETED by text("Collegamento eliminato", "Wire deleted")
    val INVALID_WIRE by text("Collegamento non valido", "Invalid wire")
    val VALUE by text("Valore", "Value")
    val CURRENT_VALUE by text("Valore attuale", "Current value")
    val HIGH_VALUE by text("1 (alto)", "1 (high)")
    val LOW_VALUE by text("0 (basso)", "0 (low)")
    val UNDEFINED_VALUE by text("indefinito", "undefined")
    val INPUT_COUNT by text("Numero di ingressi", "Number of inputs")
    val GATE_TYPE by text("Tipo di porta", "Gate type")
    val GATE_LABEL_EXAMPLE by text("Opzionale, es. U1", "Optional, e.g. U1")
    val BOOLEAN_EXPRESSION by text("Espressione booleana", "Boolean expression")
    val EXPRESSIONS by text("Espressioni", "Expressions")
    val TRUTH_TABLE_NEEDS_OUTPUT by
        text(
            "Aggiungi almeno un'uscita al circuito per calcolare la tabella di verità.",
            "Add at least one output to the circuit to compute the truth table.",
        )
    val LOGIC_EMPTY_HINT by
        text(
            "Scegli un ingresso, un'uscita o una porta logica dalla barra degli strumenti e " +
                "clicca sulla lavagna.\nPer collegare due elementi trascina da un pin all'altro.",
            "Choose an input, an output or a logic gate from the toolbar and click on the " +
                "canvas.\nTo connect two elements, drag from one pin to the other.",
        )

    fun gate(gate: String) = localized("Porta $gate", "$gate Gate")

    fun logicGateKind(gate: String) = localized("porta logica $gate", "$gate logic gate")

    fun inputNumber(number: Int) = localized("ingresso $number", "input $number")

    fun tooManyTruthTableInputs(count: Int, max: Int) =
        localized(
            "Il circuito ha $count ingressi: la tabella di verità ne supporta al massimo $max.",
            "The circuit has $count inputs: the truth table supports at most $max.",
        )
}
