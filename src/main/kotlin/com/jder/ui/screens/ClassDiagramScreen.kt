package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jder.data.ClassDiagramRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassToolMode
import com.jder.strings.ClassStrings
import com.jder.strings.CommonStrings
import com.jder.ui.components.ClassContextMenuType
import com.jder.ui.components.ClassDiagramCanvas
import com.jder.ui.components.ClassPropertiesPanel
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.PendingRelation
import com.jder.ui.components.Toasts
import com.jder.ui.components.contextMenuType
import com.jder.ui.dialogs.ClassMemberDialog
import com.jder.ui.dialogs.ClassPropertiesDialog
import com.jder.ui.dialogs.ClassRelationPropertiesDialog
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
    var showClassDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showAddAttributeDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<ClassMember?>(null) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var pendingRelation by remember { mutableStateOf<PendingRelation?>(null) }
    var contextMenu by remember { mutableStateOf<ContextMenuRequest<ClassContextMenuType>?>(null) }
    fun openEditor(type: ClassContextMenuType) {
        when (type) {
            ClassContextMenuType.CLASS -> showClassDialog = true
            ClassContextMenuType.RELATION -> showRelationDialog = true
            ClassContextMenuType.NOTE -> showNoteDialog = true
        }
    }
    fun selectedType(): ClassContextMenuType? =
        when {
            state.selectedClassId != null -> ClassContextMenuType.CLASS
            state.selectedRelationId != null -> ClassContextMenuType.RELATION
            state.selectedNoteId != null -> ClassContextMenuType.NOTE
            else -> null
        }
    fun actionsTitle(type: ClassContextMenuType): String =
        when (type) {
            ClassContextMenuType.CLASS -> ClassStrings.CLASS_ACTIONS
            ClassContextMenuType.RELATION -> CommonStrings.RELATION_ACTIONS
            ClassContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
        }
    fun actions(type: ClassContextMenuType): List<ContextMenuItem> = buildList {
        add(
            ContextMenuItem(
                when (type) {
                    ClassContextMenuType.CLASS -> CommonStrings.EDIT_PROPERTIES
                    ClassContextMenuType.RELATION -> CommonStrings.EDIT_RELATION
                    ClassContextMenuType.NOTE -> CommonStrings.EDIT_TEXT
                },
                Icons.Default.Edit,
            ) {
                openEditor(type)
            }
        )
        if (type == ClassContextMenuType.CLASS) {
            add(
                ContextMenuItem(CommonStrings.ADD_ATTRIBUTE, Icons.Default.Add) {
                    showAddAttributeDialog = true
                }
            )
            add(
                ContextMenuItem(CommonStrings.ADD_METHOD, Icons.Default.Add) {
                    showAddMethodDialog = true
                }
            )
        }
        add(
            ContextMenuItem(CommonStrings.DELETE, Icons.Default.Delete, isDestructive = true) {
                state.deleteSelection()?.let(Toasts::success)
            }
        )
    }
    DiagramEditorScaffold(
        state = state,
        repository = repository,
        themeState = themeState,
        renderImage = { renderClassDiagramToBitmap(state.diagram, it) },
        emptyHint = ClassStrings.CLASS_EMPTY_HINT,
        tools =
            listOf(
                DiagramTool.Mode(
                    CustomIcons.Rectangle,
                    ClassStrings.CREATE_CLASS,
                    ClassStrings.CLASS,
                    state.toolMode == ClassToolMode.CLASS,
                ) {
                    state.toolMode = ClassToolMode.CLASS
                },
                DiagramTool.Mode(
                    CustomIcons.Interface,
                    ClassStrings.CREATE_INTERFACE,
                    ClassStrings.INTERFACE,
                    state.toolMode == ClassToolMode.INTERFACE,
                ) {
                    state.toolMode = ClassToolMode.INTERFACE
                },
                DiagramTool.Mode(
                    Icons.Default.FormatListNumbered,
                    ClassStrings.CREATE_ENUM,
                    ClassStrings.ENUM,
                    state.toolMode == ClassToolMode.ENUM,
                ) {
                    state.toolMode = ClassToolMode.ENUM
                },
                DiagramTool.Mode(
                    Icons.Default.ArrowRightAlt,
                    ClassStrings.CREATE_CLASS_RELATION,
                    CommonStrings.RELATION,
                    state.toolMode == ClassToolMode.RELATION,
                ) {
                    state.toolMode = ClassToolMode.RELATION
                    state.pendingRelationSourceId = null
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == ClassToolMode.NOTE,
                ) {
                    state.toolMode = ClassToolMode.NOTE
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            ClassPropertiesPanel(
                state = state,
                onEditClass = { showClassDialog = true },
                onEditRelation = { showRelationDialog = true },
                onEditNote = { showNoteDialog = true },
                onAddAttribute = { showAddAttributeDialog = true },
                onAddMethod = { showAddMethodDialog = true },
                onEditMember = { memberToEdit = it },
                onDeleteMember = { memberId ->
                    state.selectedClassId?.let {
                        state.deleteMemberFromClass(it, memberId)
                        Toasts.success(ClassStrings.MEMBER_DELETED)
                    }
                },
                onClose = { state.clearSelection() },
            )
        },
    ) {
        ClassDiagramCanvas(
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
    if (showClassDialog) {
        state.classEntity(state.selectedClassId)?.let { entity ->
            ClassPropertiesDialog(
                entityId = entity.id,
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
                    Toasts.success(ClassStrings.CLASS_UPDATED)
                },
            )
        }
    }
    if (showRelationDialog) {
        state.relation(state.selectedRelationId)?.let { relation ->
            ClassRelationPropertiesDialog(
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
    memberToEdit?.let { member ->
        state.selectedClassId?.let { classId ->
            ClassMemberDialog(
                isMethod = state.diagram.classes.any { c -> c.methods.any { it.id == member.id } },
                initialMember = member,
                onDismiss = { memberToEdit = null },
                onConfirm = {
                    state.updateMemberInClass(classId, it)
                    memberToEdit = null
                    Toasts.success(ClassStrings.MEMBER_UPDATED)
                },
            )
        }
    }
    if (showAddAttributeDialog) {
        state.selectedClassId?.let { classId ->
            ClassMemberDialog(
                isMethod = false,
                onDismiss = { showAddAttributeDialog = false },
                onConfirm = {
                    state.addMemberToClass(classId, it, isMethod = false)
                    showAddAttributeDialog = false
                    Toasts.success(CommonStrings.ATTRIBUTE_ADDED)
                },
            )
        }
    }
    if (showAddMethodDialog) {
        state.selectedClassId?.let { classId ->
            ClassMemberDialog(
                isMethod = true,
                onDismiss = { showAddMethodDialog = false },
                onConfirm = {
                    state.addMemberToClass(classId, it, isMethod = true)
                    showAddMethodDialog = false
                    Toasts.success(ClassStrings.METHOD_ADDED)
                },
            )
        }
    }
    pendingRelation?.let { (sourceId, targetId) ->
        SelectClassRelationTypeDialog(
            onDismiss = { pendingRelation = null },
            onConfirm = {
                state.addRelation(sourceId, targetId, it)
                Toasts.success(CommonStrings.RELATION_CREATED)
                pendingRelation = null
            },
        )
    }
}
