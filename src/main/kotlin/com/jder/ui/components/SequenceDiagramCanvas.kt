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
import com.jder.domain.model.SequenceAnchor
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.SequenceDiagramState

private class SequenceMessagePreview(val start: Offset, val end: Offset)

private fun sequenceMessagePreview(
    diagram: SequenceDiagram,
    source: SequenceAnchor,
    cursor: Offset,
): SequenceMessagePreview? {
    val layout = sequenceLayout(diagram)
    val sourceLayout = layout.lifelines[source.lifelineId] ?: return null
    val endX =
        sequenceLifelineAt(diagram, cursor)
            ?.takeIf { it != source.lifelineId }
            ?.let { layout.lifelines[it]?.centerX } ?: cursor.x
    val startX = layout.edgeX(source.lifelineId, source.y, endX >= sourceLayout.centerX)
    return SequenceMessagePreview(
        Offset(startX ?: sourceLayout.centerX, source.y),
        Offset(endX, source.y),
    )
}

@Composable
fun SequenceDiagramCanvas(
    state: SequenceDiagramState,
    onContextMenuRequest: (Offset, SequenceContextMenuType) -> Unit,
    onMessageCreateRequest: (source: SequenceAnchor, targetId: String) -> Unit,
    onEditRequest: (SequenceElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberDiagramTextMeasurer()
    val palette = MaterialTheme.colorScheme.toDiagramPalette()
    var hovered by remember { mutableStateOf<SequenceElement?>(null) }
    var messagePreview by remember { mutableStateOf<SequenceMessagePreview?>(null) }
    val currentOnContextMenuRequest by rememberUpdatedState(onContextMenuRequest)
    val currentOnMessageCreateRequest by rememberUpdatedState(onMessageCreateRequest)
    val currentOnEditRequest by rememberUpdatedState(onEditRequest)
    val handler =
        remember(state) {
            object : DiagramPointerHandler {
                var dragged: SequenceElement? = null
                var messageSource: SequenceAnchor? = null

                override fun onTap(position: Offset) =
                    handleSequenceTap(state, position, currentOnMessageCreateRequest)

                override fun onDoubleTap(position: Offset): Boolean {
                    if (!state.isSelectTool) return false
                    val element = hitTestSequenceDiagram(state.diagram, position) ?: return false
                    selectSequenceElement(state, element)
                    currentOnEditRequest(element)
                    return true
                }

                override fun onContextClick(position: Offset, screenPosition: Offset) {
                    val element = hitTestSequenceDiagram(state.diagram, position) ?: return
                    selectSequenceElement(state, element)
                    currentOnContextMenuRequest(screenPosition, element.contextMenuType)
                }

                override fun onDragStart(position: Offset): Boolean {
                    val element = hitTestSequenceDiagram(state.diagram, position) ?: return false
                    if (element is SequenceElement.LifelineLineRef) {
                        val source = SequenceAnchor(element.id, position.y)
                        messageSource = source
                        messagePreview = sequenceMessagePreview(state.diagram, source, position)
                        return true
                    }
                    selectSequenceElement(state, element)
                    state.saveDragStartState()
                    dragged = element
                    return true
                }

                override fun onDrag(position: Offset, delta: Offset) {
                    dragged?.let { handleSequenceDrag(state, it, delta) }
                    messageSource?.let {
                        messagePreview = sequenceMessagePreview(state.diagram, it, position)
                    }
                }

                override fun onDragEnd(position: Offset) {
                    messageSource?.let { source ->
                        sequenceLifelineAt(state.diagram, position)
                            ?.takeIf { it != source.lifelineId }
                            ?.let { currentOnMessageCreateRequest(source, it) }
                    }
                    dragged = null
                    messageSource = null
                    messagePreview = null
                    state.commitDragState()
                }

                override fun onHover(position: Offset?) {
                    hovered = position?.let { hitTestSequenceDiagram(state.diagram, it) }
                }
            }
        }
    val pointerIcon =
        when {
            !state.isSelectTool || hovered is SequenceElement.LifelineLineRef ->
                PointerIcon.Crosshair
            hovered != null -> PointerIcon.Hand
            else -> PointerIcon.Default
        }
    Canvas(
        modifier =
            modifier.fillMaxSize().diagramPointerInput(state, handler).pointerHoverIcon(pointerIcon)
    ) {
        drawCanvasBackground(state, palette)
        withViewport(state) {
            drawSequenceDiagram(
                diagram = state.diagram,
                palette = palette,
                textMeasurer = textMeasurer,
                selectedId =
                    state.selectedLifelineId
                        ?: state.selectedMessageId
                        ?: state.selectedFragmentId
                        ?: state.selectedNoteId,
                hoveredId = hovered?.id,
                pendingSource = state.pendingMessageSource,
            )
            messagePreview?.let { drawSequenceMessagePreview(it.start, it.end, palette) }
        }
    }
}
