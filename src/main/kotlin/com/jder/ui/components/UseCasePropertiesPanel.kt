package com.jder.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.Actor
import com.jder.domain.model.Note
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseState
@Composable
fun UseCasePropertiesPanel(
    state: UseCaseState,
    onEditActor: () -> Unit,
    onEditUseCase: () -> Unit,
    onEditRelation: () -> Unit,
    onEditNote: () -> Unit,
    onEditSystemBoundary: () -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Proprietà",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd).size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Chiudi pannello proprietà", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Divider()
        state.selectedActorId?.let { selId ->
            state.diagram.actors.find { it.id == selId }?.let { ActorPropertiesContent(it, onEditActor) }
        }
        state.selectedUseCaseId?.let { selId ->
            state.diagram.useCases.find { it.id == selId }?.let { UseCasePropertiesContent(it, onEditUseCase) }
        }
        state.selectedRelationId?.let { selId ->
            state.diagram.relations.find { it.id == selId }?.let {
                RelationPropertiesContent(it, findElementName(it.sourceId, state), findElementName(it.targetId, state), onEditRelation)
            }
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes.find { it.id == selId }?.let { NotePropertiesContentUC(it, onEditNote) }
        }
        state.selectedSystemBoundaryId?.let { selId ->
            state.diagram.systemBoundaries.find { it.id == selId }?.let { SystemBoundaryPropertiesContent(it, onEditSystemBoundary) }
        }
    }
}
private fun findElementName(id: String, state: UseCaseState): String {
    state.diagram.actors.find { it.id == id }?.let { return it.name }
    state.diagram.useCases.find { it.id == id }?.let { return it.name }
    return "Elemento sconosciuto"
}
@Composable
private fun PropertyCard(
    title: String,
    editLabel: String,
    onEdit: () -> Unit,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            extraContent()
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(editLabel)
    }
}
@Composable
private fun ActorPropertiesContent(actor: Actor, onEdit: () -> Unit) {
    PropertyCard(title = "Attore: ${actor.name}", editLabel = "Modifica Nome", onEdit = onEdit)
}
@Composable
private fun UseCasePropertiesContent(useCase: UseCase, onEdit: () -> Unit) {
    PropertyCard(title = "Caso d'Uso: ${useCase.name}", editLabel = "Modifica Proprietà", onEdit = onEdit) {
        if (useCase.documentation.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                useCase.documentation.take(60) + if (useCase.documentation.length > 60) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
private fun RelationPropertiesContent(
    relation: UseCaseRelation,
    sourceName: String,
    targetName: String,
    onEdit: () -> Unit
) {
    PropertyCard(title = "Relazione", editLabel = "Modifica Tipo", onEdit = onEdit) {
        Spacer(Modifier.height(4.dp))
        Text("Tipo: ${relation.type.display}", style = MaterialTheme.typography.bodyMedium)
        Text("Da: $sourceName", style = MaterialTheme.typography.bodySmall)
        Text("A: $targetName", style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
private fun NotePropertiesContentUC(note: Note, onEdit: () -> Unit) {
    PropertyCard(title = "Nota", editLabel = "Modifica Testo", onEdit = onEdit) {
        Spacer(Modifier.height(4.dp))
        Text(
            note.text.take(50) + if (note.text.length > 50) "..." else "",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
@Composable
private fun SystemBoundaryPropertiesContent(boundary: SystemBoundary, onEdit: () -> Unit) {
    PropertyCard(title = "Sistema: ${boundary.name}", editLabel = "Modifica Proprietà", onEdit = onEdit) {
        Spacer(Modifier.height(4.dp))
        Text("${boundary.width.toInt()} × ${boundary.height.toInt()} px", style = MaterialTheme.typography.bodySmall)
    }
}
