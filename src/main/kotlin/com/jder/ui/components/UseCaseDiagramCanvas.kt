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
import com.jder.domain.model.UseCaseDiagramState

@Composable
fun UseCaseDiagramCanvas(
    state: UseCaseDiagramState,
    onContextMenuRequest: (Offset, UseCaseContextMenuType) -> Unit,
    onRelationCreateRequest: (String, String) -> Unit,
    onEditRequest: (UseCaseElement) -> Unit,
    modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberDiagramTextMeasurer()
  val colorScheme = MaterialTheme.colorScheme
  val palette = remember(colorScheme) { colorScheme.toDiagramPalette() }
  var hoveredId by remember { mutableStateOf<String?>(null) }
  val currentOnContextMenuRequest by rememberUpdatedState(onContextMenuRequest)
  val currentOnRelationCreateRequest by rememberUpdatedState(onRelationCreateRequest)
  val currentOnEditRequest by rememberUpdatedState(onEditRequest)
  val handler =
      remember(state) {
        object : DiagramPointerHandler {
          var dragged: UseCaseElement? = null

          override fun onTap(position: Offset) =
              handleUseCaseTap(state, position, currentOnRelationCreateRequest)

          override fun onDoubleTap(position: Offset): Boolean {
            if (!state.isSelectTool) return false
            val element = hitTestUseCaseDiagram(state.diagram, position) ?: return false
            selectUseCaseElement(state, element)
            currentOnEditRequest(element)
            return true
          }

          override fun onContextClick(position: Offset, screenPosition: Offset) {
            val element = hitTestUseCaseDiagram(state.diagram, position) ?: return
            selectUseCaseElement(state, element)
            currentOnContextMenuRequest(screenPosition, element.contextMenuType)
          }

          override fun onDragStart(position: Offset): Boolean {
            dragged = handleUseCaseDragStart(state, position)
            return dragged != null
          }

          override fun onDrag(position: Offset, delta: Offset) {
            dragged?.let { handleUseCaseDrag(state, it, delta) }
          }

          override fun onDragEnd(position: Offset) {
            dragged = null
            state.commitDragState()
          }

          override fun onHover(position: Offset?) {
            hoveredId = position?.let { hitTestUseCaseDiagram(state.diagram, it)?.id }
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
      drawUseCaseDiagram(
          diagram = state.diagram,
          palette = palette,
          textMeasurer = textMeasurer,
          selectedId =
              state.selectedActorId
                  ?: state.selectedUseCaseId
                  ?: state.selectedRelationId
                  ?: state.selectedNoteId
                  ?: state.selectedSystemBoundaryId,
          hoveredId = hoveredId,
          pendingSourceId = state.pendingRelationSourceId,
      )
    }
  }
}
