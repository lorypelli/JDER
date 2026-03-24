package com.jder.ui.utils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jder.domain.model.Actor
import com.jder.domain.model.Note
import com.jder.domain.model.SystemBoundary
import com.jder.domain.model.UseCase
import com.jder.domain.model.UseCaseDiagram
import com.jder.domain.model.UseCaseRelation
import com.jder.domain.model.UseCaseRelationType
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
fun renderUseCaseDiagramToBitmap(diagram: UseCaseDiagram): ImageBitmap {
    val padding = 80f
    val allX = mutableListOf<Float>()
    val allY = mutableListOf<Float>()
    diagram.systemBoundaries.forEach {
        allX += listOf(it.x, it.x + it.width)
        allY += listOf(it.y, it.y + it.height)
    }
    diagram.actors.forEach {
        allX += listOf(it.x, it.x + it.width)
        allY += listOf(it.y, it.y + it.height)
    }
    diagram.useCases.forEach {
        allX += listOf(it.x, it.x + it.width)
        allY += listOf(it.y, it.y + it.height)
    }
    diagram.notes.forEach {
        allX += listOf(it.x, it.x + it.width)
        allY += listOf(it.y, it.y + it.height)
    }
    val minX = (allX.minOrNull() ?: 0f) - padding
    val minY = (allY.minOrNull() ?: 0f) - padding
    val maxX = (allX.maxOrNull() ?: 800f) + padding
    val maxY = (allY.maxOrNull() ?: 600f) + padding
    val width = (maxX - minX).toInt().coerceAtLeast(800)
    val height = (maxY - minY).toInt().coerceAtLeast(600)
    val offX = -minX
    val offY = -minY
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.color = Color.WHITE
    g.fillRect(0, 0, width, height)
    diagram.systemBoundaries.forEach { drawSystemBoundary(g, it, offX, offY) }
    diagram.relations.forEach { drawRelation(g, it, diagram, offX, offY) }
    diagram.actors.forEach { drawActor(g, it, offX, offY) }
    diagram.useCases.forEach { drawUseCase(g, it, offX, offY) }
    diagram.notes.forEach { drawNote(g, it, offX, offY) }
    g.dispose()
    return img.toComposeImageBitmap()
}
private fun drawSystemBoundary(g: Graphics2D, b: SystemBoundary, offX: Float, offY: Float) {
    val x = (b.x + offX).toInt()
    val y = (b.y + offY).toInt()
    val w = b.width.toInt()
    val h = b.height.toInt()
    g.color = Color(0xF5F5F5)
    g.fillRect(x, y, w, h)
    g.color = Color(0x757575)
    g.stroke = BasicStroke(2f)
    g.drawRect(x, y, w, h)
    g.font = Font("Arial", Font.BOLD, 13)
    g.color = Color(0x424242)
    g.drawString(b.name, x + 12, y + 18)
}
private fun drawActor(g: Graphics2D, actor: Actor, offX: Float, offY: Float) {
    val cx = (actor.x + actor.width / 2 + offX).toInt()
    val baseY = (actor.y + offY).toInt()
    g.color = Color(0x1565C0)
    g.stroke = BasicStroke(2f)
    g.drawOval(cx - 10, baseY, 20, 20)
    g.drawLine(cx, baseY + 20, cx, baseY + 55)
    g.drawLine(cx - 22, baseY + 35, cx + 22, baseY + 35)
    g.drawLine(cx, baseY + 55, cx - 18, baseY + 80)
    g.drawLine(cx, baseY + 55, cx + 18, baseY + 80)
    g.font = Font("Arial", Font.PLAIN, 12)
    g.color = Color(0x212121)
    val fm = g.fontMetrics
    g.drawString(actor.name, cx - fm.stringWidth(actor.name) / 2, baseY + 95)
}
private fun drawUseCase(g: Graphics2D, uc: UseCase, offX: Float, offY: Float) {
    val x = (uc.x + offX).toInt()
    val y = (uc.y + offY).toInt()
    val w = uc.width.toInt()
    val h = uc.height.toInt()
    g.color = Color(0xE3F2FD)
    g.fill(Ellipse2D.Float(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()))
    g.color = Color(0x1976D2)
    g.stroke = BasicStroke(2f)
    g.draw(Ellipse2D.Float(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()))
    g.font = Font("Arial", Font.BOLD, 13)
    g.color = Color(0x212121)
    val fm = g.fontMetrics
    val words = uc.name.split(" ")
    val lines = mutableListOf<String>()
    var current = ""
    words.forEach { word ->
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (fm.stringWidth(candidate) <= w - 16) current = candidate
        else {
            if (current.isNotEmpty()) lines.add(current)
            current = word
        }
    }
    if (current.isNotEmpty()) lines.add(current)
    val lineH = fm.height
    val totalH = lines.size * lineH
    val startY = y + (h - totalH) / 2 + fm.ascent
    lines.forEachIndexed { i, line ->
        g.drawString(line, x + (w - fm.stringWidth(line)) / 2, startY + i * lineH)
    }
}
private fun drawNote(g: Graphics2D, note: Note, offX: Float, offY: Float) {
    val x = (note.x + offX).toInt()
    val y = (note.y + offY).toInt()
    val w = note.width.toInt()
    val h = note.height.toInt()
    val fold = 15
    g.color = Color(0xFFFDE7)
    g.fillRect(x, y, w, h)
    g.color = Color(0xFBC02D)
    g.stroke = BasicStroke(1.5f)
    g.drawRect(x, y, w, h)
    val foldXs = intArrayOf(x + w - fold, x + w, x + w - fold)
    val foldYs = intArrayOf(y, y + fold, y + fold)
    g.color = Color(0xF9A825)
    g.fillPolygon(foldXs, foldYs, 3)
    g.color = Color(0xFBC02D)
    g.drawPolygon(foldXs, foldYs, 3)
    g.font = Font("Arial", Font.PLAIN, 12)
    g.color = Color.BLACK
    val fm = g.fontMetrics
    val maxW = w - 20
    val lines = mutableListOf<String>()
    var cur = ""
    note.text.split("\n").forEach { paragraph ->
        paragraph.split(" ").forEach { word ->
            val cand = if (cur.isEmpty()) word else "$cur $word"
            if (fm.stringWidth(cand) <= maxW) cur = cand
            else {
                if (cur.isNotEmpty()) lines.add(cur)
                cur = word
            }
        }
        lines.add(cur)
        cur = ""
    }
    val lineH = fm.height
    var textY = y + 10 + fm.ascent
    lines.forEach { line ->
        if (textY + lineH <= y + h - 10) {
            g.drawString(line, x + 10, textY)
            textY += lineH
        }
    }
}
private fun drawRelation(g: Graphics2D, rel: UseCaseRelation, diagram: UseCaseDiagram, offX: Float, offY: Float) {
    val srcCenter = getCenter(rel.sourceId, diagram) ?: return
    val tgtCenter = getCenter(rel.targetId, diagram) ?: return
    val dx = tgtCenter.first - srcCenter.first
    val dy = tgtCenter.second - srcCenter.second
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return
    val nx = dx / dist
    val ny = dy / dist
    val src = getBoundaryPoint(rel.sourceId, diagram, nx, ny)
    val tgt = getBoundaryPoint(rel.targetId, diagram, -nx, -ny)
    g.color = Color(0x616161)
    val arrowSize = 13f
    when (rel.type) {
        UseCaseRelationType.ASSOCIATION -> {
            g.stroke = BasicStroke(1.5f)
            g.drawLine((src.first + offX).toInt(), (src.second + offY).toInt(), (tgt.first + offX).toInt(), (tgt.second + offY).toInt())
        }
        UseCaseRelationType.INCLUDE, UseCaseRelationType.EXTEND -> {
            g.stroke = BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(8f, 5f), 0f)
            val lineEndX = tgt.first - nx * arrowSize
            val lineEndY = tgt.second - ny * arrowSize
            g.drawLine((src.first + offX).toInt(), (src.second + offY).toInt(), (lineEndX + offX).toInt(), (lineEndY + offY).toInt())
            g.stroke = BasicStroke(1.5f)
            drawFilledArrow(g, tgt.first + offX, tgt.second + offY, nx, ny, Color(0x616161))
            val midX = (src.first + tgt.first) / 2 + offX
            val midY = (src.second + tgt.second) / 2 + offY
            g.font = Font("Arial", Font.PLAIN, 11)
            val fm = g.fontMetrics
            val label = rel.type.display
            val lw = fm.stringWidth(label)
            g.color = Color(0xFAFAFA)
            g.fillRoundRect((midX - lw / 2 - 4).toInt(), (midY - fm.height / 2 - 2).toInt(), lw + 8, fm.height + 4, 6, 6)
            g.color = Color(0x616161)
            g.drawString(label, (midX - lw / 2).toInt(), (midY + fm.ascent / 2).toInt())
        }
        UseCaseRelationType.GENERALIZATION -> {
            g.stroke = BasicStroke(1.5f)
            val lineEndX = tgt.first - nx * arrowSize
            val lineEndY = tgt.second - ny * arrowSize
            g.drawLine((src.first + offX).toInt(), (src.second + offY).toInt(), (lineEndX + offX).toInt(), (lineEndY + offY).toInt())
            drawHollowArrow(g, tgt.first + offX, tgt.second + offY, nx, ny, Color(0x616161))
        }
    }
}
private fun drawFilledArrow(g: Graphics2D, tipX: Float, tipY: Float, nx: Float, ny: Float, color: Color) {
    val size = 13f
    val px = -ny
    val bx1 = tipX - nx * size + px * size * 0.45f
    val by1 = tipY - ny * size + nx * size * 0.45f
    val bx2 = tipX - nx * size - px * size * 0.45f
    val by2 = tipY - ny * size - nx * size * 0.45f
    val xs = intArrayOf(tipX.toInt(), bx1.toInt(), bx2.toInt())
    val ys = intArrayOf(tipY.toInt(), by1.toInt(), by2.toInt())
    g.color = color
    g.fillPolygon(xs, ys, 3)
}
private fun drawHollowArrow(g: Graphics2D, tipX: Float, tipY: Float, nx: Float, ny: Float, color: Color) {
    val size = 13f
    val px = -ny
    val bx1 = tipX - nx * size + px * size * 0.45f
    val by1 = tipY - ny * size + nx * size * 0.45f
    val bx2 = tipX - nx * size - px * size * 0.45f
    val by2 = tipY - ny * size - nx * size * 0.45f
    val xs = intArrayOf(tipX.toInt(), bx1.toInt(), bx2.toInt())
    val ys = intArrayOf(tipY.toInt(), by1.toInt(), by2.toInt())
    g.color = Color.WHITE
    g.fillPolygon(xs, ys, 3)
    g.color = color
    g.stroke = BasicStroke(1.5f)
    g.drawPolygon(xs, ys, 3)
}
private fun getCenter(id: String, diagram: UseCaseDiagram): Pair<Float, Float>? {
    diagram.actors.find { it.id == id }?.let { return Pair(it.x + it.width / 2, it.y + it.height / 2) }
    diagram.useCases.find { it.id == id }?.let { return Pair(it.x + it.width / 2, it.y + it.height / 2) }
    return null
}
private fun getBoundaryPoint(id: String, diagram: UseCaseDiagram, nx: Float, ny: Float): Pair<Float, Float> {
    diagram.actors.find { it.id == id }?.let { actor ->
        val cx = actor.x + actor.width / 2
        val cy = actor.y + actor.height / 2
        val hw = actor.width / 2
        val hh = actor.height / 2
        val sx = if (nx != 0f) hw / abs(nx) else Float.MAX_VALUE
        val sy = if (ny != 0f) hh / abs(ny) else Float.MAX_VALUE
        val s = min(sx, sy)
        return Pair(cx + nx * s, cy + ny * s)
    }
    diagram.useCases.find { it.id == id }?.let { uc ->
        val cx = uc.x + uc.width / 2
        val cy = uc.y + uc.height / 2
        val rx = uc.width / 2
        val ry = uc.height / 2
        val dxr = nx / rx
        val dyr = ny / ry
        val denom = sqrt(dxr * dxr + dyr * dyr)
        if (denom == 0f) return Pair(cx, cy)
        val t = 1f / denom
        return Pair(cx + nx * t, cy + ny * t)
    }
    return getCenter(id, diagram) ?: Pair(0f, 0f)
}
