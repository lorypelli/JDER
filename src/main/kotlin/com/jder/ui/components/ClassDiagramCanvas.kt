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
import com.jder.domain.model.ClassDiagramState

@Composable
fun ClassDiagramCanvas(
    state: ClassDiagramState,
    onContextMenuRequest: (Offset, ClassDiagramContextMenuType) -> Unit,
    onRelationCreateRequest: (String, String) -> Unit,
    onEditRequest: (ClassElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberDiagramTextMeasurer()
    val palette = MaterialTheme.colorScheme.toDiagramPalette()
    var hoveredId by remember { mutableStateOf<String?>(null) }
    val currentOnContextMenuRequest by rememberUpdatedState(onContextMenuRequest)
    val currentOnRelationCreateRequest by rememberUpdatedState(onRelationCreateRequest)
    val currentOnEditRequest by rememberUpdatedState(onEditRequest)
    val handler =
        remember(state) {
            object : DiagramPointerHandler {
                var dragged: ClassElement? = null

                override fun onTap(position: Offset) =
                    handleClassDiagramTap(state, position, currentOnRelationCreateRequest)

                override fun onDoubleTap(position: Offset): Boolean {
                    if (!state.isSelectTool) return false
                    val element = hitTestClassDiagram(state.diagram, position) ?: return false
                    selectClassElement(state, element)
                    currentOnEditRequest(element)
                    return true
                }

                override fun onContextClick(position: Offset, screenPosition: Offset) {
                    val element = hitTestClassDiagram(state.diagram, position) ?: return
                    selectClassElement(state, element)
                    currentOnContextMenuRequest(screenPosition, element.contextMenuType)
                }

                override fun onDragStart(position: Offset): Boolean {
                    dragged = handleClassDiagramDragStart(state, position)
                    return dragged != null
                }

                override fun onDrag(position: Offset, delta: Offset) {
                    dragged?.let { handleClassDiagramDrag(state, it, delta) }
                }

                override fun onDragEnd(position: Offset) {
                    dragged = null
                    state.commitDragState()
                }

                override fun onHover(position: Offset?) {
                    hoveredId = position?.let { hitTestClassDiagram(state.diagram, it)?.id }
                }
            }
        }
    val pointerIcon =
        when {
            !state.isSelectTool -> PointerIcon.Crosshair
            hoveredId != null -> PointerIcon.Hand
            else -> PointerIcon.Default
        }
    Canvas(
        modifier =
            modifier.fillMaxSize().diagramPointerInput(state, handler).pointerHoverIcon(pointerIcon)
    ) {
        drawCanvasBackground(state, palette)
        withViewport(state) {
            drawClassDiagram(
                diagram = state.diagram,
                palette = palette,
                textMeasurer = textMeasurer,
                selectedId =
                    state.selectedClassId ?: state.selectedRelationId ?: state.selectedNoteId,
                hoveredId = hoveredId,
                pendingSourceId = state.pendingRelationSourceId,
            )
        }
    }
}
