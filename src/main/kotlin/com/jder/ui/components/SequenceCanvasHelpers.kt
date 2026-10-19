package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.jder.domain.model.SequenceAnchor
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.SequenceDiagramState
import com.jder.domain.model.SequenceFragment
import com.jder.domain.model.SequenceFragmentType
import com.jder.domain.model.SequenceLifelineType
import com.jder.domain.model.SequenceOperand
import com.jder.domain.model.SequenceToolMode
import kotlin.math.abs

internal const val SEQUENCE_FRAGMENT_LABEL_HEIGHT = 20f
private const val MESSAGE_HIT_DISTANCE = 6f
private const val LIFELINE_HIT_DISTANCE = 8f
private const val OPERAND_HIT_DISTANCE = 5f
private const val MIN_OPERAND_OFFSET = 24f

sealed interface SequenceElement {
    val id: String

    data class LifelineRef(override val id: String) : SequenceElement

    data class LifelineLineRef(override val id: String) : SequenceElement

    data class MessageRef(override val id: String) : SequenceElement

    data class FragmentRef(override val id: String) : SequenceElement

    data class OperandRef(val operandId: String, override val id: String) : SequenceElement

    data class NoteRef(override val id: String) : SequenceElement
}

internal val SequenceElement.contextMenuType: SequenceContextMenuType
    get() =
        when (this) {
            is SequenceElement.LifelineRef,
            is SequenceElement.LifelineLineRef -> SequenceContextMenuType.LIFELINE
            is SequenceElement.MessageRef -> SequenceContextMenuType.MESSAGE
            is SequenceElement.FragmentRef,
            is SequenceElement.OperandRef -> SequenceContextMenuType.FRAGMENT
            is SequenceElement.NoteRef -> SequenceContextMenuType.NOTE
        }

internal fun SequenceFragment.bounds(): Rect = Rect(x, y, x + width, y + height)

internal fun SequenceFragment.operandRange(): ClosedFloatingPointRange<Float> =
    MIN_OPERAND_OFFSET..maxOf(MIN_OPERAND_OFFSET, height - MIN_OPERAND_OFFSET)

internal fun SequenceFragment.operandY(operand: SequenceOperand): Float =
    y + operand.offset.coerceIn(operandRange())

internal fun hitTestSequenceDiagram(diagram: SequenceDiagram, point: Offset): SequenceElement? {
    diagram.notes
        .lastOrNull { it.contains(point) }
        ?.let {
            return SequenceElement.NoteRef(it.id)
        }
    val layout = sequenceLayout(diagram)
    diagram.messages
        .lastOrNull { message ->
            layout.messageRoutes[message.id]
                ?.zipWithNext { start, end -> distanceToSegment(point, start, end) }
                ?.any { it <= MESSAGE_HIT_DISTANCE } == true
        }
        ?.let {
            return SequenceElement.MessageRef(it.id)
        }
    diagram.fragments
        .lastOrNull {
            it.bounds().contains(point) && point.y <= it.y + SEQUENCE_FRAGMENT_LABEL_HEIGHT
        }
        ?.let {
            return SequenceElement.FragmentRef(it.id)
        }
    diagram.fragments.asReversed().forEach { fragment ->
        if (
            fragment.type.hasOperands &&
                point.x >= fragment.x &&
                point.x <= fragment.x + fragment.width
        ) {
            fragment.operands
                .lastOrNull { abs(point.y - fragment.operandY(it)) <= OPERAND_HIT_DISTANCE }
                ?.let {
                    return SequenceElement.OperandRef(it.id, fragment.id)
                }
        }
    }
    diagram.lifelines
        .lastOrNull { layout.lifelines[it.id]?.head?.contains(point) == true }
        ?.let {
            return SequenceElement.LifelineRef(it.id)
        }
    diagram.lifelines
        .lastOrNull { lifeline ->
            val lifelineLayout = layout.lifelines[lifeline.id] ?: return@lastOrNull false
            val isOnLine =
                abs(point.x - lifelineLayout.centerX) <= LIFELINE_HIT_DISTANCE &&
                    point.y > lifelineLayout.head.bottom &&
                    point.y <= lifelineLayout.lineBottom
            isOnLine ||
                layout.activations.any {
                    it.lifelineId == lifeline.id && it.bounds.inflate(2f).contains(point)
                }
        }
        ?.let {
            return SequenceElement.LifelineLineRef(it.id)
        }
    diagram.fragments
        .lastOrNull { it.bounds().contains(point) }
        ?.let {
            return SequenceElement.FragmentRef(it.id)
        }
    return null
}

internal fun sequenceLifelineAt(diagram: SequenceDiagram, point: Offset): String? =
    when (val element = hitTestSequenceDiagram(diagram, point)) {
        is SequenceElement.LifelineRef,
        is SequenceElement.LifelineLineRef -> element.id
        else -> null
    }

internal fun selectSequenceElement(state: SequenceDiagramState, element: SequenceElement) {
    when (element) {
        is SequenceElement.LifelineRef,
        is SequenceElement.LifelineLineRef -> state.selectLifeline(element.id)
        is SequenceElement.MessageRef -> state.selectMessage(element.id)
        is SequenceElement.FragmentRef,
        is SequenceElement.OperandRef -> state.selectFragment(element.id)
        is SequenceElement.NoteRef -> state.selectNote(element.id)
    }
}

internal fun handleSequenceTap(
    state: SequenceDiagramState,
    point: Offset,
    onMessageCreateRequest: (source: SequenceAnchor, targetId: String) -> Unit,
) {
    when (val mode = state.toolMode) {
        SequenceToolMode.SELECT ->
            hitTestSequenceDiagram(state.diagram, point)?.let { selectSequenceElement(state, it) }
                ?: state.clearSelection()
        SequenceToolMode.MESSAGE -> {
            val lifelineId = sequenceLifelineAt(state.diagram, point)
            val pending = state.pendingMessageSource
            when {
                lifelineId == null -> state.pendingMessageSource = null
                pending == null -> {
                    val head = sequenceLayout(state.diagram).lifelines[lifelineId]?.head
                    state.pendingMessageSource =
                        SequenceAnchor(lifelineId, maxOf(point.y, (head?.bottom ?: 0f) + 20f))
                }
                else -> {
                    onMessageCreateRequest(pending, lifelineId)
                    state.pendingMessageSource = null
                }
            }
        }
        SequenceToolMode.FRAGMENT -> {
            state.addFragment(point.x - 160f, point.y - 80f, SequenceFragmentType.OPT, "condizione")
            state.toolMode = SequenceToolMode.SELECT
        }
        SequenceToolMode.NOTE -> {
            state.addNote(point.x - 120f, point.y - 90f, "Nuova Nota")
            state.toolMode = SequenceToolMode.SELECT
        }
        else ->
            mode.lifelineType?.let { type ->
                val y = state.diagram.lifelines.firstOrNull()?.y ?: (point.y - type.headHeight / 2)
                state.addLifeline(type, point.x - 60f, y, defaultLifelineName(type))
                state.toolMode = SequenceToolMode.SELECT
            }
    }
}

private fun defaultLifelineName(type: SequenceLifelineType): String =
    when (type) {
        SequenceLifelineType.ACTOR -> "Attore"
        SequenceLifelineType.OBJECT -> ":Oggetto"
        else -> ":${type.display}"
    }

internal fun handleSequenceDrag(
    state: SequenceDiagramState,
    element: SequenceElement,
    delta: Offset,
) {
    when (element) {
        is SequenceElement.LifelineRef ->
            state.updateLifelineWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is SequenceElement.MessageRef ->
            state.updateMessageWithoutSave(element.id) { it.copy(y = it.y + delta.y) }
        is SequenceElement.FragmentRef ->
            state.updateFragmentWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is SequenceElement.OperandRef ->
            state.updateFragmentWithoutSave(element.id) { fragment ->
                fragment.copy(
                    operands =
                        fragment.operands.map {
                            if (it.id == element.operandId) {
                                it.copy(
                                    offset = (it.offset + delta.y).coerceIn(fragment.operandRange())
                                )
                            } else {
                                it
                            }
                        }
                )
            }
        is SequenceElement.NoteRef ->
            state.updateNoteWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is SequenceElement.LifelineLineRef -> {}
    }
}
