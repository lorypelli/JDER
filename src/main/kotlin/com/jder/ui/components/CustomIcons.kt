package com.jder.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.jder.domain.model.LogicComponentType

object CustomIcons {
    private fun outlineIcon(
        name: String,
        strokeWidth: Float = 2f,
        pathBuilder: PathBuilder.() -> Unit,
    ): ImageVector =
        ImageVector.Builder(
                name = name,
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .apply {
                path(
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = strokeWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathBuilder = pathBuilder,
                )
            }
            .build()

    private fun PathBuilder.circle(centerX: Float, centerY: Float, radius: Float) {
        moveTo(centerX - radius, centerY)
        arcToRelative(radius, radius, 0f, true, true, 2 * radius, 0f)
        arcToRelative(radius, radius, 0f, true, true, -2 * radius, 0f)
        close()
    }

    private fun PathBuilder.orBody(left: Float, right: Float) {
        val width = right - left
        moveTo(left, 6f)
        curveTo(left + width * 0.38f, 6f, right - width * 0.27f, 7.5f, right, 12f)
        curveTo(right - width * 0.27f, 16.5f, left + width * 0.38f, 18f, left, 18f)
        quadTo(left + 3f, 12f, left, 6f)
        close()
    }

    private fun PathBuilder.leads(inputStart: Float, inputEnd: Float, outputStart: Float) {
        moveTo(inputStart, 9f)
        horizontalLineTo(inputEnd)
        moveTo(inputStart, 15f)
        horizontalLineTo(inputEnd)
        moveTo(outputStart, 12f)
        horizontalLineTo(22.5f)
    }

    val Ellipse: ImageVector by lazy {
        outlineIcon("Ellipse") {
            moveTo(22f, 12f)
            curveTo(22f, 15.31f, 17.52f, 18f, 12f, 18f)
            curveTo(6.48f, 18f, 2f, 15.31f, 2f, 12f)
            curveTo(2f, 8.69f, 6.48f, 6f, 12f, 6f)
            curveTo(17.52f, 6f, 22f, 8.69f, 22f, 12f)
            close()
        }
    }

    val Rectangle: ImageVector by lazy {
        outlineIcon("Rectangle") {
            moveTo(4f, 6f)
            lineTo(20f, 6f)
            lineTo(20f, 18f)
            lineTo(4f, 18f)
            close()
        }
    }

    val Diamond: ImageVector by lazy {
        outlineIcon("Diamond") {
            moveTo(12f, 4f)
            lineTo(20f, 12f)
            lineTo(12f, 20f)
            lineTo(4f, 12f)
            close()
        }
    }

    val StickyNote: ImageVector by lazy {
        outlineIcon("StickyNote") {
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
    }

    val Interface: ImageVector by lazy {
        outlineIcon("Interface") {
            moveTo(3f, 12f)
            horizontalLineTo(11.5f)
            circle(16f, 12f, 4.5f)
        }
    }

    val Wire: ImageVector by lazy {
        outlineIcon("Wire") {
            moveTo(4.5f, 7f)
            horizontalLineTo(11f)
            verticalLineTo(17f)
            horizontalLineTo(19.5f)
            circle(3f, 7f, 1.5f)
            circle(21f, 17f, 1.5f)
        }
    }

    val AndGate: ImageVector by lazy {
        outlineIcon("AndGate", strokeWidth = 1.6f) {
            moveTo(6f, 6f)
            horizontalLineTo(11.5f)
            arcTo(6f, 6f, 0f, false, true, 11.5f, 18f)
            horizontalLineTo(6f)
            close()
            leads(2f, 6f, 17.5f)
        }
    }

    val NandGate: ImageVector by lazy {
        outlineIcon("NandGate", strokeWidth = 1.6f) {
            moveTo(4.5f, 6f)
            horizontalLineTo(9.5f)
            arcTo(6f, 6f, 0f, false, true, 9.5f, 18f)
            horizontalLineTo(4.5f)
            close()
            circle(17.2f, 12f, 1.7f)
            leads(1.5f, 4.5f, 18.9f)
        }
    }

    val OrGate: ImageVector by lazy {
        outlineIcon("OrGate", strokeWidth = 1.6f) {
            orBody(5f, 18f)
            leads(1.5f, 6.1f, 18f)
        }
    }

    val NorGate: ImageVector by lazy {
        outlineIcon("NorGate", strokeWidth = 1.6f) {
            orBody(4f, 15.5f)
            circle(17.2f, 12f, 1.7f)
            leads(1f, 5.1f, 18.9f)
        }
    }

    val XorGate: ImageVector by lazy {
        outlineIcon("XorGate", strokeWidth = 1.6f) {
            orBody(7f, 19f)
            moveTo(4.5f, 6f)
            quadTo(7.5f, 12f, 4.5f, 18f)
            leads(1.5f, 5.6f, 19f)
        }
    }

    val XnorGate: ImageVector by lazy {
        outlineIcon("XnorGate", strokeWidth = 1.6f) {
            orBody(6.5f, 16f)
            moveTo(4f, 6f)
            quadTo(7f, 12f, 4f, 18f)
            circle(17.7f, 12f, 1.7f)
            leads(1f, 5.1f, 19.4f)
        }
    }

    val NotGate: ImageVector by lazy {
        outlineIcon("NotGate", strokeWidth = 1.6f) {
            moveTo(6f, 6.5f)
            lineTo(6f, 17.5f)
            lineTo(15.5f, 12f)
            close()
            circle(17.2f, 12f, 1.7f)
            moveTo(2f, 12f)
            horizontalLineTo(6f)
            moveTo(18.9f, 12f)
            horizontalLineTo(22.5f)
        }
    }

    fun gate(type: LogicComponentType): ImageVector =
        when (type) {
            LogicComponentType.AND -> AndGate
            LogicComponentType.NAND -> NandGate
            LogicComponentType.OR -> OrGate
            LogicComponentType.NOR -> NorGate
            LogicComponentType.XOR -> XorGate
            LogicComponentType.XNOR -> XnorGate
            else -> NotGate
        }
}
