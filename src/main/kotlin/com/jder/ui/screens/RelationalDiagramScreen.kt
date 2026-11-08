package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jder.data.RelationalDiagramRepository
import com.jder.domain.model.RelationalColumn
import com.jder.domain.model.RelationalDiagramState
import com.jder.domain.model.RelationalToolMode
import com.jder.domain.model.foreignKey
import com.jder.strings.CommonStrings
import com.jder.strings.RelationalStrings
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.RelationalContextMenuType
import com.jder.ui.components.RelationalDiagramCanvas
import com.jder.ui.components.RelationalElement
import com.jder.ui.components.RelationalPropertiesPanel
import com.jder.ui.components.Toasts
import com.jder.ui.components.contextMenuType
import com.jder.ui.dialogs.AddColumnDialog
import com.jder.ui.dialogs.EditColumnDialog
import com.jder.ui.dialogs.ForeignKeyDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.TablePropertiesDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderRelationalDiagramToBitmap

@Composable
fun RelationalDiagramScreen(
    state: RelationalDiagramState,
    repository: RelationalDiagramRepository,
    themeState: ThemeState,
) {
    var showTableDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showAddColumnDialog by remember { mutableStateOf(false) }
    var columnToEdit by remember { mutableStateOf<ColumnEdit?>(null) }
    var foreignKeyRequest by remember { mutableStateOf<ForeignKeyRequest?>(null) }
    var contextMenu by remember {
        mutableStateOf<ContextMenuRequest<RelationalContextMenuType>?>(null)
    }
    fun openEditor(type: RelationalContextMenuType) {
        when (type) {
            RelationalContextMenuType.TABLE -> showTableDialog = true
            RelationalContextMenuType.FOREIGN_KEY ->
                state.diagram.foreignKey(state.selectedForeignKeyId)?.let {
                    columnToEdit = ColumnEdit(it.table.id, it.column)
                }
            RelationalContextMenuType.NOTE -> showNoteDialog = true
        }
    }
    fun requestForeignKey() {
        state.selectedTableId?.let { foreignKeyRequest = ForeignKeyRequest(it, null) }
    }
    fun selectedType(): RelationalContextMenuType? =
        when {
            state.selectedTableId != null -> RelationalContextMenuType.TABLE
            state.selectedForeignKeyId != null -> RelationalContextMenuType.FOREIGN_KEY
            state.selectedNoteId != null -> RelationalContextMenuType.NOTE
            else -> null
        }
    fun actionsTitle(type: RelationalContextMenuType): String =
        when (type) {
            RelationalContextMenuType.TABLE -> RelationalStrings.TABLE_ACTIONS
            RelationalContextMenuType.FOREIGN_KEY -> RelationalStrings.FOREIGN_KEY_ACTIONS
            RelationalContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
        }
    fun actions(type: RelationalContextMenuType): List<ContextMenuItem> = buildList {
        add(
            ContextMenuItem(
                if (type == RelationalContextMenuType.NOTE) CommonStrings.EDIT_TEXT
                else CommonStrings.EDIT_PROPERTIES,
                Icons.Default.Edit,
            ) {
                openEditor(type)
            }
        )
        if (type == RelationalContextMenuType.TABLE) {
            add(
                ContextMenuItem(CommonStrings.ADD_COLUMN, Icons.Default.Add) {
                    showAddColumnDialog = true
                }
            )
            add(
                ContextMenuItem(RelationalStrings.ADD_FOREIGN_KEY, Icons.Default.Key) {
                    requestForeignKey()
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
        renderImage = { renderRelationalDiagramToBitmap(state.diagram, it) },
        emptyHint = RelationalStrings.RELATIONAL_EMPTY_HINT,
        tools =
            listOf(
                DiagramTool.Mode(
                    CustomIcons.Table,
                    RelationalStrings.CREATE_TABLE,
                    RelationalStrings.TABLE,
                    state.toolMode == RelationalToolMode.TABLE,
                ) {
                    state.toolMode = RelationalToolMode.TABLE
                },
                DiagramTool.Mode(
                    Icons.Default.Key,
                    RelationalStrings.CREATE_FOREIGN_KEY,
                    RelationalStrings.FOREIGN_KEY,
                    state.toolMode == RelationalToolMode.FOREIGN_KEY,
                ) {
                    state.toolMode = RelationalToolMode.FOREIGN_KEY
                    state.pendingForeignKeySourceId = null
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == RelationalToolMode.NOTE,
                ) {
                    state.toolMode = RelationalToolMode.NOTE
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            RelationalPropertiesPanel(
                state = state,
                onEditTable = { showTableDialog = true },
                onEditNote = { showNoteDialog = true },
                onAddColumn = { showAddColumnDialog = true },
                onAddForeignKey = { requestForeignKey() },
                onEditColumn = { tableId, column -> columnToEdit = ColumnEdit(tableId, column) },
                onDeleteColumn = { tableId, columnId ->
                    state.deleteColumn(tableId, columnId)
                    Toasts.success(RelationalStrings.COLUMN_DELETED)
                },
                onDeleteForeignKey = {
                    state.deleteForeignKey(it)
                    Toasts.success(RelationalStrings.FOREIGN_KEY_DELETED)
                },
                onClose = { state.clearSelection() },
            )
        },
    ) {
        RelationalDiagramCanvas(
            state = state,
            onContextMenuRequest = { position, type ->
                contextMenu = ContextMenuRequest(position, type)
            },
            onForeignKeyCreateRequest = { sourceId, targetId ->
                foreignKeyRequest = ForeignKeyRequest(sourceId, targetId)
            },
            onEditRequest = { element ->
                if (element is RelationalElement.ColumnRef) {
                    state
                        .table(element.id)
                        ?.columns
                        ?.find { it.id == element.columnId }
                        ?.let { columnToEdit = ColumnEdit(element.id, it) }
                } else {
                    openEditor(element.contextMenuType)
                }
            },
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
    if (showTableDialog) {
        state.table(state.selectedTableId)?.let { (id, name, _, _, width, _, documentation) ->
            TablePropertiesDialog(
                tableId = id,
                tableName = name,
                tableWidth = width,
                tableDocumentation = documentation,
                onDismiss = { showTableDialog = false },
                onConfirm = { newName, newWidth, newDoc ->
                    state.updateTable(id) {
                        it.copy(name = newName, width = newWidth, documentation = newDoc)
                    }
                    showTableDialog = false
                    Toasts.success(RelationalStrings.TABLE_UPDATED)
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
    if (showAddColumnDialog) {
        state.selectedTableId?.let { tableId ->
            AddColumnDialog(
                tables = state.diagram.tables,
                onDismiss = { showAddColumnDialog = false },
                onConfirm = {
                    state.addColumns(tableId, listOf(it))
                    showAddColumnDialog = false
                    Toasts.success(RelationalStrings.COLUMN_ADDED)
                },
            )
        }
    }
    columnToEdit?.let { edit ->
        EditColumnDialog(
            column = edit.column,
            tables = state.diagram.tables,
            onDismiss = { columnToEdit = null },
            onConfirm = {
                state.updateColumn(edit.tableId, it)
                columnToEdit = null
                Toasts.success(RelationalStrings.COLUMN_UPDATED)
            },
        )
    }
    foreignKeyRequest?.let { (sourceId, targetId) ->
        state.table(sourceId)?.let { source ->
            ForeignKeyDialog(
                sourceTable = source,
                tables = state.diagram.tables,
                initialTargetId = targetId,
                onDismiss = { foreignKeyRequest = null },
                onConfirm = {
                    state.addColumns(source.id, it)
                    foreignKeyRequest = null
                    Toasts.success(RelationalStrings.FOREIGN_KEY_CREATED)
                },
            )
        }
    }
}

private class ColumnEdit(val tableId: String, val column: RelationalColumn)

private data class ForeignKeyRequest(val sourceId: String, val targetId: String?)
