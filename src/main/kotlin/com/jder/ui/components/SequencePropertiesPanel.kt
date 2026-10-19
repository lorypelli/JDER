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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.SequenceDiagramState
import com.jder.domain.model.SequenceFragment
import com.jder.domain.model.SequenceFragmentType
import com.jder.domain.model.SequenceLifeline
import com.jder.domain.model.SequenceMessage

@Composable
fun SequencePropertiesPanel(
    state: SequenceDiagramState,
    onEditLifeline: () -> Unit,
    onEditMessage: () -> Unit,
    onEditFragment: () -> Unit,
    onEditNote: () -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.lifeline(state.selectedLifelineId)?.let {
            LifelinePropertiesContent(it, state.diagram, onEditLifeline)
        }
        state.message(state.selectedMessageId)?.let {
            MessagePropertiesContent(
                message = it,
                sourceName = state.lifeline(it.sourceId)?.name ?: "?",
                targetName = state.lifeline(it.targetId)?.name ?: "?",
                onEdit = onEditMessage,
                onReverse = { state.reverseMessage(it.id) },
            )
        }
        state.fragment(state.selectedFragmentId)?.let {
            FragmentPropertiesContent(it, onEditFragment)
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
    }
}

@Composable
private fun LifelinePropertiesContent(
    lifeline: SequenceLifeline,
    diagram: SequenceDiagram,
    onEdit: () -> Unit,
) {
    PropertyCard("Linea di Vita: ${lifeline.name}") {
        PropertyDetail("Tipo: ${lifeline.type.display}")
        PropertyDetail("Messaggi inviati: ${diagram.messages.count { it.sourceId == lifeline.id }}")
        PropertyDetail(
            "Messaggi ricevuti: ${diagram.messages.count { it.targetId == lifeline.id }}"
        )
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
    DocumentationCard(lifeline.documentation)
}

@Composable
private fun MessagePropertiesContent(
    message: SequenceMessage,
    sourceName: String,
    targetName: String,
    onEdit: () -> Unit,
    onReverse: () -> Unit,
) {
    PropertyCard(if (message.label.isBlank()) "Messaggio" else "Messaggio: ${message.label}") {
        PropertyDetail("Tipo: ${message.type.display}")
        PropertyDetail("Da: $sourceName")
        PropertyDetail("A: $targetName")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(
        "Inverti Direzione",
        Icons.Default.SwapHoriz,
        onReverse,
        enabled = !message.isSelfMessage,
    )
}

@Composable
private fun FragmentPropertiesContent(fragment: SequenceFragment, onEdit: () -> Unit) {
    PropertyCard("Frammento: ${fragment.type.keyword}") {
        PropertyDetail("Tipo: ${fragment.type.display}")
        if (fragment.condition.isNotBlank()) {
            PropertyDetail(
                if (fragment.type == SequenceFragmentType.REF) "Interazione: ${fragment.condition}"
                else "Condizione: [${fragment.condition}]"
            )
        }
        if (fragment.type.hasOperands) PropertyDetail("Operandi: ${fragment.operands.size + 1}")
        PropertyDetail("Dimensioni: ${fragment.width.toInt()} × ${fragment.height.toInt()} px")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
}
