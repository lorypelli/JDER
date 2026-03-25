package com.jder.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.Note
@Composable
fun ClassDiagramPropertiesPanel(
    state: ClassDiagramState,
    onEditClass: () -> Unit,
    onEditRelation: () -> Unit,
    onEditNote: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onDeleteMember: (String) -> Unit,
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
        state.selectedClassId?.let { selId ->
            state.diagram.classes.find { it.id == selId }?.let {
                ClassEntityPropertiesContent(it, onEditClass, onAddAttribute, onAddMethod, onDeleteMember)
            }
        }
        state.selectedRelationId?.let { selId ->
            state.diagram.relations.find { it.id == selId }?.let {
                ClassRelationPropertiesContent(it, state, onEditRelation)
            }
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes.find { it.id == selId }?.let {
                NotePropertiesContentClass(it, onEditNote)
            }
        }
    }
}
@Composable
private fun PropertyCardClass(
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
            Spacer(Modifier.height(8.dp))
            extraContent()
            Spacer(Modifier.height(8.dp))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Text(editLabel, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
@Composable
private fun ClassEntityPropertiesContent(
    entity: ClassEntity,
    onEdit: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onDeleteMember: (String) -> Unit
) {
    PropertyCardClass(
        title = "${entity.type.stereotype} ${entity.name}".trim(),
        editLabel = "Modifica Proprietà",
        onEdit = onEdit,
        extraContent = {
            Text("Tipo: ${entity.type.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            if (entity.isAbstract) {
                Text("Astratta: sì", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            if (entity.documentation.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Documentazione:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(entity.documentation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    )
    if (entity.attributes.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Attributi", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                entity.attributes.forEach { MemberRow(it, onDeleteMember) }
            }
        }
    }
    if (entity.methods.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Metodi", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                entity.methods.forEach { MemberRow(it, onDeleteMember) }
            }
        }
    }
    OutlinedButton(onClick = onAddAttribute, modifier = Modifier.fillMaxWidth()) {
        Text("+ Aggiungi Attributo")
    }
    OutlinedButton(onClick = onAddMethod, modifier = Modifier.fillMaxWidth()) {
        Text("+ Aggiungi Metodo")
    }
}
@Composable
private fun MemberRow(member: ClassMember, onDelete: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val label = buildString {
            append(member.visibility.symbol)
            append(" ")
            append(member.name)
            if (member.type.isNotEmpty()) append(": ${member.type}")
            if (member.isStatic) append(" [static]")
            if (member.isAbstract) append(" [abstract]")
        }
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onTertiaryContainer)
        IconButton(
            onClick = { onDelete(member.id) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Delete, "Elimina membro", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
    }
}
@Composable
private fun ClassRelationPropertiesContent(
    relation: ClassRelation,
    state: ClassDiagramState,
    onEdit: () -> Unit
) {
    val sourceName = state.diagram.classes.find { it.id == relation.sourceId }?.name ?: "?"
    val targetName = state.diagram.classes.find { it.id == relation.targetId }?.name ?: "?"
    PropertyCardClass(
        title = "Relazione: ${relation.type.display}",
        editLabel = "Modifica Relazione",
        onEdit = onEdit,
        extraContent = {
            Text("Da: $sourceName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("A: $targetName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            if (relation.sourceMultiplicity.isNotEmpty()) {
                Text("Molteplicità sorgente: ${relation.sourceMultiplicity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            if (relation.targetMultiplicity.isNotEmpty()) {
                Text("Molteplicità destinazione: ${relation.targetMultiplicity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            if (relation.label.isNotEmpty()) {
                Text("Etichetta: ${relation.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    )
}
@Composable
private fun NotePropertiesContentClass(note: Note, onEdit: () -> Unit) {
    PropertyCardClass(
        title = "Nota",
        editLabel = "Modifica Testo",
        onEdit = onEdit,
        extraContent = {
            if (note.text.isNotEmpty()) {
                Text(note.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    )
}
