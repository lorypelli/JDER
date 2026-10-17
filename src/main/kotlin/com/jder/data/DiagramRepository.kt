package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.ERDiagram

class DiagramRepository : DiagramFileRepository<ERDiagram>(ERDiagram.serializer(), DiagramType.ER)
