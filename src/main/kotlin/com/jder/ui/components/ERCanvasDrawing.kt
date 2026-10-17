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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.jder.domain.model.Attribute
import com.jder.domain.model.AttributeType
import com.jder.domain.model.ERDiagram
import com.jder.domain.model.Entity
import com.jder.domain.model.Relationship
import kotlin.math.abs

private const val ATTRIBUTE_RADIUS = 20f
private const val COMPONENT_RADIUS = 12f
private const val COMPONENT_SPACING_X = 60f
private const val COMPONENT_SPACING_Y = 40f

internal fun DrawScope.drawERDiagram(
    diagram: ERDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
) {
  val entitiesById = diagram.entities.associateBy { it.id }
  diagram.relationships.forEach {
    drawConnectionsForRelationship(it, entitiesById, palette, textMeasurer)
  }
  diagram.entities.forEach {
    drawEntity(it, it.id == selectedId, it.id == hoveredId, palette, textMeasurer)
  }
  diagram.relationships.forEach {
    drawRelationship(it, it.id == selectedId, it.id == hoveredId, palette, textMeasurer)
  }
  diagram.notes.forEach {
    drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
  }
}

internal fun erDiagramBounds(diagram: ERDiagram, textMeasurer: TextMeasurer): Rect? {
  val bounds = BoundsBuilder()
  diagram.entities.forEach {
    bounds.include(Rect(it.x, it.y, it.x + it.width, it.y + it.height))
    includeAttributeBounds(bounds, it.attributes, it.x, it.y, it.width, it.height, textMeasurer)
  }
  diagram.relationships.forEach {
    bounds.include(Rect(it.x, it.y, it.x + it.width, it.y + it.height))
    includeAttributeBounds(bounds, it.attributes, it.x, it.y, it.width, it.height, textMeasurer)
  }
  diagram.notes.forEach { bounds.include(it.bounds()) }
  return bounds.build()
}

private fun includeAttributeBounds(
    bounds: BoundsBuilder,
    attributes: List<Attribute>,
    ownerX: Float,
    ownerY: Float,
    ownerWidth: Float,
    ownerHeight: Float,
    textMeasurer: TextMeasurer,
) {
  attributes.forEachIndexed { index, attribute ->
    val position =
        calculateAttributePosition(
            ownerX,
            ownerY,
            ownerWidth,
            ownerHeight,
            index,
            attributes.size,
            attribute,
        )
    bounds.include(Rect(position, ATTRIBUTE_RADIUS))
    val label = attributeLabel(attribute, position, textMeasurer, Color.Black, Color.Black)
    bounds.include(label.bounds.inflate(6f))
    label.multiplicity?.let { bounds.include(Rect(label.multiplicityTopLeft, it.size.toSize())) }
    if (attribute.type == AttributeType.COMPOSITE) {
      attribute.components.forEachIndexed { componentIndex, component ->
        val center = componentPosition(position, componentIndex, attribute.components.size)
        bounds.include(Rect(center, COMPONENT_RADIUS))
        val text = textMeasurer.measure(component.name, componentStyle(Color.Black))
        bounds.include(
            Rect(
                Offset(center.x + COMPONENT_RADIUS + 4f, center.y - text.size.height / 2f - 2f),
                Size(text.size.width + 12f, text.size.height + 4f),
            )
        )
      }
    }
  }
}

internal fun DrawScope.drawEntity(
    entity: Entity,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val color = if (isSelected) palette.selected else palette.primary
  val strokeWidth =
      when {
        isSelected -> 3.5f
        isHovered -> 2.5f
        else -> 2f
      }
  val topLeft = Offset(entity.x, entity.y)
  val size = Size(entity.width, entity.height)
  drawRect(color = palette.surface, topLeft = topLeft, size = size, style = Fill)
  highlightColor(isSelected, isHovered, palette.primary, palette.selected)?.let {
    drawRect(color = it, topLeft = topLeft, size = size, style = Fill)
  }
  drawRect(color = color, topLeft = topLeft, size = size, style = Stroke(width = strokeWidth))
  if (entity.isWeak) {
    drawRect(
        color = color,
        topLeft = Offset(entity.x + 4, entity.y + 4),
        size = Size(entity.width - 8, entity.height - 8),
        style = Stroke(width = 1.5f),
    )
  }
  val name =
      textMeasurer.measureBlock(
          text = entity.name,
          style = shapeNameStyle(palette.text, isSelected || isHovered),
          maxWidth = entity.width - 16f,
          maxLines = 2,
      )
  drawTextCentered(name, Offset(entity.x + entity.width / 2, entity.y + entity.height / 2))
  entity.attributes.forEachIndexed { index, attribute ->
    drawAttribute(
        attribute = attribute,
        ownerX = entity.x,
        ownerY = entity.y,
        ownerWidth = entity.width,
        ownerHeight = entity.height,
        index = index,
        total = entity.attributes.size,
        isRectangle = true,
        palette = palette,
        textMeasurer = textMeasurer,
    )
  }
}

internal fun DrawScope.drawRelationship(
    relationship: Relationship,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val color = if (isSelected) palette.selected else palette.error
  val strokeWidth =
      when {
        isSelected -> 3.5f
        isHovered -> 2.5f
        else -> 2f
      }
  val centerX = relationship.x + relationship.width / 2
  val centerY = relationship.y + relationship.height / 2
  val path =
      Path().apply {
        moveTo(centerX, relationship.y)
        lineTo(relationship.x + relationship.width, centerY)
        lineTo(centerX, relationship.y + relationship.height)
        lineTo(relationship.x, centerY)
        close()
      }
  drawPath(path = path, color = palette.surface, style = Fill)
  highlightColor(isSelected, isHovered, palette.error, palette.selected)?.let {
    drawPath(path = path, color = it, style = Fill)
  }
  drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
  val name =
      textMeasurer.measureBlock(
          text = relationship.name,
          style = shapeNameStyle(palette.text, isSelected || isHovered),
          maxWidth = relationship.width - 12f,
          maxLines = 2,
      )
  drawTextCentered(name, Offset(centerX, centerY))
  relationship.attributes.forEachIndexed { index, attribute ->
    drawAttribute(
        attribute = attribute,
        ownerX = relationship.x,
        ownerY = relationship.y,
        ownerWidth = relationship.width,
        ownerHeight = relationship.height,
        index = index,
        total = relationship.attributes.size,
        isRectangle = false,
        palette = palette,
        textMeasurer = textMeasurer,
    )
  }
}

internal fun DrawScope.drawConnectionsForRelationship(
    relationship: Relationship,
    entitiesById: Map<String, Entity>,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val centerX = relationship.x + relationship.width / 2
  val centerY = relationship.y + relationship.height / 2
  val cardinalityColor = palette.tertiary
  relationship.connections.forEach { connection ->
    val entity = entitiesById[connection.entityId] ?: return@forEach
    val entityCenterX = entity.x + entity.width / 2
    val entityCenterY = entity.y + entity.height / 2
    drawLine(
        color = palette.outline,
        start = Offset(centerX, centerY),
        end = Offset(entityCenterX, entityCenterY),
        strokeWidth = 1.5f,
    )
    val textLayoutResult =
        textMeasurer.measure(
            text = connection.cardinality.display,
            style =
                TextStyle(color = cardinalityColor, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        )
    val textWidth = textLayoutResult.size.width.toFloat()
    val textHeight = textLayoutResult.size.height.toFloat()
    val labelX = (centerX + entityCenterX) / 2 - textWidth / 2
    val labelY = (centerY + entityCenterY) / 2 - textHeight / 2
    drawRoundRect(
        color = palette.textBackground,
        topLeft = Offset(labelX - 6, labelY - 3),
        size = Size(textWidth + 12, textHeight + 6),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = cardinalityColor.copy(alpha = 0.6f),
        topLeft = Offset(labelX - 6, labelY - 3),
        size = Size(textWidth + 12, textHeight + 6),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 2f),
    )
    drawText(textLayoutResult = textLayoutResult, topLeft = Offset(labelX, labelY))
  }
}

private fun highlightColor(
    isSelected: Boolean,
    isHovered: Boolean,
    baseColor: Color,
    selectedColor: Color,
): Color? =
    when {
      isSelected -> selectedColor.copy(alpha = 0.08f)
      isHovered -> baseColor.copy(alpha = 0.05f)
      else -> null
    }

private fun shapeNameStyle(textColor: Color, isEmphasized: Boolean) =
    TextStyle(
        color = textColor,
        fontSize = 14.sp,
        fontWeight = if (isEmphasized) FontWeight.SemiBold else FontWeight.Normal,
    )

private fun componentStyle(color: Color) =
    TextStyle(color = color, fontSize = 11.sp, fontWeight = FontWeight.Normal)

private fun componentPosition(parent: Offset, index: Int, total: Int): Offset {
  val startY = parent.y - ((total - 1) * COMPONENT_SPACING_Y / 2f)
  return Offset(
      parent.x + ATTRIBUTE_RADIUS + COMPONENT_SPACING_X,
      startY + index * COMPONENT_SPACING_Y,
  )
}

private class AttributeLabel(
    val text: TextLayoutResult,
    val topLeft: Offset,
    val multiplicity: TextLayoutResult?,
    val multiplicityTopLeft: Offset,
) {
  val bounds: Rect
    get() = Rect(topLeft, text.size.toSize())
}

private fun attributeLabel(
    attribute: Attribute,
    position: Offset,
    textMeasurer: TextMeasurer,
    textColor: Color,
    multiplicityColor: Color,
): AttributeLabel {
  val text =
      textMeasurer.measure(
          text = attribute.name,
          style =
              TextStyle(
                  color = textColor,
                  fontSize = 13.sp,
                  fontWeight = if (attribute.isPrimaryKey) FontWeight.Bold else FontWeight.SemiBold,
              ),
      )
  val isComposite = attribute.type == AttributeType.COMPOSITE && attribute.components.isNotEmpty()
  val textX =
      if (isComposite) position.x - ATTRIBUTE_RADIUS - text.size.width - 10f
      else position.x + ATTRIBUTE_RADIUS + 10f
  val topLeft = Offset(textX, position.y - text.size.height / 2f)
  val multiplicity =
      if (attribute.type == AttributeType.MULTIVALUED && attribute.multiplicity.isNotBlank()) {
        textMeasurer.measure(
            text = attribute.multiplicity,
            style = TextStyle(color = multiplicityColor, fontSize = 11.sp),
        )
      } else null
  return AttributeLabel(
      text = text,
      topLeft = topLeft,
      multiplicity = multiplicity,
      multiplicityTopLeft = Offset(textX, topLeft.y + text.size.height + 4f),
  )
}

private fun DrawScope.drawAttribute(
    attribute: Attribute,
    ownerX: Float,
    ownerY: Float,
    ownerWidth: Float,
    ownerHeight: Float,
    index: Int,
    total: Int,
    isRectangle: Boolean,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
  val position =
      calculateAttributePosition(ownerX, ownerY, ownerWidth, ownerHeight, index, total, attribute)
  val centerX = ownerX + ownerWidth / 2
  val centerY = ownerY + ownerHeight / 2
  val dx = position.x - centerX
  val dy = position.y - centerY
  val distance = Offset(dx, dy).getDistance()
  val dirX = if (distance > 0) dx / distance else 1f
  val dirY = if (distance > 0) dy / distance else 0f
  val arrowStartX = position.x - dirX * 60f
  val arrowStartY = position.y - dirY * 60f
  val connectionPoint =
      if (isRectangle) {
        getClosestPointOnRectangle(
            ownerX,
            ownerY,
            ownerWidth,
            ownerHeight,
            arrowStartX,
            arrowStartY,
        )
      } else {
        getClosestPointOnDiamond(
            centerX,
            centerY,
            ownerWidth,
            ownerHeight,
            arrowStartX,
            arrowStartY,
        )
      }
  drawLine(
      color = palette.text.copy(alpha = 0.4f),
      start = connectionPoint,
      end = position,
      strokeWidth = 1.5f,
  )
  val normalColor = palette.primary
  val compositeColor = palette.secondary
  val componentColor = palette.secondary.copy(alpha = 0.7f)
  drawCircle(
      color = palette.surfaceVariant,
      radius = ATTRIBUTE_RADIUS,
      center = position,
      style = Fill,
  )
  when (attribute.type) {
    AttributeType.COMPOSITE -> {
      drawCircle(compositeColor, ATTRIBUTE_RADIUS, position, style = Stroke(width = 2.5f))
      drawCircle(compositeColor, ATTRIBUTE_RADIUS - 5, position, style = Stroke(width = 2f))
      attribute.components.forEachIndexed { componentIndex, component ->
        val center = componentPosition(position, componentIndex, attribute.components.size)
        val start = Offset(position.x + ATTRIBUTE_RADIUS, position.y)
        drawLine(compositeColor.copy(alpha = 0.4f), start, center, strokeWidth = 1.2f)
        drawCircle(palette.surfaceVariant, COMPONENT_RADIUS, center, style = Fill)
        drawCircle(componentColor, COMPONENT_RADIUS, center, style = Stroke(width = 2f))
        val text = textMeasurer.measure(component.name, componentStyle(componentColor))
        val textX = center.x + COMPONENT_RADIUS + 8
        val textY = center.y - text.size.height / 2
        val labelSize = Size(text.size.width + 8f, text.size.height + 4f)
        drawRoundRect(
            color = palette.textBackground,
            topLeft = Offset(textX - 4, textY - 2),
            size = labelSize,
            cornerRadius = CornerRadius(3f, 3f),
        )
        drawRoundRect(
            color = componentColor.copy(alpha = 0.4f),
            topLeft = Offset(textX - 4, textY - 2),
            size = labelSize,
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 1.2f),
        )
        drawText(textLayoutResult = text, topLeft = Offset(textX, textY))
      }
    }
    AttributeType.MULTIVALUED -> {
      drawCircle(normalColor, ATTRIBUTE_RADIUS, position, style = Stroke(width = 2.5f))
      drawCircle(normalColor, ATTRIBUTE_RADIUS - 5, position, style = Stroke(width = 2f))
    }
    AttributeType.DERIVED -> {
      drawCircle(
          normalColor,
          ATTRIBUTE_RADIUS,
          position,
          style =
              Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
      )
    }
    else -> {
      val strokeColor = if (attribute.isPrimaryKey) palette.tertiary else normalColor
      drawCircle(
          strokeColor,
          ATTRIBUTE_RADIUS,
          position,
          style = Stroke(width = if (attribute.isPrimaryKey) 3.5f else 2.5f),
      )
    }
  }
  val labelColor =
      when {
        attribute.isPrimaryKey -> palette.tertiary
        attribute.type == AttributeType.COMPOSITE -> compositeColor
        else -> palette.text
      }
  val label =
      attributeLabel(attribute, position, textMeasurer, labelColor, normalColor.copy(alpha = 0.9f))
  val labelBackground =
      label.bounds.let { Rect(it.left - 5f, it.top - 2f, it.right + 5f, it.bottom + 2f) }
  drawRoundRect(
      color = palette.textBackground,
      topLeft = labelBackground.topLeft,
      size = labelBackground.size,
      cornerRadius = CornerRadius(4f, 4f),
  )
  drawRoundRect(
      color = labelColor.copy(alpha = 0.5f),
      topLeft = labelBackground.topLeft,
      size = labelBackground.size,
      cornerRadius = CornerRadius(4f, 4f),
      style = Stroke(width = 1.5f),
  )
  drawText(textLayoutResult = label.text, topLeft = label.topLeft)
  label.multiplicity?.let {
    drawRoundRect(
        color = palette.textBackground,
        topLeft = label.multiplicityTopLeft - Offset(3f, 1f),
        size = Size(it.size.width + 6f, it.size.height + 2f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawText(textLayoutResult = it, topLeft = label.multiplicityTopLeft)
  }
}

internal fun getClosestPointOnRectangle(
    rectX: Float,
    rectY: Float,
    rectWidth: Float,
    rectHeight: Float,
    targetX: Float,
    targetY: Float,
): Offset {
  val centerX = rectX + rectWidth / 2
  val centerY = rectY + rectHeight / 2
  val dx = targetX - centerX
  val dy = targetY - centerY
  if (dx == 0f && dy == 0f) return Offset(centerX, centerY)
  val scaleX = if (dx != 0f) (rectWidth / 2) / abs(dx) else Float.MAX_VALUE
  val scaleY = if (dy != 0f) (rectHeight / 2) / abs(dy) else Float.MAX_VALUE
  val scale = minOf(scaleX, scaleY)
  return Offset(centerX + dx * scale, centerY + dy * scale)
}

internal fun getClosestPointOnDiamond(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    targetX: Float,
    targetY: Float,
): Offset {
  val dx = targetX - centerX
  val dy = targetY - centerY
  if (dx == 0f && dy == 0f) return Offset(centerX, centerY)
  val totalScale = 1f / (abs(dx) / (width / 2) + abs(dy) / (height / 2))
  return Offset(centerX + dx * totalScale, centerY + dy * totalScale)
}
