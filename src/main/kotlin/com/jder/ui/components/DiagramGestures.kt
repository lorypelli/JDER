package com.jder.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jder.domain.model.DiagramEditorState
import com.jder.ui.utils.isMacOS
import kotlin.math.abs
import kotlin.math.pow

private const val SCROLL_ZOOM_BASE = 1.1f
private const val SCROLL_PAN_STEP = 30f

internal interface DiagramPointerHandler {
    fun onTap(position: Offset) {}

    fun onDoubleTap(position: Offset): Boolean = false

    fun onContextClick(position: Offset, screenPosition: Offset) {}

    fun onDragStart(position: Offset): Boolean = false

    fun onDrag(position: Offset, delta: Offset) {}

    fun onDragEnd(position: Offset) {}

    fun onHover(position: Offset?) {}
}

internal fun Modifier.diagramPointerInput(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
): Modifier =
    onSizeChanged { state.viewportSize = it.toSize() }
        .pointerInput(state, handler) {
            val dragSlop = 3.dp.toPx()
            var lastTapTime = 0L
            var lastTapPosition = Offset.Zero
            fun toWorld(position: Offset) = (position - state.panOffset) / (state.zoom * density)
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: continue
                    when (event.type) {
                        PointerEventType.Scroll -> {
                            val delta = change.scrollDelta
                            if (abs(delta.y) >= abs(delta.x)) {
                                val steps = (-delta.y).coerceIn(-3f, 3f)
                                state.zoomAround(SCROLL_ZOOM_BASE.pow(steps), change.position)
                            } else {
                                state.panOffset -= Offset(delta.x * SCROLL_PAN_STEP, 0f)
                            }
                            change.consume()
                        }
                        PointerEventType.Move -> handler.onHover(toWorld(change.position))
                        PointerEventType.Exit -> handler.onHover(null)
                        PointerEventType.Press -> {
                            val buttons = event.buttons
                            val isContextClick =
                                buttons.isSecondaryPressed ||
                                    (isMacOS &&
                                        buttons.isPrimaryPressed &&
                                        event.keyboardModifiers.isCtrlPressed)
                            when {
                                isContextClick -> {
                                    change.consume()
                                    handler.onContextClick(
                                        toWorld(change.position),
                                        change.position,
                                    )
                                }
                                buttons.isTertiaryPressed ->
                                    trackPan(state, change.id, change.position)
                                buttons.isPrimaryPressed -> {
                                    change.consume()
                                    val down = change.position
                                    var isDragging = false
                                    var isElementDrag = false
                                    var last = down
                                    while (true) {
                                        val next = awaitPointerEvent()
                                        val current =
                                            next.changes.firstOrNull { it.id == change.id } ?: break
                                        current.consume()
                                        if (!current.pressed) {
                                            if (isDragging) {
                                                if (isElementDrag)
                                                    handler.onDragEnd(toWorld(current.position))
                                            } else {
                                                val time = current.uptimeMillis
                                                val isDoubleTap =
                                                    time - lastTapTime <=
                                                        viewConfiguration.doubleTapTimeoutMillis &&
                                                        (down - lastTapPosition).getDistance() <=
                                                            dragSlop * 4
                                                val world = toWorld(down)
                                                if (!(isDoubleTap && handler.onDoubleTap(world))) {
                                                    handler.onTap(world)
                                                }
                                                lastTapTime = if (isDoubleTap) 0L else time
                                                lastTapPosition = down
                                            }
                                            break
                                        }
                                        if (
                                            !isDragging &&
                                                (current.position - down).getDistance() > dragSlop
                                        ) {
                                            isDragging = true
                                            isElementDrag = handler.onDragStart(toWorld(down))
                                        }
                                        if (isDragging) {
                                            val delta = current.position - last
                                            if (isElementDrag) {
                                                handler.onDrag(
                                                    toWorld(current.position),
                                                    delta / (state.zoom * density),
                                                )
                                            } else {
                                                state.panOffset += delta
                                            }
                                            last = current.position
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

private suspend fun AwaitPointerEventScope.trackPan(
    state: DiagramEditorState<*>,
    pointerId: PointerId,
    start: Offset,
) {
    var last = start
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == pointerId } ?: return
        change.consume()
        if (!event.buttons.isTertiaryPressed) return
        state.panOffset += change.position - last
        last = change.position
    }
}
