package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.Attribute
import com.jder.domain.model.DiagramState
import com.jder.domain.model.ERDiagram
import com.jder.domain.model.Entity
import com.jder.domain.model.ToolMode
import kotlin.math.abs

private const val ATTRIBUTE_HIT_RADIUS = 24f

sealed interface ERElement {
  val ownerId: String

  data class EntityElement(override val ownerId: String) : ERElement

  data class RelationshipElement(override val ownerId: String) : ERElement

  data class NoteElement(override val ownerId: String) : ERElement

  data class AttributeElement(
      val attributeId: String,
      override val ownerId: String,
      val isEntityAttribute: Boolean,
  ) : ERElement
}

internal val ERElement.contextMenuType: ContextMenuType
  get() =
      when (this) {
        is ERElement.EntityElement -> ContextMenuType.ENTITY
        is ERElement.RelationshipElement -> ContextMenuType.RELATIONSHIP
        is ERElement.NoteElement -> ContextMenuType.NOTE
        is ERElement.AttributeElement ->
            if (isEntityAttribute) ContextMenuType.ENTITY else ContextMenuType.RELATIONSHIP
      }

internal fun isPointInDiamond(
    point: Offset,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
): Boolean {
  val centerX = x + width / 2
  val centerY = y + height / 2
  val dx = abs(point.x - centerX) / (width / 2)
  val dy = abs(point.y - centerY) / (height / 2)
  return (dx + dy) <= 1.0
}

internal fun Entity.contains(point: Offset): Boolean =
    point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height

internal fun calculateAttributePosition(
    entityX: Float,
    entityY: Float,
    entityWidth: Float,
    entityHeight: Float,
    index: Int,
    total: Int,
    attribute: Attribute,
): Offset {
  val centerX = entityX + entityWidth / 2
  val centerY = entityY + entityHeight / 2
  if (attribute.x != 0f || attribute.y != 0f)
      return Offset(centerX + attribute.x, centerY + attribute.y)
  val arrowLength = 60f
  val verticalSpacing = 60f
  val startY = centerY - ((total - 1) * verticalSpacing / 2f)
  return Offset(entityX + entityWidth + arrowLength, startY + (index * verticalSpacing))
}

private fun attributeAt(
    attributes: List<Attribute>,
    ownerX: Float,
    ownerY: Float,
    ownerWidth: Float,
    ownerHeight: Float,
    point: Offset,
): Attribute? =
    attributes.indices
        .reversed()
        .firstOrNull { index ->
          val position =
              calculateAttributePosition(
                  ownerX,
                  ownerY,
                  ownerWidth,
                  ownerHeight,
                  index,
                  attributes.size,
                  attributes[index],
              )
          (position - point).getDistance() <= ATTRIBUTE_HIT_RADIUS
        }
        ?.let { attributes[it] }

internal fun hitTestER(diagram: ERDiagram, point: Offset): ERElement? {
  diagram.notes
      .lastOrNull { it.contains(point) }
      ?.let {
        return ERElement.NoteElement(it.id)
      }
  diagram.relationships.asReversed().forEach { relationship ->
    attributeAt(
            relationship.attributes,
            relationship.x,
            relationship.y,
            relationship.width,
            relationship.height,
            point,
        )
        ?.let {
          return ERElement.AttributeElement(it.id, relationship.id, isEntityAttribute = false)
        }
  }
  diagram.relationships
      .lastOrNull { isPointInDiamond(point, it.x, it.y, it.width, it.height) }
      ?.let {
        return ERElement.RelationshipElement(it.id)
      }
  diagram.entities.asReversed().forEach { entity ->
    attributeAt(entity.attributes, entity.x, entity.y, entity.width, entity.height, point)?.let {
      return ERElement.AttributeElement(it.id, entity.id, isEntityAttribute = true)
    }
  }
  diagram.entities
      .lastOrNull { it.contains(point) }
      ?.let {
        return ERElement.EntityElement(it.id)
      }
  return null
}

internal fun selectERElement(state: DiagramState, element: ERElement) {
  when (element) {
    is ERElement.EntityElement -> state.selectEntity(element.ownerId)
    is ERElement.RelationshipElement -> state.selectRelationship(element.ownerId)
    is ERElement.NoteElement -> state.selectNote(element.ownerId)
    is ERElement.AttributeElement ->
        if (element.isEntityAttribute) state.selectEntity(element.ownerId)
        else state.selectRelationship(element.ownerId)
  }
}

internal fun handleCanvasTap(state: DiagramState, offset: Offset) {
  when (state.toolMode) {
    ToolMode.SELECT ->
        hitTestER(state.diagram, offset)?.let { selectERElement(state, it) }
            ?: state.clearSelection()
    ToolMode.ENTITY -> {
      state.addEntity(offset.x - 70f, offset.y - 35f, "Nuova Entità")
      state.toolMode = ToolMode.SELECT
    }
    ToolMode.RELATIONSHIP -> {
      state.addRelationship(offset.x - 60f, offset.y - 60f, "Nuova Relazione")
      state.toolMode = ToolMode.SELECT
    }
    ToolMode.NOTE -> {
      state.addNote(offset.x - 120f, offset.y - 90f, "Nuova Nota")
      state.toolMode = ToolMode.SELECT
    }
  }
}

internal fun handleDragStart(state: DiagramState, offset: Offset): ERElement? {
  val element = hitTestER(state.diagram, offset) ?: return null
  selectERElement(state, element)
  state.saveDragStartState()
  return element
}

internal fun handleDrag(state: DiagramState, element: ERElement, delta: Offset) {
  when (element) {
    is ERElement.EntityElement ->
        state.updateEntityWithoutSave(element.ownerId) {
          it.copy(x = it.x + delta.x, y = it.y + delta.y)
        }
    is ERElement.RelationshipElement ->
        state.updateRelationshipWithoutSave(element.ownerId) {
          it.copy(x = it.x + delta.x, y = it.y + delta.y)
        }
    is ERElement.NoteElement ->
        state.updateNoteWithoutSave(element.ownerId) {
          it.copy(x = it.x + delta.x, y = it.y + delta.y)
        }
    is ERElement.AttributeElement ->
        if (element.isEntityAttribute) {
          state.updateEntityWithoutSave(element.ownerId) { entity ->
            entity.copy(
                attributes =
                    orbitAttribute(
                        entity.attributes,
                        element.attributeId,
                        entity.x,
                        entity.y,
                        entity.width,
                        entity.height,
                        delta,
                    )
            )
          }
        } else {
          state.updateRelationshipWithoutSave(element.ownerId) { relationship ->
            relationship.copy(
                attributes =
                    orbitAttribute(
                        relationship.attributes,
                        element.attributeId,
                        relationship.x,
                        relationship.y,
                        relationship.width,
                        relationship.height,
                        delta,
                    )
            )
          }
        }
  }
}

private fun orbitAttribute(
    attributes: List<Attribute>,
    attributeId: String,
    ownerX: Float,
    ownerY: Float,
    ownerWidth: Float,
    ownerHeight: Float,
    delta: Offset,
): List<Attribute> =
    attributes.mapIndexed { index, attribute ->
      if (attribute.id != attributeId) return@mapIndexed attribute
      val center = Offset(ownerX + ownerWidth / 2, ownerY + ownerHeight / 2)
      val current =
          calculateAttributePosition(
              ownerX,
              ownerY,
              ownerWidth,
              ownerHeight,
              index,
              attributes.size,
              attribute,
          )
      val radius = (current - center).getDistance().takeIf { it > 0f } ?: (ownerWidth / 2 + 60f)
      val moved = current + delta - center
      val length = moved.getDistance()
      val direction = if (length > 0f) moved / length else Offset(1f, 0f)
      attribute.copy(x = direction.x * radius, y = direction.y * radius)
    }
