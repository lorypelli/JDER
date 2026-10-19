package com.jder.domain.model

import kotlinx.serialization.Serializable

@Serializable data class RelationalReference(val tableId: String, val columnId: String)

@Serializable
data class RelationalColumn(
    val id: String,
    val name: String,
    val type: String = "",
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = false,
    val isUnique: Boolean = false,
    val reference: RelationalReference? = null,
)

@Serializable
data class RelationalTable(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float = 220f,
    val columns: List<RelationalColumn> = emptyList(),
    val documentation: String = "",
) {
    val primaryKey: List<RelationalColumn>
        get() = columns.filter { it.isPrimaryKey }
}

@Serializable
data class RelationalDiagram(
    val name: String,
    val tables: List<RelationalTable> = emptyList(),
    val notes: List<Note> = emptyList(),
)

fun RelationalDiagram.validateReferences() {
    val tablesById = tables.associateBy { it.id }
    val errors = mutableListOf<String>()
    tables.forEach tableLoop@{ table ->
        table.columns.forEach columnLoop@{ column ->
            val reference = column.reference ?: return@columnLoop
            val referencedTable =
                tablesById[reference.tableId]
                    ?: run {
                        errors +=
                            "La colonna ${table.id}.${column.id} riferisce una tabella inesistente: " +
                                reference.tableId
                        return@columnLoop
                    }
            if (referencedTable.columns.none { it.id == reference.columnId }) {
                errors +=
                    "La colonna ${table.id}.${column.id} riferisce una colonna inesistente: " +
                        "${reference.tableId}.${reference.columnId}"
            }
        }
    }
    if (errors.isNotEmpty()) throw DiagramValidationException(errors)
}

class RelationalForeignKey(
    val table: RelationalTable,
    val column: RelationalColumn,
    val referencedTable: RelationalTable,
    val referencedColumn: RelationalColumn,
)

fun RelationalDiagram.foreignKeys(): List<RelationalForeignKey> {
    val tablesById = tables.associateBy { it.id }
    return tables.flatMap { table ->
        table.columns.mapNotNull { column ->
            val reference = column.reference ?: return@mapNotNull null
            val referencedTable = tablesById[reference.tableId] ?: return@mapNotNull null
            referencedTable.columns
                .find { it.id == reference.columnId }
                ?.let {
                    RelationalForeignKey(
                        table = table,
                        column = column,
                        referencedTable = referencedTable,
                        referencedColumn = it,
                    )
                }
        }
    }
}

fun RelationalDiagram.foreignKey(columnId: String?): RelationalForeignKey? =
    foreignKeys().find { it.column.id == columnId }

fun RelationalDiagram.qualifiedColumnName(reference: RelationalReference): String? {
    val table = tables.find { it.id == reference.tableId } ?: return null
    val column = table.columns.find { it.id == reference.columnId } ?: return null
    return "${table.name}.${column.name}"
}
