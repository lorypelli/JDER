package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.RelationalDiagram

class RelationalDiagramRepository :
    DiagramFileRepository<RelationalDiagram>(RelationalDiagram.serializer(), DiagramType.RELATIONAL)
