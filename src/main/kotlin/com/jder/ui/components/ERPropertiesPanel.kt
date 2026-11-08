package com.jder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.Attribute
import com.jder.domain.model.Connection
import com.jder.domain.model.ERDiagramState
import com.jder.domain.model.Entity
import com.jder.domain.model.Relationship
import com.jder.domain.model.isManyToMany
import com.jder.ui.strings.UiStrings

@Composable
fun ERPropertiesPanel(
    state: ERDiagramState,
    onEditEntity: () -> Unit,
    onEditRelationship: () -> Unit,
    onEditNote: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddConnection: () -> Unit,
    onEditAttribute: (attribute: Attribute) -> Unit,
    onDeleteAttribute: (attributeId: String) -> Unit,
    onEditConnection: (entityId: String, connection: Connection) -> Unit,
    onDeleteConnection: (entityId: String) -> Unit,
    onConvertToAssociativeEntity: () -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.selectedEntityId?.let { selId ->
            val entity = state.diagram.entities.find { it.id == selId }
            entity?.let {
                EntityPropertiesContent(
                    entity = it,
                    onEditEntity = onEditEntity,
                    onAddAttribute = onAddAttribute,
                    onEditAttribute = onEditAttribute,
                    onDeleteAttribute = onDeleteAttribute,
                )
            }
        }
        state.selectedRelationshipId?.let { selId ->
            val relationship = state.diagram.relationships.find { it.id == selId }
            relationship?.let {
                RelationshipPropertiesContent(
                    relationship = it,
                    entities = state.diagram.entities,
                    onEditRelationship = onEditRelationship,
                    onAddAttribute = onAddAttribute,
                    onAddConnection = onAddConnection,
                    onEditAttribute = onEditAttribute,
                    onDeleteAttribute = onDeleteAttribute,
                    onEditConnection = onEditConnection,
                    onDeleteConnection = onDeleteConnection,
                    onConvertToAssociativeEntity = onConvertToAssociativeEntity,
                )
            }
        }
        state.selectedNoteId?.let { selId ->
            val note = state.diagram.notes.find { it.id == selId }
            note?.let { NotePropertiesContent(note = it, onEdit = onEditNote) }
        }
    }
}

@Composable
private fun EntityPropertiesContent(
    entity: Entity,
    onEditEntity: () -> Unit,
    onAddAttribute: () -> Unit,
    onEditAttribute: (attribute: Attribute) -> Unit,
    onDeleteAttribute: (attributeId: String) -> Unit,
) {
    PropertyCard("${UiStrings.ENTITY}: ${entity.name}") {
        PropertyDetail(
            "${UiStrings.TYPE}: ${if (entity.isWeak) UiStrings.WEAK else UiStrings.STRONG}"
        )
        PropertyDetail("${UiStrings.ATTRIBUTES}: ${entity.attributes.size}")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEditEntity)
    PropertyOutlinedButton(UiStrings.ADD_ATTRIBUTE, Icons.Default.Add, onAddAttribute)
    AttributeList(entity.attributes, onEditAttribute, onDeleteAttribute)
    DocumentationCard(entity.documentation)
}

@Composable
private fun RelationshipPropertiesContent(
    relationship: Relationship,
    entities: List<Entity>,
    onEditRelationship: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddConnection: () -> Unit,
    onEditAttribute: (attribute: Attribute) -> Unit,
    onDeleteAttribute: (attributeId: String) -> Unit,
    onEditConnection: (entityId: String, connection: Connection) -> Unit,
    onDeleteConnection: (entityId: String) -> Unit,
    onConvertToAssociativeEntity: () -> Unit,
) {
    PropertyCard("${UiStrings.RELATION}: ${relationship.name}") {
        PropertyDetail("${UiStrings.CONNECTIONS}: ${relationship.connections.size}")
        PropertyDetail("${UiStrings.ATTRIBUTES}: ${relationship.attributes.size}")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEditRelationship)
    PropertyOutlinedButton(UiStrings.ADD_ATTRIBUTE, Icons.Default.Add, onAddAttribute)
    PropertyOutlinedButton(UiStrings.ADD_CONNECTION, Icons.Default.Link, onAddConnection)
    PropertyOutlinedButton(
        UiStrings.CONVERT_TO_ASSOCIATIVE_ENTITY,
        Icons.Default.AutoFixHigh,
        onConvertToAssociativeEntity,
        enabled = relationship.isManyToMany(),
    )
    AttributeList(relationship.attributes, onEditAttribute, onDeleteAttribute)
    if (relationship.connections.isNotEmpty()) {
        PropertyListTitle("${UiStrings.CONNECTIONS}:")
        relationship.connections.forEach {
            PropertyItemCard(
                title =
                    entities.find { entity -> entity.id == it.entityId }?.name
                        ?: UiStrings.UNKNOWN_ENTITY,
                subtitle = "${UiStrings.CARDINALITY}: ${it.cardinality.display}",
                editDescription = UiStrings.EDIT_CONNECTION,
                onEdit = { onEditConnection(it.entityId, it) },
                deleteDescription = UiStrings.DELETE_CONNECTION,
                onDelete = { onDeleteConnection(it.entityId) },
            )
        }
    }
    DocumentationCard(relationship.documentation)
}

@Composable
private fun AttributeList(
    attributes: List<Attribute>,
    onEdit: (attribute: Attribute) -> Unit,
    onDelete: (attributeId: String) -> Unit,
) {
    if (attributes.isEmpty()) return
    PropertyListTitle("${UiStrings.ATTRIBUTES}:")
    attributes.forEach {
        PropertyItemCard(
            title = it.name,
            subtitle = it.type.display,
            isEmphasized = it.isPrimaryKey,
            editDescription = UiStrings.EDIT_ATTRIBUTE,
            onEdit = { onEdit(it) },
            deleteDescription = UiStrings.DELETE_ATTRIBUTE,
            onDelete = { onDelete(it.id) },
        )
    }
}
