package com.jder.ui.components

import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
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
private const val TOUCH_DOUBLE_TAP_SLOP_FACTOR = 4
private val mouseDragSlop = 3.dp
private val mouseDoubleTapSlop = 12.dp

internal interface DiagramPointerHandler {
    fun onTap(position: Offset) {}

    fun onDoubleTap(position: Offset): Boolean = false

    fun onContextClick(position: Offset, screenPosition: Offset) {}

    fun onDragStart(position: Offset): Boolean = false

    fun onDrag(position: Offset, delta: Offset) {}

    fun onDragEnd(position: Offset) {}

    fun onHover(position: Offset?) {}
}

private enum class TouchStart {
    TAP,
    DRAG,
    PINCH,
    CANCEL,
}

private class TapTracker {
    private var lastTime = 0L
    private var lastPosition = Offset.Zero

    fun isDoubleTap(position: Offset, time: Long, timeout: Long, slop: Float): Boolean {
        val isDoubleTap =
            time - lastTime <= timeout && (position - lastPosition).getDistance() <= slop
        lastTime = if (isDoubleTap) 0L else time
        lastPosition = position
        return isDoubleTap
    }
}

internal fun Modifier.diagramPointerInput(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
): Modifier = onSizeChanged {
    state.viewportSize = it.toSize()
}
    .pointerInput(state, handler) {
        val taps = TapTracker()
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
                    PointerEventType.Move -> handler.onHover(toWorld(state, change.position))
                    PointerEventType.Exit -> handler.onHover(null)
                    PointerEventType.Press ->
                        if (change.type == PointerType.Mouse) {
                            trackMouse(state, handler, event, change, taps)
                        } else {
                            trackTouch(state, handler, change, taps)
                        }
                }
            }
        }
    }

private fun AwaitPointerEventScope.toWorld(state: DiagramEditorState<*>, position: Offset) =
    (position - state.panOffset) / (state.zoom * density)

private fun AwaitPointerEventScope.tap(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    taps: TapTracker,
    position: Offset,
    time: Long,
    slop: Float,
) {
    val world = toWorld(state, position)
    val isDoubleTap =
        taps.isDoubleTap(position, time, viewConfiguration.doubleTapTimeoutMillis, slop)
    if (!(isDoubleTap && handler.onDoubleTap(world))) handler.onTap(world)
}

private fun AwaitPointerEventScope.drag(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    isElementDrag: Boolean,
    position: Offset,
    delta: Offset,
) {
    if (isElementDrag) {
        handler.onDrag(toWorld(state, position), delta / (state.zoom * density))
    } else {
        state.panOffset += delta
    }
}

private suspend fun AwaitPointerEventScope.trackMouse(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    event: PointerEvent,
    change: PointerInputChange,
    taps: TapTracker,
) {
    val buttons = event.buttons
    val isContextClick =
        buttons.isSecondaryPressed ||
            (isMacOS && buttons.isPrimaryPressed && event.keyboardModifiers.isCtrlPressed)
    when {
        isContextClick -> {
            change.consume()
            handler.onContextClick(toWorld(state, change.position), change.position)
        }
        buttons.isTertiaryPressed -> trackPan(state, change.id, change.position)
        buttons.isPrimaryPressed -> trackPrimaryButton(state, handler, change, taps)
    }
}

private suspend fun AwaitPointerEventScope.trackPrimaryButton(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    change: PointerInputChange,
    taps: TapTracker,
) {
    change.consume()
    val down = change.position
    val dragSlop = mouseDragSlop.toPx()
    var isDragging = false
    var isElementDrag = false
    var last = down
    while (true) {
        val next = awaitPointerEvent()
        val current = next.changes.firstOrNull { it.id == change.id } ?: return
        current.consume()
        if (!current.pressed) {
            if (isDragging) {
                if (isElementDrag) handler.onDragEnd(toWorld(state, current.position))
            } else {
                tap(state, handler, taps, down, current.uptimeMillis, mouseDoubleTapSlop.toPx())
            }
            return
        }
        if (!isDragging && (current.position - down).getDistance() > dragSlop) {
            isDragging = true
            isElementDrag = handler.onDragStart(toWorld(state, down))
        }
        if (isDragging) {
            drag(state, handler, isElementDrag, current.position, current.position - last)
            last = current.position
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

private suspend fun AwaitPointerEventScope.trackTouch(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    change: PointerInputChange,
    taps: TapTracker,
) {
    change.consume()
    val down = change.position
    val slop = viewConfiguration.touchSlop
    val start =
        withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            awaitTouchStart(change.id, down, slop)
        }
    when (start) {
        null -> {
            handler.onContextClick(toWorld(state, down), down)
            awaitRelease()
        }
        TouchStart.TAP -> {
            val time = currentEvent.changes.first().uptimeMillis
            tap(state, handler, taps, down, time, slop * TOUCH_DOUBLE_TAP_SLOP_FACTOR)
        }
        TouchStart.DRAG -> trackTouchDrag(state, handler, change.id, down)
        TouchStart.PINCH -> trackPinch(state)
        TouchStart.CANCEL -> Unit
    }
}

private suspend fun AwaitPointerEventScope.awaitTouchStart(
    pointerId: PointerId,
    down: Offset,
    slop: Float,
): TouchStart {
    while (true) {
        val event = awaitPointerEvent()
        val current = event.changes.firstOrNull { it.id == pointerId }
        val start =
            when {
                current == null || current.isConsumed -> TouchStart.CANCEL
                event.changes.count { it.pressed } > 1 -> TouchStart.PINCH
                !current.pressed -> TouchStart.TAP
                (current.position - down).getDistance() > slop -> TouchStart.DRAG
                else -> null
            }
        event.changes.forEach { it.consume() }
        if (start != null) return start
    }
}

private suspend fun AwaitPointerEventScope.trackTouchDrag(
    state: DiagramEditorState<*>,
    handler: DiagramPointerHandler,
    pointerId: PointerId,
    down: Offset,
) {
    val isElementDrag = handler.onDragStart(toWorld(state, down))
    var last = down
    var event = currentEvent
    while (true) {
        val current = event.changes.firstOrNull { it.id == pointerId }
        val pressedCount = event.changes.count { it.pressed }
        event.changes.forEach { it.consume() }
        if (current == null || !current.pressed || pressedCount > 1) {
            if (isElementDrag) handler.onDragEnd(toWorld(state, current?.position ?: last))
            if (pressedCount > 0) trackPinch(state)
            return
        }
        drag(state, handler, isElementDrag, current.position, current.position - last)
        last = current.position
        event = awaitPointerEvent()
    }
}

private suspend fun AwaitPointerEventScope.trackPinch(state: DiagramEditorState<*>) {
    var event = currentEvent
    while (event.changes.any { it.pressed }) {
        event.changes.forEach { it.consume() }
        state.panOffset += event.calculatePan()
        val zoom = event.calculateZoom()
        if (zoom != 1f) state.zoomAround(zoom, event.calculateCentroid())
        event = awaitPointerEvent()
    }
    event.changes.forEach { it.consume() }
}

private suspend fun AwaitPointerEventScope.awaitRelease() {
    do {
        val event = awaitPointerEvent()
        event.changes.forEach { it.consume() }
    } while (event.changes.any { it.pressed })
}
