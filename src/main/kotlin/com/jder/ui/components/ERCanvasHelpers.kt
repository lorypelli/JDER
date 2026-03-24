package com.jder.ui.components
import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.Attribute
import com.jder.domain.model.DiagramState
import com.jder.domain.model.ToolMode
import kotlin.math.abs
import kotlin.math.sqrt
internal fun isPointInDiamond(point: Offset, x: Float, y: Float, width: Float, height: Float): Boolean {
    val centerX = x + width / 2
    val centerY = y + height / 2
    val dx = abs(point.x - centerX) / (width / 2)
    val dy = abs(point.y - centerY) / (height / 2)
    return (dx + dy) <= 1.0
}
internal fun calculateAttributePosition(
    entityX: Float,
    entityY: Float,
    entityWidth: Float,
    entityHeight: Float,
    index: Int,
    total: Int,
    attribute: Attribute
): Offset {
    val centerX = entityX + entityWidth / 2
    val centerY = entityY + entityHeight / 2
    if (attribute.x != 0f || attribute.y != 0f) return Offset(centerX + attribute.x, centerY + attribute.y)
    val arrowLength = 60f
    val verticalSpacing = 60f
    val startY = centerY - ((total - 1) * verticalSpacing / 2f)
    return Offset(entityX + entityWidth + arrowLength, startY + (index * verticalSpacing))
}
internal fun handleCanvasTap(state: DiagramState, offset: Offset) {
    when (state.toolMode) {
        ToolMode.SELECT -> {
            state.diagram.relationships.firstOrNull { isPointInDiamond(offset, it.x, it.y, it.width, it.height) }?.let {
                state.selectRelationship(it.id)
                return
            }
            state.diagram.entities.firstOrNull {
                offset.x >= it.x && offset.x <= it.x + it.width &&
                offset.y >= it.y && offset.y <= it.y + it.height
            }?.let {
                state.selectEntity(it.id)
                return
            }
            state.diagram.notes.firstOrNull {
                offset.x >= it.x && offset.x <= it.x + it.width &&
                offset.y >= it.y && offset.y <= it.y + it.height
            }?.let {
                state.selectNote(it.id)
            } ?: run {
                state.clearSelection()
            }
        }
        ToolMode.ENTITY -> {
            state.addEntity(offset.x - 70f, offset.y - 35f, "Nuova Entità")
            state.toolMode = ToolMode.SELECT
        }
        ToolMode.RELATIONSHIP -> {
            state.addRelationship(offset.x - 60f, offset.y - 60f, "Nuova Relazione")
            state.toolMode = ToolMode.SELECT
        }
        ToolMode.NOTE -> {
            state.addNote(offset.x - 105f, offset.y - 77.5f, "Nuova Nota")
            state.toolMode = ToolMode.SELECT
        }
    }
}
internal fun handleDragStart(state: DiagramState, offset: Offset): Triple<String?, String?, String?> {
    val adjusted = Offset(
        (offset.x - state.panOffset.x) / state.zoom,
        (offset.y - state.panOffset.y) / state.zoom
    )
    state.diagram.entities.forEach { entity ->
        entity.attributes.forEachIndexed { index, attribute ->
            val attrPos = calculateAttributePosition(entity.x, entity.y, entity.width, entity.height, index, entity.attributes.size, attribute)
            val distance = sqrt((adjusted.x - attrPos.x).let { it * it } + (adjusted.y - attrPos.y).let { it * it })
            if (distance <= 30f) {
                state.saveDragStartState()
                return Triple(attribute.id, entity.id, null)
            }
        }
    }
    state.diagram.relationships.forEach { relationship ->
        relationship.attributes.forEachIndexed { index, attribute ->
            val attrPos = calculateAttributePosition(relationship.x, relationship.y, relationship.width, relationship.height, index, relationship.attributes.size, attribute)
            val distance = sqrt((adjusted.x - attrPos.x).let { it * it } + (adjusted.y - attrPos.y).let { it * it })
            if (distance <= 30f) {
                state.saveDragStartState()
                return Triple(attribute.id, null, relationship.id)
            }
        }
    }
    state.diagram.entities.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
        adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectEntity(it.id)
        state.saveDragStartState()
        return Triple(null, null, null)
    }
    state.diagram.relationships.find { isPointInDiamond(adjusted, it.x, it.y, it.width, it.height) }?.let {
        state.selectRelationship(it.id)
        state.saveDragStartState()
        return Triple(null, null, null)
    }
    state.diagram.notes.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
        adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectNote(it.id)
        state.saveDragStartState()
        return Triple(null, null, null)
    }
    return Triple(null, null, null)
}
internal fun handleDrag(
    state: DiagramState,
    dragAmount: Offset,
    draggedAttributeId: String?,
    draggedAttributeForEntity: String?,
    draggedAttributeForRelationship: String?
) {
    draggedAttributeId?.let { attrId ->
        draggedAttributeForEntity?.let { entityId ->
            state.updateEntityWithoutSave(entityId) { entity ->
                entity.copy(
                    attributes = entity.attributes.mapIndexed { index, attr ->
                        if (attr.id != attrId) return@mapIndexed attr
                        val centerX = entity.x + entity.width / 2
                        val centerY = entity.y + entity.height / 2
                        val currentAttrX = if (attr.x == 0f && attr.y == 0f) entity.x + entity.width + 60f else centerX + attr.x
                        val currentAttrY = if (attr.x == 0f && attr.y == 0f) {
                            val startY = centerY - ((entity.attributes.size - 1) * 60f / 2f)
                            startY + (index * 60f)
                        } else centerY + attr.y
                        val currentDx = currentAttrX - centerX
                        val currentDy = currentAttrY - centerY
                        val currentDistFromCenter = sqrt(currentDx * currentDx + currentDy * currentDy)
                        val fixedDistance = if (currentDistFromCenter > 0) currentDistFromCenter else (entity.width / 2 + 60f)
                        val newAttrX = currentAttrX + dragAmount.x / state.zoom
                        val newAttrY = currentAttrY + dragAmount.y / state.zoom
                        val dx = newAttrX - centerX
                        val dy = newAttrY - centerY
                        val currentDistance = sqrt(dx * dx + dy * dy)
                        val normalizedX = if (currentDistance > 0) dx / currentDistance else 1f
                        val normalizedY = if (currentDistance > 0) dy / currentDistance else 0f
                        attr.copy(x = normalizedX * fixedDistance, y = normalizedY * fixedDistance)
                    }
                )
            }
            return
        }
        draggedAttributeForRelationship?.let { relId ->
            state.updateRelationshipWithoutSave(relId) { rel ->
                rel.copy(
                    attributes = rel.attributes.mapIndexed { index, attr ->
                        if (attr.id != attrId) return@mapIndexed attr
                        val centerX = rel.x + rel.width / 2
                        val centerY = rel.y + rel.height / 2
                        val currentAttrX = if (attr.x == 0f && attr.y == 0f) rel.x + rel.width + 60f else centerX + attr.x
                        val currentAttrY = if (attr.x == 0f && attr.y == 0f) {
                            val startY = centerY - ((rel.attributes.size - 1) * 60f / 2f)
                            startY + (index * 60f)
                        } else centerY + attr.y
                        val currentDx = currentAttrX - centerX
                        val currentDy = currentAttrY - centerY
                        val currentDistFromCenter = sqrt(currentDx * currentDx + currentDy * currentDy)
                        val fixedDistance = if (currentDistFromCenter > 0) currentDistFromCenter else (rel.width / 2 + 60f)
                        val newAttrX = currentAttrX + dragAmount.x / state.zoom
                        val newAttrY = currentAttrY + dragAmount.y / state.zoom
                        val dx = newAttrX - centerX
                        val dy = newAttrY - centerY
                        val currentDistance = sqrt(dx * dx + dy * dy)
                        val normalizedX = if (currentDistance > 0) dx / currentDistance else 1f
                        val normalizedY = if (currentDistance > 0) dy / currentDistance else 0f
                        attr.copy(x = normalizedX * fixedDistance, y = normalizedY * fixedDistance)
                    }
                )
            }
            return
        }
    }
    state.selectedEntityId?.let {
        state.updateEntityWithoutSave(it) { entity ->
            entity.copy(x = entity.x + dragAmount.x / state.zoom, y = entity.y + dragAmount.y / state.zoom)
        }
    }
    state.selectedRelationshipId?.let {
        state.updateRelationshipWithoutSave(it) { rel ->
            rel.copy(x = rel.x + dragAmount.x / state.zoom, y = rel.y + dragAmount.y / state.zoom)
        }
    }
    state.selectedNoteId?.let {
        state.updateNoteWithoutSave(it) { note ->
            note.copy(x = note.x + dragAmount.x / state.zoom, y = note.y + dragAmount.y / state.zoom)
        }
    }
}
