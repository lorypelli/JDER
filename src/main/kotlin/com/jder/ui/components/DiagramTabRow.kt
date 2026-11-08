package com.jder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jder.domain.model.DiagramType
import com.jder.ui.strings.UiStrings

private const val WORD_JOINER = '\u2060'
private val tabMinHeight = 48.dp
private val tabHorizontalPadding = 8.dp
private val tabVerticalPadding = 10.dp

@Composable
fun DiagramTabRow(
    currentType: DiagramType,
    onTypeChange: (DiagramType) -> Unit,
    isModified: (DiagramType) -> Boolean,
    modifier: Modifier = Modifier,
) {
    key(UiStrings.language) {
        TabRow(selectedTabIndex = currentType.ordinal, modifier = modifier) {
            DiagramType.entries.forEach { type ->
                val label = type.tabLabel.replace("/", "/$WORD_JOINER")
                Tab(
                    selected = currentType == type,
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.focusProperties { canFocus = false },
                ) {
                    Box(
                        modifier =
                            Modifier.heightIn(min = tabMinHeight)
                                .padding(
                                    horizontal = tabHorizontalPadding,
                                    vertical = tabVerticalPadding,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isModified(type)) "$label •" else label,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
