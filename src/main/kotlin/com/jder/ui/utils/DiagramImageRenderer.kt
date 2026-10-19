package com.jder.ui.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.jder.domain.model.ClassDiagram
import com.jder.domain.model.ERDiagram
import com.jder.domain.model.LogicDiagram
import com.jder.domain.model.LogicSimulation
import com.jder.domain.model.RelationalDiagram
import com.jder.domain.model.SequenceDiagram
import com.jder.domain.model.UseCaseDiagram
import com.jder.ui.components.DiagramPalette
import com.jder.ui.components.classDiagramBounds
import com.jder.ui.components.createDiagramTextMeasurer
import com.jder.ui.components.drawClassDiagram
import com.jder.ui.components.drawERDiagram
import com.jder.ui.components.drawLogicDiagram
import com.jder.ui.components.drawRelationalDiagram
import com.jder.ui.components.drawSequenceDiagram
import com.jder.ui.components.drawUseCaseDiagram
import com.jder.ui.components.erDiagramBounds
import com.jder.ui.components.logicDiagramBounds
import com.jder.ui.components.relationalDiagramBounds
import com.jder.ui.components.sequenceDiagramBounds
import com.jder.ui.components.useCaseDiagramBounds
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val EXPORT_PADDING = 40f
private const val EXPORT_SCALE = 2f
private const val MAX_EXPORT_PIXELS = 48_000_000f
private const val MAX_EXPORT_DIMENSION = 16_000f

private val exportTextMeasurer by lazy { createDiagramTextMeasurer() }

fun renderERDiagramToBitmap(diagram: ERDiagram, palette: DiagramPalette): ImageBitmap =
    renderDiagramImage(erDiagramBounds(diagram, exportTextMeasurer), palette) {
        drawERDiagram(diagram, palette, exportTextMeasurer)
    }

fun renderRelationalDiagramToBitmap(
    diagram: RelationalDiagram,
    palette: DiagramPalette,
): ImageBitmap =
    renderDiagramImage(relationalDiagramBounds(diagram), palette) {
        drawRelationalDiagram(diagram, palette, exportTextMeasurer)
    }

fun renderClassDiagramToBitmap(diagram: ClassDiagram, palette: DiagramPalette): ImageBitmap =
    renderDiagramImage(classDiagramBounds(diagram), palette) {
        drawClassDiagram(diagram, palette, exportTextMeasurer)
    }

fun renderUseCaseDiagramToBitmap(diagram: UseCaseDiagram, palette: DiagramPalette): ImageBitmap =
    renderDiagramImage(useCaseDiagramBounds(diagram, exportTextMeasurer), palette) {
        drawUseCaseDiagram(diagram, palette, exportTextMeasurer)
    }

fun renderSequenceDiagramToBitmap(diagram: SequenceDiagram, palette: DiagramPalette): ImageBitmap =
    renderDiagramImage(sequenceDiagramBounds(diagram, exportTextMeasurer), palette) {
        drawSequenceDiagram(diagram, palette, exportTextMeasurer)
    }

fun renderLogicDiagramToBitmap(
    diagram: LogicDiagram,
    simulation: LogicSimulation,
    showSignals: Boolean,
    palette: DiagramPalette,
): ImageBitmap =
    renderDiagramImage(logicDiagramBounds(diagram, exportTextMeasurer), palette) {
        drawLogicDiagram(diagram, palette, exportTextMeasurer, simulation, showSignals)
    }

private fun renderDiagramImage(
    contentBounds: Rect?,
    palette: DiagramPalette,
    draw: DrawScope.() -> Unit,
): ImageBitmap {
    val bounds = (contentBounds ?: Rect(0f, 0f, 400f, 300f)).inflate(EXPORT_PADDING)
    val scale =
        minOf(
            EXPORT_SCALE,
            sqrt(MAX_EXPORT_PIXELS / (bounds.width * bounds.height)),
            MAX_EXPORT_DIMENSION / bounds.width,
            MAX_EXPORT_DIMENSION / bounds.height,
        )
    val width = (bounds.width * scale).roundToInt().coerceAtLeast(1)
    val height = (bounds.height * scale).roundToInt().coerceAtLeast(1)
    val image = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        Density(1f),
        LayoutDirection.Ltr,
        Canvas(image),
        Size(width.toFloat(), height.toFloat()),
    ) {
        drawRect(palette.background)
        withTransform({
            scale(scale, scale, Offset.Zero)
            translate(-bounds.left, -bounds.top)
        }) {
            draw()
        }
    }
    return image
}
