package com.jder.data

import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.validateReferences

class StateMachineDiagramRepository(kind: StateMachineKind) :
    DiagramFileRepository<StateMachineDiagram>(
        StateMachineDiagram.serializer(),
        kind.diagramType,
        StateMachineDiagram::validateReferences,
    )
