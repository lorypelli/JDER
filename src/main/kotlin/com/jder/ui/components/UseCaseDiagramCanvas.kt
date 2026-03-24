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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.jder.domain.model.Actor
import com.jder.domain.model.Note
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseRelationType
import com.jder.domain.model.UseCaseState
import com.jder.domain.model.UseCaseToolMode
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
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
                                val scrollDelta = change.scrollDelta
                                val zoomFactor = if (scrollDelta.y < 0) 1.1f else 0.9f
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
                                    state.panOffset = Offset(
                                        state.panOffset.x + dragAmount.x,
                                        state.panOffset.y + dragAmount.y
                                    )
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
                        clickedActor?.let {
                            state.selectActor(it.id)
                            onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.ACTOR)
                        }
                        clickedUseCase?.let {
                            state.selectUseCase(it.id)
                            onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.USE_CASE)
                        }
                        clickedRelation?.let {
                            state.selectRelation(it.id)
                            onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.RELATION)
                        }
                        clickedNote?.let {
                            state.selectNote(it.id)
                            onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.NOTE)
                        }
                        clickedSystem?.let {
                            state.selectSystemBoundary(it.id)
                            onContextMenuRequest?.invoke(offset, UseCaseContextMenuType.SYSTEM)
                        }
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
                            clickedActor?.let {
                                state.selectActor(it.id)
                                onContextMenuRequest?.invoke(position, UseCaseContextMenuType.ACTOR)
                            }
                            clickedUseCase?.let {
                                state.selectUseCase(it.id)
                                onContextMenuRequest?.invoke(position, UseCaseContextMenuType.USE_CASE)
                            }
                            clickedRelation?.let {
                                state.selectRelation(it.id)
                                onContextMenuRequest?.invoke(position, UseCaseContextMenuType.RELATION)
                            }
                            clickedNote?.let {
                                state.selectNote(it.id)
                                onContextMenuRequest?.invoke(position, UseCaseContextMenuType.NOTE)
                            }
                            clickedSystem?.let {
                                state.selectSystemBoundary(it.id)
                                onContextMenuRequest?.invoke(position, UseCaseContextMenuType.SYSTEM)
                            }
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
            drawUseCaseGrid(size, gridColor, state.zoom, state.panOffset)
            state.diagram.systemBoundaries.forEach { boundary ->
                val isSelected = state.selectedSystemBoundaryId == boundary.id
                drawSystemBoundary(
                    boundary = boundary,
                    isSelected = isSelected,
                    color = useCaseColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer
                )
            }
            state.diagram.relations.forEach { relation ->
                val isSelected = state.selectedRelationId == relation.id
                drawUseCaseRelation(
                    relation = relation,
                    diagram = state.diagram,
                    isSelected = isSelected,
                    color = relationColor,
                    selectedColor = selectedColor,
                    textMeasurer = textMeasurer,
                    textBackgroundColor = textBackgroundColor
                )
            }
            state.diagram.actors.forEach { actor ->
                val isSelected = state.selectedActorId == actor.id
                val isPending = state.pendingRelationSourceId == actor.id
                drawActor(
                    actor = actor,
                    isSelected = isSelected,
                    isPendingSource = isPending,
                    actorColor = actorColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    textMeasurer = textMeasurer
                )
            }
            state.diagram.useCases.forEach { useCase ->
                val isSelected = state.selectedUseCaseId == useCase.id
                val isPending = state.pendingRelationSourceId == useCase.id
                drawUseCase(
                    useCase = useCase,
                    isSelected = isSelected,
                    isPendingSource = isPending,
                    useCaseColor = useCaseColor,
                    selectedColor = selectedColor,
                    textColor = textColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer
                )
            }
            state.diagram.notes.forEach { note ->
                val isSelected = state.selectedNoteId == note.id
                drawUseCaseNote(
                    note = note,
                    isSelected = isSelected,
                    textMeasurer = textMeasurer,
                    selectedColor = selectedColor
                )
            }
        }
    }
}
private fun DrawScope.drawSystemBoundary(
    boundary: SystemBoundary,
    isSelected: Boolean,
    color: Color,
    selectedColor: Color,
    textColor: Color,
    surfaceColor: Color,
    textMeasurer: TextMeasurer
) {
    val borderColor = if (isSelected) selectedColor else color.copy(alpha = 0.7f)
    val strokeWidth = if (isSelected) 3f else 2f
    drawRect(
        color = surfaceColor.copy(alpha = 0.4f),
        topLeft = Offset(boundary.x, boundary.y),
        size = Size(boundary.width, boundary.height),
        style = Fill
    )
    drawRect(
        color = borderColor,
        topLeft = Offset(boundary.x, boundary.y),
        size = Size(boundary.width, boundary.height),
        style = Stroke(width = strokeWidth)
    )
    val labelResult = textMeasurer.measure(
        text = boundary.name,
        style = TextStyle(
            color = if (isSelected) selectedColor else textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    )
    val labelPadding = 8f
    drawRoundRect(
        color = borderColor.copy(alpha = 0.15f),
        topLeft = Offset(boundary.x + labelPadding - 4f, boundary.y + labelPadding - 3f),
        size = Size(labelResult.size.width + 16f, labelResult.size.height + 6f),
        cornerRadius = CornerRadius(4f),
        style = Fill
    )
    drawText(
        textLayoutResult = labelResult,
        topLeft = Offset(boundary.x + labelPadding + 4f, boundary.y + labelPadding)
    )
}
private fun DrawScope.drawUseCaseGrid(canvasSize: Size, color: Color, zoom: Float, panOffset: Offset, spacing: Float = 20f) {
    val viewportWidth = canvasSize.width / zoom
    val viewportHeight = canvasSize.height / zoom
    val startX = -panOffset.x / zoom
    val startY = -panOffset.y / zoom
    val endX = startX + viewportWidth
    val endY = startY + viewportHeight
    val firstVerticalLine = (startX / spacing).toInt() * spacing
    val firstHorizontalLine = (startY / spacing).toInt() * spacing
    val numVerticalLines = ((endX - firstVerticalLine) / spacing).toInt() + 2
    val numHorizontalLines = ((endY - firstHorizontalLine) / spacing).toInt() + 2
    for (i in 0..numVerticalLines) {
        val x = firstVerticalLine + i * spacing
        drawLine(color = color, start = Offset(x, startY), end = Offset(x, endY), strokeWidth = 1f)
    }
    for (i in 0..numHorizontalLines) {
        val y = firstHorizontalLine + i * spacing
        drawLine(color = color, start = Offset(startX, y), end = Offset(endX, y), strokeWidth = 1f)
    }
}
private fun DrawScope.drawActor(
    actor: Actor,
    isSelected: Boolean,
    isPendingSource: Boolean,
    actorColor: Color,
    selectedColor: Color,
    textColor: Color,
    textMeasurer: TextMeasurer
) {
    val color = when {
        isSelected -> selectedColor
        isPendingSource -> selectedColor.copy(alpha = 0.8f)
        else -> actorColor
    }
    val strokeWidth = if (isSelected || isPendingSource) 3f else 2f
    val cx = actor.x + actor.width / 2
    if (isSelected) {
        drawRoundRect(
            color = selectedColor.copy(alpha = 0.12f),
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style = Fill
        )
        drawRoundRect(
            color = selectedColor,
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 2.5f)
        )
    } else if (isPendingSource) {
        drawRoundRect(
            color = selectedColor.copy(alpha = 0.1f),
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style = Fill
        )
        drawRoundRect(
            color = selectedColor,
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
            )
        )
    }
    drawCircle(
        color = color,
        radius = 10f,
        center = Offset(cx, actor.y + 10f),
        style = Stroke(width = strokeWidth)
    )
    drawLine(
        color = color,
        start = Offset(cx, actor.y + 20f),
        end = Offset(cx, actor.y + 55f),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(cx - 22f, actor.y + 35f),
        end = Offset(cx + 22f, actor.y + 35f),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(cx, actor.y + 55f),
        end = Offset(cx - 18f, actor.y + 80f),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(cx, actor.y + 55f),
        end = Offset(cx + 18f, actor.y + 80f),
        strokeWidth = strokeWidth
    )
    val nameResult = textMeasurer.measure(
        text = actor.name,
        style = TextStyle(
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    )
    drawText(
        textLayoutResult = nameResult,
        topLeft = Offset(
            cx - nameResult.size.width / 2,
            actor.y + actor.height + 4f
        )
    )
}
private fun DrawScope.drawUseCase(
    useCase: UseCase,
    isSelected: Boolean,
    isPendingSource: Boolean,
    useCaseColor: Color,
    selectedColor: Color,
    textColor: Color,
    surfaceColor: Color,
    textMeasurer: TextMeasurer
) {
    val color = when {
        isSelected -> selectedColor
        isPendingSource -> selectedColor.copy(alpha = 0.8f)
        else -> useCaseColor
    }
    val strokeWidth = if (isSelected || isPendingSource) 3.5f else 2f
    val fillColor = when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isPendingSource -> selectedColor.copy(alpha = 0.06f)
        else -> surfaceColor
    }
    drawOval(
        color = fillColor,
        topLeft = Offset(useCase.x, useCase.y),
        size = Size(useCase.width, useCase.height),
        style = Fill
    )
    if (isPendingSource && !isSelected) {
        drawOval(
            color = color,
            topLeft = Offset(useCase.x, useCase.y),
            size = Size(useCase.width, useCase.height),
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
            )
        )
    } else {
        drawOval(
            color = color,
            topLeft = Offset(useCase.x, useCase.y),
            size = Size(useCase.width, useCase.height),
            style = Stroke(width = strokeWidth)
        )
    }
    val nameResult = textMeasurer.measure(
        text = useCase.name,
        style = TextStyle(
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        ),
        constraints = Constraints(maxWidth = (useCase.width - 16).toInt().coerceAtLeast(1))
    )
    drawText(
        textLayoutResult = nameResult,
        topLeft = Offset(
            useCase.x + (useCase.width - nameResult.size.width) / 2,
            useCase.y + (useCase.height - nameResult.size.height) / 2
        )
    )
}
private fun DrawScope.drawUseCaseRelation(
    relation: UseCaseRelation,
    diagram: UseCaseDiagram,
    isSelected: Boolean,
    color: Color,
    selectedColor: Color,
    textMeasurer: TextMeasurer,
    textBackgroundColor: Color
) {
    val sourceCenter = getElementCenter(relation.sourceId, diagram) ?: return
    val targetCenter = getElementCenter(relation.targetId, diagram) ?: return
    val lineColor = if (isSelected) selectedColor else color
    val strokeWidth = if (isSelected) 3f else 1.5f
    val dx = targetCenter.x - sourceCenter.x
    val dy = targetCenter.y - sourceCenter.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return
    val normX = dx / dist
    val normY = dy / dist
    val sourcePoint = getElementBoundaryPoint(relation.sourceId, diagram, Offset(normX, normY)) ?: sourceCenter
    val targetPoint = getElementBoundaryPoint(relation.targetId, diagram, Offset(-normX, -normY)) ?: targetCenter
    val arrowSize = 13f
    when (relation.type) {
        UseCaseRelationType.ASSOCIATION -> {
            drawLine(color = lineColor, start = sourcePoint, end = targetPoint, strokeWidth = strokeWidth)
        }
        UseCaseRelationType.INCLUDE, UseCaseRelationType.EXTEND -> {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(
                color = lineColor,
                start = sourcePoint,
                end = lineEnd,
                strokeWidth = strokeWidth,
                pathEffect = dashEffect
            )
            drawArrowHead(targetPoint, Offset(normX, normY), lineColor, filled = true, size = arrowSize)
            val midX = (sourcePoint.x + targetPoint.x) / 2
            val midY = (sourcePoint.y + targetPoint.y) / 2
            val perpX = -normY
            val perpY = normX
            val labelResult = textMeasurer.measure(
                text = relation.type.display,
                style = TextStyle(color = lineColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)
            )
            val labelX = midX + perpX * 16f - labelResult.size.width / 2
            val labelY = midY + perpY * 16f - labelResult.size.height / 2
            drawRoundRect(
                color = textBackgroundColor,
                topLeft = Offset(labelX - 4f, labelY - 2f),
                size = Size(labelResult.size.width + 8f, labelResult.size.height + 4f),
                cornerRadius = CornerRadius(4f)
            )
            drawRoundRect(
                color = lineColor.copy(alpha = 0.5f),
                topLeft = Offset(labelX - 4f, labelY - 2f),
                size = Size(labelResult.size.width + 8f, labelResult.size.height + 4f),
                cornerRadius = CornerRadius(4f),
                style = Stroke(width = 1f)
            )
            drawText(textLayoutResult = labelResult, topLeft = Offset(labelX, labelY))
        }
        UseCaseRelationType.GENERALIZATION -> {
            val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(color = lineColor, start = sourcePoint, end = lineEnd, strokeWidth = strokeWidth)
            drawArrowHead(targetPoint, Offset(normX, normY), lineColor, filled = false, size = arrowSize)
        }
    }
}
private fun DrawScope.drawArrowHead(
    tip: Offset,
    direction: Offset,
    color: Color,
    filled: Boolean,
    size: Float = 12f
) {
    val perpX = -direction.y
    val perpY = direction.x
    val base1 = Offset(
        tip.x - direction.x * size + perpX * (size * 0.45f),
        tip.y - direction.y * size + perpY * (size * 0.45f)
    )
    val base2 = Offset(
        tip.x - direction.x * size - perpX * (size * 0.45f),
        tip.y - direction.y * size - perpY * (size * 0.45f)
    )
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(base1.x, base1.y)
        lineTo(base2.x, base2.y)
        close()
    }
    if (filled) {
        drawPath(path = path, color = color, style = Fill)
    } else {
        drawPath(path = path, color = color, style = Stroke(width = 2f))
    }
}
private fun getElementBoundaryPoint(id: String, diagram: UseCaseDiagram, dirFromCenter: Offset): Offset? {
    diagram.actors.find { it.id == id }?.let { actor ->
        val cx = actor.x + actor.width / 2
        val cy = actor.y + actor.height / 2
        val hw = actor.width / 2
        val hh = actor.height / 2
        val scaleX = if (dirFromCenter.x != 0f) hw / abs(dirFromCenter.x) else Float.MAX_VALUE
        val scaleY = if (dirFromCenter.y != 0f) hh / abs(dirFromCenter.y) else Float.MAX_VALUE
        val scale = minOf(scaleX, scaleY)
        return Offset(cx + dirFromCenter.x * scale, cy + dirFromCenter.y * scale)
    }
    diagram.useCases.find { it.id == id }?.let { useCase ->
        val cx = useCase.x + useCase.width / 2
        val cy = useCase.y + useCase.height / 2
        val rx = useCase.width / 2
        val ry = useCase.height / 2
        val dxr = dirFromCenter.x / rx
        val dyr = dirFromCenter.y / ry
        val denom = sqrt(dxr * dxr + dyr * dyr)
        if (denom == 0f) return Offset(cx, cy)
        val t = 1f / denom
        return Offset(cx + dirFromCenter.x * t, cy + dirFromCenter.y * t)
    }
    return null
}
private fun DrawScope.drawUseCaseNote(
    note: Note,
    isSelected: Boolean,
    textMeasurer: TextMeasurer,
    selectedColor: Color
) {
    val noteColor = Color(0xFFFFEB3B)
    val strokeColor = if (isSelected) selectedColor else Color(0xFFFBC02D)
    val strokeWidth = if (isSelected) 3f else 1.5f
    drawRect(color = noteColor, topLeft = Offset(note.x, note.y), size = Size(note.width, note.height), style = Fill)
    drawRect(color = strokeColor, topLeft = Offset(note.x, note.y), size = Size(note.width, note.height), style = Stroke(width = strokeWidth))
    val foldSize = 15f
    val foldPath = Path().apply {
        moveTo(note.x + note.width - foldSize, note.y)
        lineTo(note.x + note.width, note.y + foldSize)
        lineTo(note.x + note.width - foldSize, note.y + foldSize)
        close()
    }
    drawPath(path = foldPath, color = Color(0xFFF9A825), style = Fill)
    drawPath(path = foldPath, color = strokeColor, style = Stroke(width = 1f))
    val padding = 10f
    val textResult = textMeasurer.measure(
        text = note.text,
        style = TextStyle(color = Color.Black, fontSize = 12.sp),
        constraints = Constraints(
            maxWidth = (note.width - padding * 2).toInt().coerceAtLeast(1),
            maxHeight = (note.height - padding * 2).toInt().coerceAtLeast(1)
        )
    )
    drawText(textLayoutResult = textResult, topLeft = Offset(note.x + padding, note.y + padding))
}
private fun getElementCenter(id: String, diagram: UseCaseDiagram): Offset? {
    diagram.actors.find { it.id == id }?.let { return Offset(it.x + it.width / 2, it.y + it.height / 2) }
    diagram.useCases.find { it.id == id }?.let { return Offset(it.x + it.width / 2, it.y + it.height / 2) }
    return null
}
private fun isPointInActor(point: Offset, actor: Actor): Boolean =
    point.x >= actor.x && point.x <= actor.x + actor.width &&
    point.y >= actor.y && point.y <= actor.y + actor.height
private fun isPointInUseCase(point: Offset, useCase: UseCase): Boolean {
    val cx = useCase.x + useCase.width / 2
    val cy = useCase.y + useCase.height / 2
    val rx = useCase.width / 2
    val ry = useCase.height / 2
    val dx = (point.x - cx) / rx
    val dy = (point.y - cy) / ry
    return dx * dx + dy * dy <= 1f
}
private fun isPointNearRelation(point: Offset, relation: UseCaseRelation, diagram: UseCaseDiagram): Boolean {
    val start = getElementCenter(relation.sourceId, diagram) ?: return false
    val end = getElementCenter(relation.targetId, diagram) ?: return false
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return sqrt((point.x - start.x).pow(2) + (point.y - start.y).pow(2)) <= 8f
    val t = (((point.x - start.x) * dx + (point.y - start.y) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = start.x + t * dx
    val projY = start.y + t * dy
    return sqrt((point.x - projX).pow(2) + (point.y - projY).pow(2)) <= 8f
}
private fun handleUseCaseCanvasTap(
    state: UseCaseState,
    offset: Offset,
    onRelationCreateRequest: ((String, String) -> Unit)?
) {
    when (state.toolMode) {
        UseCaseToolMode.SELECT -> {
            state.diagram.actors.find { isPointInActor(offset, it) }?.let {
                state.selectActor(it.id)
                return
            }
            state.diagram.useCases.find { isPointInUseCase(offset, it) }?.let {
                state.selectUseCase(it.id)
                return
            }
            state.diagram.relations.find { isPointNearRelation(offset, it, state.diagram) }?.let {
                state.selectRelation(it.id)
                return
            }
            state.diagram.notes.find {
                offset.x >= it.x && offset.x <= it.x + it.width &&
                offset.y >= it.y && offset.y <= it.y + it.height
            }?.let {
                state.selectNote(it.id)
                return
            }
            state.diagram.systemBoundaries.find {
                offset.x >= it.x && offset.x <= it.x + it.width &&
                offset.y >= it.y && offset.y <= it.y + it.height
            }?.let {
                state.selectSystemBoundary(it.id)
                return
            }
            state.clearSelection()
        }
        UseCaseToolMode.ACTOR -> {
            state.addActor(offset.x - 25f, offset.y - 45f, "Nuovo Attore")
            state.toolMode = UseCaseToolMode.SELECT
        }
        UseCaseToolMode.USE_CASE -> {
            state.addUseCase(offset.x - 80f, offset.y - 30f, "Nuovo Caso d'Uso")
            state.toolMode = UseCaseToolMode.SELECT
        }
        UseCaseToolMode.RELATION -> {
            val clickedId = findClickedActorOrUseCase(offset, state)
            if (clickedId != null) {
                val pending = state.pendingRelationSourceId
                if (pending == null) {
                    state.pendingRelationSourceId = clickedId
                } else if (pending != clickedId) {
                    onRelationCreateRequest?.invoke(pending, clickedId)
                    state.pendingRelationSourceId = null
                } else {
                    state.pendingRelationSourceId = null
                }
            } else {
                state.pendingRelationSourceId = null
            }
        }
        UseCaseToolMode.NOTE -> {
            state.addNote(offset.x - 120f, offset.y - 90f, "Nuova Nota")
            state.toolMode = UseCaseToolMode.SELECT
        }
        UseCaseToolMode.SYSTEM -> {
            state.addSystemBoundary(offset.x - 200f, offset.y - 150f, "Sistema")
            state.toolMode = UseCaseToolMode.SELECT
        }
    }
}
private fun findClickedActorOrUseCase(offset: Offset, state: UseCaseState): String? {
    state.diagram.actors.find { isPointInActor(offset, it) }?.let { return it.id }
    state.diagram.useCases.find { isPointInUseCase(offset, it) }?.let { return it.id }
    return null
}
private fun handleUseCaseDragStart(state: UseCaseState, offset: Offset) {
    val adjusted = Offset(
        (offset.x - state.panOffset.x) / state.zoom,
        (offset.y - state.panOffset.y) / state.zoom
    )
    state.diagram.actors.find { isPointInActor(adjusted, it) }?.let {
        state.selectActor(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.useCases.find { isPointInUseCase(adjusted, it) }?.let {
        state.selectUseCase(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.notes.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
        adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectNote(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.systemBoundaries.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width &&
        adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectSystemBoundary(it.id)
        state.saveDragStartState()
    }
}
private fun handleUseCaseDrag(state: UseCaseState, dragAmount: Offset) {
    state.selectedActorId?.let {
        state.updateActorWithoutSave(it) { actor ->
            actor.copy(
                x = actor.x + dragAmount.x / state.zoom,
                y = actor.y + dragAmount.y / state.zoom
            )
        }
    }
    state.selectedUseCaseId?.let {
        state.updateUseCaseWithoutSave(it) { useCase ->
            useCase.copy(
                x = useCase.x + dragAmount.x / state.zoom,
                y = useCase.y + dragAmount.y / state.zoom
            )
        }
    }
    state.selectedNoteId?.let {
        state.updateNoteWithoutSave(it) { note ->
            note.copy(
                x = note.x + dragAmount.x / state.zoom,
                y = note.y + dragAmount.y / state.zoom
            )
        }
    }
    state.selectedSystemBoundaryId?.let {
        state.updateSystemBoundaryWithoutSave(it) { boundary ->
            boundary.copy(
                x = boundary.x + dragAmount.x / state.zoom,
                y = boundary.y + dragAmount.y / state.zoom
            )
        }
    }
}
