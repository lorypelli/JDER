package com.jder.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.FrameWindowScope
import com.jder.ui.utils.sharedList
import java.awt.Component
import java.awt.Container
import java.awt.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.awt.image.BaseMultiResolutionImage
import java.awt.image.BufferedImage

private val roles =
    mapOf(
        "default" to PointerIcon.Default,
        "pointer" to PointerIcon.Hand,
        "text" to PointerIcon.Text,
        "crosshair" to PointerIcon.Crosshair,
    )

private class CursorShape(val icon: PointerIcon, val path: String, val x: Int, val y: Int)

private class CursorMetrics(val size: Int, val padding: Float, val stroke: Float)

private val metrics by lazy {
    val (size, padding, stroke) = sharedList("cursorMetrics").map(String::toInt)
    CursorMetrics(size, padding.toFloat(), stroke.toFloat())
}

@Composable
fun FrameWindowScope.ThemedCursors() {
    val color = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.onPrimary
    LaunchedEffect(color, outline) { window.cursor = installCursors(color, outline) }
}

private fun installCursors(color: Color, outline: Color): Cursor =
    sharedList("cursors")
        .associate {
            val (role, path, x, y) = it.split('|')
            val shape = CursorShape(roles.getValue(role), path, x.toInt(), y.toInt())
            role to shape.install(color, outline)
        }
        .getValue("default")

private fun CursorShape.install(color: Color, outline: Color): Cursor {
    val field = icon.javaClass.getDeclaredField("cursor")
    field.isAccessible = true
    val previous = field.get(icon)
    val images = listOf(1, 2).map { draw(it, color, outline) }
    val cursor =
        Toolkit.getDefaultToolkit()
            .createCustomCursor(
                BaseMultiResolutionImage(*images.toTypedArray()),
                Point(x, y),
                "jder",
            )
    field.set(icon, cursor)
    Window.getWindows().forEach { it.replaceCursor(previous, cursor) }
    return cursor
}

private fun Component.replaceCursor(previous: Any?, next: Cursor) {
    if (isCursorSet && cursor === previous) cursor = next
    if (this is Container) components.forEach { it.replaceCursor(previous, next) }
}

private fun CursorShape.draw(scale: Int, color: Color, outline: Color): BufferedImage {
    val image = ImageBitmap(metrics.size * scale, metrics.size * scale)
    val shape = PathParser().parsePathString(path).toPath()
    val size = Size(image.width.toFloat(), image.height.toFloat())
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), size) {
        withTransform(
            {
                scale(scale.toFloat(), scale.toFloat(), Offset.Zero)
                translate(metrics.padding, metrics.padding)
            },
        ) {
            drawPath(shape, outline, style = Stroke(metrics.stroke, join = StrokeJoin.Round))
            drawPath(shape, color)
        }
    }
    return image.toAwtImage()
}
