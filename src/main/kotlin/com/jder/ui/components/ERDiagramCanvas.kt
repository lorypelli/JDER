package com.jder.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import com.jder.domain.model.DiagramState
import kotlin.math.sqrt
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ERDiagramCanvas(
    state: DiagramState,
    onContextMenuRequest: ((Offset, ContextMenuType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val entityColor = MaterialTheme.colorScheme.primary
    val relationshipColor = MaterialTheme.colorScheme.error
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val connectionColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val attributeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val normalAttributeColor = MaterialTheme.colorScheme.primary
    val primaryKeyColor = MaterialTheme.colorScheme.tertiary
    val compositeColor = MaterialTheme.colorScheme.secondary
    val componentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    val cardinalityColor = MaterialTheme.colorScheme.tertiary
    val textBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    var isDragging by remember { mutableStateOf(false) }
    var draggedAttributeInfo by remember { mutableStateOf(Triple<String?, String?, String?>(null, null, null)) }
    var totalDragDistance by remember { mutableStateOf(0f) }
    var isMiddleMouseDragging by remember { mutableStateOf(false) }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Scroll -> {
                                val change = event.changes.first()
                                val zoomFactor = if (change.scrollDelta.y < 0) 1.1f else 0.9f
                                val newZoom = (state.zoom * zoomFactor).coerceIn(0.25f, 3f)
                                val mousePos = change.position
                                val oldMouseWorld = Offset(
                                    (mousePos.x - state.panOffset.x) / state.zoom,
                                    (mousePos.y - state.panOffset.y) / state.zoom
                                )
                                state.zoom = newZoom
                                state.panOffset = Offset(
                                    mousePos.x - oldMouseWorld.x * newZoom,
                                    mousePos.y - oldMouseWorld.y * newZoom
                                )
                            }
                            PointerEventType.Press -> {
                                if (event.button == PointerButton.Tertiary) isMiddleMouseDragging = true
                            }
                            PointerEventType.Move -> {
                                if (isMiddleMouseDragging) {
                                    val change = event.changes.first()
                                    val dragAmount = change.position - change.previousPosition
                                    state.panOffset = Offset(state.panOffset.x + dragAmount.x, state.panOffset.y + dragAmount.y)
                                }
                            }
                            PointerEventType.Release -> {
                                if (event.button == PointerButton.Tertiary) isMiddleMouseDragging = false
                            }
                        }
                    }
                }
            }
            .pointerInput(state.diagram.entities.size, state.diagram.relationships.size) {
                detectTapGestures(
                    onTap = { offset ->
                        if (!isDragging && totalDragDistance < 5f) {
                            val adjusted = Offset(
                                (offset.x - state.panOffset.x) / state.zoom,
                                (offset.y - state.panOffset.y) / state.zoom
                            )
                            handleCanvasTap(state, adjusted)
                        }
                        isDragging = false
                        totalDragDistance = 0f
                    },
                    onLongPress = { offset ->
                        val adjusted = Offset(
                            (offset.x - state.panOffset.x) / state.zoom,
                            (offset.y - state.panOffset.y) / state.zoom
                        )
                        val clickedEntity = state.diagram.entities.find {
                            adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                            adjusted.y >= it.y && adjusted.y <= it.y + it.height
                        }
                        val clickedRelationship = if (clickedEntity == null) {
                            state.diagram.relationships.find { isPointInDiamond(adjusted, it.x, it.y, it.width, it.height) }
                        } else null
                        val clickedNote = if (clickedEntity == null && clickedRelationship == null) {
                            state.diagram.notes.find {
                                adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                adjusted.y >= it.y && adjusted.y <= it.y + it.height
                            }
                        } else null
                        clickedEntity?.let {
                            state.selectEntity(it.id)
                            onContextMenuRequest?.invoke(offset, ContextMenuType.ENTITY)
                        }
                        clickedRelationship?.let {
                            state.selectRelationship(it.id)
                            onContextMenuRequest?.invoke(offset, ContextMenuType.RELATIONSHIP)
                        }
                        clickedNote?.let {
                            state.selectNote(it.id)
                            onContextMenuRequest?.invoke(offset, ContextMenuType.NOTE)
                        }
                    }
                )
            }
            .pointerInput(state.selectedEntityId, state.selectedRelationshipId, onContextMenuRequest) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            val position = event.changes.first().position
                            val adjusted = Offset(
                                (position.x - state.panOffset.x) / state.zoom,
                                (position.y - state.panOffset.y) / state.zoom
                            )
                            val clickedEntity = state.diagram.entities.find {
                                adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                adjusted.y >= it.y && adjusted.y <= it.y + it.height
                            }
                            val clickedRelationship = if (clickedEntity == null) {
                                state.diagram.relationships.find { isPointInDiamond(adjusted, it.x, it.y, it.width, it.height) }
                            } else null
                            val clickedNote = if (clickedEntity == null && clickedRelationship == null) {
                                state.diagram.notes.find {
                                    adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                    adjusted.y >= it.y && adjusted.y <= it.y + it.height
                                }
                            } else null
                            clickedEntity?.let {
                                state.selectEntity(it.id)
                                onContextMenuRequest?.invoke(position, ContextMenuType.ENTITY)
                            }
                            clickedRelationship?.let {
                                state.selectRelationship(it.id)
                                onContextMenuRequest?.invoke(position, ContextMenuType.RELATIONSHIP)
                            }
                            clickedNote?.let {
                                state.selectNote(it.id)
                                onContextMenuRequest?.invoke(position, ContextMenuType.NOTE)
                            }
                        }
                    }
                }
            }
            .pointerInput(state.selectedEntityId, state.selectedRelationshipId) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = false
                        totalDragDistance = 0f
                        draggedAttributeInfo = handleDragStart(state, offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragDistance += sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                        if (totalDragDistance > 5f) isDragging = true
                        if (isDragging) handleDrag(state, dragAmount, draggedAttributeInfo.first, draggedAttributeInfo.second, draggedAttributeInfo.third)
                    },
                    onDragEnd = {
                        if (!isDragging) totalDragDistance = 0f
                        draggedAttributeInfo = Triple(null, null, null)
                    }
                )
            }
    ) {
        drawRect(color = backgroundColor, size = size)
        withTransform({
            translate(state.panOffset.x, state.panOffset.y)
            scale(state.zoom, state.zoom, Offset.Zero)
        }) {
            drawGrid(size, gridColor, state.zoom, state.panOffset)
            state.diagram.relationships.forEach { relationship ->
                drawConnectionsForRelationship(
                    relationship = relationship,
                    entities = state.diagram.entities,
                    color = connectionColor,
                    textMeasurer = textMeasurer,
                    cardinalityColor = cardinalityColor,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.entities.forEach { entity ->
                drawEntity(
                    entity = entity,
                    isSelected = state.selectedEntityId == entity.id,
                    isHovered = false,
                    entityColor = entityColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer,
                    attributeBackgroundColor = attributeBackgroundColor,
                    normalAttributeColor = normalAttributeColor,
                    primaryKeyColor = primaryKeyColor,
                    compositeColor = compositeColor,
                    componentColor = componentColor,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.relationships.forEach { relationship ->
                drawRelationship(
                    relationship = relationship,
                    isSelected = state.selectedRelationshipId == relationship.id,
                    isHovered = false,
                    relationshipColor = relationshipColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer,
                    attributeBackgroundColor = attributeBackgroundColor,
                    normalAttributeColor = normalAttributeColor,
                    primaryKeyColor = primaryKeyColor,
                    compositeColor = compositeColor,
                    componentColor = componentColor,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.notes.forEach { note ->
                drawNoteShape(
                    note = note,
                    isSelected = state.selectedNoteId == note.id,
                    textMeasurer = textMeasurer,
                    selectedColor = selectedColor
                )
            }
        }
    }
}
