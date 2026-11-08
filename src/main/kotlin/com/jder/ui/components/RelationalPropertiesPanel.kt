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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.RelationalColumn
import com.jder.domain.model.RelationalDiagram
import com.jder.domain.model.RelationalDiagramState
import com.jder.domain.model.RelationalForeignKey
import com.jder.domain.model.RelationalTable
import com.jder.domain.model.foreignKey
import com.jder.domain.model.qualifiedColumnName
import com.jder.strings.CommonStrings
import com.jder.strings.RelationalStrings

@Composable
fun RelationalPropertiesPanel(
    state: RelationalDiagramState,
    onEditTable: () -> Unit,
    onEditNote: () -> Unit,
    onAddColumn: () -> Unit,
    onAddForeignKey: () -> Unit,
    onEditColumn: (tableId: String, column: RelationalColumn) -> Unit,
    onDeleteColumn: (tableId: String, columnId: String) -> Unit,
    onDeleteForeignKey: (columnId: String) -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.table(state.selectedTableId)?.let {
            TablePropertiesContent(
                table = it,
                diagram = state.diagram,
                onEditTable = onEditTable,
                onAddColumn = onAddColumn,
                onAddForeignKey = onAddForeignKey,
                onEditColumn = { column -> onEditColumn(it.id, column) },
                onDeleteColumn = { columnId -> onDeleteColumn(it.id, columnId) },
            )
        }
        state.diagram.foreignKey(state.selectedForeignKeyId)?.let {
            ForeignKeyPropertiesContent(
                foreignKey = it,
                onEdit = { onEditColumn(it.table.id, it.column) },
                onDelete = { onDeleteForeignKey(it.column.id) },
            )
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
    }
}

@Composable
private fun TablePropertiesContent(
    table: RelationalTable,
    diagram: RelationalDiagram,
    onEditTable: () -> Unit,
    onAddColumn: () -> Unit,
    onAddForeignKey: () -> Unit,
    onEditColumn: (column: RelationalColumn) -> Unit,
    onDeleteColumn: (columnId: String) -> Unit,
) {
    val primaryKey = table.primaryKey.joinToString(", ") { it.name }
    PropertyCard("${RelationalStrings.TABLE}: ${table.name}") {
        PropertyDetail("${RelationalStrings.COLUMNS}: ${table.columns.size}")
        PropertyDetail(
            "${RelationalStrings.PRIMARY_KEY_DETAIL}: ${primaryKey.ifEmpty { RelationalStrings.NO_PRIMARY_KEY }}"
        )
        PropertyDetail(
            "${RelationalStrings.FOREIGN_KEYS}: ${table.columns.count { it.reference != null }}"
        )
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEditTable)
    PropertyOutlinedButton(CommonStrings.ADD_COLUMN, Icons.Default.Add, onAddColumn)
    PropertyOutlinedButton(RelationalStrings.ADD_FOREIGN_KEY, Icons.Default.Key, onAddForeignKey)
    if (table.columns.isNotEmpty()) {
        PropertyListTitle("${RelationalStrings.COLUMNS}:")
        table.columns.forEach {
            PropertyItemCard(
                title = if (it.isNullable) "${it.name}*" else it.name,
                subtitle = columnDetails(it, diagram),
                isEmphasized = it.isPrimaryKey,
                editDescription = RelationalStrings.EDIT_COLUMN,
                onEdit = { onEditColumn(it) },
                deleteDescription = RelationalStrings.DELETE_COLUMN,
                onDelete = { onDeleteColumn(it.id) },
            )
        }
    }
    DocumentationCard(table.documentation)
}

private fun columnDetails(column: RelationalColumn, diagram: RelationalDiagram): String =
    buildList {
            if (column.type.isNotBlank()) add(column.type)
            if (column.isPrimaryKey) add(RelationalStrings.PRIMARY_KEY_TAG)
            column.reference?.let {
                add(RelationalStrings.references(diagram.qualifiedColumnName(it) ?: "?"))
            }
            if (column.isUnique && !column.isPrimaryKey) add(RelationalStrings.UNIQUE_TAG)
            if (column.isNullable) add(RelationalStrings.OPTIONAL_TAG)
        }
        .joinToString(" · ")

@Composable
private fun ForeignKeyPropertiesContent(
    foreignKey: RelationalForeignKey,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    PropertyCard("${RelationalStrings.FOREIGN_KEY}: ${foreignKey.column.name}") {
        PropertyDetail("${CommonStrings.FROM}: ${foreignKey.table.name}.${foreignKey.column.name}")
        PropertyDetail(
            "${CommonStrings.TO}: ${foreignKey.referencedTable.name}.${foreignKey.referencedColumn.name}"
        )
        PropertyDetail(
            "${RelationalStrings.OPTIONAL}: ${CommonStrings.yesNo(foreignKey.column.isNullable)}"
        )
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(
        RelationalStrings.DELETE_FOREIGN_KEY,
        Icons.Default.Delete,
        onDelete,
        isDestructive = true,
    )
}
