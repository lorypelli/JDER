package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.jder.data.ClassDiagramRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassDiagramToolMode
import com.jder.ui.components.ClassDiagramCanvas
import com.jder.ui.components.ClassDiagramContextMenuType
import com.jder.ui.components.ClassDiagramPropertiesPanel
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.ToolbarToggleButton
import com.jder.ui.components.contextMenuType
import com.jder.ui.components.rememberSnackbarMessenger
import com.jder.ui.dialogs.ClassEntityDialog
import com.jder.ui.dialogs.ClassMemberDialog
import com.jder.ui.dialogs.ClassRelationDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.SelectClassRelationTypeDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderClassDiagramToBitmap

@Composable
fun ClassDiagramScreen(
    state: ClassDiagramState,
    repository: ClassDiagramRepository,
    themeState: ThemeState,
) {
  val messenger = rememberSnackbarMessenger()
  var showClassDialog by remember { mutableStateOf(false) }
  var showRelationDialog by remember { mutableStateOf(false) }
  var showNoteDialog by remember { mutableStateOf(false) }
  var showAddAttributeDialog by remember { mutableStateOf(false) }
  var showAddMethodDialog by remember { mutableStateOf(false) }
  var pendingRelation by remember { mutableStateOf<Pair<String, String>?>(null) }
  var contextMenu by remember { mutableStateOf<Pair<Offset, ClassDiagramContextMenuType>?>(null) }
  fun openEditor(type: ClassDiagramContextMenuType) {
    when (type) {
      ClassDiagramContextMenuType.CLASS -> showClassDialog = true
      ClassDiagramContextMenuType.RELATION -> showRelationDialog = true
      ClassDiagramContextMenuType.NOTE -> showNoteDialog = true
    }
  }
  DiagramEditorScaffold(
      state = state,
      repository = repository,
      themeState = themeState,
      messenger = messenger,
      renderImage = { renderClassDiagramToBitmap(state.diagram, it) },
      emptyHint =
          "Scegli Classe, Interfaccia, Enum o Nota dalla barra degli strumenti e clicca sulla lavagna",
      tools = {
        ToolbarToggleButton(
            CustomIcons.Rectangle,
            "Crea Classe",
            state.toolMode == ClassDiagramToolMode.CLASS,
        ) {
          state.toolMode = ClassDiagramToolMode.CLASS
        }
        ToolbarToggleButton(
            CustomIcons.Interface,
            "Crea Interfaccia",
            state.toolMode == ClassDiagramToolMode.INTERFACE,
        ) {
          state.toolMode = ClassDiagramToolMode.INTERFACE
        }
        ToolbarToggleButton(
            Icons.Default.FormatListNumbered,
            "Crea Enum",
            state.toolMode == ClassDiagramToolMode.ENUM,
        ) {
          state.toolMode = ClassDiagramToolMode.ENUM
        }
        ToolbarToggleButton(
            Icons.Default.ArrowRightAlt,
            "Crea Relazione (clicca la classe sorgente e poi la destinazione)",
            state.toolMode == ClassDiagramToolMode.RELATION,
        ) {
          state.toolMode = ClassDiagramToolMode.RELATION
          state.pendingRelationSourceId = null
        }
        ToolbarToggleButton(
            CustomIcons.StickyNote,
            "Crea Nota",
            state.toolMode == ClassDiagramToolMode.NOTE,
        ) {
          state.toolMode = ClassDiagramToolMode.NOTE
        }
      },
      properties = {
        ClassDiagramPropertiesPanel(
            state = state,
            onEditClass = { showClassDialog = true },
            onEditRelation = { showRelationDialog = true },
            onEditNote = { showNoteDialog = true },
            onAddAttribute = { showAddAttributeDialog = true },
            onAddMethod = { showAddMethodDialog = true },
            onDeleteMember = { memberId ->
              state.selectedClassId?.let {
                state.deleteMemberFromClass(it, memberId)
                messenger.show("Membro eliminato")
              }
            },
            onClose = { state.clearSelection() },
        )
      },
  ) {
    ClassDiagramCanvas(
        state = state,
        onContextMenuRequest = { position, type -> contextMenu = position to type },
        onRelationCreateRequest = { sourceId, targetId -> pendingRelation = sourceId to targetId },
        onEditRequest = { openEditor(it.contextMenuType) },
    )
    contextMenu?.let { (position, type) ->
      DiagramContextMenu(
          position = position,
          title =
              when (type) {
                ClassDiagramContextMenuType.CLASS -> "Azioni Classe"
                ClassDiagramContextMenuType.RELATION -> "Azioni Relazione"
                ClassDiagramContextMenuType.NOTE -> "Azioni Nota"
              },
          items =
              listOf(
                  ContextMenuItem(
                      when (type) {
                        ClassDiagramContextMenuType.CLASS -> "Modifica Proprietà"
                        ClassDiagramContextMenuType.RELATION -> "Modifica Relazione"
                        ClassDiagramContextMenuType.NOTE -> "Modifica Testo"
                      },
                      Icons.Default.Edit,
                  ) {
                    openEditor(type)
                  },
                  ContextMenuItem("Elimina", Icons.Default.Delete, isDestructive = true) {
                    state.deleteSelection()?.let(messenger::show)
                  },
              ),
          onDismiss = { contextMenu = null },
      )
    }
  }
  if (showClassDialog) {
    state.diagram.classes
        .find { it.id == state.selectedClassId }
        ?.let { entity ->
          ClassEntityDialog(
              name = entity.name,
              type = entity.type,
              isAbstract = entity.isAbstract,
              documentation = entity.documentation,
              onDismiss = { showClassDialog = false },
              onConfirm = { newName, newType, newAbstract, newDoc ->
                state.updateClass(entity.id) {
                  it.copy(
                      name = newName,
                      type = newType,
                      isAbstract = newAbstract,
                      documentation = newDoc,
                  )
                }
                showClassDialog = false
                messenger.show("Classe modificata")
              },
          )
        }
  }
  if (showRelationDialog) {
    state.diagram.relations
        .find { it.id == state.selectedRelationId }
        ?.let { relation ->
          ClassRelationDialog(
              relation = relation,
              onDismiss = { showRelationDialog = false },
              onConfirm = { newType, srcMulti, tgtMulti, lbl ->
                state.updateRelation(relation.id) {
                  it.copy(
                      type = newType,
                      sourceMultiplicity = srcMulti,
                      targetMultiplicity = tgtMulti,
                      label = lbl,
                  )
                }
                showRelationDialog = false
                messenger.show("Relazione modificata")
              },
          )
        }
  }
  if (showNoteDialog) {
    state.diagram.notes
        .find { it.id == state.selectedNoteId }
        ?.let { note ->
          NotePropertiesDialog(
              noteText = note.text,
              onDismiss = { showNoteDialog = false },
              onConfirm = { newText ->
                state.updateNote(note.id) { it.copy(text = newText) }
                showNoteDialog = false
                messenger.show("Nota modificata")
              },
          )
        }
  }
  if (showAddAttributeDialog) {
    state.selectedClassId?.let { classId ->
      ClassMemberDialog(
          isMethod = false,
          onDismiss = { showAddAttributeDialog = false },
          onConfirm = { member ->
            state.addMemberToClass(classId, member, isMethod = false)
            showAddAttributeDialog = false
            messenger.show("Attributo aggiunto")
          },
      )
    }
  }
  if (showAddMethodDialog) {
    state.selectedClassId?.let { classId ->
      ClassMemberDialog(
          isMethod = true,
          onDismiss = { showAddMethodDialog = false },
          onConfirm = { member ->
            state.addMemberToClass(classId, member, isMethod = true)
            showAddMethodDialog = false
            messenger.show("Metodo aggiunto")
          },
      )
    }
  }
  pendingRelation?.let { (sourceId, targetId) ->
    SelectClassRelationTypeDialog(
        onDismiss = { pendingRelation = null },
        onConfirm = { type ->
          state.addRelation(sourceId, targetId, type)
          messenger.show("Relazione creata")
          pendingRelation = null
        },
    )
  }
}
