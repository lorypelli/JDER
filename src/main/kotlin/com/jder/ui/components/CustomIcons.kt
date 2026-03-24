package com.jder.ui.components
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
object CustomIcons {
    val Ellipse: ImageVector
        get() = ImageVector.Builder(
                name = "Ellipse",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color.Black),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(22f, 12f)
                    curveTo(22f, 15.31f, 17.52f, 18f, 12f, 18f)
                    curveTo(6.48f, 18f, 2f, 15.31f, 2f, 12f)
                    curveTo(2f, 8.69f, 6.48f, 6f, 12f, 6f)
                    curveTo(17.52f, 6f, 22f, 8.69f, 22f, 12f)
                    close()
                }
            }.build()
    val Rectangle: ImageVector
        get() = ImageVector.Builder(
                name = "Rectangle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color.Black),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 6f)
                    lineTo(20f, 6f)
                    lineTo(20f, 18f)
                    lineTo(4f, 18f)
                    close()
                }
            }.build()
    val Diamond: ImageVector
        get() = ImageVector.Builder(
                name = "Diamond",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color.Black),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 4f)
                    lineTo(20f, 12f)
                    lineTo(12f, 20f)
                    lineTo(4f, 12f)
                    close()
                }
            }.build()
    val StickyNote: ImageVector
        get() = ImageVector.Builder(
                name = "StickyNote",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = null,
                    fillAlpha = 1.0f,
                    stroke = SolidColor(Color.Black),
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(5f, 5f)
                    lineTo(17f, 5f)
                    lineTo(17f, 17f)
                    lineTo(11f, 17f)
                    lineTo(5f, 11f)
                    close()
                    moveTo(11f, 17f)
                    lineTo(11f, 11f)
                    lineTo(17f, 11f)
                    moveTo(8f, 8f)
                    lineTo(14f, 8f)
                    moveTo(8f, 11f)
                    lineTo(11f, 11f)
                }
            }.build()
}
