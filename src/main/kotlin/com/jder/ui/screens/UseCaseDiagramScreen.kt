package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jder.data.UseCaseDiagramRepository
import com.jder.domain.model.UseCaseDiagramState
import com.jder.domain.model.UseCaseToolMode
import com.jder.strings.CommonStrings
import com.jder.strings.UseCaseStrings
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.PendingRelation
import com.jder.ui.components.Toasts
import com.jder.ui.components.UseCaseContextMenuType
import com.jder.ui.components.UseCaseDiagramCanvas
import com.jder.ui.components.UseCasePropertiesPanel
import com.jder.ui.components.contextMenuType
import com.jder.ui.dialogs.ActorPropertiesDialog
import com.jder.ui.dialogs.EditRelationTypeDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.SelectRelationTypeDialog
import com.jder.ui.dialogs.SystemBoundaryPropertiesDialog
import com.jder.ui.dialogs.UseCasePropertiesDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderUseCaseDiagramToBitmap

@Composable
fun UseCaseDiagramScreen(
    state: UseCaseDiagramState,
    repository: UseCaseDiagramRepository,
    themeState: ThemeState,
) {
    var showActorDialog by remember { mutableStateOf(false) }
    var showUseCaseDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showSystemBoundaryDialog by remember { mutableStateOf(false) }
    var pendingRelation by remember { mutableStateOf<PendingRelation?>(null) }
    var contextMenu by remember {
        mutableStateOf<ContextMenuRequest<UseCaseContextMenuType>?>(null)
    }
    fun openEditor(type: UseCaseContextMenuType) {
        when (type) {
            UseCaseContextMenuType.ACTOR -> showActorDialog = true
            UseCaseContextMenuType.USE_CASE -> showUseCaseDialog = true
            UseCaseContextMenuType.RELATION -> showRelationDialog = true
            UseCaseContextMenuType.NOTE -> showNoteDialog = true
            UseCaseContextMenuType.SYSTEM -> showSystemBoundaryDialog = true
        }
    }
    fun selectedType(): UseCaseContextMenuType? =
        when {
            state.selectedActorId != null -> UseCaseContextMenuType.ACTOR
            state.selectedUseCaseId != null -> UseCaseContextMenuType.USE_CASE
            state.selectedRelationId != null -> UseCaseContextMenuType.RELATION
            state.selectedNoteId != null -> UseCaseContextMenuType.NOTE
            state.selectedSystemBoundaryId != null -> UseCaseContextMenuType.SYSTEM
            else -> null
        }
    fun actionsTitle(type: UseCaseContextMenuType): String =
        when (type) {
            UseCaseContextMenuType.ACTOR -> UseCaseStrings.ACTOR_ACTIONS
            UseCaseContextMenuType.USE_CASE -> UseCaseStrings.USE_CASE_ACTIONS
            UseCaseContextMenuType.RELATION -> CommonStrings.RELATION_ACTIONS
            UseCaseContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
            UseCaseContextMenuType.SYSTEM -> UseCaseStrings.SYSTEM_ACTIONS
        }
    fun actions(type: UseCaseContextMenuType): List<ContextMenuItem> =
        listOf(
            ContextMenuItem(
                when (type) {
                    UseCaseContextMenuType.ACTOR -> UseCaseStrings.EDIT_NAME
                    UseCaseContextMenuType.RELATION -> UseCaseStrings.EDIT_TYPE
                    UseCaseContextMenuType.NOTE -> CommonStrings.EDIT_TEXT
                    else -> CommonStrings.EDIT_PROPERTIES
                },
                Icons.Default.Edit,
            ) {
                openEditor(type)
            },
            ContextMenuItem(CommonStrings.DELETE, Icons.Default.Delete, isDestructive = true) {
                state.deleteSelection()?.let(Toasts::success)
            },
        )
    DiagramEditorScaffold(
        state = state,
        repository = repository,
        themeState = themeState,
        renderImage = { renderUseCaseDiagramToBitmap(state.diagram, it) },
        emptyHint = UseCaseStrings.USE_CASE_EMPTY_HINT,
        tools =
            listOf(
                DiagramTool.Mode(
                    Icons.Default.Person,
                    UseCaseStrings.CREATE_ACTOR,
                    UseCaseStrings.ACTOR,
                    state.toolMode == UseCaseToolMode.ACTOR,
                ) {
                    state.toolMode = UseCaseToolMode.ACTOR
                },
                DiagramTool.Mode(
                    CustomIcons.Ellipse,
                    UseCaseStrings.CREATE_USE_CASE,
                    UseCaseStrings.USE_CASE,
                    state.toolMode == UseCaseToolMode.USE_CASE,
                ) {
                    state.toolMode = UseCaseToolMode.USE_CASE
                },
                DiagramTool.Mode(
                    Icons.Default.ArrowRightAlt,
                    UseCaseStrings.CREATE_USE_CASE_RELATION,
                    CommonStrings.RELATION,
                    state.toolMode == UseCaseToolMode.RELATION,
                ) {
                    state.toolMode = UseCaseToolMode.RELATION
                    state.pendingRelationSourceId = null
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == UseCaseToolMode.NOTE,
                ) {
                    state.toolMode = UseCaseToolMode.NOTE
                },
                DiagramTool.Mode(
                    Icons.Default.CropSquare,
                    UseCaseStrings.CREATE_SYSTEM,
                    UseCaseStrings.SYSTEM,
                    state.toolMode == UseCaseToolMode.SYSTEM,
                ) {
                    state.toolMode = UseCaseToolMode.SYSTEM
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            UseCasePropertiesPanel(
                state = state,
                onEditActor = { showActorDialog = true },
                onEditUseCase = { showUseCaseDialog = true },
                onEditRelation = { showRelationDialog = true },
                onEditNote = { showNoteDialog = true },
                onEditSystemBoundary = { showSystemBoundaryDialog = true },
                onClose = { state.clearSelection() },
            )
        },
    ) {
        UseCaseDiagramCanvas(
            state = state,
            onContextMenuRequest = { position, type ->
                contextMenu = ContextMenuRequest(position, type)
            },
            onRelationCreateRequest = { sourceId, targetId ->
                pendingRelation = PendingRelation(sourceId, targetId)
            },
            onEditRequest = { openEditor(it.contextMenuType) },
        )
        contextMenu?.let {
            DiagramContextMenu(
                position = it.position,
                title = actionsTitle(it.type),
                items = actions(it.type),
                onDismiss = { contextMenu = null },
            )
        }
    }
    if (showActorDialog) {
        state.actor(state.selectedActorId)?.let { (id, name) ->
            ActorPropertiesDialog(
                actorId = id,
                actorName = name,
                onDismiss = { showActorDialog = false },
                onConfirm = { newName ->
                    state.updateActor(id) { it.copy(name = newName) }
                    showActorDialog = false
                    Toasts.success(UseCaseStrings.ACTOR_UPDATED)
                },
            )
        }
    }
    if (showUseCaseDialog) {
        state.useCase(state.selectedUseCaseId)?.let { useCase ->
            UseCasePropertiesDialog(
                useCaseId = useCase.id,
                useCaseName = useCase.name,
                useCaseDocumentation = useCase.documentation,
                onDismiss = { showUseCaseDialog = false },
                onConfirm = { newName, newDoc ->
                    state.updateUseCase(useCase.id) {
                        it.copy(name = newName, documentation = newDoc)
                    }
                    showUseCaseDialog = false
                    Toasts.success(UseCaseStrings.USE_CASE_UPDATED)
                },
            )
        }
    }
    if (showRelationDialog) {
        state.relation(state.selectedRelationId)?.let { relation ->
            EditRelationTypeDialog(
                relation = relation,
                onDismiss = { showRelationDialog = false },
                onConfirm = { newType ->
                    state.updateRelation(relation.id) { it.copy(type = newType) }
                    showRelationDialog = false
                    Toasts.success(CommonStrings.RELATION_UPDATED)
                },
            )
        }
    }
    if (showNoteDialog) {
        state.diagram.notes
            .find { it.id == state.selectedNoteId }
            ?.let { (id, text) ->
                NotePropertiesDialog(
                    noteId = id,
                    noteText = text,
                    onDismiss = { showNoteDialog = false },
                    onConfirm = { newText ->
                        state.updateNote(id) { it.copy(text = newText) }
                        showNoteDialog = false
                        Toasts.success(CommonStrings.NOTE_UPDATED)
                    },
                )
            }
    }
    if (showSystemBoundaryDialog) {
        state.systemBoundary(state.selectedSystemBoundaryId)?.let { (id, name, _, _, width, height)
            ->
            SystemBoundaryPropertiesDialog(
                boundaryId = id,
                boundaryName = name,
                boundaryWidth = width,
                boundaryHeight = height,
                onDismiss = { showSystemBoundaryDialog = false },
                onConfirm = { newName, newWidth, newHeight ->
                    state.updateSystemBoundary(id) {
                        it.copy(name = newName, width = newWidth, height = newHeight)
                    }
                    showSystemBoundaryDialog = false
                    Toasts.success(UseCaseStrings.SYSTEM_UPDATED)
                },
            )
        }
    }
    pendingRelation?.let { (sourceId, targetId) ->
        SelectRelationTypeDialog(
            onDismiss = { pendingRelation = null },
            onConfirm = {
                state.addRelation(sourceId, targetId, it)
                Toasts.success(CommonStrings.RELATION_CREATED)
                pendingRelation = null
            },
        )
    }
}
