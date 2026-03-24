package com.jder.ui.components
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.jder.domain.model.Note
internal fun DrawScope.drawGrid(
    canvasSize: Size,
    color: Color,
    zoom: Float,
    panOffset: Offset,
    spacing: Float = 20f
) {
    val viewportWidth = canvasSize.width / zoom
    val viewportHeight = canvasSize.height / zoom
    val startX = -panOffset.x / zoom
    val startY = -panOffset.y / zoom
    val endX = startX + viewportWidth
    val endY = startY + viewportHeight
    val firstVerticalLine = (startX / spacing).toInt() * spacing
    val firstHorizontalLine = (startY / spacing).toInt() * spacing
    val numVerticalLines = ((endX - firstVerticalLine) / spacing).toInt() + 2
    val numHorizontalLines = ((endY - firstHorizontalLine) / spacing).toInt() + 2
    for (i in 0..numVerticalLines) {
        val x = firstVerticalLine + i * spacing
        drawLine(color = color, start = Offset(x, startY), end = Offset(x, endY), strokeWidth = 1f)
    }
    for (i in 0..numHorizontalLines) {
        val y = firstHorizontalLine + i * spacing
        drawLine(color = color, start = Offset(startX, y), end = Offset(endX, y), strokeWidth = 1f)
    }
}
internal fun DrawScope.drawNoteShape(
    note: Note,
    isSelected: Boolean,
    textMeasurer: TextMeasurer,
    selectedColor: Color
) {
    val noteColor = Color(0xFFFFEB3B)
    val strokeColor = if (isSelected) selectedColor else Color(0xFFFBC02D)
    val strokeWidth = if (isSelected) 3f else 1.5f
    drawRect(color = noteColor, topLeft = Offset(note.x, note.y), size = Size(note.width, note.height), style = Fill)
    drawRect(color = strokeColor, topLeft = Offset(note.x, note.y), size = Size(note.width, note.height), style = Stroke(width = strokeWidth))
    val foldSize = 15f
    val foldPath = Path().apply {
        moveTo(note.x + note.width - foldSize, note.y)
        lineTo(note.x + note.width, note.y + foldSize)
        lineTo(note.x + note.width - foldSize, note.y + foldSize)
        close()
    }
    drawPath(path = foldPath, color = Color(0xFFF9A825), style = Fill)
    drawPath(path = foldPath, color = strokeColor, style = Stroke(width = 1f))
    val padding = 10f
    val textResult = textMeasurer.measure(
        text = note.text,
        style = TextStyle(color = Color.Black, fontSize = 12.sp),
        constraints = Constraints(
            maxWidth = (note.width - padding * 2).toInt().coerceAtLeast(1),
            maxHeight = (note.height - padding * 2).toInt().coerceAtLeast(1)
        )
    )
    drawText(textLayoutResult = textResult, topLeft = Offset(note.x + padding, note.y + padding))
}
