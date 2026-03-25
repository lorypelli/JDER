package com.jder.ui.components
import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ClassDiagramToolMode
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.ClassDiagramState
import kotlin.math.pow
import kotlin.math.sqrt
internal fun classEntityHeight(entity: ClassEntity): Float {
    val headerH = if (entity.type.stereotype.isNotEmpty()) 56f else 40f
    val attrH = 4f + entity.attributes.size * 20f + if (entity.attributes.isEmpty()) 20f else 0f
    val methodH = 4f + entity.methods.size * 20f + if (entity.methods.isEmpty()) 20f else 0f
    return headerH + attrH + methodH
}
internal fun isPointInClassEntity(point: Offset, entity: ClassEntity): Boolean =
    point.x >= entity.x && point.x <= entity.x + entity.width &&
    point.y >= entity.y && point.y <= entity.y + classEntityHeight(entity)
internal fun isPointNearClassRelation(point: Offset, relation: ClassRelation, diagram: ClassDiagram): Boolean {
    val start = classElementCenter(relation.sourceId, diagram) ?: return false
    val end = classElementCenter(relation.targetId, diagram) ?: return false
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return sqrt((point.x - start.x).pow(2) + (point.y - start.y).pow(2)) <= 8f
    val t = (((point.x - start.x) * dx + (point.y - start.y) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = start.x + t * dx
    val projY = start.y + t * dy
    return sqrt((point.x - projX).pow(2) + (point.y - projY).pow(2)) <= 8f
}
internal fun classElementCenter(id: String, diagram: ClassDiagram): Offset? {
    diagram.classes.find { it.id == id }?.let { return Offset(it.x + it.width / 2, it.y + classEntityHeight(it) / 2) }
    return null
}
internal fun classEntityBoundaryPoint(id: String, diagram: ClassDiagram, direction: Offset): Offset? {
    val entity = diagram.classes.find { it.id == id } ?: return null
    val height = classEntityHeight(entity)
    val cx = entity.x + entity.width / 2
    val cy = entity.y + height / 2
    val hw = entity.width / 2
    val hh = height / 2
    if (direction.x == 0f && direction.y == 0f) return Offset(cx, cy)
    val tx = if (direction.x != 0f) hw / kotlin.math.abs(direction.x) else Float.MAX_VALUE
    val ty = if (direction.y != 0f) hh / kotlin.math.abs(direction.y) else Float.MAX_VALUE
    val t = minOf(tx, ty)
    return Offset(cx + direction.x * t, cy + direction.y * t)
}
internal fun handleClassDiagramTap(
    state: ClassDiagramState,
    offset: Offset,
    onRelationCreateRequest: ((String, String) -> Unit)?
) {
    when (state.toolMode) {
        ClassDiagramToolMode.SELECT -> {
            state.diagram.classes.find { isPointInClassEntity(offset, it) }?.let { state.selectClass(it.id); return }
            state.diagram.relations.find { isPointNearClassRelation(offset, it, state.diagram) }?.let { state.selectRelation(it.id); return }
            state.diagram.notes.find {
                offset.x >= it.x && offset.x <= it.x + it.width && offset.y >= it.y && offset.y <= it.y + it.height
            }?.let { state.selectNote(it.id); return }
            state.clearSelection()
        }
        ClassDiagramToolMode.CLASS -> {
            state.addClass(offset.x - 100f, offset.y - 40f, "NuovaClasse")
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.INTERFACE -> {
            state.addClass(offset.x - 100f, offset.y - 40f, "NuovaInterfaccia", com.jder.domain.model.ClassEntityType.INTERFACE)
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.ENUM -> {
            state.addClass(offset.x - 100f, offset.y - 40f, "NuovoEnum", com.jder.domain.model.ClassEntityType.ENUM)
            state.toolMode = ClassDiagramToolMode.SELECT
        }
        ClassDiagramToolMode.RELATION -> {
            val clicked = state.diagram.classes.find { isPointInClassEntity(offset, it) }?.id
            if (clicked != null) {
                val pending = state.pendingRelationSourceId
                when {
                    pending == null -> state.pendingRelationSourceId = clicked
                    pending != clicked -> {
                        onRelationCreateRequest?.invoke(pending, clicked)
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
internal fun handleClassDiagramDragStart(state: ClassDiagramState, offset: Offset) {
    val adjusted = Offset((offset.x - state.panOffset.x) / state.zoom, (offset.y - state.panOffset.y) / state.zoom)
    state.diagram.classes.find { isPointInClassEntity(adjusted, it) }?.let {
        state.selectClass(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.notes.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width && adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectNote(it.id)
        state.saveDragStartState()
        return
    }
}
internal fun handleClassDiagramDrag(state: ClassDiagramState, dragAmount: Offset) {
    val dx = dragAmount.x / state.zoom
    val dy = dragAmount.y / state.zoom
    state.selectedClassId?.let { id ->
        state.updateClassWithoutSave(id) { it.copy(x = it.x + dx, y = it.y + dy) }
        return
    }
    state.selectedNoteId?.let { id ->
        state.updateNoteWithoutSave(id) { it.copy(x = it.x + dx, y = it.y + dy) }
        return
    }
}
