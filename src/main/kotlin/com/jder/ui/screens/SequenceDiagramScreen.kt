package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.jder.data.SequenceDiagramRepository
import com.jder.domain.model.SequenceAnchor
import com.jder.domain.model.SequenceDiagramState
import com.jder.domain.model.SequenceToolMode
import com.jder.strings.CommonStrings
import com.jder.strings.SequenceStrings
import com.jder.strings.UseCaseStrings
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.SequenceContextMenuType
import com.jder.ui.components.SequenceDiagramCanvas
import com.jder.ui.components.SequencePropertiesPanel
import com.jder.ui.components.Toasts
import com.jder.ui.components.contextMenuType
import com.jder.ui.dialogs.CreateSequenceMessageDialog
import com.jder.ui.dialogs.EditSequenceMessageDialog
import com.jder.ui.dialogs.LifelinePropertiesDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.SequenceFragmentPropertiesDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderSequenceDiagramToBitmap

@Composable
fun SequenceDiagramScreen(
    state: SequenceDiagramState,
    repository: SequenceDiagramRepository,
    themeState: ThemeState,
) {
    var showLifelineDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var showFragmentDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingMessage by remember { mutableStateOf<PendingMessage?>(null) }
    var contextMenu by remember {
        mutableStateOf<ContextMenuRequest<SequenceContextMenuType>?>(null)
    }
    fun openEditor(type: SequenceContextMenuType) {
        when (type) {
            SequenceContextMenuType.LIFELINE -> showLifelineDialog = true
            SequenceContextMenuType.MESSAGE -> showMessageDialog = true
            SequenceContextMenuType.FRAGMENT -> showFragmentDialog = true
            SequenceContextMenuType.NOTE -> showNoteDialog = true
        }
    }
    fun selectedType(): SequenceContextMenuType? =
        when {
            state.selectedLifelineId != null -> SequenceContextMenuType.LIFELINE
            state.selectedMessageId != null -> SequenceContextMenuType.MESSAGE
            state.selectedFragmentId != null -> SequenceContextMenuType.FRAGMENT
            state.selectedNoteId != null -> SequenceContextMenuType.NOTE
            else -> null
        }
    fun actionsTitle(type: SequenceContextMenuType): String =
        when (type) {
            SequenceContextMenuType.LIFELINE -> SequenceStrings.LIFELINE_ACTIONS
            SequenceContextMenuType.MESSAGE -> SequenceStrings.MESSAGE_ACTIONS
            SequenceContextMenuType.FRAGMENT -> SequenceStrings.FRAGMENT_ACTIONS
            SequenceContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
        }
    fun actions(type: SequenceContextMenuType): List<ContextMenuItem> = buildList {
        add(
            ContextMenuItem(
                if (type == SequenceContextMenuType.NOTE) CommonStrings.EDIT_TEXT
                else CommonStrings.EDIT_PROPERTIES,
                Icons.Default.Edit,
            ) {
                openEditor(type)
            }
        )
        if (type == SequenceContextMenuType.MESSAGE) {
            val message = state.message(state.selectedMessageId)
            add(
                ContextMenuItem(
                    CommonStrings.REVERSE_DIRECTION,
                    Icons.Default.SwapHoriz,
                    enabled = message?.isSelfMessage == false,
                ) {
                    message?.let { state.reverseMessage(it.id) }
                }
            )
        }
        add(
            ContextMenuItem(CommonStrings.DELETE, Icons.Default.Delete, isDestructive = true) {
                state.deleteSelection()?.let(Toasts::success)
            }
        )
    }
    fun lifelineTool(mode: SequenceToolMode, tooltip: String, label: String, icon: ImageVector) =
        DiagramTool.Mode(icon, tooltip, label, state.toolMode == mode) { state.toolMode = mode }
    DiagramEditorScaffold(
        state = state,
        repository = repository,
        themeState = themeState,
        renderImage = { renderSequenceDiagramToBitmap(state.diagram, it) },
        emptyHint = SequenceStrings.SEQUENCE_EMPTY_HINT,
        tools =
            listOf(
                lifelineTool(
                    SequenceToolMode.ACTOR,
                    UseCaseStrings.CREATE_ACTOR,
                    UseCaseStrings.ACTOR,
                    Icons.Default.Person,
                ),
                lifelineTool(
                    SequenceToolMode.OBJECT,
                    SequenceStrings.CREATE_OBJECT,
                    SequenceStrings.OBJECT,
                    CustomIcons.Lifeline,
                ),
                DiagramTool.Group(
                    SequenceStrings.STEREOTYPES,
                    listOf(
                        lifelineTool(
                            SequenceToolMode.BOUNDARY,
                            SequenceStrings.CREATE_BOUNDARY,
                            "Boundary",
                            CustomIcons.BoundaryLifeline,
                        ),
                        lifelineTool(
                            SequenceToolMode.CONTROL,
                            SequenceStrings.CREATE_CONTROL,
                            "Control",
                            CustomIcons.ControlLifeline,
                        ),
                        lifelineTool(
                            SequenceToolMode.ENTITY,
                            SequenceStrings.CREATE_ENTITY_LIFELINE,
                            "Entity",
                            CustomIcons.EntityLifeline,
                        ),
                    ),
                ),
                DiagramTool.Divider,
                DiagramTool.Mode(
                    Icons.Default.ArrowRightAlt,
                    SequenceStrings.CREATE_MESSAGE,
                    SequenceStrings.MESSAGE,
                    state.toolMode == SequenceToolMode.MESSAGE,
                ) {
                    state.toolMode = SequenceToolMode.MESSAGE
                    state.pendingMessageSource = null
                },
                DiagramTool.Mode(
                    CustomIcons.Fragment,
                    SequenceStrings.CREATE_FRAGMENT,
                    SequenceStrings.FRAGMENT,
                    state.toolMode == SequenceToolMode.FRAGMENT,
                ) {
                    state.toolMode = SequenceToolMode.FRAGMENT
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == SequenceToolMode.NOTE,
                ) {
                    state.toolMode = SequenceToolMode.NOTE
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            SequencePropertiesPanel(
                state = state,
                onEditLifeline = { showLifelineDialog = true },
                onEditMessage = { showMessageDialog = true },
                onEditFragment = { showFragmentDialog = true },
                onEditNote = { showNoteDialog = true },
                onClose = { state.clearSelection() },
            )
        },
    ) {
        SequenceDiagramCanvas(
            state = state,
            onContextMenuRequest = { position, type ->
                contextMenu = ContextMenuRequest(position, type)
            },
            onMessageCreateRequest = { source, targetId ->
                pendingMessage = PendingMessage(source, targetId)
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
    if (showLifelineDialog) {
        state.lifeline(state.selectedLifelineId)?.let { (id, name, _, _, type, _, documentation) ->
            LifelinePropertiesDialog(
                lifelineId = id,
                lifelineName = name,
                lifelineType = type,
                lifelineDocumentation = documentation,
                onDismiss = { showLifelineDialog = false },
                onConfirm = { newName, newType, newDoc ->
                    state.updateLifeline(id) {
                        it.copy(name = newName, type = newType, documentation = newDoc)
                    }
                    showLifelineDialog = false
                    Toasts.success(SequenceStrings.LIFELINE_UPDATED)
                },
            )
        }
    }
    if (showMessageDialog) {
        state.message(state.selectedMessageId)?.let { message ->
            EditSequenceMessageDialog(
                message = message,
                onDismiss = { showMessageDialog = false },
                onConfirm = { newLabel, newType ->
                    state.updateMessage(message.id) { it.copy(label = newLabel, type = newType) }
                    showMessageDialog = false
                    Toasts.success(SequenceStrings.MESSAGE_UPDATED)
                },
            )
        }
    }
    if (showFragmentDialog) {
        state.fragment(state.selectedFragmentId)?.let { fragment ->
            SequenceFragmentPropertiesDialog(
                fragment = fragment,
                onDismiss = { showFragmentDialog = false },
                onConfirm = { newType, newCondition, newOperands, newWidth, newHeight ->
                    state.updateFragment(fragment.id) {
                        it.copy(
                            type = newType,
                            condition = newCondition,
                            operands = newOperands,
                            width = newWidth,
                            height = newHeight,
                        )
                    }
                    showFragmentDialog = false
                    Toasts.success(SequenceStrings.FRAGMENT_UPDATED)
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
    pendingMessage?.let { (source, targetId) ->
        CreateSequenceMessageDialog(
            isSelfMessage = source.lifelineId == targetId,
            onDismiss = { pendingMessage = null },
            onConfirm = { label, type ->
                state.addMessage(source.lifelineId, targetId, source.y, label, type)
                Toasts.success(SequenceStrings.MESSAGE_CREATED)
                pendingMessage = null
            },
        )
    }
}

private data class PendingMessage(val source: SequenceAnchor, val targetId: String)
