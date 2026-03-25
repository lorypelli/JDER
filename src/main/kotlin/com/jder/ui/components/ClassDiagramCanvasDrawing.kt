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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassEntityType
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.ClassRelationType
import kotlin.math.sqrt
internal fun DrawScope.drawClassEntity(
    entity: ClassEntity,
    isSelected: Boolean,
    isPendingSource: Boolean,
    primaryColor: Color,
    selectedColor: Color,
    textColor: Color,
    surfaceColor: Color,
    textMeasurer: TextMeasurer
) {
    val height = classEntityHeight(entity)
    val borderColor = when {
        isSelected -> selectedColor
        isPendingSource -> selectedColor.copy(alpha = 0.8f)
        else -> primaryColor
    }
    val strokeWidth = if (isSelected || isPendingSource) 3f else 1.5f
    val fillColor = when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isPendingSource -> selectedColor.copy(alpha = 0.06f)
        else -> surfaceColor
    }
    if (isSelected) {
        drawRoundRect(
            color = selectedColor.copy(alpha = 0.12f),
            topLeft = Offset(entity.x - 4f, entity.y - 4f),
            size = Size(entity.width + 8f, height + 8f),
            cornerRadius = CornerRadius(4f),
            style = Fill
        )
    }
    drawRect(color = fillColor, topLeft = Offset(entity.x, entity.y), size = Size(entity.width, height), style = Fill)
    drawRect(color = borderColor, topLeft = Offset(entity.x, entity.y), size = Size(entity.width, height), style = Stroke(width = strokeWidth))
    var currentY = entity.y
    val headerH = if (entity.type.stereotype.isNotEmpty()) 56f else 40f
    if (entity.type.stereotype.isNotEmpty()) {
        val stereoResult = textMeasurer.measure(
            text = entity.type.stereotype,
            style = TextStyle(color = textColor.copy(alpha = 0.7f), fontSize = 10.sp, fontStyle = FontStyle.Italic)
        )
        drawText(
            textLayoutResult = stereoResult,
            topLeft = Offset(entity.x + (entity.width - stereoResult.size.width) / 2f, currentY + 6f)
        )
        currentY += 18f
    }
    val nameStyle = TextStyle(
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = if (entity.isAbstract && entity.type == ClassEntityType.CLASS) FontStyle.Italic else FontStyle.Normal
    )
    val nameResult = textMeasurer.measure(
        text = entity.name,
        style = nameStyle,
        constraints = Constraints(maxWidth = (entity.width - 16).toInt().coerceAtLeast(1))
    )
    drawText(
        textLayoutResult = nameResult,
        topLeft = Offset(entity.x + (entity.width - nameResult.size.width) / 2f, currentY + (if (entity.type.stereotype.isNotEmpty()) 4f else 13f))
    )
    currentY = entity.y + headerH
    drawLine(color = borderColor, start = Offset(entity.x, currentY), end = Offset(entity.x + entity.width, currentY), strokeWidth = strokeWidth)
    if (entity.attributes.isEmpty()) {
        currentY += 20f
    } else {
        entity.attributes.forEach { member ->
            drawMemberText(member, entity.x, currentY, entity.width, textMeasurer, textColor, isMethod = false)
            currentY += 20f
        }
    }
    drawLine(color = borderColor, start = Offset(entity.x, currentY), end = Offset(entity.x + entity.width, currentY), strokeWidth = strokeWidth)
    if (entity.methods.isEmpty()) {
        currentY += 20f
    } else {
        entity.methods.forEach { member ->
            drawMemberText(member, entity.x, currentY, entity.width, textMeasurer, textColor, isMethod = true)
            currentY += 20f
        }
    }
}
private fun DrawScope.drawMemberText(
    member: ClassMember,
    startX: Float,
    startY: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color
,
    isMethod: Boolean
) {
    val prefix = member.visibility.symbol + " "
    val suffix = if (member.type.isNotEmpty()) ": ${member.type}" else ""
    val paramSuffix = if (isMethod) "()" else ""
    val text = "$prefix${member.name}$paramSuffix$suffix"
    val decoration = if (member.isStatic) TextDecoration.Underline else TextDecoration.None
    val fontStyle = if (member.isAbstract) FontStyle.Italic else FontStyle.Normal
    val result = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = 11.sp,
            textDecoration = decoration,
            fontStyle = fontStyle
        ),
        constraints = Constraints(maxWidth = (width - 12).toInt().coerceAtLeast(1))
    )
    drawText(textLayoutResult = result, topLeft = Offset(startX + 6f, startY + 3f))
}
internal fun DrawScope.drawClassRelation(
    relation: ClassRelation,
    diagram: ClassDiagram,
    isSelected: Boolean,
    color: Color,
    selectedColor: Color,
    textMeasurer: TextMeasurer,
    textBackgroundColor: Color
) {
    val sourceCenter = classElementCenter(relation.sourceId, diagram) ?: return
    val targetCenter = classElementCenter(relation.targetId, diagram) ?: return
    val lineColor = if (isSelected) selectedColor else color
    val strokeWidth = if (isSelected) 2.5f else 1.5f
    val dx = targetCenter.x - sourceCenter.x
    val dy = targetCenter.y - sourceCenter.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return
    val normX = dx / dist
    val normY = dy / dist
    val sourcePoint = classEntityBoundaryPoint(relation.sourceId, diagram, Offset(normX, normY)) ?: sourceCenter
    val targetPoint = classEntityBoundaryPoint(relation.targetId, diagram, Offset(-normX, -normY)) ?: targetCenter
    val isDashed = relation.type == ClassRelationType.REALIZATION || relation.type == ClassRelationType.DEPENDENCY
    val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 5f)) else null
    val arrowSize = 14f
    when (relation.type) {
        ClassRelationType.ASSOCIATION, ClassRelationType.DEPENDENCY -> {
            val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(color = lineColor, start = sourcePoint, end = lineEnd, strokeWidth = strokeWidth, pathEffect = pathEffect)
            drawOpenArrowHead(targetPoint, Offset(normX, normY), lineColor, arrowSize)
        }
        ClassRelationType.INHERITANCE, ClassRelationType.REALIZATION -> {
            val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
            drawLine(color = lineColor, start = sourcePoint, end = lineEnd, strokeWidth = strokeWidth, pathEffect = pathEffect)
            drawHollowTriangleArrow(targetPoint, Offset(normX, normY), lineColor, arrowSize, textBackgroundColor)
        }
        ClassRelationType.AGGREGATION -> {
            val diamondSize = 16f
            val diamondBase = Offset(sourcePoint.x + normX * diamondSize, sourcePoint.y + normY * diamondSize)
            drawLine(color = lineColor, start = diamondBase, end = targetPoint, strokeWidth = strokeWidth)
            drawDiamond(sourcePoint, Offset(normX, normY), lineColor, diamondSize, filled = false, fillColor = textBackgroundColor)
        }
        ClassRelationType.COMPOSITION -> {
            val diamondSize = 16f
            val diamondBase = Offset(sourcePoint.x + normX * diamondSize, sourcePoint.y + normY * diamondSize)
            drawLine(color = lineColor, start = diamondBase, end = targetPoint, strokeWidth = strokeWidth)
            drawDiamond(sourcePoint, Offset(normX, normY), lineColor, diamondSize, filled = true, fillColor = lineColor)
        }
    }
    if (relation.label.isNotEmpty()) {
        val midX = (sourcePoint.x + targetPoint.x) / 2
        val midY = (sourcePoint.y + targetPoint.y) / 2
        val labelResult = textMeasurer.measure(
            text = relation.label,
            style = TextStyle(color = lineColor, fontSize = 10.sp)
        )
        drawRoundRect(
            color = textBackgroundColor,
            topLeft = Offset(midX - labelResult.size.width / 2f - 2f, midY - labelResult.size.height / 2f - 1f),
            size = Size(labelResult.size.width + 4f, labelResult.size.height + 2f),
            cornerRadius = CornerRadius(3f),
            style = Fill
        )
        drawText(textLayoutResult = labelResult, topLeft = Offset(midX - labelResult.size.width / 2f, midY - labelResult.size.height / 2f))
    }
    if (relation.sourceMultiplicity.isNotEmpty()) {
        val multResult = textMeasurer.measure(
            text = relation.sourceMultiplicity,
            style = TextStyle(color = lineColor, fontSize = 10.sp)
        )
        val multX = sourcePoint.x + normX * 20f - multResult.size.width / 2f
        val multY = sourcePoint.y + normY * 20f - multResult.size.height / 2f
        drawText(textLayoutResult = multResult, topLeft = Offset(multX, multY))
    }
    if (relation.targetMultiplicity.isNotEmpty()) {
        val multResult = textMeasurer.measure(
            text = relation.targetMultiplicity,
            style = TextStyle(color = lineColor, fontSize = 10.sp)
        )
        val multX = targetPoint.x - normX * 24f - multResult.size.width / 2f
        val multY = targetPoint.y - normY * 24f - multResult.size.height / 2f
        drawText(textLayoutResult = multResult, topLeft = Offset(multX, multY))
    }
}
private fun DrawScope.drawOpenArrowHead(tip: Offset, dir: Offset, color: Color, size: Float) {
    val perpX = -dir.y
    val perpY = dir.x
    val base = Offset(tip.x - dir.x * size, tip.y - dir.y * size)
    val left = Offset(base.x + perpX * size * 0.4f, base.y + perpY * size * 0.4f)
    val right = Offset(base.x - perpX * size * 0.4f, base.y - perpY * size * 0.4f)
    drawLine(color = color, start = tip, end = left, strokeWidth = 1.5f)
    drawLine(color = color, start = tip, end = right, strokeWidth = 1.5f)
}
private fun DrawScope.drawHollowTriangleArrow(tip: Offset, dir: Offset, color: Color, size: Float, fillColor: Color) {
    val perpX = -dir.y
    val perpY = dir.x
    val base = Offset(tip.x - dir.x * size, tip.y - dir.y * size)
    val left = Offset(base.x + perpX * size * 0.5f, base.y + perpY * size * 0.5f)
    val right = Offset(base.x - perpX * size * 0.5f, base.y - perpY * size * 0.5f)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = color, style = Stroke(width = 1.5f))
}
private fun DrawScope.drawDiamond(tip: Offset, dir: Offset, color: Color, size: Float, filled: Boolean, fillColor: Color) {
    val perpX = -dir.y
    val perpY = dir.x
    val leftTip = Offset(tip.x + dir.x * size / 2 + perpX * size / 3, tip.y + dir.y * size / 2 + perpY * size / 3)
    val rightTip = Offset(tip.x + dir.x * size / 2 - perpX * size / 3, tip.y + dir.y * size / 2 - perpY * size / 3)
    val bottomTip = Offset(tip.x + dir.x * size, tip.y + dir.y * size)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(leftTip.x, leftTip.y)
        lineTo(bottomTip.x, bottomTip.y)
        lineTo(rightTip.x, rightTip.y)
        close()
    }
    drawPath(path = path, color = if (filled) fillColor else fillColor, style = Fill)
    drawPath(path = path, color = color, style = Stroke(width = 1.5f))
}
