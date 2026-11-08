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
import com.jder.ui.strings.UiStrings

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
    PropertyCard("${UiStrings.TABLE}: ${table.name}") {
        PropertyDetail("${UiStrings.COLUMNS}: ${table.columns.size}")
        PropertyDetail(
            "${UiStrings.PRIMARY_KEY_DETAIL}: ${primaryKey.ifEmpty { UiStrings.NO_PRIMARY_KEY }}"
        )
        PropertyDetail("${UiStrings.FOREIGN_KEYS}: ${table.columns.count { it.reference != null }}")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEditTable)
    PropertyOutlinedButton(UiStrings.ADD_COLUMN, Icons.Default.Add, onAddColumn)
    PropertyOutlinedButton(UiStrings.ADD_FOREIGN_KEY, Icons.Default.Key, onAddForeignKey)
    if (table.columns.isNotEmpty()) {
        PropertyListTitle("${UiStrings.COLUMNS}:")
        table.columns.forEach {
            PropertyItemCard(
                title = if (it.isNullable) "${it.name}*" else it.name,
                subtitle = columnDetails(it, diagram),
                isEmphasized = it.isPrimaryKey,
                editDescription = UiStrings.EDIT_COLUMN,
                onEdit = { onEditColumn(it) },
                deleteDescription = UiStrings.DELETE_COLUMN,
                onDelete = { onDeleteColumn(it.id) },
            )
        }
    }
    DocumentationCard(table.documentation)
}

private fun columnDetails(column: RelationalColumn, diagram: RelationalDiagram): String =
    buildList {
            if (column.type.isNotBlank()) add(column.type)
            if (column.isPrimaryKey) add(UiStrings.PRIMARY_KEY_TAG)
            column.reference?.let {
                add(UiStrings.references(diagram.qualifiedColumnName(it) ?: "?"))
            }
            if (column.isUnique && !column.isPrimaryKey) add(UiStrings.UNIQUE_TAG)
            if (column.isNullable) add(UiStrings.OPTIONAL_TAG)
        }
        .joinToString(" · ")

@Composable
private fun ForeignKeyPropertiesContent(
    foreignKey: RelationalForeignKey,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    PropertyCard("${UiStrings.FOREIGN_KEY}: ${foreignKey.column.name}") {
        PropertyDetail("${UiStrings.FROM}: ${foreignKey.table.name}.${foreignKey.column.name}")
        PropertyDetail(
            "${UiStrings.TO}: ${foreignKey.referencedTable.name}.${foreignKey.referencedColumn.name}"
        )
        PropertyDetail("${UiStrings.OPTIONAL}: ${UiStrings.yesNo(foreignKey.column.isNullable)}")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(UiStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(
        UiStrings.DELETE_FOREIGN_KEY,
        Icons.Default.Delete,
        onDelete,
        isDestructive = true,
    )
}
