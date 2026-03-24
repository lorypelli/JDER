package com.jder.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class Actor(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float = 50f,
    val height: Float = 90f
)
@Serializable
data class UseCase(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float = 160f,
    val height: Float = 60f,
    val documentation: String = ""
)
@Serializable
enum class UseCaseRelationType(val display: String) {
    ASSOCIATION("Associazione"),
    INCLUDE("<<include>>"),
    EXTEND("<<extend>>"),
    GENERALIZATION("Generalizzazione")
}
@Serializable
data class UseCaseRelation(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val type: UseCaseRelationType
)
@Serializable
data class SystemBoundary(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float = 400f,
    val height: Float = 300f
)
@Serializable
data class UseCaseDiagram(
    val name: String,
    val actors: List<Actor> = emptyList(),
    val useCases: List<UseCase> = emptyList(),
    val relations: List<UseCaseRelation> = emptyList(),
    val notes: List<Note> = emptyList(),
    val systemBoundaries: List<SystemBoundary> = emptyList()
)
