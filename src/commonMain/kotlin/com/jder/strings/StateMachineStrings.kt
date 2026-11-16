package com.jder.strings

object StateMachineStrings {
    val STATE by text("Stato", "State")
    val TRANSITION by text("Transizione", "Transition")
    val CREATE_STATE by text("Crea Stato", "Create State")
    val CREATE_TRANSITION by
        text(
            "Crea Transizione (clicca lo stato di partenza e poi quello di arrivo, " +
                "oppure trascina dal bordo di uno stato)",
            "Create Transition (click the start state and then the end state, " +
                "or drag from the edge of a state)",
        )
    val STATE_ACTIONS by text("Azioni Stato", "State Actions")
    val TRANSITION_ACTIONS by text("Azioni Transizione", "Transition Actions")
    val STATE_PROPERTIES by text("Proprietà Stato", "State Properties")
    val TRANSITION_PROPERTIES by text("Proprietà Transizione", "Transition Properties")
    val NEW_TRANSITION by text("Nuova Transizione", "New Transition")
    val STATE_UPDATED by text("Stato modificato", "State updated")
    val TRANSITION_CREATED by text("Transizione creata", "Transition created")
    val TRANSITION_UPDATED by text("Transizione modificata", "Transition updated")
    val STATE_DELETED by text("Stato eliminato", "State deleted")
    val TRANSITION_DELETED by text("Transizione eliminata", "Transition deleted")
    val EDIT_TRANSITION by text("Modifica transizione", "Edit transition")
    val DELETE_TRANSITION by text("Elimina transizione", "Delete transition")
    val SET_INITIAL_STATE by text("Imposta come Stato Iniziale", "Set as Initial State")
    val SET_FINAL_STATE by text("Imposta come Stato Finale", "Set as Final State")
    val REMOVE_FROM_FINAL_STATES by text("Rimuovi dagli Stati Finali", "Remove from Final States")
    val INITIAL_STATE_SET by text("Stato iniziale impostato", "Initial state set")
    val FINAL_STATE_SET by text("Stato finale impostato", "Final state set")
    val STATE_REMOVED_FROM_FINAL by
        text("Stato rimosso dagli stati finali", "State removed from the final states")
    val RESET_SHAPE by text("Ripristina Forma", "Reset Shape")
    val INITIAL by text("Iniziale", "Initial")
    val FINAL by text("Finale", "Final")
    val INITIAL_STATE by text("Stato iniziale", "Initial state")
    val FINAL_STATE by text("Stato finale", "Final state")
    val NEXT_STATE by text("Stato successivo", "Next state")
    val UNKNOWN_STATE by text("Stato sconosciuto", "Unknown state")
    val OUTGOING_TRANSITIONS by text("Transizioni uscenti", "Outgoing transitions")
    val DUPLICATE_STATE_NAME by
        text("Esiste già uno stato con questo nome", "A state with this name already exists")
    val MOORE_STATE_OUTPUT_HINT by
        text(
            "Nella macchina di Moore l'uscita dipende solo dallo stato.",
            "In a Moore machine the output depends only on the state.",
        )
    val MEALY_TRANSITION_OUTPUT_HINT by
        text(
            "Nella macchina di Mealy l'uscita dipende dallo stato e dall'ingresso: " +
                "si scrive come ingresso/uscita.",
            "In a Mealy machine the output depends on the state and on the input: " +
                "it is written as input/output.",
        )
    val FINAL_STATES_HINT by
        text(
            "Facoltativo: nella definizione classica le macchine di Mealy e di Moore " +
                "non hanno stati finali.",
            "Optional: in the classic definition, Mealy and Moore machines " +
                "have no final states.",
        )
    val MULTIPLE_INPUTS_HINT by
        text(
            "Più ingressi con lo stesso effetto si separano con una virgola, es. 0,1",
            "Separate multiple inputs with the same effect with a comma, e.g. 0,1",
        )
    val TRANSITION_TABLE by text("Tabella di transizione", "Transition table")
    val TRANSITION_TABLE_TITLE by text("Tabella di Transizione", "Transition Table")
    val TRANSITION_TABLE_ELLIPSIS by text("Tabella di Transizione...", "Transition Table...")
    val NO_STATES_FOR_TABLE by
        text(
            "Aggiungi almeno uno stato per costruire la tabella di transizione.",
            "Add at least one state to build the transition table.",
        )
    val MEALY_TABLE_HINT by
        text(
            "Ogni cella indica lo stato successivo e l'uscita (stato/uscita) per lo " +
                "stato della riga e l'ingresso della colonna.",
            "Each cell shows the next state and the output (state/output) for the " +
                "state of the row and the input of the column.",
        )
    val MOORE_TABLE_HINT by
        text(
            "Ogni cella indica lo stato successivo per lo stato della riga e " +
                "l'ingresso della colonna; l'uscita dipende solo dallo stato.",
            "Each cell shows the next state for the state of the row and the input " +
                "of the column; the output depends only on the state.",
        )
    val INITIAL_AND_FINAL_MARKS_HINT by
        text(
            "Lo stato iniziale è segnato con →, quelli finali con *.",
            "The initial state is marked with →, the final ones with *.",
        )
    val INITIAL_MARK_HINT by
        text("Lo stato iniziale è segnato con →.", "The initial state is marked with →.")
    val TO_CHECK by text("Da controllare", "To check")
    val SIMULATE_SEQUENCE by text("Simula una sequenza di ingressi", "Simulate an input sequence")
    val SIMULATE_SEQUENCE_TITLE by text("Simula una Sequenza", "Simulate a Sequence")
    val SIMULATE_SEQUENCE_ELLIPSIS by text("Simula una Sequenza...", "Simulate a Sequence...")
    val INPUT_SEQUENCE by text("Sequenza di ingressi", "Input sequence")
    val OUTPUT_SEQUENCE by text("Sequenza di uscita", "Output sequence")
    val REACHED_STATE by text("Stato raggiunto", "Reached state")
    val SEPARATE_SYMBOLS_HINT by
        text("Separa i simboli con spazi o virgole.", "Separate the symbols with spaces or commas.")
    val SEQUENCE_STOPPED by
        text(
            "Sequenza rifiutata: la macchina si ferma prima della fine",
            "Sequence rejected: the machine stops before the end",
        )
    val OUTCOME by text("Esito", "Outcome")
    val ACCEPTED by text("accettata", "accepted")
    val REJECTED by text("rifiutata", "rejected")
    val SET_INITIAL_STATE_FIRST by
        text(
            "Imposta prima lo stato iniziale della macchina.",
            "Set the initial state of the machine first.",
        )
    val MEALY_EMPTY_HINT by
        text(
            "Nella macchina di Mealy l'uscita si scrive sulle transizioni, come " +
                "ingresso/uscita.",
            "In a Mealy machine the output is written on the transitions, as input/output.",
        )
    val MOORE_EMPTY_HINT by
        text(
            "Nella macchina di Moore l'uscita si scrive negli stati, sotto il nome.",
            "In a Moore machine the output is written in the states, below the name.",
        )
    val STATE_MACHINE_EMPTY_HINT by
        text(
            "Scegli Stato dalla barra degli strumenti e clicca sulla lavagna.\n" +
                "Per creare una transizione trascina dal bordo di uno stato fino allo stato di " +
                "arrivo (anche lo stesso).",
            "Choose State from the toolbar and click on the canvas.\n" +
                "To create a transition, drag from the edge of a state to the end state " +
                "(even the same one).",
        )

    fun transitionCounts(outgoing: Int, incoming: Int) =
        localized(
            "Transizioni uscenti: $outgoing, entranti: $incoming",
            "Outgoing transitions: $outgoing, incoming: $incoming",
        )

    fun towards(state: String) = localized("verso $state", "to $state")

    fun transitionBetween(source: String, target: String) =
        localized("Da $source a $target", "From $source to $target")

    fun selfTransition(state: String) = localized("Da $state a sé stesso", "From $state to itself")

    fun repeatedInputs(state: String, inputs: String) =
        localized(
            "$state ha già una transizione con ingresso $inputs: " +
                "la macchina non sarà deterministica",
            "$state already has a transition with input $inputs: " +
                "the machine will not be deterministic",
        )

    fun typeSymbolsHint(example: String) =
        localized(
            "Scrivi i simboli uno dopo l'altro (es. $example); spazi e virgole vengono ignorati.",
            "Write the symbols one after the other (e.g. $example); spaces and commas are " +
                "ignored.",
        )

    fun sequenceAccepted(state: String) =
        localized(
            "Sequenza accettata: $state è uno stato finale",
            "Sequence accepted: $state is a final state",
        )

    fun sequenceRejected(state: String) =
        localized(
            "Sequenza rifiutata: $state non è uno stato finale",
            "Sequence rejected: $state is not a final state",
        )

    fun missingInitialState(action: String) =
        localized(
            "Manca lo stato iniziale: selezionane uno e usa «$action».",
            "The initial state is missing: select one and use “$action”.",
        )

    fun stateWithInput(state: String, input: String) =
        localized("$state con ingresso $input", "$state with input $input")

    fun nondeterministicTransitions(cases: String) =
        localized(
            "Più transizioni con lo stesso ingresso (la macchina non è deterministica): $cases",
            "Several transitions with the same input (the machine is not deterministic): $cases",
        )

    fun undefinedTransitions(cases: String) =
        localized("Transizioni non definite: $cases", "Undefined transitions: $cases")

    fun transitionsWithoutInput(transitions: String) =
        localized(
            "Transizioni senza ingresso: $transitions",
            "Transitions without input: $transitions",
        )

    fun statesWithoutOutput(states: String) =
        localized("Stati senza uscita: $states", "States without output: $states")

    fun transitionsWithoutOutput(transitions: String) =
        localized(
            "Transizioni senza uscita: $transitions",
            "Transitions without output: $transitions",
        )

    fun unreachableStates(states: String) =
        localized(
            "Stati non raggiungibili dallo stato iniziale: $states",
            "States not reachable from the initial state: $states",
        )

    fun andOthers(listed: String, others: Int) =
        localized("$listed e altri $others", "$listed and $others more")

    fun noTransitionForInput(step: Int, state: String, input: String) =
        localized(
            "t = $step: dallo stato $state non esce nessuna transizione con ingresso $input.",
            "t = $step: no transition with input $input leaves the state $state.",
        )

    fun severalTransitionsForInput(step: Int, state: String, input: String) =
        localized(
            "t = $step: dallo stato $state escono più transizioni con ingresso $input " +
                "(la macchina non è deterministica).",
            "t = $step: several transitions with input $input leave the state $state " +
                "(the machine is not deterministic).",
        )

    fun transitionToMissingState(step: Int) =
        localized(
            "t = $step: la transizione porta a uno stato inesistente.",
            "t = $step: the transition leads to a nonexistent state.",
        )

    fun missingInitialStateId(state: String) =
        localized(
            "Lo stato iniziale non esiste: $state",
            "The initial state does not exist: $state",
        )

    fun transitionMissingSource(transition: String, state: String) =
        localized(
            "La transizione $transition parte da uno stato inesistente: $state",
            "The transition $transition starts from a nonexistent state: $state",
        )

    fun transitionMissingTarget(transition: String, state: String) =
        localized(
            "La transizione $transition arriva a uno stato inesistente: $state",
            "The transition $transition ends in a nonexistent state: $state",
        )
}
