package com.jder.ui.screens

import com.jder.data.ClassDiagramRepository
import com.jder.data.ERDiagramRepository
import com.jder.data.LogicDiagramRepository
import com.jder.data.RelationalDiagramRepository
import com.jder.data.SequenceDiagramRepository
import com.jder.data.StateMachineDiagramRepository
import com.jder.data.UseCaseDiagramRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.DiagramEditorState
import com.jder.domain.model.DiagramType
import com.jder.domain.model.ERDiagramState
import com.jder.domain.model.LogicDiagramState
import com.jder.domain.model.RelationalDiagramState
import com.jder.domain.model.SequenceDiagramState
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.UseCaseDiagramState

class DiagramWorkspace {
    val erState = ERDiagramState()
    val relationalState = RelationalDiagramState()
    val useCaseState = UseCaseDiagramState()
    val sequenceState = SequenceDiagramState()
    val classState = ClassDiagramState()
    val logicState = LogicDiagramState()
    val mealyState = StateMachineDiagramState(StateMachineKind.MEALY)
    val mooreState = StateMachineDiagramState(StateMachineKind.MOORE)
    val erRepository = ERDiagramRepository()
    val relationalRepository = RelationalDiagramRepository()
    val useCaseRepository = UseCaseDiagramRepository()
    val sequenceRepository = SequenceDiagramRepository()
    val classRepository = ClassDiagramRepository()
    val logicRepository = LogicDiagramRepository()
    val mealyRepository = StateMachineDiagramRepository(StateMachineKind.MEALY)
    val mooreRepository = StateMachineDiagramRepository(StateMachineKind.MOORE)

    val hasUnsavedChanges: Boolean
        get() = DiagramType.entries.any { state(it).isModified }

    fun state(type: DiagramType): DiagramEditorState<*> =
        when (type) {
            DiagramType.ER -> erState
            DiagramType.RELATIONAL -> relationalState
            DiagramType.CLASS -> classState
            DiagramType.USE_CASE -> useCaseState
            DiagramType.SEQUENCE -> sequenceState
            DiagramType.LOGIC -> logicState
            DiagramType.MEALY -> mealyState
            DiagramType.MOORE -> mooreState
        }
}
