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
import androidx.compose.ui.unit.sp
import com.jder.domain.model.Attribute
import com.jder.domain.model.AttributeType
import com.jder.domain.model.Entity
import com.jder.domain.model.Relationship
import kotlin.math.abs
import kotlin.math.sqrt
internal fun DrawScope.drawEntity(
    entity: Entity,
    isSelected: Boolean,
    isHovered: Boolean,
    entityColor: Color,
    selectedColor: Color,
    textColor: Color,
    surfaceColor: Color,
    textMeasurer: TextMeasurer,
    attributeBackgroundColor: Color,
    normalAttributeColor: Color,
    primaryKeyColor: Color,
    compositeColor: Color,
    componentColor: Color,
    textBackgroundColor: Color
) {
    val color = if (isSelected) selectedColor else entityColor
    val strokeWidth = when {
        isSelected -> 3.5f
        isHovered -> 2.5f
        else -> 2f
    }
    val fillColor = when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isHovered -> entityColor.copy(alpha = 0.05f)
        else -> surfaceColor
    }
    drawRect(color = fillColor, topLeft = Offset(entity.x, entity.y), size = Size(entity.width, entity.height), style = Fill)
    drawRect(color = color, topLeft = Offset(entity.x, entity.y), size = Size(entity.width, entity.height), style = Stroke(width = strokeWidth))
    if (entity.isWeak) {
        drawRect(
            color = color,
            topLeft = Offset(entity.x + 4, entity.y + 4),
            size = Size(entity.width - 8, entity.height - 8),
            style = Stroke(width = 1.5f)
        )
    }
    val textLayoutResult = textMeasurer.measure(
        text = entity.name,
        style = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isHovered) FontWeight.SemiBold else FontWeight.Normal
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            entity.x + (entity.width - textLayoutResult.size.width) / 2,
            entity.y + (entity.height - textLayoutResult.size.height) / 2
        )
    )
    if (entity.attributes.isNotEmpty()) {
        entity.attributes.forEachIndexed { index, attribute ->
            drawAttribute(
                attribute = attribute,
                entityX = entity.x,
                entityY = entity.y,
                entityWidth = entity.width,
                entityHeight = entity.height,
                index = index,
                total = entity.attributes.size,
                textMeasurer = textMeasurer,
                textColor = textColor,
                isRectangle = true,
                attributeBackgroundColor = attributeBackgroundColor,
                normalAttributeColor = normalAttributeColor,
                primaryKeyColor = primaryKeyColor,
                compositeColor = compositeColor,
                componentColor = componentColor,
                textBackgroundColor = textBackgroundColor
            )
        }
    }
}
internal fun DrawScope.drawRelationship(
    relationship: Relationship,
    isSelected: Boolean,
    isHovered: Boolean,
    relationshipColor: Color,
    selectedColor: Color,
    textColor: Color,
    surfaceColor: Color,
    textMeasurer: TextMeasurer,
    attributeBackgroundColor: Color,
    normalAttributeColor: Color,
    primaryKeyColor: Color,
    compositeColor: Color,
    componentColor: Color,
    textBackgroundColor: Color
) {
    val color = if (isSelected) selectedColor else relationshipColor
    val strokeWidth = when {
        isSelected -> 3.5f
        isHovered -> 2.5f
        else -> 2f
    }
    val fillColor = when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isHovered -> relationshipColor.copy(alpha = 0.05f)
        else -> surfaceColor
    }
    val centerX = relationship.x + relationship.width / 2
    val centerY = relationship.y + relationship.height / 2
    val path = Path().apply {
        moveTo(centerX, relationship.y)
        lineTo(relationship.x + relationship.width, centerY)
        lineTo(centerX, relationship.y + relationship.height)
        lineTo(relationship.x, centerY)
        close()
    }
    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
    val textLayoutResult = textMeasurer.measure(
        text = relationship.name,
        style = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isHovered) FontWeight.SemiBold else FontWeight.Normal
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            centerX - textLayoutResult.size.width / 2,
            centerY - textLayoutResult.size.height / 2
        )
    )
    if (relationship.attributes.isNotEmpty()) {
        relationship.attributes.forEachIndexed { index, attribute ->
            drawAttribute(
                attribute = attribute,
                entityX = relationship.x,
                entityY = relationship.y,
                entityWidth = relationship.width,
                entityHeight = relationship.height,
                index = index,
                total = relationship.attributes.size,
                textMeasurer = textMeasurer,
                textColor = textColor,
                isRectangle = false,
                attributeBackgroundColor = attributeBackgroundColor,
                normalAttributeColor = normalAttributeColor,
                primaryKeyColor = primaryKeyColor,
                compositeColor = compositeColor,
                componentColor = componentColor,
                textBackgroundColor = textBackgroundColor
            )
        }
    }
}
internal fun DrawScope.drawConnectionsForRelationship(
    relationship: Relationship,
    entities: List<Entity>,
    color: Color,
    textMeasurer: TextMeasurer,
    cardinalityColor: Color,
    textBackgroundColor: Color
) {
    val centerX = relationship.x + relationship.width / 2
    val centerY = relationship.y + relationship.height / 2
    relationship.connections.forEach { connection ->
        entities.find { it.id == connection.entityId }?.let { entity ->
            val entityCenterX = entity.x + entity.width / 2
            val entityCenterY = entity.y + entity.height / 2
            drawLine(
                color = color,
                start = Offset(centerX, centerY),
                end = Offset(entityCenterX, entityCenterY),
                strokeWidth = 1.5f
            )
            val textLayoutResult = textMeasurer.measure(
                text = connection.cardinality.display,
                style = TextStyle(color = cardinalityColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
            val offsetFactor = 0.5f
            val labelCenterX = centerX + (entityCenterX - centerX) * offsetFactor
            val labelCenterY = centerY + (entityCenterY - centerY) * offsetFactor
            val textWidth = textLayoutResult.size.width.toFloat()
            val textHeight = textLayoutResult.size.height.toFloat()
            val labelX = labelCenterX - textWidth / 2
            val labelY = labelCenterY - textHeight / 2
            drawRoundRect(
                color = textBackgroundColor,
                topLeft = Offset(labelX - 6, labelY - 3),
                size = Size(textWidth + 12, textHeight + 6),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = cardinalityColor.copy(alpha = 0.6f),
                topLeft = Offset(labelX - 6, labelY - 3),
                size = Size(textWidth + 12, textHeight + 6),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 2f)
            )
            drawText(textLayoutResult = textLayoutResult, topLeft = Offset(labelX, labelY))
        }
    }
}
private fun DrawScope.drawAttribute(
    attribute: Attribute,
    entityX: Float,
    entityY: Float,
    entityWidth: Float,
    entityHeight: Float,
    index: Int,
    total: Int,
    textMeasurer: TextMeasurer,
    textColor: Color,
    isRectangle: Boolean,
    attributeBackgroundColor: Color,
    normalAttributeColor: Color,
    primaryKeyColor: Color,
    compositeColor: Color,
    componentColor: Color,
    textBackgroundColor: Color
) {
    val radius = 20f
    val arrowLength = 60f
    val verticalSpacing = 60f
    val centerX = entityX + entityWidth / 2
    val centerY = entityY + entityHeight / 2
    val startY = centerY - ((total - 1) * verticalSpacing / 2f)
    val defaultAttrX = entityX + entityWidth + arrowLength
    val defaultAttrY = startY + (index * verticalSpacing)
    val attrX = if (attribute.x != 0f) centerX + attribute.x else defaultAttrX
    val attrY = if (attribute.y != 0f) centerY + attribute.y else defaultAttrY
    val dx = attrX - centerX
    val dy = attrY - centerY
    val distance = sqrt(dx * dx + dy * dy)
    val dirX = if (distance > 0) dx / distance else 1f
    val dirY = if (distance > 0) dy / distance else 0f
    val arrowStartX = attrX - dirX * arrowLength
    val arrowStartY = attrY - dirY * arrowLength
    val connectionPoint = if (isRectangle) {
        getClosestPointOnRectangle(entityX, entityY, entityWidth, entityHeight, arrowStartX, arrowStartY)
    } else {
        getClosestPointOnDiamond(centerX, centerY, entityWidth, entityHeight, arrowStartX, arrowStartY)
    }
    drawLine(
        color = textColor.copy(alpha = 0.4f),
        start = connectionPoint,
        end = Offset(attrX, attrY),
        strokeWidth = 1.5f
    )
    when (attribute.type) {
        AttributeType.COMPOSITE -> {
            drawCircle(color = attributeBackgroundColor, radius = radius, center = Offset(attrX, attrY), style = Fill)
            drawCircle(color = compositeColor, radius = radius, center = Offset(attrX, attrY), style = Stroke(width = 2.5f))
            drawCircle(color = compositeColor, radius = radius - 5, center = Offset(attrX, attrY), style = Stroke(width = 2f))
            if (attribute.components.isNotEmpty()) {
                attribute.components.forEachIndexed { compIndex, component ->
                    drawAttributeComponent(
                        component = component,
                        parentX = attrX + radius,
                        parentY = attrY,
                        index = compIndex,
                        total = attribute.components.size,
                        textMeasurer = textMeasurer,
                        attributeBackgroundColor = attributeBackgroundColor,
                        compositeColor = compositeColor,
                        componentColor = componentColor,
                        textBackgroundColor = textBackgroundColor
                    )
                }
            }
        }
        AttributeType.MULTIVALUED -> {
            drawCircle(color = attributeBackgroundColor, radius = radius, center = Offset(attrX, attrY), style = Fill)
            drawCircle(color = normalAttributeColor, radius = radius, center = Offset(attrX, attrY), style = Stroke(width = 2.5f))
            drawCircle(color = normalAttributeColor, radius = radius - 5, center = Offset(attrX, attrY), style = Stroke(width = 2f))
        }
        AttributeType.DERIVED -> {
            drawCircle(color = attributeBackgroundColor, radius = radius, center = Offset(attrX, attrY), style = Fill)
            drawCircle(
                color = normalAttributeColor,
                radius = radius,
                center = Offset(attrX, attrY),
                style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
            )
        }
        else -> {
            val circleStrokeColor = if (attribute.isPrimaryKey) primaryKeyColor else normalAttributeColor
            drawCircle(color = attributeBackgroundColor, radius = radius, center = Offset(attrX, attrY), style = Fill)
            drawCircle(
                color = circleStrokeColor,
                radius = radius,
                center = Offset(attrX, attrY),
                style = Stroke(width = if (attribute.isPrimaryKey) 3.5f else 2.5f)
            )
        }
    }
    val attributeTextColor = when {
        attribute.isPrimaryKey -> primaryKeyColor
        attribute.type == AttributeType.COMPOSITE -> compositeColor
        else -> textColor
    }
    val textLayoutResult = textMeasurer.measure(
        text = attribute.name,
        style = TextStyle(
            color = attributeTextColor,
            fontSize = 13.sp,
            fontWeight = if (attribute.isPrimaryKey) FontWeight.Bold else FontWeight.SemiBold
        )
    )
    val isComposite = attribute.type == AttributeType.COMPOSITE && attribute.components.isNotEmpty()
    val textX = if (isComposite) attrX - radius - textLayoutResult.size.width.toFloat() - 10 else attrX + radius + 10
    val textY = attrY - textLayoutResult.size.height / 2
    drawRoundRect(
        color = textBackgroundColor,
        topLeft = Offset(textX - 5, textY - 2),
        size = Size(textLayoutResult.size.width.toFloat() + 10, textLayoutResult.size.height.toFloat() + 4),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = attributeTextColor.copy(alpha = 0.5f),
        topLeft = Offset(textX - 5, textY - 2),
        size = Size(textLayoutResult.size.width.toFloat() + 10, textLayoutResult.size.height.toFloat() + 4),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f)
    )
    drawText(textLayoutResult = textLayoutResult, topLeft = Offset(textX, textY))
    if (attribute.type == AttributeType.MULTIVALUED && attribute.multiplicity.isNotBlank()) {
        val multiplicityText = textMeasurer.measure(
            text = attribute.multiplicity,
            style = TextStyle(color = normalAttributeColor.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Normal)
        )
        val multY = textY + textLayoutResult.size.height + 4
        drawRoundRect(
            color = textBackgroundColor,
            topLeft = Offset(textX - 3, multY - 1),
            size = Size(multiplicityText.size.width.toFloat() + 6, multiplicityText.size.height.toFloat() + 2),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawText(textLayoutResult = multiplicityText, topLeft = Offset(textX, multY))
    }
}
private fun DrawScope.drawAttributeComponent(
    component: Attribute,
    parentX: Float,
    parentY: Float,
    index: Int,
    total: Int,
    textMeasurer: TextMeasurer,
    attributeBackgroundColor: Color,
    compositeColor: Color,
    componentColor: Color,
    textBackgroundColor: Color
) {
    val radius = 12f
    val horizontalSpacing = 60f
    val verticalSpacing = 40f
    val startY = parentY - ((total - 1) * verticalSpacing / 2f)
    val compX = parentX + horizontalSpacing
    val compY = startY + (index * verticalSpacing)
    drawLine(color = compositeColor.copy(alpha = 0.4f), start = Offset(parentX, parentY), end = Offset(compX, compY), strokeWidth = 1.2f)
    drawCircle(color = attributeBackgroundColor, radius = radius, center = Offset(compX, compY), style = Fill)
    drawCircle(color = componentColor, radius = radius, center = Offset(compX, compY), style = Stroke(width = 2f))
    val textLayoutResult = textMeasurer.measure(
        text = component.name,
        style = TextStyle(color = componentColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    )
    val textX = compX + radius + 8
    val textY = compY - textLayoutResult.size.height / 2
    drawRoundRect(
        color = textBackgroundColor,
        topLeft = Offset(textX - 4, textY - 2),
        size = Size(textLayoutResult.size.width.toFloat() + 8, textLayoutResult.size.height.toFloat() + 4),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = componentColor.copy(alpha = 0.4f),
        topLeft = Offset(textX - 4, textY - 2),
        size = Size(textLayoutResult.size.width.toFloat() + 8, textLayoutResult.size.height.toFloat() + 4),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 1.2f)
    )
    drawText(textLayoutResult = textLayoutResult, topLeft = Offset(textX, textY))
}
internal fun getClosestPointOnRectangle(
    rectX: Float,
    rectY: Float,
    rectWidth: Float,
    rectHeight: Float,
    targetX: Float,
    targetY: Float
): Offset {
    val centerX = rectX + rectWidth / 2
    val centerY = rectY + rectHeight / 2
    val dx = targetX - centerX
    val dy = targetY - centerY
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
    targetY: Float
): Offset {
    val dx = targetX - centerX
    val dy = targetY - centerY
    val halfWidth = width / 2
    val halfHeight = height / 2
    val totalScale = 1f / (abs(dx) / halfWidth + abs(dy) / halfHeight)
    return Offset(centerX + dx * totalScale, centerY + dy * totalScale)
}
