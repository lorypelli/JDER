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
import com.jder.domain.model.RelationalDiagramState

@Composable
fun RelationalDiagramCanvas(
    state: RelationalDiagramState,
    onContextMenuRequest: (Offset, RelationalContextMenuType) -> Unit,
    onForeignKeyCreateRequest: (String, String) -> Unit,
    onEditRequest: (RelationalElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberDiagramTextMeasurer()
    val palette = MaterialTheme.colorScheme.toDiagramPalette()
    var hoveredId by remember { mutableStateOf<String?>(null) }
    val currentOnContextMenuRequest by rememberUpdatedState(onContextMenuRequest)
    val currentOnForeignKeyCreateRequest by rememberUpdatedState(onForeignKeyCreateRequest)
    val currentOnEditRequest by rememberUpdatedState(onEditRequest)
    val handler =
        remember(state) {
            object : DiagramPointerHandler {
                var dragged: RelationalElement? = null

                override fun onTap(position: Offset) =
                    handleRelationalTap(state, position, currentOnForeignKeyCreateRequest)

                override fun onDoubleTap(position: Offset): Boolean {
                    if (!state.isSelectTool) return false
                    val element = hitTestRelationalDiagram(state.diagram, position) ?: return false
                    selectRelationalElement(state, element)
                    currentOnEditRequest(element)
                    return true
                }

                override fun onContextClick(position: Offset, screenPosition: Offset) {
                    val element = hitTestRelationalDiagram(state.diagram, position) ?: return
                    selectRelationalElement(state, element)
                    currentOnContextMenuRequest(screenPosition, element.contextMenuType)
                }

                override fun onDragStart(position: Offset): Boolean {
                    dragged = handleRelationalDragStart(state, position)
                    return dragged != null
                }

                override fun onDrag(position: Offset, delta: Offset) {
                    dragged?.let { handleRelationalDrag(state, it, delta) }
                }

                override fun onDragEnd(position: Offset) {
                    dragged = null
                    state.commitDragState()
                }

                override fun onHover(position: Offset?) {
                    hoveredId = position?.let { hitTestRelationalDiagram(state.diagram, it)?.id }
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
            drawRelationalDiagram(
                diagram = state.diagram,
                palette = palette,
                textMeasurer = textMeasurer,
                selectedId =
                    state.selectedTableId ?: state.selectedForeignKeyId ?: state.selectedNoteId,
                hoveredId = hoveredId,
                pendingSourceId = state.pendingForeignKeySourceId,
            )
        }
    }
}
