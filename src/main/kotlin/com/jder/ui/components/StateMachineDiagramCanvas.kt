package com.jder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextMeasurer
import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineDiagramState
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.StateMachineToolMode

private data class TransitionDragPreview(
    val sourceId: String,
    val targetId: String?,
    val cursor: Offset,
    val hasLeftSource: Boolean,
)

private class StateMachineLayoutCache(
    private val kind: StateMachineKind,
    private val textMeasurer: TextMeasurer,
) {
    private var diagram: StateMachineDiagram? = null
    private var layout: StateMachineLayout? = null

    operator fun get(current: StateMachineDiagram): StateMachineLayout {
        layout
            ?.takeIf { diagram === current }
            ?.let {
                return it
            }
        return stateMachineLayout(current, kind, textMeasurer).also {
            diagram = current
            layout = it
        }
    }
}

@Composable
fun StateMachineDiagramCanvas(
    state: StateMachineDiagramState,
    onContextMenuRequest: (Offset, StateMachineContextMenuType) -> Unit,
    onTransitionCreateRequest: (sourceId: String, targetId: String) -> Unit,
    onEditRequest: (StateMachineElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberDiagramTextMeasurer()
    val palette = MaterialTheme.colorScheme.toDiagramPalette()
    val layoutCache =
        remember(state, textMeasurer) { StateMachineLayoutCache(state.kind, textMeasurer) }
    var hovered by remember { mutableStateOf<StateMachineElement?>(null) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    var dragPreview by remember { mutableStateOf<TransitionDragPreview?>(null) }
    val currentOnContextMenuRequest by rememberUpdatedState(onContextMenuRequest)
    val currentOnTransitionCreateRequest by rememberUpdatedState(onTransitionCreateRequest)
    val currentOnEditRequest by rememberUpdatedState(onEditRequest)
    val handler =
        remember(state, layoutCache) {
            object : DiagramPointerHandler {
                var dragged: StateMachineElement? = null
                var draggedEdge: TransitionEdge? = null

                fun hitTest(position: Offset) =
                    hitTestStateMachine(state.diagram, layoutCache[state.diagram], position)

                override fun onTap(position: Offset) =
                    handleStateMachineTap(
                        state,
                        layoutCache[state.diagram],
                        position,
                        currentOnTransitionCreateRequest,
                    )

                override fun onDoubleTap(position: Offset): Boolean {
                    if (!state.isSelectTool) return false
                    val element = hitTest(position) ?: return false
                    selectStateMachineElement(state, element)
                    currentOnEditRequest(element)
                    return true
                }

                override fun onContextClick(position: Offset, screenPosition: Offset) {
                    val element = hitTest(position) ?: return
                    selectStateMachineElement(state, element)
                    currentOnContextMenuRequest(screenPosition, element.contextMenuType)
                }

                override fun onDragStart(position: Offset): Boolean {
                    val element = hitTest(position)
                    val source = machineStateAt(state.diagram, position)
                    val isConnecting =
                        state.toolMode == StateMachineToolMode.TRANSITION ||
                            element is StateMachineElement.RingRef
                    if (source != null && isConnecting) {
                        state.pendingTransitionSourceId = null
                        dragPreview = TransitionDragPreview(source.id, null, position, false)
                        return true
                    }
                    return when (element) {
                        is StateMachineElement.StateRef,
                        is StateMachineElement.NoteRef -> {
                            selectStateMachineElement(state, element)
                            state.saveDragStartState()
                            dragged = element
                            true
                        }
                        is StateMachineElement.TransitionRef -> {
                            selectStateMachineElement(state, element)
                            val edge = layoutCache[state.diagram].edgeOf(element.id)
                            if (edge != null) {
                                state.saveDragStartState()
                                draggedEdge = edge
                            }
                            edge != null
                        }
                        else -> false
                    }
                }

                override fun onDrag(position: Offset, delta: Offset) {
                    dragged?.let { handleStateMachineDrag(state, it, delta) }
                    draggedEdge?.let { reshapeTransitionArrow(state, it, position) }
                    val preview = dragPreview ?: return
                    val source = state.machineState(preview.sourceId) ?: return
                    val hasLeftSource =
                        preview.hasLeftSource ||
                            (position - source.center).getDistance() >
                                MACHINE_STATE_RADIUS + MACHINE_RING_HIT
                    val target =
                        machineStateAt(state.diagram, position)?.takeIf {
                            it.id != source.id || hasLeftSource
                        }
                    dragPreview =
                        TransitionDragPreview(source.id, target?.id, position, hasLeftSource)
                }

                override fun onDragEnd(position: Offset) {
                    dragPreview?.let { (sourceId, targetId) ->
                        targetId?.let { currentOnTransitionCreateRequest(sourceId, it) }
                    }
                    dragged = null
                    draggedEdge = null
                    dragPreview = null
                    state.commitDragState()
                }

                override fun onHover(position: Offset?) {
                    hoverPosition = position
                    hovered = position?.let(::hitTest)
                }
            }
        }
    val pointerIcon =
        when {
            !state.isSelectTool || hovered is StateMachineElement.RingRef -> PointerIcon.Crosshair
            hovered != null -> PointerIcon.Hand
            else -> PointerIcon.Default
        }
    Canvas(
        modifier =
            modifier.fillMaxSize().diagramPointerInput(state, handler).pointerHoverIcon(pointerIcon)
    ) {
        drawCanvasBackground(state, palette)
        withViewport(state) {
            val diagram = state.diagram
            val drag = dragPreview
            val pendingSource = state.machineState(state.pendingTransitionSourceId)
            val source = drag?.let { state.machineState(it.sourceId) } ?: pendingSource
            val cursor = drag?.cursor ?: hoverPosition?.takeIf { pendingSource != null }
            val target =
                if (drag != null) state.machineState(drag.targetId)
                else cursor?.let { machineStateAt(diagram, it) }
            val hoveredRingId =
                when (val element = hovered) {
                    is StateMachineElement.RingRef -> element.id
                    is StateMachineElement.StateRef ->
                        element.id.takeIf { state.toolMode == StateMachineToolMode.TRANSITION }
                    else -> null
                }
            drawStateMachineDiagram(
                diagram = diagram,
                kind = state.kind,
                layout = layoutCache[diagram],
                palette = palette,
                textMeasurer = textMeasurer,
                selectedId =
                    state.selectedStateId ?: state.selectedTransitionId ?: state.selectedNoteId,
                hoveredId = hovered?.id,
                highlightedRingIds = setOfNotNull(target?.id ?: hoveredRingId),
                pendingSourceId = source?.id,
            )
            if (source != null && cursor != null) {
                transitionPreviewShape(source, target, cursor)?.let {
                    drawTransitionPreview(it, palette)
                }
            }
        }
    }
}
