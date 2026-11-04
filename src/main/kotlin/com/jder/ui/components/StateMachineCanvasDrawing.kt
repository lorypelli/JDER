package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jder.domain.model.MachineState
import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineKind
import kotlin.math.sqrt

private const val ARROW_INSET = TRANSITION_ARROW_SIZE * 0.8f
private const val STATE_TEXT_PADDING = 8f
private const val FINAL_RING_GAP = 5f
private const val MOORE_TEXT_OFFSET = 0.42f
private val previewDash = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))

private fun stateTextStyle(color: Color, isSelected: Boolean) =
    TextStyle(
        color = color,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
    )

internal fun DrawScope.drawStateMachineDiagram(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    layout: StateMachineLayout,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
    highlightedRingIds: Set<String> = emptySet(),
    pendingSourceId: String? = null,
) {
    val selectedEdge = layout.edgeOf(selectedId)
    layout.edges.forEach { edge ->
        if (edge !== selectedEdge) {
            val isHovered = hoveredId in edge.transitionIds
            drawTransitionEdge(edge, palette.outline, if (isHovered) 2.2f else 1.6f)
        }
    }
    selectedEdge?.let { drawTransitionEdge(it, palette.selected, 3f) }
    layout.initialArrow?.let { drawInitialArrow(it, palette.text) }
    diagram.states.forEach {
        drawMachineState(
            state = it,
            kind = kind,
            isSelected = it.id == selectedId,
            isHovered = it.id == hoveredId,
            isRingHighlighted = it.id in highlightedRingIds,
            isPendingSource = it.id == pendingSourceId,
            palette = palette,
            textMeasurer = textMeasurer,
        )
    }
    layout.edges.forEach {
        it.labels.forEach { line ->
            drawTransitionLabel(line, line.transitionId == selectedId, palette)
        }
    }
    diagram.notes.forEach {
        drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
    }
}

internal fun stateMachineDiagramBounds(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    textMeasurer: TextMeasurer,
): Rect? {
    val (edges, initialArrow) = stateMachineLayout(diagram, kind, textMeasurer)
    val bounds = BoundsBuilder()
    diagram.states.forEach { bounds.include(Rect(it.center, MACHINE_STATE_RADIUS + 4f)) }
    edges.forEach { edge ->
        edge.points.forEach(bounds::include)
        bounds.include(Rect(edge.tip, TRANSITION_ARROW_SIZE / 2))
        edge.labels.forEach { bounds.include(it.rect.inflate(3f)) }
    }
    initialArrow?.let {
        bounds.include(it.start)
        bounds.include(it.tip)
    }
    diagram.notes.forEach { bounds.include(it.bounds()) }
    return bounds.build()
}

internal fun DrawScope.drawTransitionPreview(shape: TransitionShape, palette: DiagramPalette) {
    val color = palette.selected
    val (tip, direction) = shape.arrow()
    drawTransitionShape(shape, color, Stroke(width = 2f, pathEffect = previewDash), tip, direction)
    drawArrowHead(tip, direction, color, null, size = TRANSITION_ARROW_SIZE)
}

private fun DrawScope.drawTransitionEdge(edge: TransitionEdge, color: Color, strokeWidth: Float) {
    drawTransitionShape(edge.shape, color, Stroke(width = strokeWidth), edge.tip, edge.direction)
    drawArrowHead(edge.tip, edge.direction, color, null, size = TRANSITION_ARROW_SIZE)
}

private fun DrawScope.drawTransitionShape(
    shape: TransitionShape,
    color: Color,
    stroke: Stroke,
    tip: Offset,
    direction: Offset,
) {
    when (shape) {
        is TransitionShape.Line -> {
            val end = tip - direction * ARROW_INSET
            if ((end - shape.start).getDistance() > 1f) {
                drawLine(
                    color = color,
                    start = shape.start,
                    end = end,
                    strokeWidth = stroke.width,
                    pathEffect = stroke.pathEffect,
                )
            }
        }
        is TransitionShape.Curve -> {
            val length = shape.samplePoints().zipWithNext { a, b -> (b - a).getDistance() }.sum()
            val t = (1f - ARROW_INSET / length.coerceAtLeast(1f)).coerceIn(0f, 1f)
            val control = shape.start + (shape.control - shape.start) * t
            val end = quadraticPoint(shape.start, shape.control, shape.end, t)
            val path =
                Path().apply {
                    moveTo(shape.start.x, shape.start.y)
                    quadraticBezierTo(control.x, control.y, end.x, end.y)
                }
            drawPath(path = path, color = color, style = stroke)
        }
        is TransitionShape.Loop -> {
            val sweep = shape.sweepAngle - ARROW_INSET / shape.radius
            drawArc(
                color = color,
                startAngle = Math.toDegrees(shape.startAngle.toDouble()).toFloat(),
                sweepAngle = Math.toDegrees(sweep.toDouble()).toFloat(),
                useCenter = false,
                topLeft = shape.center - Offset(shape.radius, shape.radius),
                size = Size(shape.radius * 2, shape.radius * 2),
                style = stroke,
            )
        }
    }
}

private fun DrawScope.drawInitialArrow(arrow: InitialArrow, color: Color) {
    drawLine(
        color = color,
        start = arrow.start,
        end = arrow.tip - arrow.direction * ARROW_INSET,
        strokeWidth = 2f,
    )
    drawArrowHead(arrow.tip, arrow.direction, color, null, size = TRANSITION_ARROW_SIZE)
}

private fun DrawScope.drawTransitionLabel(
    line: TransitionLabelLine,
    isSelected: Boolean,
    palette: DiagramPalette,
) {
    val background = line.rect.inflate(2f)
    drawRoundRect(
        color = if (isSelected) palette.selected.copy(alpha = 0.15f) else palette.textBackground,
        topLeft = background.topLeft,
        size = background.size,
        cornerRadius = CornerRadius(3f),
    )
    drawText(
        textLayoutResult = line.layout,
        color = if (isSelected) palette.selected else palette.text,
        topLeft = line.topLeft,
    )
}

private fun DrawScope.drawMachineState(
    state: MachineState,
    kind: StateMachineKind,
    isSelected: Boolean,
    isHovered: Boolean,
    isRingHighlighted: Boolean,
    isPendingSource: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val center = state.center
    val radius = MACHINE_STATE_RADIUS
    val selectedColor = palette.selected
    val color =
        when {
            isSelected -> selectedColor
            isPendingSource -> selectedColor.copy(alpha = 0.8f)
            else -> palette.primary
        }
    val strokeWidth =
        when {
            isSelected || isPendingSource -> 3f
            isHovered -> 2.5f
            else -> 2f
        }
    if (isRingHighlighted) {
        drawCircle(
            color = selectedColor.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = MACHINE_RING_HIT * 2),
        )
    }
    drawCircle(color = palette.surface, radius = radius, center = center)
    when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isPendingSource -> selectedColor.copy(alpha = 0.06f)
        isHovered -> palette.primary.copy(alpha = 0.05f)
        else -> null
    }?.let { drawCircle(color = it, radius = radius, center = center) }
    val outline =
        if (isPendingSource && !isSelected) {
            Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
            )
        } else {
            Stroke(width = strokeWidth)
        }
    drawCircle(color = color, radius = radius, center = center, style = outline)
    val textRadius = if (state.isFinal) radius - FINAL_RING_GAP else radius
    if (state.isFinal) {
        drawCircle(color = color, radius = textRadius, center = center, style = outline)
    }
    val style = stateTextStyle(palette.text, isSelected)
    val output = state.output.trim()
    if (kind.hasStateOutputs && output.isNotEmpty()) {
        val textOffset = textRadius * MOORE_TEXT_OFFSET
        val maxWidth =
            2 * sqrt(textRadius * textRadius - textOffset * textOffset) - STATE_TEXT_PADDING
        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(center.x - textRadius, center.y),
            end = Offset(center.x + textRadius, center.y),
            strokeWidth = 1.5f,
        )
        drawTextCentered(
            textMeasurer.measureLine(state.name, style, maxWidth),
            Offset(center.x, center.y - textOffset),
        )
        drawTextCentered(
            textMeasurer.measureLine(output, style, maxWidth),
            Offset(center.x, center.y + textOffset),
        )
    } else {
        drawTextCentered(
            textMeasurer.measureBlock(
                text = state.name,
                style = style,
                maxWidth = textRadius * 2 - STATE_TEXT_PADDING * 2,
                maxLines = 2,
            ),
            center,
        )
    }
}
