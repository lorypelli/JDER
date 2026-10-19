package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.validateMessageEndpoints

class SequenceDiagramRepository :
    DiagramFileRepository<SequenceDiagram>(
        SequenceDiagram.serializer(),
        DiagramType.SEQUENCE,
        SequenceDiagram::validateMessageEndpoints,
    )
