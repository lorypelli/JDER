package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.LogicDiagram

class LogicDiagramRepository :
    DiagramFileRepository<LogicDiagram>(LogicDiagram.serializer(), DiagramType.LOGIC)
