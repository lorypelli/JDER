package com.jder.domain.model
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import java.util.UUID
class UseCaseState {
    var diagram by mutableStateOf(UseCaseDiagram(name = "Nuovo Diagramma Casi d'Uso"))
        private set
    var selectedActorId by mutableStateOf<String?>(null)
        private set
    var selectedUseCaseId by mutableStateOf<String?>(null)
        private set
    var selectedRelationId by mutableStateOf<String?>(null)
        private set
    var selectedNoteId by mutableStateOf<String?>(null)
        private set
    var selectedSystemBoundaryId by mutableStateOf<String?>(null)
        private set
    var toolMode by mutableStateOf(UseCaseToolMode.SELECT)
    var pendingRelationSourceId by mutableStateOf<String?>(null)
    var isModified by mutableStateOf(false)
        private set
    var currentFile by mutableStateOf<String?>(null)
    var zoom by mutableStateOf(1.0f)
    var panOffset by mutableStateOf(Offset.Zero)
    private val undoStack = mutableListOf<UseCaseDiagram>()
    private val redoStack = mutableListOf<UseCaseDiagram>()
    private val maxUndoSize = 50
    private fun saveState() {
        undoStack.add(diagram.copy(
            actors = diagram.actors.toList(),
            useCases = diagram.useCases.toList(),
            relations = diagram.relations.toList(),
            notes = diagram.notes.toList(),
            systemBoundaries = diagram.systemBoundaries.toList()
        ))
        if (undoStack.size > maxUndoSize) undoStack.removeAt(0)
        redoStack.clear()
    }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(diagram.copy(
                actors = diagram.actors.toList(),
                useCases = diagram.useCases.toList(),
                relations = diagram.relations.toList(),
                notes = diagram.notes.toList(),
                systemBoundaries = diagram.systemBoundaries.toList()
            ))
            diagram = undoStack.removeAt(undoStack.size - 1)
            isModified = true
            if (selectedActorId != null && diagram.actors.none { it.id == selectedActorId }) selectedActorId = null
            if (selectedUseCaseId != null && diagram.useCases.none { it.id == selectedUseCaseId }) selectedUseCaseId = null
            if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId }) selectedRelationId = null
            if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId }) selectedNoteId = null
            if (selectedSystemBoundaryId != null && diagram.systemBoundaries.none { it.id == selectedSystemBoundaryId }) selectedSystemBoundaryId = null
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(diagram.copy(
                actors = diagram.actors.toList(),
                useCases = diagram.useCases.toList(),
                relations = diagram.relations.toList(),
                notes = diagram.notes.toList(),
                systemBoundaries = diagram.systemBoundaries.toList()
            ))
            diagram = redoStack.removeAt(redoStack.size - 1)
            isModified = true
            if (selectedActorId != null && diagram.actors.none { it.id == selectedActorId }) selectedActorId = null
            if (selectedUseCaseId != null && diagram.useCases.none { it.id == selectedUseCaseId }) selectedUseCaseId = null
            if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId }) selectedRelationId = null
            if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId }) selectedNoteId = null
            if (selectedSystemBoundaryId != null && diagram.systemBoundaries.none { it.id == selectedSystemBoundaryId }) selectedSystemBoundaryId = null
        }
    }
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    fun addActor(x: Float, y: Float, name: String) {
        saveState()
        diagram = diagram.copy(actors = diagram.actors + Actor(id = UUID.randomUUID().toString(), name = name, x = x, y = y))
        isModified = true
    }
    fun addUseCase(x: Float, y: Float, name: String) {
        saveState()
        diagram = diagram.copy(useCases = diagram.useCases + UseCase(id = UUID.randomUUID().toString(), name = name, x = x, y = y))
        isModified = true
    }
    fun addRelation(sourceId: String, targetId: String, type: UseCaseRelationType) {
        saveState()
        diagram = diagram.copy(relations = diagram.relations + UseCaseRelation(id = UUID.randomUUID().toString(), sourceId = sourceId, targetId = targetId, type = type))
        isModified = true
    }
    fun addNote(x: Float, y: Float, text: String) {
        saveState()
        diagram = diagram.copy(notes = diagram.notes + Note(id = UUID.randomUUID().toString(), text = text, x = x, y = y))
        isModified = true
    }
    fun addSystemBoundary(x: Float, y: Float, name: String) {
        saveState()
        diagram = diagram.copy(systemBoundaries = diagram.systemBoundaries + SystemBoundary(id = UUID.randomUUID().toString(), name = name, x = x, y = y))
        isModified = true
    }
    fun updateActor(actorId: String, update: (Actor) -> Actor) {
        saveState()
        diagram = diagram.copy(actors = diagram.actors.map { if (it.id == actorId) update(it) else it })
        isModified = true
    }
    fun updateActorWithoutSave(actorId: String, update: (Actor) -> Actor) {
        diagram = diagram.copy(actors = diagram.actors.map { if (it.id == actorId) update(it) else it })
        isModified = true
    }
    fun updateUseCase(useCaseId: String, update: (UseCase) -> UseCase) {
        saveState()
        diagram = diagram.copy(useCases = diagram.useCases.map { if (it.id == useCaseId) update(it) else it })
        isModified = true
    }
    fun updateUseCaseWithoutSave(useCaseId: String, update: (UseCase) -> UseCase) {
        diagram = diagram.copy(useCases = diagram.useCases.map { if (it.id == useCaseId) update(it) else it })
        isModified = true
    }
    fun updateRelation(relationId: String, update: (UseCaseRelation) -> UseCaseRelation) {
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
    fun updateSystemBoundary(boundaryId: String, update: (SystemBoundary) -> SystemBoundary) {
        saveState()
        diagram = diagram.copy(systemBoundaries = diagram.systemBoundaries.map { if (it.id == boundaryId) update(it) else it })
        isModified = true
    }
    fun updateSystemBoundaryWithoutSave(boundaryId: String, update: (SystemBoundary) -> SystemBoundary) {
        diagram = diagram.copy(systemBoundaries = diagram.systemBoundaries.map { if (it.id == boundaryId) update(it) else it })
        isModified = true
    }
    fun saveDragStartState() {
        saveState()
    }
    fun deleteActor(actorId: String) {
        saveState()
        diagram = diagram.copy(
            actors = diagram.actors.filter { it.id != actorId },
            relations = diagram.relations.filter { it.sourceId != actorId && it.targetId != actorId }
        )
        if (selectedActorId == actorId) selectedActorId = null
        isModified = true
    }
    fun deleteUseCase(useCaseId: String) {
        saveState()
        diagram = diagram.copy(
            useCases = diagram.useCases.filter { it.id != useCaseId },
            relations = diagram.relations.filter { it.sourceId != useCaseId && it.targetId != useCaseId }
        )
        if (selectedUseCaseId == useCaseId) selectedUseCaseId = null
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
    fun deleteSystemBoundary(boundaryId: String) {
        saveState()
        diagram = diagram.copy(systemBoundaries = diagram.systemBoundaries.filter { it.id != boundaryId })
        if (selectedSystemBoundaryId == boundaryId) selectedSystemBoundaryId = null
        isModified = true
    }
    fun selectActor(actorId: String?) {
        selectedActorId = actorId
        selectedUseCaseId = null
        selectedRelationId = null
        selectedNoteId = null
        selectedSystemBoundaryId = null
    }
    fun selectUseCase(useCaseId: String?) {
        selectedUseCaseId = useCaseId
        selectedActorId = null
        selectedRelationId = null
        selectedNoteId = null
        selectedSystemBoundaryId = null
    }
    fun selectRelation(relationId: String?) {
        selectedRelationId = relationId
        selectedActorId = null
        selectedUseCaseId = null
        selectedNoteId = null
        selectedSystemBoundaryId = null
    }
    fun selectNote(noteId: String?) {
        selectedNoteId = noteId
        selectedActorId = null
        selectedUseCaseId = null
        selectedRelationId = null
        selectedSystemBoundaryId = null
    }
    fun selectSystemBoundary(boundaryId: String?) {
        selectedSystemBoundaryId = boundaryId
        selectedActorId = null
        selectedUseCaseId = null
        selectedRelationId = null
        selectedNoteId = null
    }
    fun clearSelection() {
        selectedActorId = null
        selectedUseCaseId = null
        selectedRelationId = null
        selectedNoteId = null
        selectedSystemBoundaryId = null
    }
    fun loadDiagram(newDiagram: UseCaseDiagram, filePath: String?) {
        diagram = newDiagram
        currentFile = filePath
        isModified = false
        clearSelection()
    }
    fun newDiagram() {
        diagram = UseCaseDiagram(name = "Nuovo Diagramma Casi d'Uso")
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
