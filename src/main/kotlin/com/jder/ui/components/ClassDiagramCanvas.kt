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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import com.jder.domain.model.ClassDiagramState
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ClassDiagramCanvas(
    state: ClassDiagramState,
    onContextMenuRequest: ((Offset, ClassDiagramContextMenuType) -> Unit)? = null,
    onRelationCreateRequest: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val relationColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val textBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    var isDragging by remember { mutableStateOf(false) }
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
                                state.panOffset = Offset(mousePos.x - oldMouseWorld.x * newZoom, mousePos.y - oldMouseWorld.y * newZoom)
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
            .pointerInput(state.diagram.classes.size, state.diagram.notes.size) {
                detectTapGestures(
                    onTap = { offset ->
                        if (!isDragging && totalDragDistance < 5f) {
                            val adjusted = Offset(
                                (offset.x - state.panOffset.x) / state.zoom,
                                (offset.y - state.panOffset.y) / state.zoom
                            )
                            handleClassDiagramTap(state, adjusted, onRelationCreateRequest)
                        }
                        isDragging = false
                        totalDragDistance = 0f
                    },
                    onLongPress = { offset ->
                        val adjusted = Offset(
                            (offset.x - state.panOffset.x) / state.zoom,
                            (offset.y - state.panOffset.y) / state.zoom
                        )
                        val clickedClass = state.diagram.classes.find { isPointInClassEntity(adjusted, it) }
                        val clickedRelation = state.diagram.relations.find { isPointNearClassRelation(adjusted, it, state.diagram) }
                        val clickedNote = state.diagram.notes.find {
                            adjusted.x >= it.x && adjusted.x <= it.x + it.width && adjusted.y >= it.y && adjusted.y <= it.y + it.height
                        }
                        when {
                            clickedClass != null -> {
                                state.selectClass(clickedClass.id)
                                onContextMenuRequest?.invoke(offset, ClassDiagramContextMenuType.CLASS)
                            }
                            clickedRelation != null -> {
                                state.selectRelation(clickedRelation.id)
                                onContextMenuRequest?.invoke(offset, ClassDiagramContextMenuType.RELATION)
                            }
                            clickedNote != null -> {
                                state.selectNote(clickedNote.id)
                                onContextMenuRequest?.invoke(offset, ClassDiagramContextMenuType.NOTE)
                            }
                        }
                    }
                )
            }
            .pointerInput(state.diagram.classes.size, state.diagram.notes.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handleClassDiagramDragStart(state, offset)
                        isDragging = false
                        totalDragDistance = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragDistance += kotlin.math.sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                        if (state.selectedClassId != null || state.selectedNoteId != null) {
                            isDragging = true
                            handleClassDiagramDrag(state, dragAmount)
                        }
                    },
                    onDragEnd = { isDragging = false }
                )
            }
    ) {
        val canvasSize = size
        withTransform({
            translate(state.panOffset.x, state.panOffset.y)
            scale(state.zoom, state.zoom, Offset.Zero)
        }) {
            drawGrid(canvasSize, gridColor, state.zoom, state.panOffset)
            state.diagram.relations.forEach { relation ->
                drawClassRelation(
                    relation = relation,
                    diagram = state.diagram,
                    isSelected = relation.id == state.selectedRelationId,
                    color = relationColor,
                    selectedColor = selectedColor,
                    textMeasurer = textMeasurer,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.notes.forEach { note ->
                drawNoteShape(
                    note = note,
                    isSelected = note.id == state.selectedNoteId,
                    textMeasurer = textMeasurer,
                    selectedColor = selectedColor
                )
            }
            state.diagram.classes.forEach { entity ->
                drawClassEntity(
                    entity = entity,
                    isSelected = entity.id == state.selectedClassId,
                    isPendingSource = entity.id == state.pendingRelationSourceId,
                    primaryColor = primaryColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}
