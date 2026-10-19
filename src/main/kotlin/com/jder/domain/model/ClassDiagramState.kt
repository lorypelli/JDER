package com.jder.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class ClassDiagramState :
    DiagramEditorState<ClassDiagram>({ ClassDiagram(name = "Nuovo Diagramma delle Classi") }) {
    var selectedClassId by mutableStateOf<String?>(null)
        private set

    var selectedRelationId by mutableStateOf<String?>(null)
        private set

    var selectedNoteId by mutableStateOf<String?>(null)
        private set

    var toolMode by mutableStateOf(ClassDiagramToolMode.SELECT)
    var pendingRelationSourceId by mutableStateOf<String?>(null)

    override val diagramName: String
        get() = diagram.name

    override val isEmpty: Boolean
        get() = diagram.classes.isEmpty() && diagram.relations.isEmpty() && diagram.notes.isEmpty()

    override val hasSelection: Boolean
        get() = selectedClassId != null || selectedRelationId != null || selectedNoteId != null

    override val isSelectTool: Boolean
        get() = toolMode == ClassDiagramToolMode.SELECT

    override fun withName(diagram: ClassDiagram, name: String) = diagram.copy(name = name)

    override fun pruneSelection() {
        if (selectedClassId != null && diagram.classes.none { it.id == selectedClassId })
            selectedClassId = null
        if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId })
            selectedRelationId = null
        if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId })
            selectedNoteId = null
    }

    override fun resetToolMode() {
        toolMode = ClassDiagramToolMode.SELECT
        pendingRelationSourceId = null
    }

    override fun deleteSelection(): String? {
        selectedClassId?.let {
            deleteClass(it)
            return "Classe eliminata"
        }
        selectedRelationId?.let {
            deleteRelation(it)
            return "Relazione eliminata"
        }
        selectedNoteId?.let {
            deleteNote(it)
            return "Nota eliminata"
        }
        return null
    }

    fun addClass(x: Float, y: Float, name: String, type: ClassEntityType = ClassEntityType.CLASS) {
        val newClass =
            ClassEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                x = x,
                y = y,
                type = type,
                isAbstract = type == ClassEntityType.ABSTRACT_CLASS,
            )
        edit { it.copy(classes = it.classes + newClass) }
        selectClass(newClass.id)
    }

    fun addRelation(sourceId: String, targetId: String, type: ClassRelationType) {
        val relation =
            ClassRelation(
                id = UUID.randomUUID().toString(),
                sourceId = sourceId,
                targetId = targetId,
                type = type,
            )
        edit { it.copy(relations = it.relations + relation) }
    }

    fun addNote(x: Float, y: Float, text: String) {
        val newNote = Note(id = UUID.randomUUID().toString(), text = text, x = x, y = y)
        edit { it.copy(notes = it.notes + newNote) }
        selectNote(newNote.id)
    }

    fun updateClass(classId: String, update: (ClassEntity) -> ClassEntity) {
        edit { d -> d.copy(classes = d.classes.map { if (it.id == classId) update(it) else it }) }
    }

    fun updateClassWithoutSave(classId: String, update: (ClassEntity) -> ClassEntity) {
        editWithoutSave { d ->
            d.copy(classes = d.classes.map { if (it.id == classId) update(it) else it })
        }
    }

    fun updateRelation(relationId: String, update: (ClassRelation) -> ClassRelation) {
        edit { d ->
            d.copy(relations = d.relations.map { if (it.id == relationId) update(it) else it })
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

    fun deleteClass(classId: String) {
        edit { d ->
            d.copy(
                classes = d.classes.filter { it.id != classId },
                relations = d.relations.filter { it.sourceId != classId && it.targetId != classId },
            )
        }
        if (selectedClassId == classId) selectedClassId = null
        if (pendingRelationSourceId == classId) pendingRelationSourceId = null
    }

    fun deleteRelation(relationId: String) {
        edit { d -> d.copy(relations = d.relations.filter { it.id != relationId }) }
        if (selectedRelationId == relationId) selectedRelationId = null
    }

    fun deleteNote(noteId: String) {
        edit { d -> d.copy(notes = d.notes.filter { it.id != noteId }) }
        if (selectedNoteId == noteId) selectedNoteId = null
    }

    fun addMemberToClass(classId: String, member: ClassMember, isMethod: Boolean) {
        updateClass(classId) {
            if (isMethod) it.copy(methods = it.methods + member)
            else it.copy(attributes = it.attributes + member)
        }
    }

    fun deleteMemberFromClass(classId: String, memberId: String) {
        updateClass(classId) {
            it.copy(
                attributes = it.attributes.filter { attribute -> attribute.id != memberId },
                methods = it.methods.filter { method -> method.id != memberId },
            )
        }
    }

    fun selectClass(classId: String?) {
        selectedClassId = classId
        selectedRelationId = null
        selectedNoteId = null
    }

    fun selectRelation(relationId: String?) {
        selectedRelationId = relationId
        selectedClassId = null
        selectedNoteId = null
    }

    fun selectNote(noteId: String?) {
        selectedNoteId = noteId
        selectedClassId = null
        selectedRelationId = null
    }

    override fun clearSelection() {
        selectedClassId = null
        selectedRelationId = null
        selectedNoteId = null
    }
}
