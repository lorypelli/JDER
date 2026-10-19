package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.SequenceDiagram

class SequenceDiagramRepository :
    DiagramFileRepository<SequenceDiagram>(SequenceDiagram.serializer(), DiagramType.SEQUENCE)
