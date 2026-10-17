package com.jder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jder.domain.model.LogicComponent
import com.jder.domain.model.LogicComponentType
import com.jder.domain.model.LogicDiagram
import com.jder.domain.model.LogicEndpoint
import com.jder.domain.model.LogicSimulation

private const val BUBBLE_RADIUS = 5f
private const val PIN_DOT_RADIUS = 3f
private const val XOR_GAP = 7f
private const val NOT_INSET = 8f
private const val OR_BACK_DEPTH = 0.22f

internal fun DiagramPalette.signalColor(value: Boolean?): Color =
    when (value) {
      true -> signalHigh
      false -> signalLow
      null -> signalUnknown
    }

private fun DiagramPalette.neutralWire(): Color = lerp(background, text, 0.8f)

internal fun DrawScope.drawLogicDiagram(
    diagram: LogicDiagram,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    simulation: LogicSimulation,
    showSignals: Boolean,
    selectedId: String? = null,
    hoveredId: String? = null,
    highlightedPins: List<LogicElement.PinRef> = emptyList(),
    pendingSource: LogicEndpoint? = null,
) {
  val componentsById = diagram.components.associateBy { it.id }
  val routesById = logicWireRoutes(diagram)
  val routes = diagram.wires.mapNotNull { wire -> routesById[wire.id]?.let { wire to it } }
  routes.forEach { (wire, points) ->
    val value = simulation.outputOf(wire.sourceId)
    val isSelected = wire.id == selectedId
    val color =
        when {
          isSelected -> palette.selected
          showSignals -> palette.signalColor(value)
          else -> palette.neutralWire()
        }
    val width =
        when {
          isSelected -> 3f
          wire.id == hoveredId -> 2.75f
          else -> 2f
        }
    drawWirePath(points, color, width, dashed = showSignals && value == null && !isSelected)
  }
  logicJunctions(routes.map { (wire, points) -> wire.sourceId to points }).forEach { (net, point) ->
    val color =
        if (showSignals) palette.signalColor(simulation.outputOf(net)) else palette.neutralWire()
    drawCircle(color = color, radius = 3.5f, center = point)
  }
  diagram.components.forEach {
    drawLogicComponent(
        component = it,
        palette = palette,
        textMeasurer = textMeasurer,
        simulation = simulation,
        showSignals = showSignals,
        isSelected = it.id == selectedId,
        isHovered = it.id == hoveredId,
        isPendingSource = it.id == pendingSource?.componentId,
    )
  }
  diagram.notes.forEach {
    drawNoteShape(it, it.id == selectedId, textMeasurer, palette.selected, it.id == hoveredId)
  }
  val pendingPin =
      pendingSource?.let { source ->
        source.pin?.let { LogicElement.PinRef(source.componentId, it) }
      }
  (highlightedPins + listOfNotNull(pendingPin)).forEach { pinRef ->
    val center = componentsById[pinRef.id]?.pinPosition(pinRef.pin) ?: return@forEach
    drawCircle(color = palette.selected.copy(alpha = 0.2f), radius = 8f, center = center)
    drawCircle(color = palette.selected, radius = 8f, center = center, style = Stroke(width = 2f))
  }
}

internal fun DrawScope.drawLogicWirePreview(
    anchor: Offset,
    cursor: Offset,
    fromOutput: Boolean,
    palette: DiagramPalette,
) {
  val points = if (fromOutput) logicWirePoints(anchor, cursor) else logicWirePoints(cursor, anchor)
  drawWirePath(points, palette.selected, 2f, dashed = true)
}

internal fun logicDiagramBounds(diagram: LogicDiagram, textMeasurer: TextMeasurer): Rect? {
  val bounds = BoundsBuilder()
  diagram.components.forEach { component ->
    bounds.include(component.bounds())
    if (component.type.isGate && component.label.isNotBlank()) {
      val label = gateLabel(component, textMeasurer, Color.Black)
      val centerX = component.x + component.width / 2
      bounds.include(
          Rect(
              centerX - label.size.width / 2f,
              component.y - 4f - label.size.height,
              centerX + label.size.width / 2f,
              component.y,
          )
      )
    }
  }
  logicWireRoutes(diagram).values.forEach { points -> points.forEach(bounds::include) }
  diagram.notes.forEach { bounds.include(it.bounds()) }
  return bounds.build()
}

private fun DrawScope.drawWirePath(
    points: List<Offset>,
    color: Color,
    width: Float,
    dashed: Boolean,
) {
  val path =
      Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
      }
  drawPath(
      path = path,
      color = color,
      style =
          Stroke(
              width = width,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round,
              pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null,
          ),
  )
}

private fun DrawScope.drawLogicComponent(
    component: LogicComponent,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    simulation: LogicSimulation,
    showSignals: Boolean,
    isSelected: Boolean,
    isHovered: Boolean,
    isPendingSource: Boolean,
) {
  if (isSelected || isPendingSource) {
    val halo = component.bounds().inflate(6f)
    drawRoundRect(
        color = palette.selected.copy(alpha = if (isSelected) 0.12f else 0.1f),
        topLeft = halo.topLeft,
        size = halo.size,
        cornerRadius = CornerRadius(6f),
    )
    drawRoundRect(
        color = palette.selected,
        topLeft = halo.topLeft,
        size = halo.size,
        cornerRadius = CornerRadius(6f),
        style =
            if (isSelected) Stroke(width = 2f)
            else Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))),
    )
  }
  val style =
      ComponentStyle(
          stroke = if (isSelected) palette.selected else palette.primary,
          strokeWidth = if (isSelected || isHovered) 2.5f else 2f,
          tint =
              when {
                isSelected -> palette.selected.copy(alpha = 0.08f)
                isHovered -> palette.primary.copy(alpha = 0.06f)
                else -> null
              },
      )
  when (component.type) {
    LogicComponentType.INPUT -> drawLogicInput(component, palette, textMeasurer, showSignals, style)
    LogicComponentType.OUTPUT ->
        drawLogicOutput(component, palette, textMeasurer, simulation, showSignals, style)
    else -> drawLogicGate(component, palette, textMeasurer, style)
  }
  repeat(component.inputPinCount) { index ->
    val isConnected = simulation.isConnected(component.id, index)
    val color =
        when {
          !isConnected -> palette.outline
          showSignals -> palette.signalColor(simulation.inputOf(component.id, index))
          else -> style.stroke
        }
    drawPinDot(component.inputPinPosition(index), color, isOpen = !isConnected, palette)
  }
  if (component.type.hasOutput) {
    val color =
        if (showSignals) palette.signalColor(simulation.outputOf(component.id)) else style.stroke
    drawPinDot(component.outputPinPosition(), color, isOpen = false, palette)
  }
}

private class ComponentStyle(val stroke: Color, val strokeWidth: Float, val tint: Color?)

private fun DrawScope.drawPinDot(
    center: Offset,
    color: Color,
    isOpen: Boolean,
    palette: DiagramPalette,
) {
  if (isOpen) {
    drawCircle(color = palette.surface, radius = PIN_DOT_RADIUS, center = center)
    drawCircle(
        color = color,
        radius = PIN_DOT_RADIUS,
        center = center,
        style = Stroke(width = 1.5f),
    )
  } else {
    drawCircle(color = color, radius = PIN_DOT_RADIUS, center = center)
  }
}

private fun DrawScope.drawComponentBody(
    body: Rect,
    palette: DiagramPalette,
    style: ComponentStyle,
) {
  drawRoundRect(palette.surface, body.topLeft, body.size, CornerRadius(8f))
  style.tint?.let { drawRoundRect(it, body.topLeft, body.size, CornerRadius(8f)) }
  drawRoundRect(
      color = style.stroke,
      topLeft = body.topLeft,
      size = body.size,
      cornerRadius = CornerRadius(8f),
      style = Stroke(width = style.strokeWidth),
  )
}

private fun DrawScope.drawLogicInput(
    component: LogicComponent,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    showSignals: Boolean,
    style: ComponentStyle,
) {
  val midY = component.y + component.height / 2
  val bodyRight = component.x + component.width - LOGIC_LEAD_LENGTH
  drawLine(style.stroke, Offset(bodyRight, midY), component.outputPinPosition(), strokeWidth = 2f)
  drawComponentBody(
      Rect(component.x, component.y, bodyRight, component.y + component.height),
      palette,
      style,
  )
  val label =
      textMeasurer.measureLine(
          text = component.label.ifBlank { component.type.display },
          style =
              TextStyle(color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
          maxWidth = 32f,
      )
  drawText(label, topLeft = Offset(component.x + 9f, midY - label.size.height / 2f))
  val toggle = component.toggleRect()
  val isOn = component.value
  val onColor = if (showSignals) palette.signalHigh else palette.primary
  drawRoundRect(
      color = if (isOn) onColor else palette.surfaceVariant,
      topLeft = toggle.topLeft,
      size = toggle.size,
      cornerRadius = CornerRadius(6f),
  )
  drawRoundRect(
      color = if (isOn) onColor else palette.outline,
      topLeft = toggle.topLeft,
      size = toggle.size,
      cornerRadius = CornerRadius(6f),
      style = Stroke(width = 1.5f),
  )
  val value =
      textMeasurer.measure(
          text = if (isOn) "1" else "0",
          style =
              TextStyle(
                  color = if (isOn) Color.White else palette.text,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
              ),
      )
  drawTextCentered(value, toggle.center)
}

private fun DrawScope.drawLogicOutput(
    component: LogicComponent,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    simulation: LogicSimulation,
    showSignals: Boolean,
    style: ComponentStyle,
) {
  val midY = component.y + component.height / 2
  val bodyLeft = component.x + LOGIC_LEAD_LENGTH
  drawLine(style.stroke, component.inputPinPosition(0), Offset(bodyLeft, midY), strokeWidth = 2f)
  drawComponentBody(
      Rect(bodyLeft, component.y, component.x + component.width, component.y + component.height),
      palette,
      style,
  )
  val center = Offset(component.x + 30f, midY)
  val value = simulation.inputOf(component.id, 0)
  val ledRadius = 11f
  if (showSignals && value == true) {
    drawCircle(
        color = palette.signalHigh.copy(alpha = 0.25f),
        radius = ledRadius + 4f,
        center = center,
    )
    drawCircle(color = palette.signalHigh, radius = ledRadius, center = center)
  } else {
    drawCircle(
        color = if (showSignals && value == null) palette.surface else palette.surfaceVariant,
        radius = ledRadius,
        center = center,
    )
    drawCircle(
        color = palette.outline,
        radius = ledRadius,
        center = center,
        style =
            Stroke(
                width = 1.5f,
                pathEffect =
                    if (showSignals && value == null)
                        PathEffect.dashPathEffect(floatArrayOf(4f, 3f))
                    else null,
            ),
    )
  }
  if (showSignals) {
    val text =
        textMeasurer.measure(
            text =
                when (value) {
                  true -> "1"
                  false -> "0"
                  null -> "?"
                },
            style =
                TextStyle(
                    color = if (value == true) Color.White else palette.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
    drawTextCentered(text, center)
  }
  val label =
      textMeasurer.measureLine(
          text = component.label.ifBlank { component.type.display },
          style =
              TextStyle(color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
          maxWidth = 36f,
      )
  drawText(label, topLeft = Offset(component.x + 48f, midY - label.size.height / 2f))
}

private fun gateLabel(component: LogicComponent, textMeasurer: TextMeasurer, color: Color) =
    textMeasurer.measureLine(
        text = component.label,
        style = TextStyle(color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        maxWidth = 160f,
    )

private fun DrawScope.drawLogicGate(
    component: LogicComponent,
    palette: DiagramPalette,
    textMeasurer: TextMeasurer,
    style: ComponentStyle,
) {
  val type = component.type
  val top = component.y
  val bottom = component.y + component.height
  val midY = (top + bottom) / 2
  val left = component.x + LOGIC_LEAD_LENGTH
  val outputStart = component.x + component.width - LOGIC_LEAD_LENGTH
  val right = if (type.isNegated) outputStart - 2 * BUBBLE_RADIUS else outputStart
  val bodyLeft =
      when (type) {
        LogicComponentType.XOR,
        LogicComponentType.XNOR -> left + XOR_GAP
        LogicComponentType.NOT -> left + NOT_INSET
        else -> left
      }
  val backDepth = (right - bodyLeft) * OR_BACK_DEPTH
  repeat(component.inputPinCount) { index ->
    val pin = component.inputPinPosition(index)
    val endX =
        when (type) {
          LogicComponentType.OR,
          LogicComponentType.NOR -> curveX(bodyLeft, backDepth, top, bottom, pin.y)
          LogicComponentType.XOR,
          LogicComponentType.XNOR -> curveX(left, backDepth, top, bottom, pin.y)
          else -> bodyLeft
        }
    drawLine(style.stroke, pin, Offset(endX, pin.y), strokeWidth = 2f)
  }
  val body = gateBodyPath(type, bodyLeft, top, right, bottom)
  drawPath(path = body, color = palette.surface, style = Fill)
  style.tint?.let { drawPath(path = body, color = it, style = Fill) }
  drawPath(
      path = body,
      color = style.stroke,
      style = Stroke(width = style.strokeWidth, join = StrokeJoin.Round),
  )
  if (type == LogicComponentType.XOR || type == LogicComponentType.XNOR) {
    val back =
        Path().apply {
          moveTo(left, top)
          quadraticBezierTo(left + backDepth, midY, left, bottom)
        }
    drawPath(
        path = back,
        color = style.stroke,
        style = Stroke(width = style.strokeWidth, cap = StrokeCap.Round),
    )
  }
  if (type.isNegated) {
    val center = Offset(right + BUBBLE_RADIUS, midY)
    drawCircle(color = palette.surface, radius = BUBBLE_RADIUS, center = center)
    drawCircle(
        color = style.stroke,
        radius = BUBBLE_RADIUS,
        center = center,
        style = Stroke(width = style.strokeWidth),
    )
  }
  drawLine(style.stroke, Offset(outputStart, midY), component.outputPinPosition(), strokeWidth = 2f)
  if (component.label.isNotBlank()) {
    val label = gateLabel(component, textMeasurer, palette.text)
    drawText(
        label,
        topLeft =
            Offset(
                component.x + component.width / 2 - label.size.width / 2f,
                component.y - 4f - label.size.height,
            ),
    )
  }
}

private fun curveX(baseX: Float, depth: Float, top: Float, bottom: Float, y: Float): Float {
  val t = ((y - top) / (bottom - top)).coerceIn(0f, 1f)
  return baseX + 2 * t * (1 - t) * depth
}

private fun gateBodyPath(
    type: LogicComponentType,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): Path {
  val width = right - left
  val height = bottom - top
  val midY = (top + bottom) / 2
  return Path().apply {
    when (type) {
      LogicComponentType.AND,
      LogicComponentType.NAND -> {
        val radiusX = minOf(height / 2, width / 2)
        moveTo(left, top)
        lineTo(right - radiusX, top)
        arcTo(Rect(right - 2 * radiusX, top, right, bottom), -90f, 180f, false)
        lineTo(left, bottom)
        close()
      }
      LogicComponentType.NOT -> {
        moveTo(left, top + 2f)
        lineTo(right, midY)
        lineTo(left, bottom - 2f)
        close()
      }
      else -> {
        moveTo(left, top)
        cubicTo(left + width * 0.45f, top, right - width * 0.18f, top + height * 0.15f, right, midY)
        cubicTo(
            right - width * 0.18f,
            bottom - height * 0.15f,
            left + width * 0.45f,
            bottom,
            left,
            bottom,
        )
        quadraticBezierTo(left + width * OR_BACK_DEPTH, midY, left, top)
        close()
      }
    }
  }
}
