package com.jder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jder.data.ClassDiagramRepository
import com.jder.data.DiagramRepository
import com.jder.data.LogicDiagramRepository
import com.jder.data.UseCaseRepository
import com.jder.domain.model.ClassDiagramState
import com.jder.domain.model.DiagramState
import com.jder.domain.model.DiagramType
import com.jder.domain.model.LogicDiagramState
import com.jder.domain.model.UseCaseState
import com.jder.ui.components.DiagramTabRow
import com.jder.ui.theme.ThemeState

@Composable
fun AppScreen(
    erState: DiagramState,
    useCaseState: UseCaseState,
    classState: ClassDiagramState,
    logicState: LogicDiagramState,
    repository: DiagramRepository,
    useCaseRepository: UseCaseRepository,
    classRepository: ClassDiagramRepository,
    logicRepository: LogicDiagramRepository,
    themeState: ThemeState,
) {
  var currentType by remember { mutableStateOf(DiagramType.ER) }
  Column(modifier = Modifier.fillMaxSize()) {
    DiagramTabRow(
        currentType = currentType,
        onTypeChange = { currentType = it },
        isModified = { type ->
          when (type) {
            DiagramType.ER -> erState.isModified
            DiagramType.CLASS_DIAGRAM -> classState.isModified
            DiagramType.USE_CASE -> useCaseState.isModified
            DiagramType.LOGIC -> logicState.isModified
          }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      when (currentType) {
        DiagramType.ER ->
            MainScreen(state = erState, repository = repository, themeState = themeState)
        DiagramType.CLASS_DIAGRAM ->
            ClassDiagramScreen(
                state = classState,
                repository = classRepository,
                themeState = themeState,
            )
        DiagramType.USE_CASE ->
            UseCaseScreen(
                state = useCaseState,
                repository = useCaseRepository,
                themeState = themeState,
            )
        DiagramType.LOGIC ->
            LogicDiagramScreen(
                state = logicState,
                repository = logicRepository,
                themeState = themeState,
            )
      }
    }
  }
}
