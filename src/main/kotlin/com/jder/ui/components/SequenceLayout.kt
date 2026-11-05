package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.SequenceLifeline
import com.jder.domain.model.SequenceLifelineType
import com.jder.domain.model.SequenceMessage
import com.jder.domain.model.SequenceMessageType

internal const val SEQUENCE_SELF_MESSAGE_WIDTH = 36f
internal const val SEQUENCE_SELF_MESSAGE_HEIGHT = 24f
private const val ACTIVATION_WIDTH = 12f
private const val ACTIVATION_NEST_OFFSET = 6f
private const val MIN_ACTIVATION_LENGTH = 30f
private const val ACTIVATION_PADDING = 10f
private const val SELF_ACTIVATION_LENGTH = 24f
private const val MIN_LIFELINE_LENGTH = 220f

internal val SequenceLifelineType.headHeight: Float
    get() =
        when (this) {
            SequenceLifelineType.OBJECT -> 40f
            SequenceLifelineType.ACTOR -> 70f
            else -> 58f
        }

internal class SequenceLifelineLayout(
    val lifeline: SequenceLifeline,
    val head: Rect,
    val lineBottom: Float,
    val isDestroyed: Boolean,
) {
    val centerX: Float
        get() = head.center.x
}

internal class SequenceActivation(val lifelineId: String, val bounds: Rect)

internal data class SequenceLayout(
    val lifelines: Map<String, SequenceLifelineLayout>,
    val activations: List<SequenceActivation>,
    val messageRoutes: Map<String, List<Offset>>,
) {
    fun edgeX(lifelineId: String, y: Float, isRightSide: Boolean): Float? {
        val lifeline = lifelines[lifelineId] ?: return null
        val activation =
            activations
                .filter {
                    it.lifelineId == lifelineId && y >= it.bounds.top && y <= it.bounds.bottom
                }
                .maxByOrNull { it.bounds.right } ?: return lifeline.centerX
        return if (isRightSide) activation.bounds.right else activation.bounds.left
    }
}

private class OpenActivation(val callerId: String, val top: Float) {
    var lastActivity = top
}

private class ActivationSpan(val lifelineId: String, val top: Float, val bottom: Float)

internal fun sequenceLayout(diagram: SequenceDiagram): SequenceLayout {
    val messages = diagram.messages.sortedBy { it.y }
    val createdAt = HashMap<String, Float>()
    val destroyedAt = HashMap<String, Float>()
    messages
        .filter { !it.isSelfMessage }
        .forEach {
            when (it.type) {
                SequenceMessageType.CREATE -> createdAt.getOrPut(it.targetId) { it.y }
                SequenceMessageType.DESTROY -> destroyedAt.getOrPut(it.targetId) { it.y }
                else -> {}
            }
        }
    val heads =
        diagram.lifelines.associate { (id, _, x, y, type, width) ->
            val height = type.headHeight
            val top = createdAt[id]?.minus(height / 2) ?: y
            id to Rect(x, top, x + width, top + height)
        }
    val spans = activationSpans(messages).filter { it.lifelineId in heads }
    val activations =
        spans.mapNotNull { span ->
            val centerX = heads[span.lifelineId]?.center?.x ?: return@mapNotNull null
            val depth =
                spans.count {
                    it !== span &&
                        it.lifelineId == span.lifelineId &&
                        it.top < span.top &&
                        span.top < it.bottom
                }
            val left = centerX - ACTIVATION_WIDTH / 2 + depth * ACTIVATION_NEST_OFFSET
            SequenceActivation(
                span.lifelineId,
                Rect(left, span.top, left + ACTIVATION_WIDTH, span.bottom),
            )
        }
    val bottom =
        listOfNotNull(
                heads.values.maxOfOrNull { it.bottom + MIN_LIFELINE_LENGTH },
                messages.maxOfOrNull { it.y + if (it.isSelfMessage) 84f else 60f },
                activations.maxOfOrNull { it.bounds.bottom + 20f },
                diagram.fragments.maxOfOrNull { it.y + it.height + 20f },
            )
            .maxOrNull() ?: 0f
    val lifelines =
        diagram.lifelines.associate {
            val head = heads.getValue(it.id)
            val destroyY = destroyedAt[it.id]
            it.id to
                SequenceLifelineLayout(
                    lifeline = it,
                    head = head,
                    lineBottom = maxOf(destroyY ?: bottom, head.bottom),
                    isDestroyed = destroyY != null,
                )
        }
    val layout = SequenceLayout(lifelines, activations, emptyMap())
    val routes = LinkedHashMap<String, List<Offset>>()
    messages.forEach {
        val source = lifelines[it.sourceId] ?: return@forEach
        val target = lifelines[it.targetId] ?: return@forEach
        routes[it.id] =
            if (it.isSelfMessage) {
                selfMessageRoute(layout, it)
            } else {
                val isRightward = target.centerX >= source.centerX
                val startX = layout.edgeX(source.lifeline.id, it.y, isRightward)
                val endX =
                    if (it.type == SequenceMessageType.CREATE) {
                        if (isRightward) target.head.left else target.head.right
                    } else {
                        layout.edgeX(target.lifeline.id, it.y, !isRightward)
                    }
                listOf(Offset(startX ?: source.centerX, it.y), Offset(endX ?: target.centerX, it.y))
            }
    }
    return SequenceLayout(lifelines, activations, routes)
}

private fun selfMessageRoute(layout: SequenceLayout, message: SequenceMessage): List<Offset> {
    val startX = layout.edgeX(message.sourceId, message.y, isRightSide = true) ?: 0f
    val endY = message.y + SEQUENCE_SELF_MESSAGE_HEIGHT
    val loopX = startX + SEQUENCE_SELF_MESSAGE_WIDTH
    return listOf(
        Offset(startX, message.y),
        Offset(loopX, message.y),
        Offset(loopX, endY),
        Offset(layout.edgeX(message.sourceId, endY, isRightSide = true) ?: startX, endY),
    )
}

private fun activationSpans(messages: List<SequenceMessage>): List<ActivationSpan> {
    val open = HashMap<String, MutableList<OpenActivation>>()
    val spans = mutableListOf<ActivationSpan>()
    fun close(lifelineId: String, activation: OpenActivation, bottom: Float) {
        spans +=
            ActivationSpan(
                lifelineId,
                activation.top,
                maxOf(bottom, activation.top + MIN_ACTIVATION_LENGTH),
            )
    }
    fun touch(lifelineId: String, y: Float) {
        open[lifelineId]?.lastOrNull()?.let { it.lastActivity = maxOf(it.lastActivity, y) }
    }
    messages.forEach { message ->
        val sourceId = message.sourceId
        val targetId = message.targetId
        when {
            message.isSelfMessage -> {
                val returnY = message.y + SEQUENCE_SELF_MESSAGE_HEIGHT
                touch(sourceId, returnY)
                if (message.type == SequenceMessageType.SYNC) {
                    spans +=
                        ActivationSpan(sourceId, returnY - 4f, returnY + SELF_ACTIVATION_LENGTH)
                }
            }
            message.type == SequenceMessageType.REPLY -> {
                open[sourceId]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { stack ->
                        val index =
                            stack.indexOfLast { it.callerId == targetId }.takeIf { it >= 0 }
                                ?: stack.lastIndex
                        close(sourceId, stack.removeAt(index), message.y)
                    }
                touch(targetId, message.y)
            }
            message.type == SequenceMessageType.DESTROY -> {
                touch(sourceId, message.y)
                open.remove(targetId)?.forEach {
                    spans += ActivationSpan(targetId, it.top, maxOf(it.top, message.y))
                }
            }
            message.type == SequenceMessageType.CREATE -> touch(sourceId, message.y)
            else -> {
                touch(sourceId, message.y)
                open.getOrPut(targetId) { mutableListOf() }.add(OpenActivation(sourceId, message.y))
            }
        }
    }
    open.keys.forEach { lifelineId ->
        open.getValue(lifelineId).forEach {
            close(lifelineId, it, it.lastActivity + ACTIVATION_PADDING)
        }
    }
    return spans
}
