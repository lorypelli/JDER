package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.unit.sp
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassEntityType
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.ClassRelationType
import kotlin.math.abs
import kotlin.math.sqrt

internal fun DrawScope.drawClassDiagram(
    diagram: ClassDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
    pendingSourceId: String? = null,
) {
  diagram.relations.forEach {
    drawClassRelation(it, diagram, it.id == selectedId, palette, textMeasurer)
  }
  diagram.notes.forEach {
    drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
  }
  diagram.classes.forEach {
    drawClassEntity(
        entity = it,
        isSelected = it.id == selectedId,
        isHovered = it.id == hoveredId,
        isPendingSource = it.id == pendingSourceId,
        palette = palette,
        textMeasurer = textMeasurer,
    )
  }
}

internal fun classDiagramBounds(diagram: ClassDiagram): Rect? {
  val bounds = BoundsBuilder()
  diagram.classes.forEach {
    bounds.include(Rect(it.x, it.y, it.x + it.width, it.y + classEntityHeight(it)))
  }
  diagram.notes.forEach { bounds.include(it.bounds()) }
  return bounds.build()
}

internal fun DrawScope.drawClassEntity(
    entity: ClassEntity,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val height = classEntityHeight(entity)
  val selectedColor = palette.selected
  val borderColor =
      when {
        isSelected -> selectedColor
        isPendingSource -> selectedColor.copy(alpha = 0.8f)
        else -> palette.primary
      }
  val strokeWidth =
      when {
        isSelected || isPendingSource -> 3f
        isHovered -> 2.25f
        else -> 1.5f
      }
  if (isSelected || isPendingSource) {
    drawRoundRect(
        color = selectedColor.copy(alpha = if (isSelected) 0.12f else 0.1f),
        topLeft = Offset(entity.x - 4f, entity.y - 4f),
        size = Size(entity.width + 8f, height + 8f),
        cornerRadius = CornerRadius(4f),
        style = Fill,
    )
    drawRoundRect(
        color = selectedColor,
        topLeft = Offset(entity.x - 4f, entity.y - 4f),
        size = Size(entity.width + 8f, height + 8f),
        cornerRadius = CornerRadius(4f),
        style =
            if (isSelected) Stroke(width = 2.5f)
            else Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
    )
  }
  val topLeft = Offset(entity.x, entity.y)
  val size = Size(entity.width, height)
  drawRect(color = palette.surface, topLeft = topLeft, size = size, style = Fill)
  when {
    isSelected -> selectedColor.copy(alpha = 0.08f)
    isPendingSource -> selectedColor.copy(alpha = 0.06f)
    isHovered -> palette.primary.copy(alpha = 0.05f)
    else -> null
  }?.let { drawRect(color = it, topLeft = topLeft, size = size, style = Fill) }
  drawRect(color = borderColor, topLeft = topLeft, size = size, style = Stroke(width = strokeWidth))
  var currentY = entity.y
  val headerH = if (entity.type.stereotype.isNotEmpty()) 56f else 40f
  if (entity.type.stereotype.isNotEmpty()) {
    val stereoResult =
        textMeasurer.measure(
            text = entity.type.stereotype,
            style =
                TextStyle(
                    color = palette.text.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                ),
        )
    drawText(
        textLayoutResult = stereoResult,
        topLeft = Offset(entity.x + (entity.width - stereoResult.size.width) / 2f, currentY + 6f),
    )
    currentY += 18f
  }
  val nameResult =
      textMeasurer.measureLine(
          text = entity.name,
          style =
              TextStyle(
                  color = palette.text,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontStyle =
                      if (entity.isAbstract && entity.type == ClassEntityType.CLASS)
                          FontStyle.Italic
                      else FontStyle.Normal,
              ),
          maxWidth = entity.width - 16,
      )
  drawText(
      textLayoutResult = nameResult,
      topLeft =
          Offset(
              entity.x + (entity.width - nameResult.size.width) / 2f,
              currentY + (if (entity.type.stereotype.isNotEmpty()) 4f else 13f),
          ),
  )
  currentY = entity.y + headerH
  drawLine(
      color = borderColor,
      start = Offset(entity.x, currentY),
      end = Offset(entity.x + entity.width, currentY),
      strokeWidth = strokeWidth,
  )
  if (entity.attributes.isEmpty()) {
    currentY += 20f
  } else {
    entity.attributes.forEach { member ->
      drawMemberText(member, entity.x, currentY, entity.width, textMeasurer, palette.text, false)
      currentY += 20f
    }
  }
  currentY += 4f
  drawLine(
      color = borderColor,
      start = Offset(entity.x, currentY),
      end = Offset(entity.x + entity.width, currentY),
      strokeWidth = strokeWidth,
  )
  entity.methods.forEach { member ->
    drawMemberText(member, entity.x, currentY, entity.width, textMeasurer, palette.text, true)
    currentY += 20f
  }
}

private fun DrawScope.drawMemberText(
    member: ClassMember,
    startX: Float,
    startY: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color,
    isMethod: Boolean,
) {
  val prefix = member.visibility.symbol + " "
  val suffix = if (member.type.isNotEmpty()) ": ${member.type}" else ""
  val paramSuffix = if (isMethod) "()" else ""
  val result =
      textMeasurer.measureLine(
          text = "$prefix${member.name}$paramSuffix$suffix",
          style =
              TextStyle(
                  color = textColor,
                  fontSize = 11.sp,
                  textDecoration =
                      if (member.isStatic) TextDecoration.Underline else TextDecoration.None,
                  fontStyle = if (member.isAbstract) FontStyle.Italic else FontStyle.Normal,
              ),
          maxWidth = width - 12,
      )
  drawText(textLayoutResult = result, topLeft = Offset(startX + 6f, startY + 3f))
}

internal fun DrawScope.drawClassRelation(
    relation: ClassRelation,
    diagram: ClassDiagram,
    isSelected: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val sourceCenter = classElementCenter(relation.sourceId, diagram) ?: return
  val targetCenter = classElementCenter(relation.targetId, diagram) ?: return
  val lineColor = if (isSelected) palette.selected else palette.outline
  val textBackgroundColor = palette.textBackground
  val strokeWidth = if (isSelected) 2.5f else 1.5f
  val dx = targetCenter.x - sourceCenter.x
  val dy = targetCenter.y - sourceCenter.y
  val dist = sqrt(dx * dx + dy * dy)
  if (dist == 0f) return
  val normX = dx / dist
  val normY = dy / dist
  val sourcePoint =
      classEntityBoundaryPoint(relation.sourceId, diagram, Offset(normX, normY)) ?: sourceCenter
  val targetPoint =
      classEntityBoundaryPoint(relation.targetId, diagram, Offset(-normX, -normY)) ?: targetCenter
  val isDashed =
      relation.type == ClassRelationType.REALIZATION ||
          relation.type == ClassRelationType.DEPENDENCY
  val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 5f)) else null
  val arrowSize = 14f
  when (relation.type) {
    ClassRelationType.ASSOCIATION,
    ClassRelationType.DEPENDENCY -> {
      val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
      drawLine(
          color = lineColor,
          start = sourcePoint,
          end = lineEnd,
          strokeWidth = strokeWidth,
          pathEffect = pathEffect,
      )
      drawOpenArrowHead(targetPoint, Offset(normX, normY), lineColor, arrowSize)
    }
    ClassRelationType.INHERITANCE,
    ClassRelationType.REALIZATION -> {
      val lineEnd = Offset(targetPoint.x - normX * arrowSize, targetPoint.y - normY * arrowSize)
      drawLine(
          color = lineColor,
          start = sourcePoint,
          end = lineEnd,
          strokeWidth = strokeWidth,
          pathEffect = pathEffect,
      )
      drawHollowTriangleArrow(
          targetPoint,
          Offset(normX, normY),
          lineColor,
          arrowSize,
          palette.surface,
      )
    }
    ClassRelationType.AGGREGATION,
    ClassRelationType.COMPOSITION -> {
      val diamondSize = 16f
      val diamondBase =
          Offset(sourcePoint.x + normX * diamondSize, sourcePoint.y + normY * diamondSize)
      drawLine(color = lineColor, start = diamondBase, end = targetPoint, strokeWidth = strokeWidth)
      drawDiamond(
          sourcePoint,
          Offset(normX, normY),
          lineColor,
          diamondSize,
          fillColor =
              if (relation.type == ClassRelationType.COMPOSITION) lineColor else palette.surface,
      )
    }
  }
  if (relation.label.isNotEmpty()) {
    val midX = (sourcePoint.x + targetPoint.x) / 2
    val midY = (sourcePoint.y + targetPoint.y) / 2
    val labelResult =
        textMeasurer.measure(
            text = relation.label,
            style = TextStyle(color = lineColor, fontSize = 10.sp),
        )
    drawRoundRect(
        color = textBackgroundColor,
        topLeft =
            Offset(
                midX - labelResult.size.width / 2f - 2f,
                midY - labelResult.size.height / 2f - 1f,
            ),
        size = Size(labelResult.size.width + 4f, labelResult.size.height + 2f),
        cornerRadius = CornerRadius(3f),
        style = Fill,
    )
    drawText(
        textLayoutResult = labelResult,
        topLeft = Offset(midX - labelResult.size.width / 2f, midY - labelResult.size.height / 2f),
    )
  }
  fun drawMultiplicity(text: String, anchor: Offset) {
    val result = textMeasurer.measure(text, TextStyle(color = lineColor, fontSize = 10.sp))
    val perpendicular = Offset(-normY, normX)
    val clearance =
        abs(perpendicular.x) * result.size.width / 2f +
            abs(perpendicular.y) * result.size.height / 2f +
            4f
    drawTextCentered(result, anchor + perpendicular * clearance)
  }
  if (relation.sourceMultiplicity.isNotEmpty()) {
    drawMultiplicity(relation.sourceMultiplicity, sourcePoint + Offset(normX, normY) * 24f)
  }
  if (relation.targetMultiplicity.isNotEmpty()) {
    drawMultiplicity(relation.targetMultiplicity, targetPoint - Offset(normX, normY) * 24f)
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

private fun DrawScope.drawHollowTriangleArrow(
    tip: Offset,
    dir: Offset,
    color: Color,
    size: Float,
    fillColor: Color,
) {
  val perpX = -dir.y
  val perpY = dir.x
  val base = Offset(tip.x - dir.x * size, tip.y - dir.y * size)
  val left = Offset(base.x + perpX * size * 0.5f, base.y + perpY * size * 0.5f)
  val right = Offset(base.x - perpX * size * 0.5f, base.y - perpY * size * 0.5f)
  val path =
      Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
      }
  drawPath(path = path, color = fillColor, style = Fill)
  drawPath(path = path, color = color, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawDiamond(
    tip: Offset,
    dir: Offset,
    color: Color,
    size: Float,
    fillColor: Color,
) {
  val perpX = -dir.y
  val perpY = dir.x
  val leftTip =
      Offset(
          tip.x + dir.x * size / 2 + perpX * size / 3,
          tip.y + dir.y * size / 2 + perpY * size / 3,
      )
  val rightTip =
      Offset(
          tip.x + dir.x * size / 2 - perpX * size / 3,
          tip.y + dir.y * size / 2 - perpY * size / 3,
      )
  val bottomTip = Offset(tip.x + dir.x * size, tip.y + dir.y * size)
  val path =
      Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(leftTip.x, leftTip.y)
        lineTo(bottomTip.x, bottomTip.y)
        lineTo(rightTip.x, rightTip.y)
        close()
      }
  drawPath(path = path, color = fillColor, style = Fill)
  drawPath(path = path, color = color, style = Stroke(width = 1.5f))
}
