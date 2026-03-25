package com.jder.domain.model
import kotlinx.serialization.Serializable
@Serializable
enum class ClassEntityType(val stereotype: String) {
    CLASS(""),
    INTERFACE("<<interface>>"),
    ENUM("<<enum>>"),
    ABSTRACT_CLASS("<<abstract>>")
}
@Serializable
enum class Visibility(val symbol: String) {
    PUBLIC("+"),
    PRIVATE("-"),
    PROTECTED("#"),
    PACKAGE("~")
}
@Serializable
data class ClassMember(
    val id: String,
    val name: String,
    val type: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val isStatic: Boolean = false,
    val isAbstract: Boolean = false
)
@Serializable
enum class ClassRelationType(val display: String) {
    ASSOCIATION("Associazione"),
    AGGREGATION("Aggregazione"),
    COMPOSITION("Composizione"),
    INHERITANCE("Ereditarietà"),
    REALIZATION("Realizzazione"),
    DEPENDENCY("Dipendenza")
}
@Serializable
data class ClassRelation(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val type: ClassRelationType,
    val sourceMultiplicity: String = "",
    val targetMultiplicity: String = "",
    val label: String = ""
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
    val documentation: String = ""
)
@Serializable
data class ClassDiagram(
    val name: String,
    val classes: List<ClassEntity> = emptyList(),
    val relations: List<ClassRelation> = emptyList(),
    val notes: List<Note> = emptyList()
)
