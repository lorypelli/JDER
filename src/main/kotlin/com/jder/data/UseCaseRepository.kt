package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.domain.model.UseCaseDiagram

class UseCaseRepository :
    DiagramFileRepository<UseCaseDiagram>(UseCaseDiagram.serializer(), DiagramType.USE_CASE)
