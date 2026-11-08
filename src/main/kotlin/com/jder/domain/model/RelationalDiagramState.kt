package com.jder.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jder.ui.strings.UiStrings
import java.util.UUID

class RelationalDiagramState :
    DiagramEditorState<RelationalDiagram>({ RelationalDiagram(name = "") }) {
    var selectedTableId by mutableStateOf<String?>(null)
        private set

    var selectedForeignKeyId by mutableStateOf<String?>(null)
        private set

    var selectedNoteId by mutableStateOf<String?>(null)
        private set

    var toolMode by mutableStateOf(RelationalToolMode.SELECT)
    var pendingForeignKeySourceId by mutableStateOf<String?>(null)

    override val diagramName: String
        get() = diagram.name.ifEmpty { UiStrings.NEW_RELATIONAL_SCHEMA }

    override val isEmpty: Boolean
        get() = diagram.tables.isEmpty() && diagram.notes.isEmpty()

    override val hasSelection: Boolean
        get() = selectedTableId != null || selectedForeignKeyId != null || selectedNoteId != null

    override val isSelectTool: Boolean
        get() = toolMode == RelationalToolMode.SELECT

    override fun withName(diagram: RelationalDiagram, name: String) = diagram.copy(name = name)

    override fun pruneSelection() {
        if (selectedTableId != null && table(selectedTableId) == null) selectedTableId = null
        if (selectedForeignKeyId != null && diagram.foreignKey(selectedForeignKeyId) == null)
            selectedForeignKeyId = null
        if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId })
            selectedNoteId = null
        pendingForeignKeySourceId?.let { if (table(it) == null) pendingForeignKeySourceId = null }
    }

    override fun resetToolMode() {
        toolMode = RelationalToolMode.SELECT
        pendingForeignKeySourceId = null
    }

    override fun deleteSelection(): String? {
        selectedTableId?.let {
            deleteTable(it)
            return UiStrings.TABLE_DELETED
        }
        selectedForeignKeyId?.let {
            deleteForeignKey(it)
            return UiStrings.FOREIGN_KEY_DELETED
        }
        selectedNoteId?.let {
            deleteNote(it)
            return UiStrings.NOTE_DELETED
        }
        return null
    }

    fun table(id: String?): RelationalTable? = diagram.tables.find { it.id == id }

    fun addTable(x: Float, y: Float, name: String) {
        val table = RelationalTable(id = UUID.randomUUID().toString(), name = name, x = x, y = y)
        edit { it.copy(tables = it.tables + table) }
        selectTable(table.id)
    }

    fun addColumns(tableId: String, columns: List<RelationalColumn>) {
        updateTable(tableId) { it.copy(columns = it.columns + columns) }
    }

    fun addNote(x: Float, y: Float, text: String) {
        val newNote = Note(id = UUID.randomUUID().toString(), text = text, x = x, y = y)
        edit { it.copy(notes = it.notes + newNote) }
        selectNote(newNote.id)
    }

    fun updateTable(tableId: String, update: (RelationalTable) -> RelationalTable) {
        edit { d -> d.copy(tables = d.tables.map { if (it.id == tableId) update(it) else it }) }
    }

    fun updateTableWithoutSave(tableId: String, update: (RelationalTable) -> RelationalTable) {
        editWithoutSave { d ->
            d.copy(tables = d.tables.map { if (it.id == tableId) update(it) else it })
        }
    }

    fun updateColumn(tableId: String, column: RelationalColumn) {
        updateTable(tableId) { table ->
            table.copy(columns = table.columns.map { if (it.id == column.id) column else it })
        }
    }

    fun updateNote(noteId: String, update: (Note) -> Note) {
        edit { d -> d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it }) }
    }

    fun updateNoteWithoutSave(noteId: String, update: (Note) -> Note) {
        editWithoutSave { d ->
            d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it })
        }
    }

    fun deleteTable(tableId: String) {
        edit { d ->
            d.copy(tables = d.tables.filter { it.id != tableId }).withoutReferences {
                it.tableId == tableId
            }
        }
        pruneSelection()
    }

    fun deleteColumn(tableId: String, columnId: String) {
        edit { d ->
            d.copy(
                    tables =
                        d.tables.map { table ->
                            if (table.id == tableId) {
                                table.copy(columns = table.columns.filter { it.id != columnId })
                            } else {
                                table
                            }
                        }
                )
                .withoutReferences { it.tableId == tableId && it.columnId == columnId }
        }
        pruneSelection()
    }

    fun deleteForeignKey(columnId: String) {
        edit { d ->
            d.copy(
                tables =
                    d.tables.map { table ->
                        table.copy(
                            columns =
                                table.columns.map {
                                    if (it.id == columnId) it.copy(reference = null) else it
                                }
                        )
                    }
            )
        }
        if (selectedForeignKeyId == columnId) selectedForeignKeyId = null
    }

    fun deleteNote(noteId: String) {
        edit { d -> d.copy(notes = d.notes.filter { it.id != noteId }) }
        if (selectedNoteId == noteId) selectedNoteId = null
    }

    fun selectTable(tableId: String?) {
        clearSelection()
        selectedTableId = tableId
    }

    fun selectForeignKey(columnId: String?) {
        clearSelection()
        selectedForeignKeyId = columnId
    }

    fun selectNote(noteId: String?) {
        clearSelection()
        selectedNoteId = noteId
    }

    override fun clearSelection() {
        selectedTableId = null
        selectedForeignKeyId = null
        selectedNoteId = null
    }

    private fun RelationalDiagram.withoutReferences(
        isRemoved: (RelationalReference) -> Boolean
    ): RelationalDiagram =
        copy(
            tables =
                tables.map { table ->
                    table.copy(
                        columns =
                            table.columns.map {
                                if (it.reference?.let(isRemoved) == true) {
                                    it.copy(reference = null)
                                } else {
                                    it
                                }
                            }
                    )
                }
        )
}
