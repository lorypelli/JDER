package com.jder.data

import com.jder.domain.model.DiagramType
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val DIAGRAM_TYPE_KEY = "diagramType"

class WrongDiagramTypeException(found: DiagramType, expected: DiagramType) :
    Exception(
        "il file contiene ${found.displayName}: aprilo dalla scheda \"${found.tabLabel}\" " +
            "invece che da \"${expected.tabLabel}\""
    )

open class DiagramFileRepository<T>(
    private val serializer: KSerializer<T>,
    private val type: DiagramType,
) {
    fun saveDiagram(diagram: T, file: File): Result<Unit> =
        try {
            val content = diagramJson.encodeToJsonElement(serializer, diagram).jsonObject
            val tagged = JsonObject(mapOf(DIAGRAM_TYPE_KEY to JsonPrimitive(type.key)) + content)
            file.writeText(diagramJson.encodeToString(JsonObject.serializer(), tagged))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun loadDiagram(file: File): Result<T> =
        try {
            val content = diagramJson.parseToJsonElement(file.readText()).jsonObject
            val found = detectDiagramType(content)
            if (found != null && found != type) throw WrongDiagramTypeException(found, type)
            Result.success(diagramJson.decodeFromJsonElement(serializer, content))
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun detectDiagramType(content: JsonObject): DiagramType? {
        val declared = content[DIAGRAM_TYPE_KEY]?.jsonPrimitive?.contentOrNull
        DiagramType.entries
            .find { it.key == declared }
            ?.let {
                return it
            }
        return DiagramType.entries.find { it.markerKeys.any { it in content } }
    }
}
