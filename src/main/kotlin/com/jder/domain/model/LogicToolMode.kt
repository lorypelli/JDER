package com.jder.domain.model

enum class LogicToolMode(val componentType: LogicComponentType? = null) {
    SELECT,
    INPUT(LogicComponentType.INPUT),
    OUTPUT(LogicComponentType.OUTPUT),
    AND(LogicComponentType.AND),
    OR(LogicComponentType.OR),
    NOT(LogicComponentType.NOT),
    NAND(LogicComponentType.NAND),
    NOR(LogicComponentType.NOR),
    XOR(LogicComponentType.XOR),
    XNOR(LogicComponentType.XNOR),
    WIRE,
    NOTE,
}
