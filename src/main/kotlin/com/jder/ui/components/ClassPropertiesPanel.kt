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
import com.jder.strings.ClassStrings
import com.jder.strings.CommonStrings

@Composable
fun ClassPropertiesPanel(
    state: ClassDiagramState,
    onEditClass: () -> Unit,
    onEditRelation: () -> Unit,
    onEditNote: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onEditMember: (ClassMember) -> Unit,
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
                        onEditMember,
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
            state.diagram.notes
                .find { it.id == selId }
                ?.let { NotePropertiesContent(it, onEditNote) }
        }
    }
}

@Composable
private fun ClassEntityPropertiesContent(
    entity: ClassEntity,
    onEdit: () -> Unit,
    onAddAttribute: () -> Unit,
    onAddMethod: () -> Unit,
    onEditMember: (ClassMember) -> Unit,
    onDeleteMember: (memberId: String) -> Unit,
) {
    PropertyCard("${ClassStrings.CLASS}: ${entity.name}") {
        PropertyDetail("${CommonStrings.TYPE}: ${entity.type.display}")
        if (entity.isAbstract) {
            PropertyDetail("${ClassStrings.CLASS_ABSTRACT}: ${CommonStrings.yesNo(true)}")
        }
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
    PropertyOutlinedButton(CommonStrings.ADD_ATTRIBUTE, Icons.Default.Add, onAddAttribute)
    PropertyOutlinedButton(CommonStrings.ADD_METHOD, Icons.Default.Add, onAddMethod)
    MemberList("${CommonStrings.ATTRIBUTES}:", entity.attributes, onEditMember, onDeleteMember)
    MemberList("${ClassStrings.METHODS}:", entity.methods, onEditMember, onDeleteMember)
    DocumentationCard(entity.documentation)
}

@Composable
private fun MemberList(
    title: String,
    members: List<ClassMember>,
    onEdit: (ClassMember) -> Unit,
    onDelete: (memberId: String) -> Unit,
) {
    if (members.isEmpty()) return
    PropertyListTitle(title)
    members.forEach {
        PropertyItemCard(
            title = "${it.visibility.symbol} ${it.name}",
            subtitle = memberDetails(it),
            deleteDescription = ClassStrings.DELETE_MEMBER,
            onDelete = { onDelete(it.id) },
            editDescription = ClassStrings.EDIT_MEMBER,
            onEdit = { onEdit(it) },
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
    PropertyCard(CommonStrings.RELATION) {
        PropertyDetail("${CommonStrings.TYPE}: ${relation.type.display}")
        PropertyDetail("${CommonStrings.FROM}: $sourceName")
        PropertyDetail("${CommonStrings.TO}: $targetName")
        if (relation.sourceMultiplicity.isNotEmpty()) {
            PropertyDetail("${ClassStrings.SOURCE_MULTIPLICITY}: ${relation.sourceMultiplicity}")
        }
        if (relation.targetMultiplicity.isNotEmpty()) {
            PropertyDetail("${ClassStrings.TARGET_MULTIPLICITY}: ${relation.targetMultiplicity}")
        }
        if (relation.label.isNotEmpty()) PropertyDetail("${CommonStrings.LABEL}: ${relation.label}")
    }
    Spacer(Modifier.height(8.dp))
    PropertyButton(CommonStrings.EDIT_PROPERTIES, Icons.Default.Edit, onEdit)
}
