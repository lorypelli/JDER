package com.jder.ui.components
import androidx.compose.ui.geometry.Offset
import com.jder.domain.model.Actor
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseState
import com.jder.domain.model.UseCaseToolMode
import kotlin.math.pow
import kotlin.math.sqrt
internal fun isPointInActor(point: Offset, actor: Actor): Boolean =
    point.x >= actor.x && point.x <= actor.x + actor.width &&
    point.y >= actor.y && point.y <= actor.y + actor.height
internal fun isPointInUseCase(point: Offset, useCase: UseCase): Boolean {
    val cx = useCase.x + useCase.width / 2
    val cy = useCase.y + useCase.height / 2
    val rx = useCase.width / 2
    val ry = useCase.height / 2
    val dx = (point.x - cx) / rx
    val dy = (point.y - cy) / ry
    return dx * dx + dy * dy <= 1f
}
internal fun isPointNearRelation(point: Offset, relation: UseCaseRelation, diagram: UseCaseDiagram): Boolean {
    val start = getElementCenter(relation.sourceId, diagram) ?: return false
    val end = getElementCenter(relation.targetId, diagram) ?: return false
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return sqrt((point.x - start.x).pow(2) + (point.y - start.y).pow(2)) <= 8f
    val t = (((point.x - start.x) * dx + (point.y - start.y) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = start.x + t * dx
    val projY = start.y + t * dy
    return sqrt((point.x - projX).pow(2) + (point.y - projY).pow(2)) <= 8f
}
internal fun findClickedActorOrUseCase(offset: Offset, state: UseCaseState): String? {
    state.diagram.actors.find { isPointInActor(offset, it) }?.let { return it.id }
    state.diagram.useCases.find { isPointInUseCase(offset, it) }?.let { return it.id }
    return null
}
internal fun handleUseCaseCanvasTap(
    state: UseCaseState,
    offset: Offset,
    onRelationCreateRequest: ((String, String) -> Unit)?
) {
    when (state.toolMode) {
        UseCaseToolMode.SELECT -> {
            state.diagram.actors.find { isPointInActor(offset, it) }?.let { state.selectActor(it.id); return }
            state.diagram.useCases.find { isPointInUseCase(offset, it) }?.let { state.selectUseCase(it.id); return }
            state.diagram.relations.find { isPointNearRelation(offset, it, state.diagram) }?.let { state.selectRelation(it.id); return }
            state.diagram.notes.find {
                offset.x >= it.x && offset.x <= it.x + it.width && offset.y >= it.y && offset.y <= it.y + it.height
            }?.let { state.selectNote(it.id); return }
            state.diagram.systemBoundaries.find {
                offset.x >= it.x && offset.x <= it.x + it.width && offset.y >= it.y && offset.y <= it.y + it.height
            }?.let { state.selectSystemBoundary(it.id); return }
            state.clearSelection()
        }
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
                        onRelationCreateRequest?.invoke(pending, clickedId)
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
internal fun handleUseCaseDragStart(state: UseCaseState, offset: Offset) {
    val adjusted = Offset((offset.x - state.panOffset.x) / state.zoom, (offset.y - state.panOffset.y) / state.zoom)
    state.diagram.actors.find { isPointInActor(adjusted, it) }?.let {
        state.selectActor(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.useCases.find { isPointInUseCase(adjusted, it) }?.let {
        state.selectUseCase(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.notes.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width && adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectNote(it.id)
        state.saveDragStartState()
        return
    }
    state.diagram.systemBoundaries.find {
        adjusted.x >= it.x && adjusted.x <= it.x + it.width && adjusted.y >= it.y && adjusted.y <= it.y + it.height
    }?.let {
        state.selectSystemBoundary(it.id)
        state.saveDragStartState()
    }
}
internal fun handleUseCaseDrag(state: UseCaseState, dragAmount: Offset) {
    state.selectedActorId?.let {
        state.updateActorWithoutSave(it) { actor ->
            actor.copy(x = actor.x + dragAmount.x / state.zoom, y = actor.y + dragAmount.y / state.zoom)
        }
    }
    state.selectedUseCaseId?.let {
        state.updateUseCaseWithoutSave(it) { useCase ->
            useCase.copy(x = useCase.x + dragAmount.x / state.zoom, y = useCase.y + dragAmount.y / state.zoom)
        }
    }
    state.selectedNoteId?.let {
        state.updateNoteWithoutSave(it) { note ->
            note.copy(x = note.x + dragAmount.x / state.zoom, y = note.y + dragAmount.y / state.zoom)
        }
    }
    state.selectedSystemBoundaryId?.let {
        state.updateSystemBoundaryWithoutSave(it) { boundary ->
            boundary.copy(x = boundary.x + dragAmount.x / state.zoom, y = boundary.y + dragAmount.y / state.zoom)
        }
    }
}
