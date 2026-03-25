package com.jder.domain.model
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import java.util.UUID
class ClassDiagramState {
    var diagram by mutableStateOf(ClassDiagram(name = "Nuovo Diagramma delle Classi"))
        private set
    var selectedClassId by mutableStateOf<String?>(null)
        private set
    var selectedRelationId by mutableStateOf<String?>(null)
        private set
    var selectedNoteId by mutableStateOf<String?>(null)
        private set
    var toolMode by mutableStateOf(ClassDiagramToolMode.SELECT)
    var pendingRelationSourceId by mutableStateOf<String?>(null)
    var isModified by mutableStateOf(false)
        private set
    var currentFile by mutableStateOf<String?>(null)
    var zoom by mutableStateOf(1.0f)
    var panOffset by mutableStateOf(Offset.Zero)
    private val undoStack = mutableListOf<ClassDiagram>()
    private val redoStack = mutableListOf<ClassDiagram>()
    private val maxUndoSize = 50
    private fun saveState() {
        undoStack.add(diagram.copy(
            classes = diagram.classes.map { it.copy(attributes = it.attributes.toList(), methods = it.methods.toList()) },
            relations = diagram.relations.toList(),
            notes = diagram.notes.toList()
        ))
        if (undoStack.size > maxUndoSize) undoStack.removeAt(0)
        redoStack.clear()
    }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(diagram.copy(
                classes = diagram.classes.map { it.copy(attributes = it.attributes.toList(), methods = it.methods.toList()) },
                relations = diagram.relations.toList(),
                notes = diagram.notes.toList()
            ))
            diagram = undoStack.removeAt(undoStack.size - 1)
            isModified = true
            if (selectedClassId != null && diagram.classes.none { it.id == selectedClassId }) selectedClassId = null
            if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId }) selectedRelationId = null
            if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId }) selectedNoteId = null
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(diagram.copy(
                classes = diagram.classes.map { it.copy(attributes = it.attributes.toList(), methods = it.methods.toList()) },
                relations = diagram.relations.toList(),
                notes = diagram.notes.toList()
            ))
            diagram = redoStack.removeAt(redoStack.size - 1)
            isModified = true
            if (selectedClassId != null && diagram.classes.none { it.id == selectedClassId }) selectedClassId = null
            if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId }) selectedRelationId = null
            if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId }) selectedNoteId = null
        }
    }
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    fun addClass(x: Float, y: Float, name: String, type: ClassEntityType = ClassEntityType.CLASS) {
        saveState()
        diagram = diagram.copy(classes = diagram.classes + ClassEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            x = x,
            y = y,
            type = type,
            isAbstract = type == ClassEntityType.ABSTRACT_CLASS
        ))
        isModified = true
    }
    fun addRelation(sourceId: String, targetId: String, type: ClassRelationType) {
        saveState()
        diagram = diagram.copy(relations = diagram.relations + ClassRelation(
            id = UUID.randomUUID().toString(),
            sourceId = sourceId,
            targetId = targetId,
            type = type
        ))
        isModified = true
    }
    fun addNote(x: Float, y: Float, text: String) {
        saveState()
        diagram = diagram.copy(notes = diagram.notes + Note(
            id = UUID.randomUUID().toString(),
            text = text,
            x = x,
            y = y
        ))
        isModified = true
    }
    fun updateClass(classId: String, update: (ClassEntity) -> ClassEntity) {
        saveState()
        diagram = diagram.copy(classes = diagram.classes.map { if (it.id == classId) update(it) else it })
        isModified = true
    }
    fun updateClassWithoutSave(classId: String, update: (ClassEntity) -> ClassEntity) {
        diagram = diagram.copy(classes = diagram.classes.map { if (it.id == classId) update(it) else it })
        isModified = true
    }
    fun updateRelation(relationId: String, update: (ClassRelation) -> ClassRelation) {
        saveState()
        diagram = diagram.copy(relations = diagram.relations.map { if (it.id == relationId) update(it) else it })
        isModified = true
    }
    fun updateNote(noteId: String, update: (Note) -> Note) {
        saveState()
        diagram = diagram.copy(notes = diagram.notes.map { if (it.id == noteId) update(it) else it })
        isModified = true
    }
    fun updateNoteWithoutSave(noteId: String, update: (Note) -> Note) {
        diagram = diagram.copy(notes = diagram.notes.map { if (it.id == noteId) update(it) else it })
        isModified = true
    }
    fun saveDragStartState() {
        saveState()
    }
    fun deleteClass(classId: String) {
        saveState()
        diagram = diagram.copy(
            classes = diagram.classes.filter { it.id != classId },
            relations = diagram.relations.filter { it.sourceId != classId && it.targetId != classId }
        )
        if (selectedClassId == classId) selectedClassId = null
        isModified = true
    }
    fun deleteRelation(relationId: String) {
        saveState()
        diagram = diagram.copy(relations = diagram.relations.filter { it.id != relationId })
        if (selectedRelationId == relationId) selectedRelationId = null
        isModified = true
    }
    fun deleteNote(noteId: String) {
        saveState()
        diagram = diagram.copy(notes = diagram.notes.filter { it.id != noteId })
        if (selectedNoteId == noteId) selectedNoteId = null
        isModified = true
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
                attributes = it.attributes.filter { a -> a.id != memberId },
                methods = it.methods.filter { m -> m.id != memberId }
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
    fun clearSelection() {
        selectedClassId = null
        selectedRelationId = null
        selectedNoteId = null
    }
    fun loadDiagram(newDiagram: ClassDiagram, filePath: String?) {
        diagram = newDiagram
        currentFile = filePath
        isModified = false
        clearSelection()
    }
    fun newDiagram() {
        diagram = ClassDiagram(name = "Nuovo Diagramma delle Classi")
        currentFile = null
        isModified = false
        clearSelection()
    }
    fun markAsSaved(filePath: String) {
        currentFile = filePath
        isModified = false
    }
    fun updateDiagramName(newName: String) {
        diagram = diagram.copy(name = newName)
    }
    fun resetView() {
        zoom = 1.0f
        panOffset = Offset.Zero
    }
}
