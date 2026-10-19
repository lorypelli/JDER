package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.jder.domain.model.SequenceAnchor
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.SequenceFragment
import com.jder.domain.model.SequenceFragmentType
import com.jder.domain.model.SequenceLifelineType
import com.jder.domain.model.SequenceMessage
import com.jder.domain.model.SequenceMessageType

private const val MESSAGE_LABEL_MAX_WIDTH = 360f
private const val ICON_RADIUS = 14f

internal fun DrawScope.drawSequenceDiagram(
    diagram: SequenceDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
    pendingSource: SequenceAnchor? = null,
) {
    val layout = sequenceLayout(diagram)
    diagram.fragments.forEach {
        drawSequenceFragment(
            fragment = it,
            isSelected = it.id == selectedId,
            isHovered = it.id == hoveredId,
            palette = palette,
        )
    }
    diagram.lifelines.forEach { lifeline ->
        layout.lifelines[lifeline.id]?.let {
            drawLifelineLine(layout = it, isSelected = lifeline.id == selectedId, palette = palette)
        }
    }
    layout.activations.forEach {
        drawActivation(activation = it, isSelected = it.lifelineId == selectedId, palette = palette)
    }
    diagram.lifelines.forEach { lifeline ->
        layout.lifelines[lifeline.id]?.let {
            drawLifelineHead(
                layout = it,
                isSelected = lifeline.id == selectedId,
                isHovered = lifeline.id == hoveredId,
                isPendingSource = lifeline.id == pendingSource?.lifelineId,
                palette = palette,
                textMeasurer = textMeasurer,
            )
        }
    }
    diagram.fragments.forEach {
        drawSequenceFragmentLabels(
            fragment = it,
            isSelected = it.id == selectedId,
            palette = palette,
            textMeasurer = textMeasurer,
        )
    }
    diagram.messages.forEach { message ->
        layout.messageRoutes[message.id]?.let {
            drawSequenceMessage(
                message = message,
                points = it,
                isSelected = message.id == selectedId,
                isHovered = message.id == hoveredId,
                palette = palette,
                textMeasurer = textMeasurer,
            )
        }
    }
    pendingSource?.let { anchor ->
        layout.lifelines[anchor.lifelineId]?.let {
            val center = Offset(it.centerX, anchor.y)
            drawCircle(color = palette.selected.copy(alpha = 0.2f), radius = 8f, center = center)
            drawCircle(
                color = palette.selected,
                radius = 8f,
                center = center,
                style = Stroke(width = 2f),
            )
        }
    }
    diagram.notes.forEach {
        drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
    }
}

internal fun DrawScope.drawSequenceMessagePreview(
    start: Offset,
    end: Offset,
    palette: DiagramPalette,
) {
    drawLine(
        color = palette.selected,
        start = start,
        end = end,
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
    )
    val direction = end - start
    val length = direction.getDistance()
    if (length > 12f) drawArrowHead(end, direction / length, palette.selected, null, size = 10f)
}

internal fun sequenceDiagramBounds(diagram: SequenceDiagram, textMeasurer: TextMeasurer): Rect? {
    val layout = sequenceLayout(diagram)
    val bounds = BoundsBuilder()
    layout.lifelines.values.forEach {
        bounds.include(it.head)
        bounds.include(Offset(it.centerX, it.lineBottom + if (it.isDestroyed) 10f else 0f))
    }
    layout.activations.forEach { bounds.include(it.bounds) }
    diagram.messages.forEach { message ->
        val points = layout.messageRoutes[message.id] ?: return@forEach
        points.forEach(bounds::include)
        messageLabel(message, points, textMeasurer, Color.Black)?.let { bounds.include(it.bounds) }
    }
    diagram.fragments.forEach { bounds.include(it.bounds()) }
    diagram.notes.forEach { bounds.include(it.bounds()) }
    return bounds.build()
}

private class MessageLabel(val text: TextLayoutResult, val topLeft: Offset) {
    val bounds: Rect
        get() = labelBackground(text, topLeft)
}

private fun labelBackground(text: TextLayoutResult, topLeft: Offset): Rect =
    Rect(topLeft, text.size.toSize()).inflate(2f)

private fun DrawScope.drawLabel(text: TextLayoutResult, topLeft: Offset, palette: DiagramPalette) {
    val background = labelBackground(text, topLeft)
    drawRoundRect(
        color = palette.textBackground,
        topLeft = background.topLeft,
        size = background.size,
        cornerRadius = CornerRadius(3f),
    )
    drawText(text, topLeft = topLeft)
}

private fun messageLabel(
    message: SequenceMessage,
    points: List<Offset>,
    textMeasurer: TextMeasurer,
    color: Color,
): MessageLabel? {
    val label =
        when {
            message.label.isNotBlank() -> message.label
            message.type == SequenceMessageType.CREATE -> "«create»"
            message.type == SequenceMessageType.DESTROY -> "«destroy»"
            else -> return null
        }
    val text =
        textMeasurer.measureLine(
            text = label,
            style = TextStyle(color = color, fontSize = 12.sp),
            maxWidth = MESSAGE_LABEL_MAX_WIDTH,
        )
    val topLeft =
        if (message.isSelfMessage) {
            Offset(points[1].x + 6f, (points[1].y + points[2].y) / 2 - text.size.height / 2f)
        } else {
            val centerX = (points.first().x + points.last().x) / 2
            Offset(centerX - text.size.width / 2f, points.first().y - text.size.height - 4f)
        }
    return MessageLabel(text, topLeft)
}

private fun DrawScope.drawSequenceMessage(
    message: SequenceMessage,
    points: List<Offset>,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val color = if (isSelected) palette.selected else palette.outline
    val strokeWidth =
        when {
            isSelected -> 2.5f
            isHovered -> 2f
            else -> 1.5f
        }
    val isDashed =
        message.type == SequenceMessageType.REPLY || message.type == SequenceMessageType.CREATE
    val path =
        Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
    drawPath(
        path = path,
        color = color,
        style =
            Stroke(
                width = strokeWidth,
                join = StrokeJoin.Round,
                pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 5f)) else null,
            ),
    )
    val tip = points.last()
    val lastSegment = tip - points[points.size - 2]
    val length = lastSegment.getDistance()
    if (length > 0f) {
        val direction = lastSegment / length
        when (message.type) {
            SequenceMessageType.SYNC,
            SequenceMessageType.DESTROY -> drawArrowHead(tip, direction, color, null, size = 11f)
            else -> drawOpenArrowHead(tip, direction, color, 11f)
        }
    }
    messageLabel(message, points, textMeasurer, if (isSelected) palette.selected else palette.text)
        ?.let { drawLabel(it.text, it.topLeft, palette) }
}

private fun DrawScope.drawLifelineLine(
    layout: SequenceLifelineLayout,
    isSelected: Boolean,
    palette: DiagramPalette,
) {
    val color = if (isSelected) palette.selected else palette.outline
    drawLine(
        color = color,
        start = Offset(layout.centerX, layout.head.bottom),
        end = Offset(layout.centerX, layout.lineBottom),
        strokeWidth = if (isSelected) 2f else 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
    )
    if (layout.isDestroyed) {
        val center = Offset(layout.centerX, layout.lineBottom)
        val crossColor = if (isSelected) palette.selected else palette.text
        drawLine(crossColor, center + Offset(-9f, -9f), center + Offset(9f, 9f), 2.5f)
        drawLine(crossColor, center + Offset(-9f, 9f), center + Offset(9f, -9f), 2.5f)
    }
}

private fun DrawScope.drawActivation(
    activation: SequenceActivation,
    isSelected: Boolean,
    palette: DiagramPalette,
) {
    val bounds = activation.bounds
    drawRect(color = palette.surface, topLeft = bounds.topLeft, size = bounds.size)
    drawRect(
        color = palette.primary.copy(alpha = 0.18f),
        topLeft = bounds.topLeft,
        size = bounds.size,
    )
    drawRect(
        color = if (isSelected) palette.selected else palette.primary,
        topLeft = bounds.topLeft,
        size = bounds.size,
        style = Stroke(width = 1.5f),
    )
}

private fun DrawScope.drawLifelineHead(
    layout: SequenceLifelineLayout,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val lifeline = layout.lifeline
    val head = layout.head
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
    if (isSelected || isPendingSource) {
        val halo = head.inflate(6f)
        drawRoundRect(
            color = selectedColor.copy(alpha = if (isSelected) 0.12f else 0.1f),
            topLeft = halo.topLeft,
            size = halo.size,
            cornerRadius = CornerRadius(6f),
        )
        drawRoundRect(
            color = selectedColor,
            topLeft = halo.topLeft,
            size = halo.size,
            cornerRadius = CornerRadius(6f),
            style =
                if (isSelected) Stroke(width = 2.5f)
                else
                    Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
        )
    }
    val nameStyle =
        TextStyle(
            color = palette.text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    if (lifeline.type == SequenceLifelineType.OBJECT) {
        drawRect(color = palette.surface, topLeft = head.topLeft, size = head.size)
        if (isHovered && !isSelected) {
            drawRect(
                color = palette.primary.copy(alpha = 0.05f),
                topLeft = head.topLeft,
                size = head.size,
            )
        }
        drawRect(
            color = color,
            topLeft = head.topLeft,
            size = head.size,
            style = Stroke(width = strokeWidth),
        )
        val name =
            textMeasurer.measureBlock(
                text = lifeline.name,
                style = nameStyle,
                maxWidth = head.width - 12f,
                maxLines = 2,
            )
        drawTextCentered(name, head.center)
        return
    }
    val centerX = head.center.x
    val nameTop =
        if (lifeline.type == SequenceLifelineType.ACTOR) {
            drawStickFigure(centerX, head.top, color, strokeWidth)
            head.top + 52f
        } else {
            drawStereotypeIcon(lifeline.type, centerX, head.top, color, strokeWidth, palette)
            head.top + 38f
        }
    val name = textMeasurer.measureLine(lifeline.name, nameStyle, maxWidth = head.width)
    drawText(name, topLeft = Offset(centerX - name.size.width / 2f, nameTop))
}

private fun DrawScope.drawStickFigure(
    centerX: Float,
    top: Float,
    color: Color,
    strokeWidth: Float,
) {
    drawCircle(
        color = color,
        radius = 8f,
        center = Offset(centerX, top + 9f),
        style = Stroke(width = strokeWidth),
    )
    drawLine(color, Offset(centerX, top + 17f), Offset(centerX, top + 34f), strokeWidth)
    drawLine(color, Offset(centerX - 14f, top + 23f), Offset(centerX + 14f, top + 23f), strokeWidth)
    drawLine(color, Offset(centerX, top + 34f), Offset(centerX - 11f, top + 48f), strokeWidth)
    drawLine(color, Offset(centerX, top + 34f), Offset(centerX + 11f, top + 48f), strokeWidth)
}

private fun DrawScope.drawStereotypeIcon(
    type: SequenceLifelineType,
    centerX: Float,
    top: Float,
    color: Color,
    strokeWidth: Float,
    palette: DiagramPalette,
) {
    val stroke = Stroke(width = strokeWidth)
    when (type) {
        SequenceLifelineType.BOUNDARY -> {
            val center = Offset(centerX + 6f, top + 18f)
            drawLine(
                color,
                Offset(centerX - 18f, top + 6f),
                Offset(centerX - 18f, top + 30f),
                strokeWidth,
            )
            drawLine(
                color,
                Offset(centerX - 18f, center.y),
                Offset(center.x - ICON_RADIUS, center.y),
                strokeWidth,
            )
            drawCircle(color = palette.surface, radius = ICON_RADIUS, center = center)
            drawCircle(color = color, radius = ICON_RADIUS, center = center, style = stroke)
        }
        SequenceLifelineType.CONTROL -> {
            val center = Offset(centerX, top + 20f)
            drawCircle(color = palette.surface, radius = ICON_RADIUS, center = center)
            drawCircle(color = color, radius = ICON_RADIUS, center = center, style = stroke)
            val tip = Offset(centerX - 3f, top + 6f)
            drawLine(color, tip, Offset(centerX + 4f, top + 1f), strokeWidth)
            drawLine(color, tip, Offset(centerX + 4f, top + 11f), strokeWidth)
        }
        else -> {
            val center = Offset(centerX, top + 17f)
            drawCircle(color = palette.surface, radius = ICON_RADIUS, center = center)
            drawCircle(color = color, radius = ICON_RADIUS, center = center, style = stroke)
            drawLine(
                color,
                Offset(centerX - ICON_RADIUS, top + 34f),
                Offset(centerX + ICON_RADIUS, top + 34f),
                strokeWidth,
            )
        }
    }
}

private fun fragmentColor(isSelected: Boolean, palette: DiagramPalette): Color =
    if (isSelected) palette.selected else palette.secondary.copy(alpha = 0.8f)

private fun DrawScope.drawSequenceFragment(
    fragment: SequenceFragment,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
) {
    val bounds = fragment.bounds()
    val color = fragmentColor(isSelected, palette)
    val strokeWidth =
        when {
            isSelected -> 2.5f
            isHovered -> 2f
            else -> 1.5f
        }
    drawRect(
        color = palette.surface.copy(alpha = 0.35f),
        topLeft = bounds.topLeft,
        size = bounds.size,
    )
    drawRect(
        color = color,
        topLeft = bounds.topLeft,
        size = bounds.size,
        style = Stroke(width = strokeWidth),
    )
    if (fragment.type.hasOperands) {
        fragment.operands.forEach { operand ->
            val y = fragment.operandY(operand)
            drawLine(
                color = color,
                start = Offset(fragment.x, y),
                end = Offset(fragment.x + fragment.width, y),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
            )
        }
    }
}

private fun DrawScope.drawSequenceFragmentLabels(
    fragment: SequenceFragment,
    isSelected: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val color = fragmentColor(isSelected, palette)
    val keyword =
        textMeasurer.measure(
            text = fragment.type.keyword,
            style = TextStyle(color = palette.text, fontSize = 12.sp, fontWeight = FontWeight.Bold),
        )
    val labelWidth = keyword.size.width + 22f
    val labelBottom = fragment.y + SEQUENCE_FRAGMENT_LABEL_HEIGHT
    val label =
        Path().apply {
            moveTo(fragment.x, fragment.y)
            lineTo(fragment.x + labelWidth, fragment.y)
            lineTo(fragment.x + labelWidth, labelBottom - 6f)
            lineTo(fragment.x + labelWidth - 6f, labelBottom)
            lineTo(fragment.x, labelBottom)
            close()
        }
    drawPath(path = label, color = palette.surface)
    drawPath(path = label, color = color.copy(alpha = 0.15f))
    drawPath(path = label, color = color, style = Stroke(width = 1.5f))
    drawText(
        keyword,
        topLeft =
            Offset(
                fragment.x + 8f,
                fragment.y + (SEQUENCE_FRAGMENT_LABEL_HEIGHT - keyword.size.height) / 2f,
            ),
    )
    val guardStyle = TextStyle(color = palette.text, fontSize = 12.sp)
    if (fragment.type == SequenceFragmentType.REF) {
        if (fragment.condition.isNotBlank()) {
            val name =
                textMeasurer.measureBlock(
                    text = fragment.condition,
                    style = guardStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    maxWidth = fragment.width - 24f,
                    maxLines = 2,
                )
            val center =
                Offset(
                    fragment.x + fragment.width / 2,
                    (labelBottom + fragment.y + fragment.height) / 2,
                )
            drawLabel(name, center - Offset(name.size.width / 2f, name.size.height / 2f), palette)
        }
    } else if (fragment.condition.isNotBlank()) {
        val guard =
            textMeasurer.measureLine(
                text = "[${fragment.condition}]",
                style = guardStyle,
                maxWidth = fragment.width - labelWidth - 16f,
            )
        drawLabel(
            guard,
            Offset(
                fragment.x + labelWidth + 8f,
                fragment.y + (SEQUENCE_FRAGMENT_LABEL_HEIGHT - guard.size.height) / 2f,
            ),
            palette,
        )
    }
    if (fragment.type.hasOperands) {
        fragment.operands
            .filter { it.condition.isNotBlank() }
            .forEach { operand ->
                val guard =
                    textMeasurer.measureLine(
                        text = "[${operand.condition}]",
                        style = guardStyle,
                        maxWidth = fragment.width - 16f,
                    )
                drawLabel(guard, Offset(fragment.x + 8f, fragment.operandY(operand) + 4f), palette)
            }
    }
}
