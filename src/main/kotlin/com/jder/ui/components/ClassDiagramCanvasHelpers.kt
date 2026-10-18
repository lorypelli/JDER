package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassDiagramToolMode
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassEntityType
import com.jder.domain.model.ClassRelation
import kotlin.math.abs

sealed interface ClassElement {
    val id: String

    data class ClassRef(override val id: String) : ClassElement

    data class RelationRef(override val id: String) : ClassElement

    data class NoteRef(override val id: String) : ClassElement
}

internal val ClassElement.contextMenuType: ClassDiagramContextMenuType
    get() =
        when (this) {
            is ClassElement.ClassRef -> ClassDiagramContextMenuType.CLASS
            is ClassElement.RelationRef -> ClassDiagramContextMenuType.RELATION
            is ClassElement.NoteRef -> ClassDiagramContextMenuType.NOTE
        }

internal fun classEntityHeight(entity: ClassEntity): Float {
    val headerH = if (entity.type.stereotype.isNotEmpty()) 56f else 40f
    val attrH = 4f + entity.attributes.size * 20f + if (entity.attributes.isEmpty()) 20f else 0f
    val methodH = 4f + entity.methods.size * 20f + if (entity.methods.isEmpty()) 20f else 0f
    return headerH + attrH + methodH
}

internal fun isPointInClassEntity(point: Offset, entity: ClassEntity): Boolean =
    point.x >= entity.x &&
        point.x <= entity.x + entity.width &&
        point.y >= entity.y &&
        point.y <= entity.y + classEntityHeight(entity)

internal fun isPointNearClassRelation(
    point: Offset,
    relation: ClassRelation,
    diagram: ClassDiagram,
): Boolean {
    val start = classElementCenter(relation.sourceId, diagram) ?: return false
    val end = classElementCenter(relation.targetId, diagram) ?: return false
    return distanceToSegment(point, start, end) <= 8f
}

internal fun distanceToSegment(point: Offset, start: Offset, end: Offset): Float {
    val segment = end - start
    val lengthSquared = segment.getDistanceSquared()
    if (lengthSquared == 0f) return (point - start).getDistance()
    val t =
        (((point.x - start.x) * segment.x + (point.y - start.y) * segment.y) / lengthSquared)
            .coerceIn(0f, 1f)
    return (point - (start + segment * t)).getDistance()
}

internal fun classElementCenter(id: String, diagram: ClassDiagram): Offset? {
    val entity = diagram.classes.find { it.id == id } ?: return null
    return Offset(entity.x + entity.width / 2, entity.y + classEntityHeight(entity) / 2)
}

internal fun classEntityBoundaryPoint(
    id: String,
    diagram: ClassDiagram,
    direction: Offset,
): Offset? {
    val entity = diagram.classes.find { it.id == id } ?: return null
    val height = classEntityHeight(entity)
    val cx = entity.x + entity.width / 2
    val cy = entity.y + height / 2
    val hw = entity.width / 2
    val hh = height / 2
    if (direction.x == 0f && direction.y == 0f) return Offset(cx, cy)
    val tx = if (direction.x != 0f) hw / abs(direction.x) else Float.MAX_VALUE
    val ty = if (direction.y != 0f) hh / abs(direction.y) else Float.MAX_VALUE
    val t = minOf(tx, ty)
    return Offset(cx + direction.x * t, cy + direction.y * t)
}

internal fun hitTestClassDiagram(diagram: ClassDiagram, point: Offset): ClassElement? {
    diagram.classes
        .lastOrNull { isPointInClassEntity(point, it) }
        ?.let {
            return ClassElement.ClassRef(it.id)
        }
    diagram.notes
        .lastOrNull { it.contains(point) }
        ?.let {
            return ClassElement.NoteRef(it.id)
        }
    diagram.relations
        .lastOrNull { isPointNearClassRelation(point, it, diagram) }
        ?.let {
            return ClassElement.RelationRef(it.id)
        }
    return null
}

internal fun selectClassElement(state: ClassDiagramState, element: ClassElement) {
    when (element) {
        is ClassElement.ClassRef -> state.selectClass(element.id)
        is ClassElement.RelationRef -> state.selectRelation(element.id)
        is ClassElement.NoteRef -> state.selectNote(element.id)
    }
}

internal fun handleClassDiagramTap(
    state: ClassDiagramState,
    offset: Offset,
    onRelationCreateRequest: (String, String) -> Unit,
) {
    when (state.toolMode) {
        ClassDiagramToolMode.SELECT ->
            hitTestClassDiagram(state.diagram, offset)?.let { selectClassElement(state, it) }
                ?: state.clearSelection()
        ClassDiagramToolMode.CLASS -> {
            state.addClass(offset.x - 100f, offset.y - 40f, "NuovaClasse")
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.INTERFACE -> {
            state.addClass(
                offset.x - 100f,
                offset.y - 40f,
                "NuovaInterfaccia",
                ClassEntityType.INTERFACE,
            )
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.ENUM -> {
            state.addClass(offset.x - 100f, offset.y - 40f, "NuovoEnum", ClassEntityType.ENUM)
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.RELATION -> {
            val clicked = state.diagram.classes.lastOrNull { isPointInClassEntity(offset, it) }?.id
            if (clicked != null) {
                val pending = state.pendingRelationSourceId
                when {
                    pending == null -> state.pendingRelationSourceId = clicked
                    pending != clicked -> {
                        onRelationCreateRequest(pending, clicked)
                        state.pendingRelationSourceId = null
                    }
                    else -> state.pendingRelationSourceId = null
                }
            } else {
                state.pendingRelationSourceId = null
            }
        }
        ClassDiagramToolMode.NOTE -> {
            state.addNote(offset.x - 120f, offset.y - 90f, "Nuova Nota")
            state.toolMode = ClassDiagramToolMode.SELECT
        }
    }
}

internal fun handleClassDiagramDragStart(state: ClassDiagramState, offset: Offset): ClassElement? {
    val element = hitTestClassDiagram(state.diagram, offset) ?: return null
    selectClassElement(state, element)
    if (element is ClassElement.RelationRef) return null
    state.saveDragStartState()
    return element
}

internal fun handleClassDiagramDrag(
    state: ClassDiagramState,
    element: ClassElement,
    delta: Offset,
) {
    when (element) {
        is ClassElement.ClassRef ->
            state.updateClassWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is ClassElement.NoteRef ->
            state.updateNoteWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is ClassElement.RelationRef -> {}
    }
}
