package com.jder.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class SequenceDiagramState :
    DiagramEditorState<SequenceDiagram>({ SequenceDiagram(name = "Nuovo Diagramma di Sequenza") }) {
    var selectedLifelineId by mutableStateOf<String?>(null)
        private set

    var selectedMessageId by mutableStateOf<String?>(null)
        private set

    var selectedFragmentId by mutableStateOf<String?>(null)
        private set

    var selectedNoteId by mutableStateOf<String?>(null)
        private set

    var toolMode by mutableStateOf(SequenceToolMode.SELECT)
    var pendingMessageSource by mutableStateOf<SequenceAnchor?>(null)

    override val diagramName: String
        get() = diagram.name

    override val isEmpty: Boolean
        get() =
            diagram.lifelines.isEmpty() &&
                diagram.messages.isEmpty() &&
                diagram.fragments.isEmpty() &&
                diagram.notes.isEmpty()

    override val hasSelection: Boolean
        get() =
            selectedLifelineId != null ||
                selectedMessageId != null ||
                selectedFragmentId != null ||
                selectedNoteId != null

    override val isSelectTool: Boolean
        get() = toolMode == SequenceToolMode.SELECT

    override fun withName(diagram: SequenceDiagram, name: String) = diagram.copy(name = name)

    override fun pruneSelection() {
        if (selectedLifelineId != null && lifeline(selectedLifelineId) == null)
            selectedLifelineId = null
        if (selectedMessageId != null && message(selectedMessageId) == null)
            selectedMessageId = null
        if (selectedFragmentId != null && fragment(selectedFragmentId) == null)
            selectedFragmentId = null
        if (selectedNoteId != null && diagram.notes.none { it.id == selectedNoteId })
            selectedNoteId = null
        pendingMessageSource?.let {
            if (lifeline(it.lifelineId) == null) pendingMessageSource = null
        }
    }

    override fun resetToolMode() {
        toolMode = SequenceToolMode.SELECT
        pendingMessageSource = null
    }

    override fun deleteSelection(): String? {
        selectedLifelineId?.let {
            deleteLifeline(it)
            return "Linea di vita eliminata"
        }
        selectedMessageId?.let {
            deleteMessage(it)
            return "Messaggio eliminato"
        }
        selectedFragmentId?.let {
            deleteFragment(it)
            return "Frammento eliminato"
        }
        selectedNoteId?.let {
            deleteNote(it)
            return "Nota eliminata"
        }
        return null
    }

    fun lifeline(id: String?): SequenceLifeline? = diagram.lifelines.find { it.id == id }

    fun message(id: String?): SequenceMessage? = diagram.messages.find { it.id == id }

    fun fragment(id: String?): SequenceFragment? = diagram.fragments.find { it.id == id }

    fun addLifeline(type: SequenceLifelineType, x: Float, y: Float, name: String) {
        val lifeline =
            SequenceLifeline(
                id = UUID.randomUUID().toString(),
                name = name,
                x = x,
                y = y,
                type = type,
            )
        edit { it.copy(lifelines = it.lifelines + lifeline) }
        selectLifeline(lifeline.id)
    }

    fun addMessage(
        sourceId: String,
        targetId: String,
        y: Float,
        label: String,
        type: SequenceMessageType,
    ) {
        val message =
            SequenceMessage(
                id = UUID.randomUUID().toString(),
                sourceId = sourceId,
                targetId = targetId,
                y = y,
                label = label,
                type = type,
            )
        edit { it.copy(messages = it.messages + message) }
        selectMessage(message.id)
    }

    fun addFragment(x: Float, y: Float, type: SequenceFragmentType, condition: String) {
        val fragment =
            SequenceFragment(
                id = UUID.randomUUID().toString(),
                type = type,
                x = x,
                y = y,
                condition = condition,
            )
        edit { it.copy(fragments = it.fragments + fragment) }
        selectFragment(fragment.id)
    }

    fun addNote(x: Float, y: Float, text: String) {
        val newNote = Note(id = UUID.randomUUID().toString(), text = text, x = x, y = y)
        edit { it.copy(notes = it.notes + newNote) }
        selectNote(newNote.id)
    }

    fun updateLifeline(lifelineId: String, update: (SequenceLifeline) -> SequenceLifeline) {
        edit { d ->
            d.copy(lifelines = d.lifelines.map { if (it.id == lifelineId) update(it) else it })
        }
    }

    fun updateLifelineWithoutSave(
        lifelineId: String,
        update: (SequenceLifeline) -> SequenceLifeline,
    ) {
        editWithoutSave { d ->
            d.copy(lifelines = d.lifelines.map { if (it.id == lifelineId) update(it) else it })
        }
    }

    fun updateMessage(messageId: String, update: (SequenceMessage) -> SequenceMessage) {
        edit { d ->
            d.copy(messages = d.messages.map { if (it.id == messageId) update(it) else it })
        }
    }

    fun updateMessageWithoutSave(messageId: String, update: (SequenceMessage) -> SequenceMessage) {
        editWithoutSave { d ->
            d.copy(messages = d.messages.map { if (it.id == messageId) update(it) else it })
        }
    }

    fun reverseMessage(messageId: String) {
        updateMessage(messageId) { it.copy(sourceId = it.targetId, targetId = it.sourceId) }
    }

    fun updateFragment(fragmentId: String, update: (SequenceFragment) -> SequenceFragment) {
        edit { d ->
            d.copy(fragments = d.fragments.map { if (it.id == fragmentId) update(it) else it })
        }
    }

    fun updateFragmentWithoutSave(
        fragmentId: String,
        update: (SequenceFragment) -> SequenceFragment,
    ) {
        editWithoutSave { d ->
            d.copy(fragments = d.fragments.map { if (it.id == fragmentId) update(it) else it })
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

    fun deleteLifeline(lifelineId: String) {
        edit { d ->
            d.copy(
                lifelines = d.lifelines.filter { it.id != lifelineId },
                messages =
                    d.messages.filter { it.sourceId != lifelineId && it.targetId != lifelineId },
            )
        }
        pruneSelection()
    }

    fun deleteMessage(messageId: String) {
        edit { d -> d.copy(messages = d.messages.filter { it.id != messageId }) }
        if (selectedMessageId == messageId) selectedMessageId = null
    }

    fun deleteFragment(fragmentId: String) {
        edit { d -> d.copy(fragments = d.fragments.filter { it.id != fragmentId }) }
        if (selectedFragmentId == fragmentId) selectedFragmentId = null
    }

    fun deleteNote(noteId: String) {
        edit { d -> d.copy(notes = d.notes.filter { it.id != noteId }) }
        if (selectedNoteId == noteId) selectedNoteId = null
    }

    fun selectLifeline(lifelineId: String?) {
        clearSelection()
        selectedLifelineId = lifelineId
    }

    fun selectMessage(messageId: String?) {
        clearSelection()
        selectedMessageId = messageId
    }

    fun selectFragment(fragmentId: String?) {
        clearSelection()
        selectedFragmentId = fragmentId
    }

    fun selectNote(noteId: String?) {
        clearSelection()
        selectedNoteId = noteId
    }

    override fun clearSelection() {
        selectedLifelineId = null
        selectedMessageId = null
        selectedFragmentId = null
        selectedNoteId = null
    }
}
