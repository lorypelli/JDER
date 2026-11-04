package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.jder.domain.model.MachineState
import com.jder.domain.model.MachineTransition
import com.jder.domain.model.StateMachineDiagram
import com.jder.domain.model.StateMachineKind
import com.jder.domain.model.label
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

internal const val MACHINE_STATE_RADIUS = 36f
internal const val MACHINE_RING_HIT = 7f
internal const val TRANSITION_ARROW_SIZE = 12f
private const val TWO_PI = (2 * PI).toFloat()
private const val LABEL_GAP = 5f
private const val INITIAL_ARROW_LENGTH = 42f
private const val LOOP_DISTANCE = 1.5f * MACHINE_STATE_RADIUS
private const val LOOP_RADIUS = 0.75f * MACHINE_STATE_RADIUS
private const val LOOP_SPREAD = 0.8f * PI.toFloat()
private const val LOOP_CLEARANCE = PI.toFloat() / 4
private const val INITIAL_CLEARANCE = PI.toFloat() / 6
private const val LOOP_FOOT_ANGLE = 0.46f
private const val LOOP_ANGLE_STEP = 15f
private const val CURVE_SAMPLES = 24
private const val LINE_SAMPLES = 12
private const val NEARBY_EDGE_DISTANCE = LOOP_DISTANCE + LOOP_RADIUS + 16f
private const val BEND_FACTOR = 0.25f
private const val MIN_BEND = 28f
private const val MAX_BEND = 80f
private const val STRAIGHT_SNAP = 10f
private const val OBSTACLE_MARGIN = 12f
private const val MAX_AVOID_FACTOR = 1.5f
private const val AVOID_ITERATIONS = 4
private const val PREFERRED_LOOP_ANGLE = -PI.toFloat() / 2
private const val PREFERRED_INITIAL_ANGLE = PI.toFloat()

internal val transitionLabelStyle = TextStyle(fontSize = 12.sp)

internal sealed interface TransitionShape {
    class Line(val start: Offset, val end: Offset) : TransitionShape

    class Curve(val start: Offset, val control: Offset, val end: Offset) : TransitionShape

    class Loop(
        val center: Offset,
        val radius: Float,
        val startAngle: Float,
        val sweepAngle: Float,
    ) : TransitionShape
}

internal class TransitionLabelLine(
    val transitionId: String,
    val layout: TextLayoutResult,
    val topLeft: Offset,
) {
    val rect: Rect
        get() =
            Rect(
                topLeft,
                Offset(
                    topLeft.x + layout.size.width.toFloat(),
                    topLeft.y + layout.size.height.toFloat(),
                ),
            )
}

internal class TransitionEdge(
    val transitionIds: List<String>,
    val sourceCenter: Offset,
    val targetCenter: Offset,
    val shape: TransitionShape,
    val tip: Offset,
    val direction: Offset,
    val points: List<Offset>,
    val labels: List<TransitionLabelLine>,
) {
    val isLoop: Boolean
        get() = shape is TransitionShape.Loop
}

internal class InitialArrow(val start: Offset, val tip: Offset, val direction: Offset)

internal data class StateMachineLayout(
    val edges: List<TransitionEdge>,
    val initialArrow: InitialArrow?,
) {
    private val edgesByTransition =
        edges.flatMap { edge -> edge.transitionIds.map { it to edge } }.toMap()

    fun edgeOf(transitionId: String?): TransitionEdge? = edgesByTransition[transitionId]
}

private data class ArrowKey(
    val sourceId: String,
    val targetId: String,
    val bend: Float?,
    val loopAngle: Float?,
) {
    val isLoop: Boolean
        get() = sourceId == targetId
}

internal val MachineState.center: Offset
    get() = Offset(x, y)

internal fun stateMachineLayout(
    diagram: StateMachineDiagram,
    kind: StateMachineKind,
    textMeasurer: TextMeasurer,
): StateMachineLayout {
    val statesById = diagram.states.associateBy { it.id }
    val groups =
        diagram.transitions
            .filter { it.sourceId in statesById && it.targetId in statesById }
            .groupBy { ArrowKey(it.sourceId, it.targetId, it.bend, it.loopAngle) }
    val pairs = groups.keys.mapTo(mutableSetOf()) { it.sourceId to it.targetId }
    val occupied = mutableMapOf<String, MutableList<Float>>()
    fun angles(stateId: String) = occupied.getOrPut(stateId) { mutableListOf() }
    val edges = mutableListOf<TransitionEdge>()
    groups.forEach { (key, transitions) ->
        if (key.isLoop) return@forEach
        val source = statesById.getValue(key.sourceId)
        val target = statesById.getValue(key.targetId)
        val hasReverse = (key.targetId to key.sourceId) in pairs
        val edge =
            betweenStatesEdge(
                source,
                target,
                key.bend,
                hasReverse,
                diagram.states,
                transitions,
                kind,
                textMeasurer,
            ) ?: return@forEach
        angles(source.id) += angleOf(edge.points.first() - source.center)
        angles(target.id) += angleOf(edge.tip - target.center)
        edges += edge
    }
    val automaticIds =
        groups.keys.filter { it.isLoop && it.loopAngle == null }.map { it.sourceId } +
            listOfNotNull(diagram.initialStateId)
    automaticIds.distinct().mapNotNull(statesById::get).forEach { state ->
        edges.forEach { edge ->
            edge.points.forEach {
                val vector = it - state.center
                if (vector.getDistance() <= NEARBY_EDGE_DISTANCE) {
                    angles(state.id) += angleOf(vector)
                }
            }
        }
    }
    groups.entries
        .filter { it.key.isLoop }
        .sortedBy { it.key.loopAngle == null }
        .forEach { (key, transitions) ->
            val state = statesById.getValue(key.sourceId)
            val stateAngles = angles(state.id)
            val angle =
                key.loopAngle?.let(::toRadians)
                    ?: freeDirection(stateAngles, PREFERRED_LOOP_ANGLE, LOOP_CLEARANCE)
            stateAngles += listOf(angle, angle - LOOP_FOOT_ANGLE, angle + LOOP_FOOT_ANGLE)
            edges += loopEdge(state, angle, transitions, kind, textMeasurer)
        }
    val initialArrow =
        statesById[diagram.initialStateId]?.let {
            val angle =
                freeDirection(occupied[it.id].orEmpty(), PREFERRED_INITIAL_ANGLE, INITIAL_CLEARANCE)
            val direction = unitVector(angle)
            InitialArrow(
                start = it.center + direction * (MACHINE_STATE_RADIUS + INITIAL_ARROW_LENGTH),
                tip = it.center + direction * MACHINE_STATE_RADIUS,
                direction = -direction,
            )
        }
    return StateMachineLayout(edges, initialArrow)
}

internal fun bendTowards(edge: TransitionEdge, cursor: Offset): Float {
    val delta = edge.targetCenter - edge.sourceCenter
    val distance = delta.getDistance()
    if (distance < 1f) return 0f
    val normal = Offset(-delta.y, delta.x) / distance
    val middle = (edge.sourceCenter + edge.targetCenter) / 2f
    val offset = 2 * dot(cursor - middle, normal)
    return if (abs(offset) < STRAIGHT_SNAP) 0f else offset / distance
}

internal fun loopAngleTowards(edge: TransitionEdge, cursor: Offset): Float {
    val vector = cursor - edge.sourceCenter
    if (vector.getDistance() < 1f) return 0f
    val degrees = Math.toDegrees(angleOf(vector).toDouble()).toFloat()
    return ((degrees / LOOP_ANGLE_STEP).roundToInt() * LOOP_ANGLE_STEP + 360f) % 360f
}

internal fun transitionPreviewShape(
    source: MachineState,
    target: MachineState?,
    cursor: Offset,
): TransitionShape? {
    val center = source.center
    return when {
        target == null -> {
            val direction = (cursor - center).normalizedOrNull() ?: return null
            if ((cursor - center).getDistance() <= MACHINE_STATE_RADIUS) return null
            TransitionShape.Line(center + direction * MACHINE_STATE_RADIUS, cursor)
        }
        target.id == source.id -> {
            val angle = (cursor - center).normalizedOrNull()?.let(::angleOf) ?: PREFERRED_LOOP_ANGLE
            loopShape(center, angle)
        }
        else -> {
            val direction = (target.center - center).normalizedOrNull() ?: return null
            TransitionShape.Line(
                center + direction * MACHINE_STATE_RADIUS,
                target.center - direction * MACHINE_STATE_RADIUS,
            )
        }
    }
}

internal fun TransitionShape.arrow(): Pair<Offset, Offset> =
    when (this) {
        is TransitionShape.Line -> end to ((end - start).normalizedOrNull() ?: Offset(1f, 0f))
        is TransitionShape.Curve -> end to ((end - control).normalizedOrNull() ?: Offset(1f, 0f))
        is TransitionShape.Loop -> {
            val endAngle = startAngle + sweepAngle
            (center + unitVector(endAngle) * radius) to Offset(-sin(endAngle), cos(endAngle))
        }
    }

internal fun TransitionShape.samplePoints(): List<Offset> =
    when (this) {
        is TransitionShape.Line ->
            (0..LINE_SAMPLES).map { start + (end - start) * (it / LINE_SAMPLES.toFloat()) }
        is TransitionShape.Curve ->
            (0..CURVE_SAMPLES).map {
                quadraticPoint(start, control, end, it / CURVE_SAMPLES.toFloat())
            }
        is TransitionShape.Loop ->
            (0..CURVE_SAMPLES).map {
                center + unitVector(startAngle + sweepAngle * it / CURVE_SAMPLES) * radius
            }
    }

internal fun quadraticPoint(start: Offset, control: Offset, end: Offset, t: Float): Offset {
    val u = 1 - t
    return start * (u * u) + control * (2 * u * t) + end * (t * t)
}

internal fun distanceToPolyline(point: Offset, points: List<Offset>): Float =
    points.zipWithNext { a, b -> distanceToSegment(point, a, b) }.minOrNull()
        ?: points.firstOrNull()?.let { (it - point).getDistance() }
        ?: Float.MAX_VALUE

private fun betweenStatesEdge(
    source: MachineState,
    target: MachineState,
    bend: Float?,
    hasReverse: Boolean,
    states: List<MachineState>,
    transitions: List<MachineTransition>,
    kind: StateMachineKind,
    textMeasurer: TextMeasurer,
): TransitionEdge? {
    val delta = target.center - source.center
    val distance = delta.getDistance()
    val direction = delta.normalizedOrNull() ?: return null
    val normal = Offset(-direction.y, direction.x)
    val offset =
        when {
            bend != null -> bend * distance
            hasReverse -> (distance * BEND_FACTOR).coerceIn(MIN_BEND, MAX_BEND)
            else -> avoidingOffset(source, target, states)
        }
    val shape: TransitionShape
    val labelAnchor: Offset
    val labelNormal: Offset
    if (abs(offset) >= 0.5f) {
        val control = (source.center + target.center) / 2f + normal * offset
        val start =
            source.center +
                ((control - source.center).normalizedOrNull() ?: direction) * MACHINE_STATE_RADIUS
        val end =
            target.center +
                ((control - target.center).normalizedOrNull() ?: -direction) * MACHINE_STATE_RADIUS
        shape = TransitionShape.Curve(start, control, end)
        labelAnchor = quadraticPoint(start, control, end, 0.5f)
        labelNormal = if (offset > 0f) normal else -normal
    } else {
        val start = source.center + direction * MACHINE_STATE_RADIUS
        val end = target.center - direction * MACHINE_STATE_RADIUS
        shape = TransitionShape.Line(start, end)
        labelAnchor = (start + end) / 2f
        labelNormal = if (normal.y > 0f || (normal.y == 0f && normal.x < 0f)) -normal else normal
    }
    return edgeWithLabels(
        shape,
        labelAnchor,
        labelNormal,
        transitions,
        kind,
        textMeasurer,
        source.center,
        target.center,
    )
}

private fun avoidingOffset(
    source: MachineState,
    target: MachineState,
    states: List<MachineState>,
): Float {
    val delta = target.center - source.center
    val distance = delta.getDistance()
    val direction = delta / distance
    val normal = Offset(-direction.y, direction.x)
    val clearance = MACHINE_STATE_RADIUS + OBSTACLE_MARGIN
    val obstacles =
        states
            .filter { it.id != source.id && it.id != target.id }
            .map {
                val relative = it.center - source.center
                dot(relative, direction) / distance to dot(relative, normal)
            }
            .filter { (position, _) -> position > 0f && position < 1f }
    val constraints = mutableSetOf<Pair<Float, Float>>()
    var offset = 0f
    repeat(AVOID_ITERATIONS) {
        val hits =
            obstacles.filter { (position, side) ->
                (position to side) !in constraints &&
                    abs(2 * position * (1 - position) * offset - side) < clearance
            }
        if (hits.isEmpty()) return offset
        constraints += hits
        var above = 0f
        var below = 0f
        constraints.forEach { (position, side) ->
            val factor = 2 * position * (1 - position)
            above = max(above, (side + clearance) / factor)
            below = min(below, (side - clearance) / factor)
        }
        val prefersAbove =
            when {
                above < -below - 1f -> true
                -below < above - 1f -> false
                else -> normal.y > 0f || (normal.y == 0f && normal.x > 0f)
            }
        offset =
            (if (prefersAbove) above else below).coerceIn(
                -distance * MAX_AVOID_FACTOR,
                distance * MAX_AVOID_FACTOR,
            )
    }
    return offset
}

private fun loopEdge(
    state: MachineState,
    angle: Float,
    transitions: List<MachineTransition>,
    kind: StateMachineKind,
    textMeasurer: TextMeasurer,
): TransitionEdge {
    val shape = loopShape(state.center, angle)
    val direction = unitVector(angle)
    val anchor = shape.center + direction * shape.radius
    return edgeWithLabels(
        shape,
        anchor,
        direction,
        transitions,
        kind,
        textMeasurer,
        state.center,
        state.center,
    )
}

private fun loopShape(center: Offset, angle: Float): TransitionShape.Loop =
    TransitionShape.Loop(
        center = center + unitVector(angle) * LOOP_DISTANCE,
        radius = LOOP_RADIUS,
        startAngle = angle - LOOP_SPREAD,
        sweepAngle = 2 * LOOP_SPREAD,
    )

private fun edgeWithLabels(
    shape: TransitionShape,
    anchor: Offset,
    normal: Offset,
    transitions: List<MachineTransition>,
    kind: StateMachineKind,
    textMeasurer: TextMeasurer,
    sourceCenter: Offset,
    targetCenter: Offset,
): TransitionEdge {
    val layouts =
        transitions.map { it.id to textMeasurer.measure(it.label(kind), transitionLabelStyle) }
    val width = layouts.maxOf { it.second.size.width }.toFloat()
    val height = layouts.sumOf { it.second.size.height }.toFloat()
    val extent = abs(normal.x) * width / 2 + abs(normal.y) * height / 2
    val center = anchor + normal * (LABEL_GAP + extent)
    var top = center.y - height / 2
    val labels =
        layouts.map { (id, layout) ->
            val line =
                TransitionLabelLine(id, layout, Offset(center.x - layout.size.width / 2f, top))
            top += layout.size.height
            line
        }
    val (tip, direction) = shape.arrow()
    return TransitionEdge(
        transitionIds = transitions.map { it.id },
        sourceCenter = sourceCenter,
        targetCenter = targetCenter,
        shape = shape,
        tip = tip,
        direction = direction,
        points = shape.samplePoints(),
        labels = labels,
    )
}

private fun freeDirection(occupied: List<Float>, preferred: Float, clearance: Float): Float {
    if (occupied.none { angleBetween(it, preferred) < clearance }) return preferred
    val sorted = occupied.map(::normalizeAngle).sorted()
    var bestStart = sorted.last()
    var bestGap = sorted.first() + TWO_PI - sorted.last()
    sorted.zipWithNext { a, b ->
        if (b - a > bestGap) {
            bestGap = b - a
            bestStart = a
        }
    }
    return bestStart + bestGap / 2
}

private fun normalizeAngle(angle: Float): Float = ((angle % TWO_PI) + TWO_PI) % TWO_PI

private fun angleBetween(a: Float, b: Float): Float {
    val difference = normalizeAngle(a - b)
    return min(difference, TWO_PI - difference)
}

private fun toRadians(degrees: Float): Float = Math.toRadians(degrees.toDouble()).toFloat()

private fun angleOf(vector: Offset): Float = atan2(vector.y, vector.x)

private fun unitVector(angle: Float): Offset = Offset(cos(angle), sin(angle))

private fun dot(a: Offset, b: Offset): Float = a.x * b.x + a.y * b.y

private fun Offset.normalizedOrNull(): Offset? {
    val length = getDistance()
    return if (length < 0.001f) null else this / length
}
