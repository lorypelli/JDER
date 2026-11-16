package com.jder.domain.model

enum class SequenceToolMode(val lifelineType: SequenceLifelineType? = null) {
    SELECT,
    ACTOR(SequenceLifelineType.ACTOR),
    OBJECT(SequenceLifelineType.OBJECT),
    BOUNDARY(SequenceLifelineType.BOUNDARY),
    CONTROL(SequenceLifelineType.CONTROL),
    ENTITY(SequenceLifelineType.ENTITY),
    MESSAGE,
    FRAGMENT,
    NOTE,
}
