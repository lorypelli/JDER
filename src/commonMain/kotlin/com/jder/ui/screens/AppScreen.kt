package com.jder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jder.domain.model.DiagramType
import com.jder.ui.components.DiagramTabRow
import com.jder.ui.dialogs.DiagramTypeDialog
import com.jder.ui.theme.ThemeState

@Composable
fun AppScreen(
    workspace: DiagramWorkspace,
    themeState: ThemeState,
    currentType: DiagramType?,
    onTypeChange: (DiagramType) -> Unit,
) {
    if (currentType == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        DiagramTypeDialog(themeState = themeState, onTypeSelected = onTypeChange)
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        DiagramTabRow(
            currentType = currentType,
            onTypeChange = onTypeChange,
            isModified = { workspace.state(it).isModified },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentType) {
                DiagramType.ER ->
                    ERDiagramScreen(
                        state = workspace.erState,
                        repository = workspace.erRepository,
                        themeState = themeState,
                    )
                DiagramType.RELATIONAL ->
                    RelationalDiagramScreen(
                        state = workspace.relationalState,
                        repository = workspace.relationalRepository,
                        themeState = themeState,
                    )
                DiagramType.CLASS ->
                    ClassDiagramScreen(
                        state = workspace.classState,
                        repository = workspace.classRepository,
                        themeState = themeState,
                    )
                DiagramType.USE_CASE ->
                    UseCaseDiagramScreen(
                        state = workspace.useCaseState,
                        repository = workspace.useCaseRepository,
                        themeState = themeState,
                    )
                DiagramType.SEQUENCE ->
                    SequenceDiagramScreen(
                        state = workspace.sequenceState,
                        repository = workspace.sequenceRepository,
                        themeState = themeState,
                    )
                DiagramType.LOGIC ->
                    LogicDiagramScreen(
                        state = workspace.logicState,
                        repository = workspace.logicRepository,
                        themeState = themeState,
                    )
                DiagramType.MEALY ->
                    StateMachineDiagramScreen(
                        state = workspace.mealyState,
                        repository = workspace.mealyRepository,
                        themeState = themeState,
                    )
                DiagramType.MOORE ->
                    StateMachineDiagramScreen(
                        state = workspace.mooreState,
                        repository = workspace.mooreRepository,
                        themeState = themeState,
                    )
            }
        }
    }
}
