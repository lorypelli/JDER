package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.RelationalDiagram
import com.jder.domain.model.validateReferences

class RelationalDiagramRepository :
    DiagramFileRepository<RelationalDiagram>(
        RelationalDiagram.serializer(),
        DiagramType.RELATIONAL,
        RelationalDiagram::validateReferences,
    )
