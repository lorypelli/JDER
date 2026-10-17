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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation

@Composable
fun ClassDiagramPropertiesPanel(
    state: ClassDiagramState,
    onEditClass: () -> Unit,
    onEditRelation: () -> Unit,
    onEditNote: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onDeleteMember: (memberId: String) -> Unit,
    onClose: () -> Unit,
) {
  val scrollState = rememberScrollState()
  Column(
      modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    PropertiesPanelHeader(onClose)
    state.selectedClassId?.let { selId ->
      state.diagram.classes
          .find { it.id == selId }
          ?.let {
            ClassEntityPropertiesContent(
                it,
                onEditClass,
                onAddAttribute,
                onAddMethod,
                onDeleteMember,
            )
          }
    }
    state.selectedRelationId?.let { selId ->
      state.diagram.relations
          .find { it.id == selId }
          ?.let { ClassRelationPropertiesContent(it, state, onEditRelation) }
    }
    state.selectedNoteId?.let { selId ->
      state.diagram.notes.find { it.id == selId }?.let { NotePropertiesContent(it, onEditNote) }
    }
  }
}

@Composable
private fun ClassEntityPropertiesContent(
    entity: ClassEntity,
    onEdit: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onDeleteMember: (memberId: String) -> Unit,
) {
  PropertyCard("Classe: ${entity.name}") {
    PropertyDetail("Tipo: ${entity.type.display}")
    if (entity.isAbstract) PropertyDetail("Astratta: sì")
  }
  Spacer(Modifier.height(8.dp))
  PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
  PropertyOutlinedButton("Aggiungi Attributo", Icons.Default.Add, onAddAttribute)
  PropertyOutlinedButton("Aggiungi Metodo", Icons.Default.Add, onAddMethod)
  MemberList("Attributi:", entity.attributes, onDeleteMember)
  MemberList("Metodi:", entity.methods, onDeleteMember)
  DocumentationCard(entity.documentation)
}

@Composable
private fun MemberList(
    title: String,
    members: List<ClassMember>,
    onDelete: (memberId: String) -> Unit,
) {
  if (members.isEmpty()) return
  PropertyListTitle(title)
  members.forEach {
    PropertyItemCard(
        title = "${it.visibility.symbol} ${it.name}",
        subtitle = memberDetails(it),
        deleteDescription = "Elimina membro",
        onDelete = { onDelete(it.id) },
    )
  }
}

private fun memberDetails(member: ClassMember): String =
    buildString {
          append(member.type)
          if (member.isStatic) append(" [static]")
          if (member.isAbstract) append(" [abstract]")
        }
        .trim()

@Composable
private fun ClassRelationPropertiesContent(
    relation: ClassRelation,
    state: ClassDiagramState,
    onEdit: () -> Unit,
) {
  val sourceName = state.diagram.classes.find { it.id == relation.sourceId }?.name ?: "?"
  val targetName = state.diagram.classes.find { it.id == relation.targetId }?.name ?: "?"
  PropertyCard("Relazione") {
    PropertyDetail("Tipo: ${relation.type.display}")
    PropertyDetail("Da: $sourceName")
    PropertyDetail("A: $targetName")
    if (relation.sourceMultiplicity.isNotEmpty()) {
      PropertyDetail("Molteplicità sorgente: ${relation.sourceMultiplicity}")
    }
    if (relation.targetMultiplicity.isNotEmpty()) {
      PropertyDetail("Molteplicità destinazione: ${relation.targetMultiplicity}")
    }
    if (relation.label.isNotEmpty()) PropertyDetail("Etichetta: ${relation.label}")
  }
  Spacer(Modifier.height(8.dp))
  PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
}
