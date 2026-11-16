package com.jder.domain.model

import com.jder.strings.ClassStrings
import com.jder.strings.CommonStrings
import kotlinx.serialization.Serializable

@Serializable
enum class ClassEntityType(val stereotype: String) {
    CLASS(""),
    INTERFACE("<<interface>>"),
    ENUM("<<enum>>"),
    ABSTRACT_CLASS("<<abstract>>");

    val display: String
        get() =
            when (this) {
                CLASS -> ClassStrings.CLASS
                INTERFACE -> ClassStrings.INTERFACE
                ENUM -> ClassStrings.ENUMERATION
                ABSTRACT_CLASS -> ClassStrings.ABSTRACT_CLASS
            }
}

@Serializable
enum class Visibility(val symbol: String) {
    PUBLIC("+"),
    PRIVATE("-"),
    PROTECTED("#"),
    PACKAGE("~");

    val display: String
        get() =
            when (this) {
                PUBLIC -> ClassStrings.PUBLIC
                PRIVATE -> ClassStrings.PRIVATE
                PROTECTED -> ClassStrings.PROTECTED
                PACKAGE -> "Package"
            }
}

@Serializable
data class ClassMember(
    val id: String,
    val name: String,
    val type: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val isStatic: Boolean = false,
    val isAbstract: Boolean = false,
)

@Serializable
enum class ClassRelationType {
    ASSOCIATION,
    AGGREGATION,
    COMPOSITION,
    INHERITANCE,
    REALIZATION,
    DEPENDENCY;

    val display: String
        get() =
            when (this) {
                ASSOCIATION -> CommonStrings.ASSOCIATION
                AGGREGATION -> ClassStrings.AGGREGATION
                COMPOSITION -> ClassStrings.COMPOSITION
                INHERITANCE -> ClassStrings.INHERITANCE
                REALIZATION -> ClassStrings.REALIZATION
                DEPENDENCY -> ClassStrings.DEPENDENCY
            }
}

@Serializable
data class ClassRelation(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val type: ClassRelationType,
    val sourceMultiplicity: String = "",
    val targetMultiplicity: String = "",
    val label: String = "",
)

@Serializable
data class ClassEntity(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float = 200f,
    val type: ClassEntityType = ClassEntityType.CLASS,
    val attributes: List<ClassMember> = emptyList(),
    val methods: List<ClassMember> = emptyList(),
    val isAbstract: Boolean = false,
    val documentation: String = "",
)

@Serializable
data class ClassDiagram(
    val name: String,
    val classes: List<ClassEntity> = emptyList(),
    val relations: List<ClassRelation> = emptyList(),
    val notes: List<Note> = emptyList(),
)
