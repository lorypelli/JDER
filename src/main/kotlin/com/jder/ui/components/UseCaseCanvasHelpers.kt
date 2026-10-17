package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.Actor
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseDiagramState
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseToolMode

private const val ACTOR_LABEL_HEIGHT = 20f

sealed interface UseCaseElement {
  val id: String

  data class ActorRef(override val id: String) : UseCaseElement

  data class UseCaseRef(override val id: String) : UseCaseElement

  data class RelationRef(override val id: String) : UseCaseElement

  data class NoteRef(override val id: String) : UseCaseElement

  data class SystemRef(override val id: String) : UseCaseElement
}

internal val UseCaseElement.contextMenuType: UseCaseContextMenuType
  get() =
      when (this) {
        is UseCaseElement.ActorRef -> UseCaseContextMenuType.ACTOR
        is UseCaseElement.UseCaseRef -> UseCaseContextMenuType.USE_CASE
        is UseCaseElement.RelationRef -> UseCaseContextMenuType.RELATION
        is UseCaseElement.NoteRef -> UseCaseContextMenuType.NOTE
        is UseCaseElement.SystemRef -> UseCaseContextMenuType.SYSTEM
      }

internal fun isPointInActor(point: Offset, actor: Actor): Boolean =
    point.x >= actor.x &&
        point.x <= actor.x + actor.width &&
        point.y >= actor.y &&
        point.y <= actor.y + actor.height + ACTOR_LABEL_HEIGHT

internal fun isPointInUseCase(point: Offset, useCase: UseCase): Boolean {
  val cx = useCase.x + useCase.width / 2
  val cy = useCase.y + useCase.height / 2
  val rx = useCase.width / 2
  val ry = useCase.height / 2
  val dx = (point.x - cx) / rx
  val dy = (point.y - cy) / ry
  return dx * dx + dy * dy <= 1f
}

internal fun SystemBoundary.contains(point: Offset): Boolean =
    point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height

internal fun isPointNearRelation(
    point: Offset,
    relation: UseCaseRelation,
    diagram: UseCaseDiagram,
): Boolean {
  val start = useCaseElementCenter(relation.sourceId, diagram) ?: return false
  val end = useCaseElementCenter(relation.targetId, diagram) ?: return false
  return distanceToSegment(point, start, end) <= 8f
}

internal fun findClickedActorOrUseCase(offset: Offset, state: UseCaseDiagramState): String? =
    state.diagram.useCases.lastOrNull { isPointInUseCase(offset, it) }?.id
        ?: state.diagram.actors.lastOrNull { isPointInActor(offset, it) }?.id

internal fun hitTestUseCaseDiagram(diagram: UseCaseDiagram, point: Offset): UseCaseElement? {
  diagram.notes
      .lastOrNull { it.contains(point) }
      ?.let {
        return UseCaseElement.NoteRef(it.id)
      }
  diagram.useCases
      .lastOrNull { isPointInUseCase(point, it) }
      ?.let {
        return UseCaseElement.UseCaseRef(it.id)
      }
  diagram.actors
      .lastOrNull { isPointInActor(point, it) }
      ?.let {
        return UseCaseElement.ActorRef(it.id)
      }
  diagram.relations
      .lastOrNull { isPointNearRelation(point, it, diagram) }
      ?.let {
        return UseCaseElement.RelationRef(it.id)
      }
  diagram.systemBoundaries
      .lastOrNull { it.contains(point) }
      ?.let {
        return UseCaseElement.SystemRef(it.id)
      }
  return null
}

internal fun selectUseCaseElement(state: UseCaseDiagramState, element: UseCaseElement) {
  when (element) {
    is UseCaseElement.ActorRef -> state.selectActor(element.id)
    is UseCaseElement.UseCaseRef -> state.selectUseCase(element.id)
    is UseCaseElement.RelationRef -> state.selectRelation(element.id)
    is UseCaseElement.NoteRef -> state.selectNote(element.id)
    is UseCaseElement.SystemRef -> state.selectSystemBoundary(element.id)
  }
}

internal fun handleUseCaseTap(
    state: UseCaseDiagramState,
    offset: Offset,
    onRelationCreateRequest: (String, String) -> Unit,
) {
  when (state.toolMode) {
    UseCaseToolMode.SELECT ->
        hitTestUseCaseDiagram(state.diagram, offset)?.let { selectUseCaseElement(state, it) }
            ?: state.clearSelection()
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
        when {
          pending == null -> state.pendingRelationSourceId = clickedId
          pending != clickedId -> {
            onRelationCreateRequest(pending, clickedId)
            state.pendingRelationSourceId = null
          }
          else -> state.pendingRelationSourceId = null
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

internal fun handleUseCaseDragStart(state: UseCaseDiagramState, offset: Offset): UseCaseElement? {
  val element = hitTestUseCaseDiagram(state.diagram, offset) ?: return null
  selectUseCaseElement(state, element)
  if (element is UseCaseElement.RelationRef) return null
  state.saveDragStartState()
  return element
}

internal fun handleUseCaseDrag(state: UseCaseDiagramState, element: UseCaseElement, delta: Offset) {
  when (element) {
    is UseCaseElement.ActorRef ->
        state.updateActorWithoutSave(element.id) { it.copy(x = it.x + delta.x, y = it.y + delta.y) }
    is UseCaseElement.UseCaseRef ->
        state.updateUseCaseWithoutSave(element.id) {
          it.copy(x = it.x + delta.x, y = it.y + delta.y)
        }
    is UseCaseElement.NoteRef ->
        state.updateNoteWithoutSave(element.id) { it.copy(x = it.x + delta.x, y = it.y + delta.y) }
    is UseCaseElement.SystemRef ->
        state.updateSystemBoundaryWithoutSave(element.id) {
          it.copy(x = it.x + delta.x, y = it.y + delta.y)
        }
    is UseCaseElement.RelationRef -> {}
  }
}
