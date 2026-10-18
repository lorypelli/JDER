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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jder.domain.model.Actor
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagramState
import com.jder.domain.model.UseCaseRelation

@Composable
fun UseCasePropertiesPanel(
    state: UseCaseDiagramState,
    onEditActor: () -> Unit,
    onEditUseCase: () -> Unit,
    onEditRelation: () -> Unit,
    onEditNote: () -> Unit,
    onEditSystemBoundary: () -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PropertiesPanelHeader(onClose)
        state.selectedActorId?.let { selId ->
            state.diagram.actors
                .find { it.id == selId }
                ?.let { ActorPropertiesContent(it, onEditActor) }
        }
        state.selectedUseCaseId?.let { selId ->
            state.diagram.useCases
                .find { it.id == selId }
                ?.let { UseCasePropertiesContent(it, onEditUseCase) }
        }
        state.selectedRelationId?.let { selId ->
            state.diagram.relations
                .find { it.id == selId }
                ?.let {
                    RelationPropertiesContent(
                        it,
                        findElementName(it.sourceId, state),
                        findElementName(it.targetId, state),
                        onEditRelation,
                    )
                }
        }
        state.selectedNoteId?.let { selId ->
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
        state.selectedSystemBoundaryId?.let { selId ->
            state.diagram.systemBoundaries
                .find { it.id == selId }
                ?.let { SystemBoundaryPropertiesContent(it, onEditSystemBoundary) }
        }
    }
}

private fun findElementName(id: String, state: UseCaseDiagramState): String {
    state.diagram.actors
        .find { it.id == id }
        ?.let {
            return it.name
        }
    state.diagram.useCases
        .find { it.id == id }
        ?.let {
            return it.name
        }
    return "Elemento sconosciuto"
}

@Composable
private fun ActorPropertiesContent(actor: Actor, onEdit: () -> Unit) {
    PropertyCard("Attore: ${actor.name}")
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
}

@Composable
private fun UseCasePropertiesContent(useCase: UseCase, onEdit: () -> Unit) {
    PropertyCard("Caso d'Uso: ${useCase.name}")
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
    DocumentationCard(useCase.documentation)
}

@Composable
private fun RelationPropertiesContent(
    relation: UseCaseRelation,
    sourceName: String,
    targetName: String,
    onEdit: () -> Unit,
) {
    PropertyCard("Relazione") {
        PropertyDetail("Tipo: ${relation.type.display}")
        PropertyDetail("Da: $sourceName")
        PropertyDetail("A: $targetName")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
}

@Composable
private fun SystemBoundaryPropertiesContent(boundary: SystemBoundary, onEdit: () -> Unit) {
    PropertyCard("Sistema: ${boundary.name}") {
        PropertyDetail("Dimensioni: ${boundary.width.toInt()} × ${boundary.height.toInt()} px")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton("Modifica Proprietà", Icons.Default.Edit, onEdit)
}
