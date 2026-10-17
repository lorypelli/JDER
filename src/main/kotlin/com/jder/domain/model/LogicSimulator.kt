package com.jder.domain.model

class LogicSimulation(
    val values: Map<String, Boolean?>,
    val isStable: Boolean,
    private val drivers: Map<Pair<String, Int>, String>,
) {
  fun outputOf(componentId: String): Boolean? = values[componentId]

  fun inputOf(componentId: String, pin: Int): Boolean? =
      drivers[componentId to pin]?.let { values[it] }

  fun isConnected(componentId: String, pin: Int): Boolean = (componentId to pin) in drivers

  companion object {
    val EMPTY = LogicSimulation(emptyMap(), true, emptyMap())
  }
}

class LogicTruthTableRow(val inputs: List<Boolean>, val outputs: List<Boolean?>)

class LogicTruthTable(
    val inputs: List<LogicComponent>,
    val outputs: List<LogicComponent>,
    val rows: List<LogicTruthTableRow>,
)

private class LogicExpression(val text: String, val precedence: Int)

object LogicSimulator {
  const val MAX_TRUTH_TABLE_INPUTS = 10
  private const val MAX_EXPRESSION_LENGTH = 240
  private const val OR_PRECEDENCE = 1
  private const val XOR_PRECEDENCE = 2
  private const val AND_PRECEDENCE = 3
  private const val ATOM_PRECEDENCE = 4
  private val UNKNOWN = LogicExpression("?", ATOM_PRECEDENCE)

  fun simulate(
      diagram: LogicDiagram,
      previous: Map<String, Boolean?> = emptyMap(),
      inputOverrides: Map<String, Boolean> = emptyMap(),
  ): LogicSimulation {
    val components = diagram.components
    val drivers = driversOf(diagram)
    var values: Map<String, Boolean?> =
        components.associate { component ->
          component.id to
              if (component.type == LogicComponentType.INPUT) {
                inputOverrides[component.id] ?: component.value
              } else {
                previous[component.id]
              }
        }
    val maxIterations = components.size + 8
    repeat(maxIterations) {
      val next = step(components, drivers, values)
      if (next == values) return LogicSimulation(values, true, drivers)
      values = next
    }
    val oscillating = mutableSetOf<String>()
    repeat(maxIterations) {
      val next = step(components, drivers, values)
      next.forEach { (id, value) -> if (value != values[id]) oscillating += id }
      values = next
    }
    values = values.mapValues { (id, value) -> if (id in oscillating) null else value }
    repeat(maxIterations) {
      val next = step(components, drivers, values, oscillating)
      if (next == values) return LogicSimulation(values, false, drivers)
      values = next
    }
    return LogicSimulation(values, false, drivers)
  }

  fun truthTable(diagram: LogicDiagram): LogicTruthTable? {
    val order = compareBy<LogicComponent>({ it.label.lowercase() }, { it.y }, { it.x })
    val inputs = diagram.components.filter { it.type == LogicComponentType.INPUT }.sortedWith(order)
    val outputs =
        diagram.components.filter { it.type == LogicComponentType.OUTPUT }.sortedWith(order)
    if (inputs.size > MAX_TRUTH_TABLE_INPUTS) return null
    val rows =
        (0 until (1 shl inputs.size)).map { combination ->
          val inputValues =
              inputs.indices.map { index -> (combination shr (inputs.size - 1 - index)) and 1 == 1 }
          val simulation =
              simulate(
                  diagram,
                  inputOverrides =
                      inputs.zip(inputValues).associate { (input, value) -> input.id to value },
              )
          LogicTruthTableRow(inputValues, outputs.map { simulation.outputOf(it.id) })
        }
    return LogicTruthTable(inputs, outputs, rows)
  }

  fun expression(diagram: LogicDiagram, componentId: String): String? {
    val byId = diagram.components.associateBy { it.id }
    val root = byId[componentId] ?: return null
    if (root.type == LogicComponentType.INPUT) return null
    val drivers = driversOf(diagram)
    val memo = HashMap<String, LogicExpression>()
    fun build(id: String, visiting: Set<String>): LogicExpression {
      memo[id]?.let {
        return it
      }
      val component = byId[id] ?: return UNKNOWN
      if (id in visiting) return LogicExpression(component.label.ifBlank { "…" }, ATOM_PRECEDENCE)
      val nested = visiting + id
      val operands =
          (0 until component.inputPinCount).map { pin ->
            drivers[id to pin]?.let { build(it, nested) } ?: UNKNOWN
          }
      val result =
          when (component.type) {
            LogicComponentType.INPUT ->
                LogicExpression(component.label.ifBlank { "?" }, ATOM_PRECEDENCE)
            LogicComponentType.OUTPUT -> operands.firstOrNull() ?: UNKNOWN
            LogicComponentType.NOT -> negate(operands.first())
            LogicComponentType.AND -> join(operands, " · ", AND_PRECEDENCE)
            LogicComponentType.NAND -> negate(join(operands, " · ", AND_PRECEDENCE))
            LogicComponentType.OR -> join(operands, " + ", OR_PRECEDENCE)
            LogicComponentType.NOR -> negate(join(operands, " + ", OR_PRECEDENCE))
            LogicComponentType.XOR -> join(operands, " ⊕ ", XOR_PRECEDENCE)
            LogicComponentType.XNOR -> negate(join(operands, " ⊕ ", XOR_PRECEDENCE))
          }
      val bounded =
          if (result.text.length > MAX_EXPRESSION_LENGTH) LogicExpression("…", ATOM_PRECEDENCE)
          else result
      memo[id] = bounded
      return bounded
    }
    return build(componentId, emptySet()).text
  }

  internal fun evaluate(type: LogicComponentType, inputs: List<Boolean?>): Boolean? =
      when (type) {
        LogicComponentType.INPUT -> null
        LogicComponentType.OUTPUT -> inputs.firstOrNull()
        LogicComponentType.NOT -> inputs.firstOrNull()?.not()
        LogicComponentType.AND -> and(inputs)
        LogicComponentType.NAND -> and(inputs)?.not()
        LogicComponentType.OR -> or(inputs)
        LogicComponentType.NOR -> or(inputs)?.not()
        LogicComponentType.XOR -> xor(inputs)
        LogicComponentType.XNOR -> xor(inputs)?.not()
      }

  private fun and(inputs: List<Boolean?>): Boolean? =
      when {
        inputs.any { it == false } -> false
        inputs.any { it == null } -> null
        else -> true
      }

  private fun or(inputs: List<Boolean?>): Boolean? =
      when {
        inputs.any { it == true } -> true
        inputs.any { it == null } -> null
        else -> false
      }

  private fun xor(inputs: List<Boolean?>): Boolean? =
      if (inputs.any { it == null }) null else inputs.count { it == true } % 2 == 1

  private fun step(
      components: List<LogicComponent>,
      drivers: Map<Pair<String, Int>, String>,
      values: Map<String, Boolean?>,
      forcedUnknown: Set<String> = emptySet(),
  ): Map<String, Boolean?> =
      components.associate { component ->
        component.id to
            when {
              component.id in forcedUnknown -> null
              component.type == LogicComponentType.INPUT -> values[component.id]
              else ->
                  evaluate(
                      component.type,
                      (0 until component.inputPinCount).map { pin ->
                        drivers[component.id to pin]?.let { values[it] }
                      },
                  )
            }
      }

  private fun driversOf(diagram: LogicDiagram): Map<Pair<String, Int>, String> {
    val byId = diagram.components.associateBy { it.id }
    val drivers = HashMap<Pair<String, Int>, String>()
    diagram.wires.forEach { wire ->
      val source = byId[wire.sourceId] ?: return@forEach
      val target = byId[wire.targetId] ?: return@forEach
      if (source.type.hasOutput && wire.targetPin in 0 until target.inputPinCount) {
        drivers[wire.targetId to wire.targetPin] = wire.sourceId
      }
    }
    return drivers
  }

  private fun join(
      operands: List<LogicExpression>,
      operator: String,
      precedence: Int,
  ): LogicExpression =
      LogicExpression(
          operands.joinToString(operator) {
            val ambiguous = it.precedence == XOR_PRECEDENCE && precedence == OR_PRECEDENCE
            if (it.precedence < precedence || ambiguous) "(${it.text})" else it.text
          },
          precedence,
      )

  private fun negate(expression: LogicExpression): LogicExpression =
      LogicExpression(
          if (expression.precedence == ATOM_PRECEDENCE) "¬${expression.text}"
          else "¬(${expression.text})",
          ATOM_PRECEDENCE,
      )
}
