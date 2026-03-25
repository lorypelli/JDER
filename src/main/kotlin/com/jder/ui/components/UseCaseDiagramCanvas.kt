package com.jder.ui.components
import androidx.compose.foundation.Canvas
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
import com.jder.domain.model.UseCaseState
import kotlin.math.sqrt
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UseCaseDiagramCanvas(
    state: UseCaseState,
    onContextMenuRequest: ((Offset, UseCaseContextMenuType) -> Unit)? = null,
    onRelationCreateRequest: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val actorColor = MaterialTheme.colorScheme.primary
    val useCaseColor = MaterialTheme.colorScheme.secondary
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.background
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
            .pointerInput(state.diagram.actors.size, state.diagram.useCases.size, state.diagram.notes.size, state.diagram.systemBoundaries.size) {
                detectTapGestures(
                    onTap = { offset ->
                        if (!isDragging && totalDragDistance < 5f) {
                            val adjusted = Offset(
                                (offset.x - state.panOffset.x) / state.zoom,
                                (offset.y - state.panOffset.y) / state.zoom
                            )
                            handleUseCaseCanvasTap(state, adjusted, onRelationCreateRequest)
                        }
                        isDragging = false
                        totalDragDistance = 0f
                    },
                    onLongPress = { offset ->
                        val adjusted = Offset(
                            (offset.x - state.panOffset.x) / state.zoom,
                            (offset.y - state.panOffset.y) / state.zoom
                        )
                        val clickedActor = state.diagram.actors.find { isPointInActor(adjusted, it) }
                        val clickedUseCase = if (clickedActor == null) state.diagram.useCases.find { isPointInUseCase(adjusted, it) } else null
                        val clickedRelation = if (clickedActor == null && clickedUseCase == null) {
                            state.diagram.relations.find { isPointNearRelation(adjusted, it, state.diagram) }
                        } else null
                        val clickedNote = if (clickedActor == null && clickedUseCase == null && clickedRelation == null) {
                            state.diagram.notes.find {
                                adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                adjusted.y >= it.y && adjusted.y <= it.y + it.height
                            }
                        } else null
                        val clickedSystem = if (clickedActor == null && clickedUseCase == null && clickedRelation == null && clickedNote == null) {
                            state.diagram.systemBoundaries.find {
                                adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                adjusted.y >= it.y && adjusted.y <= it.y + it.height
                            }
                        } else null
                        clickedActor?.let { state.selectActor(it.id); onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.ACTOR) }
                        clickedUseCase?.let { state.selectUseCase(it.id); onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.USE_CASE) }
                        clickedRelation?.let { state.selectRelation(it.id); onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.RELATION) }
                        clickedNote?.let { state.selectNote(it.id); onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.NOTE) }
                        clickedSystem?.let { state.selectSystemBoundary(it.id); onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.SYSTEM) }
                    }
                )
            }
            .pointerInput(state.selectedActorId, state.selectedUseCaseId, onContextMenuRequest) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            val position = event.changes.first().position
                            val adjusted = Offset(
                                (position.x - state.panOffset.x) / state.zoom,
                                (position.y - state.panOffset.y) / state.zoom
                            )
                            val clickedActor = state.diagram.actors.find { isPointInActor(adjusted, it) }
                            val clickedUseCase = if (clickedActor == null) state.diagram.useCases.find { isPointInUseCase(adjusted, it) } else null
                            val clickedRelation = if (clickedActor == null && clickedUseCase == null) {
                                state.diagram.relations.find { isPointNearRelation(adjusted, it, state.diagram) }
                            } else null
                            val clickedNote = if (clickedActor == null && clickedUseCase == null && clickedRelation == null) {
                                state.diagram.notes.find {
                                    adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                    adjusted.y >= it.y && adjusted.y <= it.y + it.height
                                }
                            } else null
                            val clickedSystem = if (clickedActor == null && clickedUseCase == null && clickedRelation == null && clickedNote == null) {
                                state.diagram.systemBoundaries.find {
                                    adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
                                    adjusted.y >= it.y && adjusted.y <= it.y + it.height
                                }
                            } else null
                            clickedActor?.let { state.selectActor(it.id); onContextMenuRequest?.invoke(position, UseCaseContextMenuType.ACTOR) }
                            clickedUseCase?.let { state.selectUseCase(it.id); onContextMenuRequest?.invoke(position, UseCaseContextMenuType.USE_CASE) }
                            clickedRelation?.let { state.selectRelation(it.id); onContextMenuRequest?.invoke(position, UseCaseContextMenuType.RELATION) }
                            clickedNote?.let { state.selectNote(it.id); onContextMenuRequest?.invoke(position, UseCaseContextMenuType.NOTE) }
                            clickedSystem?.let { state.selectSystemBoundary(it.id); onContextMenuRequest?.invoke(position, UseCaseContextMenuType.SYSTEM) }
                        }
                    }
                }
            }
            .pointerInput(state.selectedActorId, state.selectedUseCaseId, state.selectedNoteId, state.selectedSystemBoundaryId) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = false
                        totalDragDistance = 0f
                        handleUseCaseDragStart(state, offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragDistance += sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                        if (totalDragDistance > 5f) isDragging = true
                        if (isDragging) handleUseCaseDrag(state, dragAmount)
                    },
                    onDragEnd = {
                        if (!isDragging) totalDragDistance = 0f
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
            state.diagram.systemBoundaries.forEach { boundary ->
                drawSystemBoundary(
                    boundary = boundary,
                    isSelected = state.selectedSystemBoundaryId == boundary.id,
                    color = useCaseColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer
                )
            }
            state.diagram.relations.forEach { relation ->
                drawUseCaseRelation(
                    relation = relation,
                    diagram = state.diagram,
                    isSelected = state.selectedRelationId == relation.id,
                    color = relationColor,
                    selectedColor = selectedColor,
                    textMeasurer = textMeasurer,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.actors.forEach { actor ->
                drawActor(
                    actor = actor,
                    isSelected = state.selectedActorId == actor.id,
                    isPendingSource = state.pendingRelationSourceId == actor.id,
                    actorColor = actorColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    textMeasurer = textMeasurer
                )
            }
            state.diagram.useCases.forEach { useCase ->
                drawUseCase(
                    useCase = useCase,
                    isSelected = state.selectedUseCaseId == useCase.id,
                    isPendingSource = state.pendingRelationSourceId == useCase.id,
                    useCaseColor = useCaseColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer
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
