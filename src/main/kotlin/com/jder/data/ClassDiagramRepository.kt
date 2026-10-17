package com.jder.data

import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.DiagramType

class ClassDiagramRepository :
    DiagramFileRepository<ClassDiagram>(ClassDiagram.serializer(), DiagramType.CLASS_DIAGRAM)
