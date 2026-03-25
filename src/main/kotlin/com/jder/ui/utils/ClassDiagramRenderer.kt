package com.jder.ui.utils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ClassEntity
import com.jder.domain.model.ClassMember
import com.jder.domain.model.ClassRelation
import com.jder.domain.model.ClassRelationType
import com.jder.domain.model.Note
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
fun renderClassDiagramToBitmap(diagram: ClassDiagram): ImageBitmap {
    val padding = 80f
    val allX = mutableListOf<Float>()
    val allY = mutableListOf<Float>()
    diagram.classes.forEach {
        allX += listOf(it.x, it.x + it.width)
        val h = classEntityRenderedHeight(it)
        allY += listOf(it.y, it.y + h)
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
    diagram.relations.forEach { drawClassRelationR(g, it, diagram, offX, offY) }
    diagram.notes.forEach { drawNoteR(g, it, offX, offY) }
    diagram.classes.forEach { drawClassEntityR(g, it, offX, offY) }
    g.dispose()
    return img.toComposeImageBitmap()
}
private fun classEntityRenderedHeight(entity: ClassEntity): Float {
    val headerH = if (entity.type.stereotype.isNotEmpty()) 56f else 40f
    val attrH = 4f + entity.attributes.size * 20f + if (entity.attributes.isEmpty()) 20f else 0f
    val methodH = 4f + entity.methods.size * 20f + if (entity.methods.isEmpty()) 20f else 0f
    return headerH + attrH + methodH
}
private fun drawClassEntityR(g: Graphics2D, entity: ClassEntity, offX: Float, offY: Float) {
    val x = (entity.x + offX).toInt()
    val height = classEntityRenderedHeight(entity)
    val y = (entity.y + offY).toInt()
    val w = entity.width.toInt()
    val h = height.toInt()
    g.color = Color(0xF8F9FA)
    g.fillRect(x, y, w, h)
    g.color = Color(0x3F51B5)
    g.stroke = BasicStroke(1.5f)
    g.drawRect(x, y, w, h)
    var currentY = y
    val headerH = if (entity.type.stereotype.isNotEmpty()) 56 else 40
    if (entity.type.stereotype.isNotEmpty()) {
        g.font = Font("Arial", Font.ITALIC, 10)
        g.color = Color(0x9E9E9E)
        val fm = g.fontMetrics
        val stereoW = fm.stringWidth(entity.type.stereotype)
        g.drawString(entity.type.stereotype, x + (w - stereoW) / 2, currentY + 14)
        currentY += 18
    }
    g.font = Font("Arial", Font.BOLD, 13)
    g.color = Color(0x212121)
    val fm = g.fontMetrics
    val nameW = fm.stringWidth(entity.name)
    g.drawString(entity.name, x + (w - nameW) / 2, currentY + (if (entity.type.stereotype.isNotEmpty()) 18 else 25))
    currentY = y + headerH
    g.color = Color(0x3F51B5)
    g.stroke = BasicStroke(1f)
    g.drawLine(x, currentY, x + w, currentY)
    g.font = Font("Arial", Font.PLAIN, 11)
    g.color = Color(0x212121)
    if (entity.attributes.isEmpty()) {
        currentY += 20
    } else {
        entity.attributes.forEach { member ->
            val memberText = formatMember(member, isMethod = false)
            g.drawString(memberText, x + 6, currentY + 14)
            currentY += 20
        }
    }
    g.color = Color(0x3F51B5)
    g.stroke = BasicStroke(1f)
    g.drawLine(x, currentY, x + w, currentY)
    g.font = Font("Arial", Font.PLAIN, 11)
    g.color = Color(0x212121)
    if (entity.methods.isEmpty()) {
        currentY += 20
    } else {
        entity.methods.forEach { member ->
            val memberText = formatMember(member, isMethod = true)
            g.drawString(memberText, x + 6, currentY + 14)
            currentY += 20
        }
    }
}
private fun formatMember(member: ClassMember, isMethod: Boolean): String {
    val prefix = member.visibility.symbol + " "
    val suffix = if (member.type.isNotEmpty()) ": ${member.type}" else ""
    val paramSuffix = if (isMethod) "()" else ""
    return "$prefix${member.name}$paramSuffix$suffix"
}
private fun drawNoteR(g: Graphics2D, note: Note, offX: Float, offY: Float) {
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
private fun classEntityCenter(entity: ClassEntity): Pair<Float, Float> {
    val h = classEntityRenderedHeight(entity)
    return Pair(entity.x + entity.width / 2, entity.y + h / 2)
}
private fun classEntityBoundaryR(entity: ClassEntity, nx: Float, ny: Float): Pair<Float, Float> {
    val h = classEntityRenderedHeight(entity)
    val cx = entity.x + entity.width / 2
    val cy = entity.y + h / 2
    val hw = entity.width / 2
    val hh = h / 2
    val sx = if (nx != 0f) hw / abs(nx) else Float.MAX_VALUE
    val sy = if (ny != 0f) hh / abs(ny) else Float.MAX_VALUE
    val s = min(sx, sy)
    return Pair(cx + nx * s, cy + ny * s)
}
private fun drawClassRelationR(g: Graphics2D, rel: ClassRelation, diagram: ClassDiagram, offX: Float, offY: Float) {
    val srcEntity = diagram.classes.find { it.id == rel.sourceId } ?: return
    val tgtEntity = diagram.classes.find { it.id == rel.targetId } ?: return
    val srcC = classEntityCenter(srcEntity)
    val tgtC = classEntityCenter(tgtEntity)
    val dx = tgtC.first - srcC.first
    val dy = tgtC.second - srcC.second
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return
    val nx = dx / dist
    val ny = dy / dist
    val src = classEntityBoundaryR(srcEntity, nx, ny)
    val tgt = classEntityBoundaryR(tgtEntity, -nx, -ny)
    val sx = (src.first + offX).toInt()
    val sy = (src.second + offY).toInt()
    val tx = (tgt.first + offX).toInt()
    val ty = (tgt.second + offY).toInt()
    val arrowSize = 14f
    g.color = Color(0x607D8B)
    when (rel.type) {
        ClassRelationType.ASSOCIATION, ClassRelationType.DEPENDENCY -> {
            if (rel.type == ClassRelationType.DEPENDENCY) {
                g.stroke = BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(8f, 5f), 0f)
            } else {
                g.stroke = BasicStroke(1.5f)
            }
            val lineEndX = (tgt.first - nx * arrowSize + offX).toInt()
            val lineEndY = (tgt.second - ny * arrowSize + offY).toInt()
            g.drawLine(sx, sy, lineEndX, lineEndY)
            g.stroke = BasicStroke(1.5f)
            drawOpenArrowR(g, tgt.first + offX, tgt.second + offY, nx, ny)
        }
        ClassRelationType.INHERITANCE, ClassRelationType.REALIZATION -> {
            if (rel.type == ClassRelationType.REALIZATION) {
                g.stroke = BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(8f, 5f), 0f)
            } else {
                g.stroke = BasicStroke(1.5f)
            }
            val lineEndX = (tgt.first - nx * arrowSize + offX).toInt()
            val lineEndY = (tgt.second - ny * arrowSize + offY).toInt()
            g.drawLine(sx, sy, lineEndX, lineEndY)
            g.stroke = BasicStroke(1.5f)
            drawHollowTriangleR(g, tgt.first + offX, tgt.second + offY, nx, ny)
        }
        ClassRelationType.AGGREGATION -> {
            g.stroke = BasicStroke(1.5f)
            val diamondBase = Pair(src.first + nx * arrowSize * 2, src.second + ny * arrowSize * 2)
            g.drawLine((diamondBase.first + offX).toInt(), (diamondBase.second + offY).toInt(), tx, ty)
            drawDiamondR(g, src.first + offX, src.second + offY, nx, ny, filled = false)
        }
        ClassRelationType.COMPOSITION -> {
            g.stroke = BasicStroke(1.5f)
            val diamondBase = Pair(src.first + nx * arrowSize * 2, src.second + ny * arrowSize * 2)
            g.drawLine((diamondBase.first + offX).toInt(), (diamondBase.second + offY).toInt(), tx, ty)
            drawDiamondR(g, src.first + offX, src.second + offY, nx, ny, filled = true)
        }
    }
    if (rel.label.isNotEmpty()) {
        g.font = Font("Arial", Font.PLAIN, 10)
        g.color = Color(0x607D8B)
        val fm = g.fontMetrics
        val midX = ((src.first + tgt.first) / 2 + offX).toInt()
        val midY = ((src.second + tgt.second) / 2 + offY).toInt()
        g.drawString(rel.label, midX - fm.stringWidth(rel.label) / 2, midY - 4)
    }
    if (rel.sourceMultiplicity.isNotEmpty()) {
        g.font = Font("Arial", Font.PLAIN, 10)
        g.color = Color(0x607D8B)
        g.drawString(rel.sourceMultiplicity, (src.first + nx * 20 + offX).toInt(), (src.second + ny * 20 + offY).toInt())
    }
    if (rel.targetMultiplicity.isNotEmpty()) {
        g.font = Font("Arial", Font.PLAIN, 10)
        g.color = Color(0x607D8B)
        g.drawString(rel.targetMultiplicity, (tgt.first - nx * 24 + offX).toInt(), (tgt.second - ny * 24 + offY).toInt())
    }
}
private fun drawOpenArrowR(g: Graphics2D, tipX: Float, tipY: Float, nx: Float, ny: Float) {
    val size = 14f
    val px = -ny
    val baseX = tipX - nx * size
    val baseY = tipY - ny * size
    g.drawLine(tipX.toInt(), tipY.toInt(), (baseX + px * size * 0.4f).toInt(), (baseY + (-nx) * size * 0.4f).toInt())
    g.drawLine(tipX.toInt(), tipY.toInt(), (baseX - px * size * 0.4f).toInt(), (baseY - (-nx) * size * 0.4f).toInt())
}
private fun drawHollowTriangleR(g: Graphics2D, tipX: Float, tipY: Float, nx: Float, ny: Float) {
    val size = 14f
    val px = -ny
    val baseX = tipX - nx * size
    val baseY = tipY - ny * size
    val xs = intArrayOf(tipX.toInt(), (baseX + px * size * 0.5f).toInt(), (baseX - px * size * 0.5f).toInt())
    val ys = intArrayOf(tipY.toInt(), (baseY + (-nx) * size * 0.5f).toInt(), (baseY - (-nx) * size * 0.5f).toInt())
    g.color = Color.WHITE
    g.fillPolygon(xs, ys, 3)
    g.color = Color(0x607D8B)
    g.drawPolygon(xs, ys, 3)
}
private fun drawDiamondR(g: Graphics2D, tipX: Float, tipY: Float, nx: Float, ny: Float, filled: Boolean) {
    val size = 14f
    val px = -ny
    val mid = Pair(tipX + nx * size / 2, tipY + ny * size / 2)
    val leftX = mid.first + px * size / 3
    val leftY = mid.second + (-nx) * size / 3
    val rightX = mid.first - px * size / 3
    val rightY = mid.second - (-nx) * size / 3
    val bottomX = tipX + nx * size
    val bottomY = tipY + ny * size
    val xs = intArrayOf(tipX.toInt(), leftX.toInt(), bottomX.toInt(), rightX.toInt())
    val ys = intArrayOf(tipY.toInt(), leftY.toInt(), bottomY.toInt(), rightY.toInt())
    if (filled) {
        g.color = Color(0x607D8B)
        g.fillPolygon(xs, ys, 4)
    } else {
        g.color = Color.WHITE
        g.fillPolygon(xs, ys, 4)
    }
    g.color = Color(0x607D8B)
    g.drawPolygon(xs, ys, 4)
}
