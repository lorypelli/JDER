package com.jder.ui.components
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.jder.domain.model.Actor
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseRelationType
import kotlin.math.abs
import kotlin.math.sqrt
internal fun DrawScope.drawSystemBoundary(
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
    drawText(textLayoutResult = labelResult, topLeft = Offset(boundary.x + labelPadding + 4f, boundary.y + labelPadding))
}
internal fun DrawScope.drawActor(
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
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
        )
    }
    drawCircle(color = color, radius = 10f, center = Offset(cx, actor.y + 10f), style = Stroke(width = strokeWidth))
    drawLine(color = color, start = Offset(cx, actor.y + 20f), end = Offset(cx, actor.y + 55f), strokeWidth = strokeWidth)
    drawLine(color = color, start = Offset(cx - 22f, actor.y + 35f), end = Offset(cx + 22f, actor.y + 35f), strokeWidth = strokeWidth)
    drawLine(color = color, start = Offset(cx, actor.y + 55f), end = Offset(cx - 18f, actor.y + 80f), strokeWidth = strokeWidth)
    drawLine(color = color, start = Offset(cx, actor.y + 55f), end = Offset(cx + 18f, actor.y + 80f), strokeWidth = strokeWidth)
    val nameResult = textMeasurer.measure(
        text = actor.name,
        style = TextStyle(color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    )
    drawText(textLayoutResult = nameResult, topLeft = Offset(cx - nameResult.size.width / 2, actor.y + actor.height + 4f))
}
internal fun DrawScope.drawUseCase(
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
    drawOval(color = fillColor, topLeft = Offset(useCase.x, useCase.y), size = Size(useCase.width, useCase.height), style = Fill)
    if (isPendingSource && !isSelected) {
        drawOval(
            color = color,
            topLeft = Offset(useCase.x, useCase.y),
            size = Size(useCase.width, useCase.height),
            style = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
        )
    } else {
        drawOval(color = color, topLeft = Offset(useCase.x, useCase.y), size = Size(useCase.width, useCase.height), style = Stroke(width = strokeWidth))
    }
    val nameResult = textMeasurer.measure(
        text = useCase.name,
        style = TextStyle(color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
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
internal fun DrawScope.drawUseCaseRelation(
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
            drawLine(color = lineColor, start = sourcePoint, end = lineEnd, strokeWidth = strokeWidth, pathEffect = dashEffect)
            drawArrowHead(targetPoint, Offset(normX, normY), lineColor, filled = true, size = arrowSize)
            val midX = (sourcePoint.x + targetPoint.x) / 2
            val midY = (sourcePoint.y + targetPoint.y) / 2
            val perpX = -normY
            val labelResult = textMeasurer.measure(
                text = relation.type.display,
                style = TextStyle(color = lineColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)
            )
            val labelX = midX + perpX * 16f - labelResult.size.width / 2
            val labelY = midY + normX * 16f - labelResult.size.height / 2
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
private fun DrawScope.drawArrowHead(tip: Offset, direction: Offset, color: Color, filled: Boolean, size: Float = 12f) {
    val perpX = -direction.y
    val perpY = direction.x
    val base1 = Offset(tip.x - direction.x * size + perpX * (size * 0.45f), tip.y - direction.y * size + perpY * (size * 0.45f))
    val base2 = Offset(tip.x - direction.x * size - perpX * (size * 0.45f), tip.y - direction.y * size - perpY * (size * 0.45f))
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(base1.x, base1.y)
        lineTo(base2.x, base2.y)
        close()
    }
    if (filled) drawPath(path = path, color = color, style = Fill)
    else drawPath(path = path, color = color, style = Stroke(width = 2f))
}
internal fun getElementCenter(id: String, diagram: UseCaseDiagram): Offset? {
    diagram.actors.find { it.id == id }?.let { return Offset(it.x + it.width / 2, it.y + it.height / 2) }
    diagram.useCases.find { it.id == id }?.let { return Offset(it.x + it.width / 2, it.y + it.height / 2) }
    return null
}
internal fun getElementBoundaryPoint(id: String, diagram: UseCaseDiagram, dirFromCenter: Offset): Offset? {
    diagram.actors.find { it.id == id }?.let { actor ->
        val cx = actor.x + actor.width / 2
        val cy = actor.y + actor.height / 2
        val hw = actor.width / 2
        val hh = actor.height / 2
        val scaleX = if (dirFromCenter.x != 0f) hw / abs(dirFromCenter.x) else Float.MAX_VALUE
        val scaleY = if (dirFromCenter.y != 0f) hh / abs(dirFromCenter.y) else Float.MAX_VALUE
        return Offset(cx + dirFromCenter.x * minOf(scaleX, scaleY), cy + dirFromCenter.y * minOf(scaleX, scaleY))
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
