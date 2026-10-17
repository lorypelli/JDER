package com.jder.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.jder.domain.model.LogicDiagram
import com.jder.domain.model.snapToLogicGrid
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val ROUTE_STEP = 10f
private const val OBSTACLE_MARGIN = 5f
private const val STUB_INSET = OBSTACLE_MARGIN + 1f
private const val OBSTACLE_PENALTY = 10_000f
private const val OVERLAP_PENALTY = 1_000f
private const val CROSSING_PENALTY = 30f
private const val BEND_PENALTY = 5f

private class RouteSegment(val start: Offset, val end: Offset, val net: String) {
  val isHorizontal = start.y == end.y
  val minX = min(start.x, end.x)
  val maxX = max(start.x, end.x)
  val minY = min(start.y, end.y)
  val maxY = max(start.y, end.y)
}

private class RouteCandidate(val points: List<Offset>, val preference: Float)

private class PricedRoute(val points: List<Offset>, val cost: Float)

private object LogicRouteCache {
  private var diagram: LogicDiagram? = null
  private var routes: Map<String, List<Offset>> = emptyMap()

  @Synchronized
  fun routesFor(diagram: LogicDiagram): Map<String, List<Offset>> {
    if (diagram !== this.diagram) {
      routes = computeRoutes(diagram)
      this.diagram = diagram
    }
    return routes
  }
}

internal fun logicWireRoutes(diagram: LogicDiagram): Map<String, List<Offset>> =
    LogicRouteCache.routesFor(diagram)

internal fun logicWirePoints(start: Offset, end: Offset): List<Offset> =
    routeCandidates(start, end, Rect(start, 0f), Rect(end, 0f))
        .minBy { it.preference + (it.points.size - 2) * BEND_PENALTY }
        .points

private fun computeRoutes(diagram: LogicDiagram): Map<String, List<Offset>> {
  val componentsById = diagram.components.associateBy { it.id }
  val obstacles = diagram.components.map { it.bounds().inflate(OBSTACLE_MARGIN) }
  val placed = mutableListOf<RouteSegment>()
  val routes = LinkedHashMap<String, List<Offset>>()
  diagram.wires.forEach { wire ->
    val source = componentsById[wire.sourceId] ?: return@forEach
    val target = componentsById[wire.targetId] ?: return@forEach
    if (!source.type.hasOutput || wire.targetPin !in 0 until target.inputPinCount) return@forEach
    val start = source.outputPinPosition()
    val end = target.inputPinPosition(wire.targetPin)
    val sourceBounds = source.bounds()
    val targetBounds = target.bounds()
    val area =
        Rect(
            min(start.x, end.x) - 60f,
            min(sourceBounds.top, targetBounds.top) - 60f,
            max(start.x, end.x) + 60f,
            max(sourceBounds.bottom, targetBounds.bottom) + 60f,
        )
    val nearObstacles = obstacles.filter { it.overlaps(area) }
    val nearSegments =
        placed.filter {
          it.net != wire.sourceId &&
              it.maxX >= area.left &&
              it.minX <= area.right &&
              it.maxY >= area.top &&
              it.minY <= area.bottom
        }
    fun cheapest(candidates: List<RouteCandidate>) =
        candidates
            .map {
              PricedRoute(
                  it.points,
                  it.preference + routeCost(it.points, nearObstacles, nearSegments),
              )
            }
            .minBy { it.cost }
    var best = cheapest(routeCandidates(start, end, sourceBounds, targetBounds))
    if (best.cost >= OVERLAP_PENALTY && end.x - start.x >= 2 * LOGIC_LEAD_LENGTH) {
      val detour = cheapest(detourCandidates(start, end, sourceBounds, targetBounds))
      if (detour.cost < best.cost) best = detour
    }
    routes[wire.id] = best.points
    placed +=
        best.points
            .zipWithNext { a, b -> RouteSegment(a, b, wire.sourceId) }
            .filter { it.start != it.end }
  }
  return routes
}

private fun routeCandidates(
    start: Offset,
    end: Offset,
    source: Rect,
    target: Rect,
): List<RouteCandidate> {
  val candidates = mutableListOf<RouteCandidate>()
  if (end.x - start.x >= 2 * LOGIC_LEAD_LENGTH) {
    if (start.y == end.y) candidates += RouteCandidate(listOf(start, end), 0f)
    val preferred = snapToLogicGrid((start.x + end.x) / 2)
    var x = start.x + LOGIC_LEAD_LENGTH
    while (x <= end.x - LOGIC_LEAD_LENGTH) {
      candidates +=
          RouteCandidate(
              listOf(start, Offset(x, start.y), Offset(x, end.y), end),
              abs(x - preferred) * 0.5f,
          )
      x += ROUTE_STEP
    }
    return candidates
  }
  val top = min(source.top, target.top)
  val bottom = max(source.bottom, target.bottom)
  val middle = snapToLogicGrid((start.y + end.y) / 2)
  val levels = buildList {
    for (step in 1..3) {
      add(bottom + ROUTE_STEP * (step + 1))
      add(top - ROUTE_STEP * (step + 1))
    }
    if (abs(start.y - end.y) >= 4 * ROUTE_STEP) {
      for (step in -2..2) add(middle + step * ROUTE_STEP)
    }
  }
  for (outStep in 2..4) {
    for (inStep in 2..4) {
      val outX = start.x + outStep * ROUTE_STEP
      val inX = end.x - inStep * ROUTE_STEP
      levels.forEach { level ->
        candidates +=
            RouteCandidate(
                listOf(
                    start,
                    Offset(outX, start.y),
                    Offset(outX, level),
                    Offset(inX, level),
                    Offset(inX, end.y),
                    end,
                ),
                (outStep + inStep) * 2f + abs(level - (start.y + end.y) / 2) * 0.3f,
            )
      }
    }
  }
  return candidates
}

private fun detourCandidates(
    start: Offset,
    end: Offset,
    source: Rect,
    target: Rect,
): List<RouteCandidate> {
  val candidates = mutableListOf<RouteCandidate>()
  val middle = (start.y + end.y) / 2
  val firstLevel = snapToLogicGrid(min(source.top, target.top) - 60f)
  val lastLevel = snapToLogicGrid(max(source.bottom, target.bottom) + 60f)
  for (outStep in listOf(1, 2, 4)) {
    for (inStep in listOf(1, 2, 4)) {
      val outX = start.x + outStep * ROUTE_STEP
      val inX = end.x - inStep * ROUTE_STEP
      if (inX < outX) continue
      var level = firstLevel
      while (level <= lastLevel) {
        if (level != start.y && level != end.y) {
          candidates +=
              RouteCandidate(
                  listOf(
                      start,
                      Offset(outX, start.y),
                      Offset(outX, level),
                      Offset(inX, level),
                      Offset(inX, end.y),
                      end,
                  ),
                  (outStep + inStep) * 2f + abs(level - middle) * 0.3f,
              )
        }
        level += ROUTE_STEP
      }
    }
  }
  return candidates
}

private fun routeCost(
    points: List<Offset>,
    obstacles: List<Rect>,
    segments: List<RouteSegment>,
): Float {
  var cost = (points.size - 2) * BEND_PENALTY
  val last = points.size - 2
  for (index in 0..last) {
    val a = points[index]
    val b = points[index + 1]
    if (a == b) continue
    val direction = (b - a) / (b - a).getDistance()
    val from = if (index == 0) a + direction * STUB_INSET else a
    val to = if (index == last) b - direction * STUB_INSET else b
    obstacles.forEach { if (segmentHitsRect(from, to, it)) cost += OBSTACLE_PENALTY }
    segments.forEach {
      when {
        overlapsCollinear(a, b, it) -> cost += OVERLAP_PENALTY
        crosses(a, b, it) -> cost += CROSSING_PENALTY
      }
    }
  }
  return cost
}

private fun segmentHitsRect(a: Offset, b: Offset, rect: Rect): Boolean =
    if (a.y == b.y) {
      a.y > rect.top && a.y < rect.bottom && max(a.x, b.x) > rect.left && min(a.x, b.x) < rect.right
    } else {
      a.x > rect.left && a.x < rect.right && max(a.y, b.y) > rect.top && min(a.y, b.y) < rect.bottom
    }

private fun overlapsCollinear(a: Offset, b: Offset, segment: RouteSegment): Boolean {
  val isHorizontal = a.y == b.y
  if (isHorizontal != segment.isHorizontal) return false
  return if (isHorizontal) {
    abs(a.y - segment.start.y) < 1f &&
        min(max(a.x, b.x), segment.maxX) - max(min(a.x, b.x), segment.minX) > 1f
  } else {
    abs(a.x - segment.start.x) < 1f &&
        min(max(a.y, b.y), segment.maxY) - max(min(a.y, b.y), segment.minY) > 1f
  }
}

private fun crosses(a: Offset, b: Offset, segment: RouteSegment): Boolean {
  val isHorizontal = a.y == b.y
  if (isHorizontal == segment.isHorizontal) return false
  return if (isHorizontal) {
    segment.start.x > min(a.x, b.x) &&
        segment.start.x < max(a.x, b.x) &&
        a.y > segment.minY &&
        a.y < segment.maxY
  } else {
    segment.start.y > min(a.y, b.y) &&
        segment.start.y < max(a.y, b.y) &&
        a.x > segment.minX &&
        a.x < segment.maxX
  }
}

internal class LogicJunction(val net: String, val point: Offset)

internal fun logicJunctions(routesByNet: Map<String, List<List<Offset>>>): List<LogicJunction> {
  val junctions = mutableListOf<LogicJunction>()
  for (net in routesByNet.keys) {
    val netRoutes = routesByNet.getValue(net)
    if (netRoutes.size < 2) continue
    val segments =
        netRoutes
            .flatMap { points -> points.zipWithNext { a, b -> RouteSegment(a, b, net) } }
            .filter { it.start != it.end }
    netRoutes
        .flatMap { it.subList(1, it.size - 1) }
        .distinct()
        .forEach { point ->
          var arms = 0
          val horizontal = segments.filter { it.isHorizontal && abs(it.start.y - point.y) < 0.5f }
          val vertical =
              segments.filter { it.start.x == it.end.x && abs(it.start.x - point.x) < 0.5f }
          if (horizontal.any { it.minX < point.x - 0.5f && it.maxX >= point.x - 0.5f }) arms++
          if (horizontal.any { it.maxX > point.x + 0.5f && it.minX <= point.x + 0.5f }) arms++
          if (vertical.any { it.minY < point.y - 0.5f && it.maxY >= point.y - 0.5f }) arms++
          if (vertical.any { it.maxY > point.y + 0.5f && it.minY <= point.y + 0.5f }) arms++
          if (arms >= 3) junctions += LogicJunction(net, point)
        }
  }
  return junctions
}
