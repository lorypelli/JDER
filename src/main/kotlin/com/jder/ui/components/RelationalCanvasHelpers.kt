package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.jder.domain.model.RelationalDiagram
import com.jder.domain.model.RelationalDiagramState
import com.jder.domain.model.RelationalForeignKey
import com.jder.domain.model.RelationalTable
import com.jder.domain.model.RelationalToolMode
import com.jder.domain.model.foreignKeys
import kotlin.math.abs

internal const val RELATIONAL_HEADER_HEIGHT = 32f
internal const val RELATIONAL_ROW_HEIGHT = 22f
private const val TABLE_BOTTOM_PADDING = 4f
private const val FOREIGN_KEY_STUB = 24f
private const val FOREIGN_KEY_HIT_DISTANCE = 6f
private const val ROUTE_MARGIN = 10f
private const val ROUTE_STEP = 10f
private const val MAX_DETOUR_STEPS = 20

sealed interface RelationalElement {
    val id: String

    data class TableRef(override val id: String) : RelationalElement

    data class ColumnRef(val columnId: String, override val id: String) : RelationalElement

    data class ForeignKeyRef(override val id: String) : RelationalElement

    data class NoteRef(override val id: String) : RelationalElement
}

internal val RelationalElement.contextMenuType: RelationalContextMenuType
    get() =
        when (this) {
            is RelationalElement.TableRef,
            is RelationalElement.ColumnRef -> RelationalContextMenuType.TABLE
            is RelationalElement.ForeignKeyRef -> RelationalContextMenuType.FOREIGN_KEY
            is RelationalElement.NoteRef -> RelationalContextMenuType.NOTE
        }

internal val RelationalTable.height: Float
    get() =
        RELATIONAL_HEADER_HEIGHT +
            maxOf(columns.size, 1) * RELATIONAL_ROW_HEIGHT +
            TABLE_BOTTOM_PADDING

internal fun RelationalTable.bounds(): Rect = Rect(x, y, x + width, y + height)

internal fun RelationalTable.rowTop(index: Int): Float =
    y + RELATIONAL_HEADER_HEIGHT + index * RELATIONAL_ROW_HEIGHT

private fun RelationalTable.rowCenterY(columnId: String): Float =
    rowTop(columns.indexOfFirst { it.id == columnId }.coerceAtLeast(0)) + RELATIONAL_ROW_HEIGHT / 2

private fun RelationalTable.columnIdAt(point: Offset): String? {
    val offset = point.y - y - RELATIONAL_HEADER_HEIGHT
    if (offset < 0f) return null
    return columns.getOrNull((offset / RELATIONAL_ROW_HEIGHT).toInt())?.id
}

internal fun foreignKeyRoute(
    diagram: RelationalDiagram,
    foreignKey: RelationalForeignKey,
): List<Offset> {
    val source = foreignKey.table
    val target = foreignKey.referencedTable
    val startY = source.rowCenterY(foreignKey.column.id)
    val endY = target.rowCenterY(foreignKey.referencedColumn.id)
    val obstacles =
        diagram.tables
            .filter { it.id != source.id && it.id != target.id }
            .map { it.bounds().inflate(ROUTE_MARGIN) }
    fun route(startX: Float, middleX: Float, endX: Float) =
        listOf(
            Offset(startX, startY),
            Offset(middleX, startY),
            Offset(middleX, endY),
            Offset(endX, endY),
        )
    fun isClear(points: List<Offset>) =
        points.zipWithNext { start, end -> obstacles.none { it.crosses(start, end) } }.all { it }
    val sourceRight = source.x + source.width
    val targetRight = target.x + target.width
    val candidates =
        when {
            target.x >= sourceRight + 2 * FOREIGN_KEY_STUB ->
                elbowPositions(sourceRight, target.x).map { route(sourceRight, it, target.x) }
            targetRight <= source.x - 2 * FOREIGN_KEY_STUB ->
                elbowPositions(source.x, targetRight).map { route(source.x, it, targetRight) }
            else -> {
                val right = maxOf(sourceRight, targetRight) + FOREIGN_KEY_STUB
                val left = minOf(source.x, target.x) - FOREIGN_KEY_STUB
                (0..MAX_DETOUR_STEPS).asSequence().flatMap { step ->
                    sequenceOf(
                        route(sourceRight, right + step * ROUTE_STEP, targetRight),
                        route(source.x, left - step * ROUTE_STEP, target.x),
                    )
                }
            }
        }
    return candidates.firstOrNull { isClear(it) } ?: candidates.first()
}

private fun elbowPositions(startX: Float, endX: Float): Sequence<Float> {
    val middle = (startX + endX) / 2
    val reach = maxOf(abs(endX - startX) / 2 - FOREIGN_KEY_STUB, 0f)
    return generateSequence(0f) { it + ROUTE_STEP }
        .takeWhile { it <= reach }
        .flatMap { sequenceOf(middle - it, middle + it) }
}

private fun Rect.crosses(start: Offset, end: Offset): Boolean =
    maxOf(start.x, end.x) > left &&
        minOf(start.x, end.x) < right &&
        maxOf(start.y, end.y) > top &&
        minOf(start.y, end.y) < bottom

internal fun hitTestRelationalDiagram(
    diagram: RelationalDiagram,
    point: Offset,
): RelationalElement? {
    diagram.notes
        .lastOrNull { it.contains(point) }
        ?.let {
            return RelationalElement.NoteRef(it.id)
        }
    diagram.tables
        .lastOrNull { it.bounds().contains(point) }
        ?.let { table ->
            return table.columnIdAt(point)?.let { RelationalElement.ColumnRef(it, table.id) }
                ?: RelationalElement.TableRef(table.id)
        }
    diagram
        .foreignKeys()
        .lastOrNull { foreignKey ->
            foreignKeyRoute(diagram, foreignKey)
                .zipWithNext { start, end -> distanceToSegment(point, start, end) }
                .any { it <= FOREIGN_KEY_HIT_DISTANCE }
        }
        ?.let {
            return RelationalElement.ForeignKeyRef(it.column.id)
        }
    return null
}

internal fun selectRelationalElement(state: RelationalDiagramState, element: RelationalElement) {
    when (element) {
        is RelationalElement.TableRef,
        is RelationalElement.ColumnRef -> state.selectTable(element.id)
        is RelationalElement.ForeignKeyRef -> state.selectForeignKey(element.id)
        is RelationalElement.NoteRef -> state.selectNote(element.id)
    }
}

internal fun handleRelationalTap(
    state: RelationalDiagramState,
    offset: Offset,
    onForeignKeyCreateRequest: (String, String) -> Unit,
) {
    when (state.toolMode) {
        RelationalToolMode.SELECT ->
            hitTestRelationalDiagram(state.diagram, offset)?.let {
                selectRelationalElement(state, it)
            } ?: state.clearSelection()
        RelationalToolMode.TABLE -> {
            state.addTable(offset.x - 110f, offset.y - 30f, "NuovaTabella")
            state.toolMode = RelationalToolMode.SELECT
        }
        RelationalToolMode.FOREIGN_KEY -> {
            val clicked = state.diagram.tables.lastOrNull { it.bounds().contains(offset) }?.id
            if (clicked != null) {
                val pending = state.pendingForeignKeySourceId
                when {
                    pending == null -> state.pendingForeignKeySourceId = clicked
                    pending != clicked -> {
                        onForeignKeyCreateRequest(pending, clicked)
                        state.pendingForeignKeySourceId = null
                    }
                    else -> state.pendingForeignKeySourceId = null
                }
            } else {
                state.pendingForeignKeySourceId = null
            }
        }
        RelationalToolMode.NOTE -> {
            state.addNote(offset.x - 120f, offset.y - 90f, "Nuova Nota")
            state.toolMode = RelationalToolMode.SELECT
        }
    }
}

internal fun handleRelationalDragStart(
    state: RelationalDiagramState,
    offset: Offset,
): RelationalElement? {
    val element = hitTestRelationalDiagram(state.diagram, offset) ?: return null
    selectRelationalElement(state, element)
    if (element is RelationalElement.ForeignKeyRef) return null
    state.saveDragStartState()
    return element
}

internal fun handleRelationalDrag(
    state: RelationalDiagramState,
    element: RelationalElement,
    delta: Offset,
) {
    when (element) {
        is RelationalElement.TableRef,
        is RelationalElement.ColumnRef ->
            state.updateTableWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is RelationalElement.NoteRef ->
            state.updateNoteWithoutSave(element.id) {
                it.copy(x = it.x + delta.x, y = it.y + delta.y)
            }
        is RelationalElement.ForeignKeyRef -> {}
    }
}
