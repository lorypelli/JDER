package com.jder.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jder.domain.model.DiagramType
import com.jder.strings.DiagramTypeStrings
import com.jder.ui.components.ToolbarPaletteTheme
import com.jder.ui.components.diagramTypeKeys
import com.jder.ui.components.logError
import com.jder.ui.theme.ThemeState
import com.jder.ui.utils.isCompactWidth
import com.jder.ui.utils.loadImageResource
import com.jder.ui.utils.shortcutLabel

private const val WIDE_COLUMNS = 4
private const val NARROW_COLUMNS = 3
private const val COMPACT_COLUMNS = 2
private val dialogMaxWidth = 1100.dp
private val wideLayoutMinWidth = 1040.dp
private val dialogPadding = 24.dp
private val compactDialogPadding = 16.dp
private const val NO_BREAK_SPACE = '\u00A0'

@Composable
fun DiagramTypeDialog(themeState: ThemeState, onTypeSelected: (DiagramType) -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        val focusRequester = remember { FocusRequester() }
        val isCompact = isCompactWidth()
        val padding = if (isCompact) compactDialogPadding else dialogPadding
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        Surface(
            modifier =
                Modifier.padding(padding)
                    .widthIn(max = dialogMaxWidth)
                    .focusRequester(focusRequester)
                    .focusTarget()
                    .onPreviewKeyEvent {
                        val typeIndex = diagramTypeKeys.indexOf(it.key)
                        val isShortcut =
                            it.type == KeyEventType.KeyDown &&
                                (it.isCtrlPressed || it.isMetaPressed) &&
                                typeIndex >= 0
                        if (isShortcut) onTypeSelected(DiagramType.entries[typeIndex])
                        isShortcut
                    },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            BoxWithConstraints {
                val columns =
                    when {
                        isCompact -> COMPACT_COLUMNS
                        maxWidth >= wideLayoutMinWidth -> WIDE_COLUMNS
                        else -> NARROW_COLUMNS
                    }
                DiagramTypeGrid(themeState, columns, isCompact, padding, onTypeSelected)
            }
        }
    }
}

@Composable
private fun DiagramTypeGrid(
    themeState: ThemeState,
    columns: Int,
    isCompact: Boolean,
    padding: Dp,
    onTypeSelected: (DiagramType) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isCompact) {
            DiagramTypeTitle()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarPaletteTheme(themeState)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiagramTypeTitle(modifier = Modifier.weight(1f))
                ToolbarPaletteTheme(themeState)
            }
        }
        DiagramType.entries.chunked(columns).forEach { rowTypes ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowTypes.forEach { type ->
                    DiagramTypeCard(
                        type = type,
                        isDarkTheme = themeState.isDarkTheme,
                        isCompact = isCompact,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowTypes.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DiagramTypeTitle(modifier: Modifier = Modifier) {
    Text(
        text = DiagramTypeStrings.CHOOSE_DIAGRAM_TYPE,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
private fun DiagramTypeCard(
    type: DiagramType,
    isDarkTheme: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenshotPath = "screenshots/${type.key}-${if (isDarkTheme) "dark" else "light"}.png"
    val screenshot by
        produceState<ImageBitmap?>(null, screenshotPath) {
            value =
                loadImageResource(screenshotPath) {
                    logError(DiagramTypeStrings.previewLoadFailed(screenshotPath), it)
                }
        }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = if (isHovered) 2.dp else 1.dp,
                color =
                    if (isHovered) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
            ),
        interactionSource = interactionSource,
    ) {
        Column {
            val imageModifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            val image = screenshot
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Medium,
                )
            } else {
                Box(modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val labelLines = if (isCompact) 2 else 1
                Text(
                    text =
                        if (isCompact) type.tabLabel
                        else type.tabLabel.replace(' ', NO_BREAK_SPACE),
                    style = MaterialTheme.typography.titleMedium,
                    minLines = labelLines,
                    maxLines = labelLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!isCompact) {
                    Text(
                        text = shortcutLabel("${type.ordinal + 1}"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
