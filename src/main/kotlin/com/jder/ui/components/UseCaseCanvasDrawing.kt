package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jder.domain.model.Actor
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseRelationType
import kotlin.math.abs
import kotlin.math.sqrt

internal fun DrawScope.drawUseCaseDiagram(
    diagram: UseCaseDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
    pendingSourceId: String? = null,
) {
    diagram.systemBoundaries.forEach {
        drawSystemBoundary(it, it.id == selectedId, it.id == hoveredId, palette, textMeasurer)
    }
    diagram.relations.forEach {
        drawUseCaseRelation(it, diagram, it.id == selectedId, palette, textMeasurer)
    }
    diagram.actors.forEach {
        drawActor(
            actor = it,
            isSelected = it.id == selectedId,
            isHovered = it.id == hoveredId,
            isPendingSource = it.id == pendingSourceId,
            palette = palette,
            textMeasurer = textMeasurer,
        )
    }
    diagram.useCases.forEach {
        drawUseCase(
            useCase = it,
            isSelected = it.id == selectedId,
            isHovered = it.id == hoveredId,
            isPendingSource = it.id == pendingSourceId,
            palette = palette,
            textMeasurer = textMeasurer,
        )
    }
    diagram.notes.forEach {
        drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
    }
}

internal fun useCaseDiagramBounds(diagram: UseCaseDiagram, textMeasurer: TextMeasurer): Rect? {
    val bounds = BoundsBuilder()
    diagram.systemBoundaries.forEach {
        bounds.include(Rect(it.x, it.y, it.x + it.width, it.y + it.height))
    }
    diagram.actors.forEach {
        bounds.include(Rect(it.x - 6f, it.y - 6f, it.x + it.width + 6f, it.y + it.height + 6f))
        val name = textMeasurer.measure(it.name, actorNameStyle(Color.Black, false))
        val centerX = it.x + it.width / 2
        bounds.include(
            Rect(
                centerX - name.size.width / 2f,
                it.y + it.height + 4f,
                centerX + name.size.width / 2f,
                it.y + it.height + 4f + name.size.height,
            )
        )
    }
    diagram.useCases.forEach { bounds.include(Rect(it.x, it.y, it.x + it.width, it.y + it.height)) }
    diagram.notes.forEach { bounds.include(it.bounds()) }
    return bounds.build()
}

private fun actorNameStyle(color: Color, isSelected: Boolean) =
    TextStyle(
        color = color,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
    )

internal fun DrawScope.drawSystemBoundary(
    boundary: SystemBoundary,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val borderColor = if (isSelected) palette.selected else palette.secondary.copy(alpha = 0.7f)
    val strokeWidth =
        when {
            isSelected -> 3f
            isHovered -> 2.5f
            else -> 2f
        }
    drawRect(
        color = palette.surface.copy(alpha = 0.4f),
        topLeft = Offset(boundary.x, boundary.y),
        size = Size(boundary.width, boundary.height),
        style = Fill,
    )
    drawRect(
        color = borderColor,
        topLeft = Offset(boundary.x, boundary.y),
        size = Size(boundary.width, boundary.height),
        style = Stroke(width = strokeWidth),
    )
    val labelResult =
        textMeasurer.measureLine(
            text = boundary.name,
            style =
                TextStyle(
                    color = if (isSelected) palette.selected else palette.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            maxWidth = boundary.width - 32f,
        )
    val labelPadding = 8f
    drawRoundRect(
        color = borderColor.copy(alpha = 0.15f),
        topLeft = Offset(boundary.x + labelPadding - 4f, boundary.y + labelPadding - 3f),
        size = Size(labelResult.size.width + 16f, labelResult.size.height + 6f),
        cornerRadius = CornerRadius(4f),
        style = Fill,
    )
    drawText(
        textLayoutResult = labelResult,
        topLeft = Offset(boundary.x + labelPadding + 4f, boundary.y + labelPadding),
    )
}

internal fun DrawScope.drawActor(
    actor: Actor,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
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
    val cx = actor.x + actor.width / 2
    if (isSelected || isPendingSource) {
        drawRoundRect(
            color = selectedColor.copy(alpha = if (isSelected) 0.12f else 0.1f),
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style = Fill,
        )
        drawRoundRect(
            color = selectedColor,
            topLeft = Offset(actor.x - 6f, actor.y - 6f),
            size = Size(actor.width + 12f, actor.height + 12f),
            cornerRadius = CornerRadius(6f),
            style =
                if (isSelected) Stroke(width = 2.5f)
                else
                    Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
        )
    }
    drawCircle(
        color = color,
        radius = 10f,
        center = Offset(cx, actor.y + 10f),
        style = Stroke(width = strokeWidth),
    )
    drawLine(color, Offset(cx, actor.y + 20f), Offset(cx, actor.y + 55f), strokeWidth)
    drawLine(color, Offset(cx - 22f, actor.y + 35f), Offset(cx + 22f, actor.y + 35f), strokeWidth)
    drawLine(color, Offset(cx, actor.y + 55f), Offset(cx - 18f, actor.y + 80f), strokeWidth)
    drawLine(color, Offset(cx, actor.y + 55f), Offset(cx + 18f, actor.y + 80f), strokeWidth)
    val nameResult = textMeasurer.measure(actor.name, actorNameStyle(palette.text, isSelected))
    drawText(
        textLayoutResult = nameResult,
        topLeft = Offset(cx - nameResult.size.width / 2, actor.y + actor.height + 4f),
    )
}

internal fun DrawScope.drawUseCase(
    useCase: UseCase,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val selectedColor = palette.selected
    val color =
        when {
            isSelected -> selectedColor
            isPendingSource -> selectedColor.copy(alpha = 0.8f)
            else -> palette.secondary
        }
    val strokeWidth =
        when {
            isSelected || isPendingSource -> 3.5f
            isHovered -> 2.5f
            else -> 2f
        }
    val topLeft = Offset(useCase.x, useCase.y)
    val size = Size(useCase.width, useCase.height)
    drawOval(color = palette.surface, topLeft = topLeft, size = size, style = Fill)
    when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isPendingSource -> selectedColor.copy(alpha = 0.06f)
        isHovered -> palette.secondary.copy(alpha = 0.05f)
        else -> null
    }?.let { drawOval(color = it, topLeft = topLeft, size = size, style = Fill) }
    drawOval(
        color = color,
        topLeft = topLeft,
        size = size,
        style =
            if (isPendingSource && !isSelected) {
                Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
                )
            } else {
                Stroke(width = strokeWidth)
            },
    )
    val nameResult =
        textMeasurer.measureBlock(
            text = useCase.name,
            style =
                TextStyle(
                    color = palette.text,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
            maxWidth = useCase.width - 24,
            maxLines = 3,
        )
    drawTextCentered(
        nameResult,
        Offset(useCase.x + useCase.width / 2, useCase.y + useCase.height / 2),
    )
}

internal fun DrawScope.drawUseCaseRelation(
    relation: UseCaseRelation,
    diagram: UseCaseDiagram,
    isSelected: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val sourceCenter = useCaseElementCenter(relation.sourceId, diagram) ?: return
    val targetCenter = useCaseElementCenter(relation.targetId, diagram) ?: return
    val lineColor = if (isSelected) palette.selected else palette.outline
    val strokeWidth = if (isSelected) 3f else 1.5f
    val dx = targetCenter.x - sourceCenter.x
    val dy = targetCenter.y - sourceCenter.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return
    val normX = dx / dist
    val normY = dy / dist
    val sourcePoint =
        useCaseElementBoundaryPoint(relation.sourceId, diagram, Offset(normX, normY))
            ?: sourceCenter
    val targetPoint =
        useCaseElementBoundaryPoint(relation.targetId, diagram, Offset(-normX, -normY))
            ?: targetCenter
    val arrowSize = 13f
    when (relation.type) {
        UseCaseRelationType.ASSOCIATION -> {
            drawLine(
                color = lineColor,
                start = sourcePoint,
                end = targetPoint,
                strokeWidth = strokeWidth,
            )
        }
        UseCaseRelationType.INCLUDE,
        UseCaseRelationType.EXTEND -> {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            val lineEnd =
                Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(
                color = lineColor,
                start = sourcePoint,
                end = lineEnd,
                strokeWidth = strokeWidth,
                pathEffect = dashEffect,
            )
            drawArrowHead(targetPoint, Offset(normX, normY), lineColor, null, size = arrowSize)
            val midX = (sourcePoint.x + targetPoint.x) / 2
            val midY = (sourcePoint.y + targetPoint.y) / 2
            val perpX = -normY
            val labelResult =
                textMeasurer.measure(
                    text = relation.type.display,
                    style =
                        TextStyle(
                            color = lineColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                )
            val labelX = midX + perpX * 16f - labelResult.size.width / 2
            val labelY = midY + normX * 16f - labelResult.size.height / 2
            drawRoundRect(
                color = palette.textBackground,
                topLeft = Offset(labelX - 4f, labelY - 2f),
                size = Size(labelResult.size.width + 8f, labelResult.size.height + 4f),
                cornerRadius = CornerRadius(4f),
            )
            drawRoundRect(
                color = lineColor.copy(alpha = 0.5f),
                topLeft = Offset(labelX - 4f, labelY - 2f),
                size = Size(labelResult.size.width + 8f, labelResult.size.height + 4f),
                cornerRadius = CornerRadius(4f),
                style = Stroke(width = 1f),
            )
            drawText(textLayoutResult = labelResult, topLeft = Offset(labelX, labelY))
        }
        UseCaseRelationType.GENERALIZATION -> {
            val lineEnd =
                Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(
                color = lineColor,
                start = sourcePoint,
                end = lineEnd,
                strokeWidth = strokeWidth,
            )
            drawArrowHead(
                targetPoint,
                Offset(normX, normY),
                lineColor,
                palette.surface,
                size = arrowSize,
            )
        }
    }
}

internal fun useCaseElementCenter(id: String, diagram: UseCaseDiagram): Offset? {
    diagram.actors
        .find { it.id == id }
        ?.let {
            return Offset(it.x + it.width / 2, it.y + it.height / 2)
        }
    diagram.useCases
        .find { it.id == id }
        ?.let {
            return Offset(it.x + it.width / 2, it.y + it.height / 2)
        }
    return null
}

internal fun useCaseElementBoundaryPoint(
    id: String,
    diagram: UseCaseDiagram,
    dirFromCenter: Offset,
): Offset? {
    diagram.actors
        .find { it.id == id }
        ?.let { actor ->
            val cx = actor.x + actor.width / 2
            val cy = actor.y + actor.height / 2
            val hw = actor.width / 2
            val hh = actor.height / 2
            val scaleX = if (dirFromCenter.x != 0f) hw / abs(dirFromCenter.x) else Float.MAX_VALUE
            val scaleY = if (dirFromCenter.y != 0f) hh / abs(dirFromCenter.y) else Float.MAX_VALUE
            return Offset(
                cx + dirFromCenter.x * minOf(scaleX, scaleY),
                cy + dirFromCenter.y * minOf(scaleX, scaleY),
            )
        }
    diagram.useCases
        .find { it.id == id }
        ?.let { useCase ->
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
