package com.jder.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jder.data.ERDiagramRepository
import com.jder.domain.model.Attribute
import com.jder.domain.model.Connection
import com.jder.domain.model.ERDiagramState
import com.jder.domain.model.ERToolMode
import com.jder.domain.model.isManyToMany
import com.jder.strings.CommonStrings
import com.jder.strings.ERStrings
import com.jder.ui.components.ContextMenuItem
import com.jder.ui.components.ContextMenuRequest
import com.jder.ui.components.CustomIcons
import com.jder.ui.components.DiagramContextMenu
import com.jder.ui.components.DiagramSelectionMenu
import com.jder.ui.components.DiagramTool
import com.jder.ui.components.ERContextMenuType
import com.jder.ui.components.ERDiagramCanvas
import com.jder.ui.components.ERElement
import com.jder.ui.components.ERPropertiesPanel
import com.jder.ui.components.Toasts
import com.jder.ui.dialogs.AddAttributeDialog
import com.jder.ui.dialogs.CreateConnectionDialog
import com.jder.ui.dialogs.EditAttributeDialog
import com.jder.ui.dialogs.EditConnectionDialog
import com.jder.ui.dialogs.EntityPropertiesDialog
import com.jder.ui.dialogs.NotePropertiesDialog
import com.jder.ui.dialogs.RelationshipPropertiesDialog
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.renderERDiagramToBitmap

@Composable
fun ERDiagramScreen(
    state: ERDiagramState,
    repository: ERDiagramRepository,
    themeState: ThemeState,
) {
    var showEntityDialog by remember { mutableStateOf(false) }
    var showRelationshipDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showAddAttributeDialog by remember { mutableStateOf(false) }
    var attributeToEdit by remember { mutableStateOf<Attribute?>(null) }
    var showCreateConnectionDialog by remember { mutableStateOf(false) }
    var connectionToEdit by remember { mutableStateOf<ConnectionEdit?>(null) }
    var contextMenu by remember { mutableStateOf<ContextMenuRequest<ERContextMenuType>?>(null) }
    fun openEditor(type: ERContextMenuType) {
        when (type) {
            ERContextMenuType.ENTITY -> showEntityDialog = true
            ERContextMenuType.RELATIONSHIP -> showRelationshipDialog = true
            ERContextMenuType.NOTE -> showNoteDialog = true
        }
    }
    fun convertSelectedRelationship() {
        state.selectedRelationshipId?.let {
            state.convertToAssociativeEntity(it)
            Toasts.success(ERStrings.RELATIONSHIP_CONVERTED)
        }
    }
    fun selectedType(): ERContextMenuType? =
        when {
            state.selectedEntityId != null -> ERContextMenuType.ENTITY
            state.selectedRelationshipId != null -> ERContextMenuType.RELATIONSHIP
            state.selectedNoteId != null -> ERContextMenuType.NOTE
            else -> null
        }
    fun actionsTitle(type: ERContextMenuType): String =
        when (type) {
            ERContextMenuType.ENTITY -> ERStrings.ENTITY_ACTIONS
            ERContextMenuType.RELATIONSHIP -> CommonStrings.RELATION_ACTIONS
            ERContextMenuType.NOTE -> CommonStrings.NOTE_ACTIONS
        }
    fun actions(type: ERContextMenuType): List<ContextMenuItem> {
        val isManyToMany = state.relationship(state.selectedRelationshipId)?.isManyToMany() == true
        return buildList {
            add(
                ContextMenuItem(
                    if (type == ERContextMenuType.NOTE) CommonStrings.EDIT_TEXT
                    else CommonStrings.EDIT_PROPERTIES,
                    Icons.Default.Edit,
                ) {
                    openEditor(type)
                }
            )
            if (type != ERContextMenuType.NOTE) {
                add(
                    ContextMenuItem(CommonStrings.ADD_ATTRIBUTE, Icons.Default.Add) {
                        showAddAttributeDialog = true
                    }
                )
            }
            if (type == ERContextMenuType.RELATIONSHIP) {
                add(
                    ContextMenuItem(ERStrings.ADD_CONNECTION, Icons.Default.Link) {
                        showCreateConnectionDialog = true
                    }
                )
                add(
                    ContextMenuItem(
                        ERStrings.CONVERT_TO_ASSOCIATIVE_ENTITY,
                        Icons.Default.AutoFixHigh,
                        enabled = isManyToMany,
                        onClick = { convertSelectedRelationship() },
                    )
                )
            }
            add(
                ContextMenuItem(CommonStrings.DELETE, Icons.Default.Delete, isDestructive = true) {
                    state.deleteSelection()?.let(Toasts::success)
                }
            )
        }
    }
    DiagramEditorScaffold(
        state = state,
        repository = repository,
        themeState = themeState,
        renderImage = { renderERDiagramToBitmap(state.diagram, it) },
        emptyHint = ERStrings.ER_EMPTY_HINT,
        tools =
            listOf(
                DiagramTool.Mode(
                    CustomIcons.Rectangle,
                    ERStrings.CREATE_ENTITY,
                    ERStrings.ENTITY,
                    state.toolMode == ERToolMode.ENTITY,
                ) {
                    state.toolMode = ERToolMode.ENTITY
                },
                DiagramTool.Mode(
                    CustomIcons.Diamond,
                    ERStrings.CREATE_RELATIONSHIP,
                    CommonStrings.RELATION,
                    state.toolMode == ERToolMode.RELATIONSHIP,
                ) {
                    state.toolMode = ERToolMode.RELATIONSHIP
                },
                DiagramTool.Mode(
                    CustomIcons.StickyNote,
                    CommonStrings.CREATE_NOTE,
                    CommonStrings.NOTE,
                    state.toolMode == ERToolMode.NOTE,
                ) {
                    state.toolMode = ERToolMode.NOTE
                },
            ),
        selectionMenu = {
            selectedType()?.let { DiagramSelectionMenu(actionsTitle(it), actions(it)) }
        },
        properties = {
            ERPropertiesPanel(
                state = state,
                onEditEntity = { showEntityDialog = true },
                onEditRelationship = { showRelationshipDialog = true },
                onEditNote = { showNoteDialog = true },
                onAddAttribute = { showAddAttributeDialog = true },
                onAddConnection = { showCreateConnectionDialog = true },
                onEditAttribute = { attributeToEdit = it },
                onDeleteAttribute = { attributeId ->
                    state.selectedEntityId?.let { state.deleteAttributeFromEntity(it, attributeId) }
                    state.selectedRelationshipId?.let {
                        state.deleteAttributeFromRelationship(it, attributeId)
                    }
                    Toasts.success(CommonStrings.ATTRIBUTE_DELETED)
                },
                onEditConnection = { entityId, connection ->
                    connectionToEdit = ConnectionEdit(entityId, connection)
                },
                onDeleteConnection = { entityId ->
                    state.selectedRelationshipId?.let {
                        state.deleteConnection(it, entityId)
                        Toasts.success(ERStrings.CONNECTION_DELETED)
                    }
                },
                onConvertToAssociativeEntity = ::convertSelectedRelationship,
                onClose = { state.clearSelection() },
            )
        },
    ) {
        ERDiagramCanvas(
            state = state,
            onContextMenuRequest = { position, type ->
                contextMenu = ContextMenuRequest(position, type)
            },
            onEditRequest = { element ->
                if (element is ERElement.AttributeRef) {
                    val attributes =
                        if (element.isEntityAttribute) {
                            state.diagram.entities.find { it.id == element.id }?.attributes
                        } else {
                            state.diagram.relationships.find { it.id == element.id }?.attributes
                        }
                    attributeToEdit = attributes?.find { it.id == element.attributeId }
                } else {
                    openEditor(
                        when (element) {
                            is ERElement.EntityRef -> ERContextMenuType.ENTITY
                            is ERElement.RelationshipRef -> ERContextMenuType.RELATIONSHIP
                            else -> ERContextMenuType.NOTE
                        }
                    )
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
    if (showEntityDialog) {
        state.entity(state.selectedEntityId)?.let { entity ->
            EntityPropertiesDialog(
                entity = entity,
                onDismiss = { showEntityDialog = false },
                onConfirm = { updated ->
                    state.updateEntity(entity.id) { updated }
                    showEntityDialog = false
                    Toasts.success(ERStrings.ENTITY_UPDATED)
                },
            )
        }
    }
    if (showRelationshipDialog) {
        state.relationship(state.selectedRelationshipId)?.let {
            RelationshipPropertiesDialog(
                relationship = it,
                onDismiss = { showRelationshipDialog = false },
                onConfirm = { updated ->
                    state.updateRelationship(it.id) { updated }
                    showRelationshipDialog = false
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
    if (showAddAttributeDialog) {
        AddAttributeDialog(
            onDismiss = { showAddAttributeDialog = false },
            onConfirm = { attribute ->
                state.selectedEntityId?.let { state.addAttributeToEntity(it, attribute) }
                state.selectedRelationshipId?.let {
                    state.addAttributeToRelationship(it, attribute)
                }
                showAddAttributeDialog = false
                Toasts.success(CommonStrings.ATTRIBUTE_ADDED)
            },
        )
    }
    attributeToEdit?.let { attribute ->
        EditAttributeDialog(
            attribute = attribute,
            onDismiss = { attributeToEdit = null },
            onConfirm = { updated ->
                state.selectedEntityId?.let {
                    state.updateAttributeInEntity(it, attribute.id, updated)
                }
                state.selectedRelationshipId?.let {
                    state.updateAttributeInRelationship(it, attribute.id, updated)
                }
                attributeToEdit = null
                Toasts.success(CommonStrings.ATTRIBUTE_UPDATED)
            },
        )
    }
    if (showCreateConnectionDialog) {
        state.selectedRelationshipId?.let { relId ->
            val connectedIds =
                state.diagram.relationships
                    .find { it.id == relId }
                    ?.connections
                    ?.map { it.entityId }
                    ?.toSet() ?: emptySet()
            CreateConnectionDialog(
                entities = state.diagram.entities.filter { it.id !in connectedIds },
                emptyMessage =
                    if (state.diagram.entities.isEmpty()) ERStrings.NO_ENTITIES_YET
                    else ERStrings.ALL_ENTITIES_CONNECTED,
                onDismiss = { showCreateConnectionDialog = false },
                onConfirm = { entityId, cardinality ->
                    state.addConnection(relId, entityId, cardinality)
                    showCreateConnectionDialog = false
                    Toasts.success(ERStrings.CONNECTION_CREATED)
                },
            )
        }
    }
    connectionToEdit?.let { (entityId, connection) ->
        state.selectedRelationshipId?.let { relId ->
            val connectedIds =
                state.diagram.relationships
                    .find { it.id == relId }
                    ?.connections
                    ?.map { it.entityId }
                    ?.filter { it != entityId }
                    ?.toSet() ?: emptySet()
            EditConnectionDialog(
                entities = state.diagram.entities.filter { it.id !in connectedIds },
                currentEntityId = entityId,
                currentCardinality = connection.cardinality,
                onDismiss = { connectionToEdit = null },
                onConfirm = { newEntityId, newCardinality ->
                    state.updateConnection(relId, entityId, newEntityId, newCardinality)
                    connectionToEdit = null
                    Toasts.success(ERStrings.CONNECTION_UPDATED)
                },
            )
        }
    }
}

private data class ConnectionEdit(val entityId: String, val connection: Connection)
