package com.jder.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class UseCaseDiagramState :
    DiagramEditorState<UseCaseDiagram>({ UseCaseDiagram(name = "Nuovo Diagramma Casi d'Uso") }) {
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

  override val diagramName: String
    get() = diagram.name

  override val isEmpty: Boolean
    get() =
        diagram.actors.isEmpty() &&
            diagram.useCases.isEmpty() &&
            diagram.relations.isEmpty() &&
            diagram.notes.isEmpty() &&
            diagram.systemBoundaries.isEmpty()

  override val hasSelection: Boolean
    get() =
        selectedActorId != null ||
            selectedUseCaseId != null ||
            selectedRelationId != null ||
            selectedNoteId != null ||
            selectedSystemBoundaryId != null

  override val isSelectTool: Boolean
    get() = toolMode == UseCaseToolMode.SELECT

  override fun withName(diagram: UseCaseDiagram, name: String) = diagram.copy(name = name)

  override fun pruneSelection() {
    if (selectedActorId != null && diagram.actors.none { it.id == selectedActorId })
        selectedActorId = null
    if (selectedUseCaseId != null && diagram.useCases.none { it.id == selectedUseCaseId })
        selectedUseCaseId = null
    if (selectedRelationId != null && diagram.relations.none { it.id == selectedRelationId })
        selectedRelationId = null
    if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId })
        selectedNoteId = null
    if (
        selectedSystemBoundaryId != null &&
            diagram.systemBoundaries.none { it.id == selectedSystemBoundaryId }
    )
        selectedSystemBoundaryId = null
  }

  override fun resetToolMode() {
    toolMode = UseCaseToolMode.SELECT
    pendingRelationSourceId = null
  }

  override fun deleteSelection(): String? {
    selectedActorId?.let {
      deleteActor(it)
      return "Attore eliminato"
    }
    selectedUseCaseId?.let {
      deleteUseCase(it)
      return "Caso d'uso eliminato"
    }
    selectedRelationId?.let {
      deleteRelation(it)
      return "Relazione eliminata"
    }
    selectedNoteId?.let {
      deleteNote(it)
      return "Nota eliminata"
    }
    selectedSystemBoundaryId?.let {
      deleteSystemBoundary(it)
      return "Sistema eliminato"
    }
    return null
  }

  fun addActor(x: Float, y: Float, name: String) {
    val actor = Actor(id = UUID.randomUUID().toString(), name = name, x = x, y = y)
    edit { it.copy(actors = it.actors + actor) }
    selectActor(actor.id)
  }

  fun addUseCase(x: Float, y: Float, name: String) {
    val useCase = UseCase(id = UUID.randomUUID().toString(), name = name, x = x, y = y)
    edit { it.copy(useCases = it.useCases + useCase) }
    selectUseCase(useCase.id)
  }

  fun addRelation(sourceId: String, targetId: String, type: UseCaseRelationType) {
    val relation =
        UseCaseRelation(
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

  fun addSystemBoundary(x: Float, y: Float, name: String) {
    val boundary = SystemBoundary(id = UUID.randomUUID().toString(), name = name, x = x, y = y)
    edit { it.copy(systemBoundaries = it.systemBoundaries + boundary) }
    selectSystemBoundary(boundary.id)
  }

  fun updateActor(actorId: String, update: (Actor) -> Actor) {
    edit { d -> d.copy(actors = d.actors.map { if (it.id == actorId) update(it) else it }) }
  }

  fun updateActorWithoutSave(actorId: String, update: (Actor) -> Actor) {
    editWithoutSave { d ->
      d.copy(actors = d.actors.map { if (it.id == actorId) update(it) else it })
    }
  }

  fun updateUseCase(useCaseId: String, update: (UseCase) -> UseCase) {
    edit { d -> d.copy(useCases = d.useCases.map { if (it.id == useCaseId) update(it) else it }) }
  }

  fun updateUseCaseWithoutSave(useCaseId: String, update: (UseCase) -> UseCase) {
    editWithoutSave { d ->
      d.copy(useCases = d.useCases.map { if (it.id == useCaseId) update(it) else it })
    }
  }

  fun updateRelation(relationId: String, update: (UseCaseRelation) -> UseCaseRelation) {
    edit { d ->
      d.copy(relations = d.relations.map { if (it.id == relationId) update(it) else it })
    }
  }

  fun updateNote(noteId: String, update: (Note) -> Note) {
    edit { d -> d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it }) }
  }

  fun updateNoteWithoutSave(noteId: String, update: (Note) -> Note) {
    editWithoutSave { d -> d.copy(notes = d.notes.map { if (it.id == noteId) update(it) else it }) }
  }

  fun updateSystemBoundary(boundaryId: String, update: (SystemBoundary) -> SystemBoundary) {
    edit { d ->
      d.copy(
          systemBoundaries = d.systemBoundaries.map { if (it.id == boundaryId) update(it) else it }
      )
    }
  }

  fun updateSystemBoundaryWithoutSave(
      boundaryId: String,
      update: (SystemBoundary) -> SystemBoundary,
  ) {
    editWithoutSave { d ->
      d.copy(
          systemBoundaries = d.systemBoundaries.map { if (it.id == boundaryId) update(it) else it }
      )
    }
  }

  fun deleteActor(actorId: String) {
    edit { d ->
      d.copy(
          actors = d.actors.filter { it.id != actorId },
          relations = d.relations.filter { it.sourceId != actorId && it.targetId != actorId },
      )
    }
    if (selectedActorId == actorId) selectedActorId = null
    if (pendingRelationSourceId == actorId) pendingRelationSourceId = null
  }

  fun deleteUseCase(useCaseId: String) {
    edit { d ->
      d.copy(
          useCases = d.useCases.filter { it.id != useCaseId },
          relations = d.relations.filter { it.sourceId != useCaseId && it.targetId != useCaseId },
      )
    }
    if (selectedUseCaseId == useCaseId) selectedUseCaseId = null
    if (pendingRelationSourceId == useCaseId) pendingRelationSourceId = null
  }

  fun deleteRelation(relationId: String) {
    edit { d -> d.copy(relations = d.relations.filter { it.id != relationId }) }
    if (selectedRelationId == relationId) selectedRelationId = null
  }

  fun deleteNote(noteId: String) {
    edit { d -> d.copy(notes = d.notes.filter { it.id != noteId }) }
    if (selectedNoteId == noteId) selectedNoteId = null
  }

  fun deleteSystemBoundary(boundaryId: String) {
    edit { d -> d.copy(systemBoundaries = d.systemBoundaries.filter { it.id != boundaryId }) }
    if (selectedSystemBoundaryId == boundaryId) selectedSystemBoundaryId = null
  }

  fun selectActor(actorId: String?) {
    clearSelection()
    selectedActorId = actorId
  }

  fun selectUseCase(useCaseId: String?) {
    clearSelection()
    selectedUseCaseId = useCaseId
  }

  fun selectRelation(relationId: String?) {
    clearSelection()
    selectedRelationId = relationId
  }

  fun selectNote(noteId: String?) {
    clearSelection()
    selectedNoteId = noteId
  }

  fun selectSystemBoundary(boundaryId: String?) {
    clearSelection()
    selectedSystemBoundaryId = boundaryId
  }

  override fun clearSelection() {
    selectedActorId = null
    selectedUseCaseId = null
    selectedRelationId = null
    selectedNoteId = null
    selectedSystemBoundaryId = null
  }
}
