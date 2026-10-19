package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.jder.domain.model.RelationalColumn
import com.jder.domain.model.RelationalDiagram
import com.jder.domain.model.RelationalTable
import com.jder.domain.model.foreignKeys

private const val CELL_PADDING = 6f
private const val BADGE_GAP = 3f

internal fun DrawScope.drawRelationalDiagram(
    diagram: RelationalDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    selectedId: String? = null,
    hoveredId: String? = null,
    pendingSourceId: String? = null,
) {
    val foreignKeys = diagram.foreignKeys()
    val highlightedColumnIds =
        foreignKeys
            .find { it.column.id == selectedId }
            ?.let { setOf(it.column.id, it.referencedColumn.id) }
            .orEmpty()
    foreignKeys.forEach {
        drawForeignKey(
            points = foreignKeyRoute(diagram, it),
            isSelected = it.column.id == selectedId,
            isHovered = it.column.id == hoveredId,
            palette = palette,
        )
    }
    diagram.tables.forEach {
        drawRelationalTable(
            table = it,
            isSelected = it.id == selectedId,
            isHovered = it.id == hoveredId,
            isPendingSource = it.id == pendingSourceId,
            highlightedColumnIds = highlightedColumnIds,
            palette = palette,
            textMeasurer = textMeasurer,
        )
    }
    diagram.notes.forEach {
        drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
    }
}

internal fun relationalDiagramBounds(diagram: RelationalDiagram): Rect? {
    val bounds = BoundsBuilder()
    diagram.tables.forEach { bounds.include(it.bounds()) }
    diagram.foreignKeys().forEach { foreignKeyRoute(diagram, it).forEach(bounds::include) }
    diagram.notes.forEach { bounds.include(it.bounds()) }
    return bounds.build()
}

private fun DrawScope.drawForeignKey(
    points: List<Offset>,
    isSelected: Boolean,
    isHovered: Boolean,
    palette: DiagramPalette,
) {
    val color = if (isSelected) palette.selected else palette.outline
    val strokeWidth =
        when {
            isSelected -> 2.5f
            isHovered -> 2.25f
            else -> 1.5f
        }
    val path =
        Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
    )
    drawCircle(color = color, radius = 3f, center = points.first())
    val tip = points.last()
    val lastSegment = tip - points[points.size - 2]
    drawArrowHead(tip, lastSegment / lastSegment.getDistance(), color, null, size = 10f)
}

private fun DrawScope.drawRelationalTable(
    table: RelationalTable,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
    highlightedColumnIds: Set<String>,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
) {
    val bounds = table.bounds()
    val selectedColor = palette.selected
    val borderColor =
        when {
            isSelected -> selectedColor
            isPendingSource -> selectedColor.copy(alpha = 0.8f)
            else -> palette.primary
        }
    val strokeWidth =
        when {
            isSelected || isPendingSource -> 3f
            isHovered -> 2.25f
            else -> 1.5f
        }
    if (isSelected || isPendingSource) {
        val halo = bounds.inflate(4f)
        drawRoundRect(
            color = selectedColor.copy(alpha = if (isSelected) 0.12f else 0.1f),
            topLeft = halo.topLeft,
            size = halo.size,
            cornerRadius = CornerRadius(4f),
            style = Fill,
        )
        drawRoundRect(
            color = selectedColor,
            topLeft = halo.topLeft,
            size = halo.size,
            cornerRadius = CornerRadius(4f),
            style =
                if (isSelected) Stroke(width = 2.5f)
                else
                    Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
        )
    }
    drawRect(color = palette.surface, topLeft = bounds.topLeft, size = bounds.size, style = Fill)
    when {
        isSelected -> selectedColor.copy(alpha = 0.08f)
        isPendingSource -> selectedColor.copy(alpha = 0.06f)
        isHovered -> palette.primary.copy(alpha = 0.05f)
        else -> null
    }?.let { drawRect(color = it, topLeft = bounds.topLeft, size = bounds.size, style = Fill) }
    drawRect(
        color = borderColor.copy(alpha = 0.12f),
        topLeft = bounds.topLeft,
        size = Size(table.width, RELATIONAL_HEADER_HEIGHT),
        style = Fill,
    )
    table.columns.forEachIndexed { index, column ->
        if (column.id in highlightedColumnIds) {
            drawRect(
                color = selectedColor.copy(alpha = 0.16f),
                topLeft = Offset(table.x, table.rowTop(index)),
                size = Size(table.width, RELATIONAL_ROW_HEIGHT),
                style = Fill,
            )
        }
    }
    drawRect(
        color = borderColor,
        topLeft = bounds.topLeft,
        size = bounds.size,
        style = Stroke(width = strokeWidth),
    )
    val title =
        textMeasurer.measureLine(
            text = table.name,
            style = TextStyle(color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            maxWidth = table.width - 16f,
        )
    drawTextCentered(
        title,
        Offset(table.x + table.width / 2, table.y + RELATIONAL_HEADER_HEIGHT / 2),
    )
    drawLine(
        color = borderColor,
        start = Offset(table.x, table.y + RELATIONAL_HEADER_HEIGHT),
        end = Offset(table.x + table.width, table.y + RELATIONAL_HEADER_HEIGHT),
        strokeWidth = strokeWidth,
    )
    val badges = table.columns.map { textMeasurer.columnBadges(it, palette) }
    val badgesWidth = badges.maxOfOrNull(::badgeRowWidth) ?: 0f
    val gutterWidth = if (badgesWidth > 0f) badgesWidth + 2 * CELL_PADDING else CELL_PADDING
    table.columns.forEachIndexed { index, column ->
        val top = table.rowTop(index)
        val centerY = top + RELATIONAL_ROW_HEIGHT / 2
        if (index > 0) {
            drawLine(
                color = palette.outline.copy(alpha = 0.25f),
                start = Offset(table.x + 1f, top),
                end = Offset(table.x + table.width - 1f, top),
                strokeWidth = 1f,
            )
        }
        var badgeX = table.x + CELL_PADDING
        badges[index].forEach {
            drawText(it, topLeft = Offset(badgeX, centerY - it.size.height / 2f))
            badgeX += it.size.width + BADGE_GAP
        }
        val type =
            column.type
                .takeIf { it.isNotBlank() }
                ?.let {
                    textMeasurer.measureLine(
                        text = it,
                        style =
                            TextStyle(color = palette.text.copy(alpha = 0.6f), fontSize = 11.sp),
                        maxWidth = (table.width - gutterWidth) / 2,
                    )
                }
        val name =
            textMeasurer.measureLine(
                text = if (column.isNullable) "${column.name}*" else column.name,
                style =
                    TextStyle(
                        color = palette.text,
                        fontSize = 12.sp,
                        fontWeight =
                            if (column.isPrimaryKey) FontWeight.SemiBold else FontWeight.Normal,
                        textDecoration =
                            if (column.isPrimaryKey) TextDecoration.Underline
                            else TextDecoration.None,
                    ),
                maxWidth =
                    table.width -
                        gutterWidth -
                        CELL_PADDING -
                        (type?.let { it.size.width + CELL_PADDING } ?: 0f),
            )
        drawText(name, topLeft = Offset(table.x + gutterWidth, centerY - name.size.height / 2f))
        type?.let {
            drawText(
                it,
                topLeft =
                    Offset(
                        table.x + table.width - CELL_PADDING - it.size.width,
                        centerY - it.size.height / 2f,
                    ),
            )
        }
    }
}

private fun TextMeasurer.columnBadges(
    column: RelationalColumn,
    palette: DiagramPalette,
): List<TextLayoutResult> = buildList {
    if (column.isPrimaryKey) add(badge("PK", palette.tertiary))
    if (column.reference != null) add(badge("FK", palette.secondary))
    if (column.isUnique && !column.isPrimaryKey) add(badge("UQ", palette.primary))
}

private fun TextMeasurer.badge(label: String, color: Color): TextLayoutResult =
    measure(label, TextStyle(color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold))

private fun badgeRowWidth(row: List<TextLayoutResult>): Float =
    if (row.isEmpty()) 0f else row.sumOf { it.size.width } + BADGE_GAP * (row.size - 1)
