package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.strings.DiagramTypeStrings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val DIAGRAM_TYPE_KEY = "diagramType"

class WrongDiagramTypeException(found: DiagramType, expected: DiagramType) :
    Exception(
        DiagramTypeStrings.wrongDiagramType(found.displayName, found.tabLabel, expected.tabLabel),
    )

open class DiagramFileRepository<T>(
    private val serializer: KSerializer<T>,
    private val type: DiagramType,
    private val validate: (T) -> Unit = {},
) {
    fun encodeDiagram(diagram: T): String {
        validate(diagram)
        val content = diagramJson.encodeToJsonElement(serializer, diagram).jsonObject
        val tagged = JsonObject(mapOf(DIAGRAM_TYPE_KEY to JsonPrimitive(type.key)) + content)
        return diagramJson.encodeToString(JsonObject.serializer(), tagged)
    }

    fun decodeDiagram(text: String): T {
        val content = parseJsonObject(text)
        val found = detectDiagramType(content)
        if (found != null && found != type) throw WrongDiagramTypeException(found, type)
        val diagram = diagramJson.decodeFromJsonElement(serializer, content)
        validate(diagram)
        return diagram
    }
}

fun detectDiagramType(text: String): DiagramType? = detectDiagramType(parseJsonObject(text))

private fun parseJsonObject(text: String): JsonObject =
    diagramJson.parseToJsonElement(text).jsonObject

private fun detectDiagramType(content: JsonObject): DiagramType? {
    val declared = content[DIAGRAM_TYPE_KEY]?.jsonPrimitive?.contentOrNull
    DiagramType.entries
        .find { it.key == declared }
        ?.let {
            return it
        }
    return DiagramType.entries.find { type -> type.markerKeys.any { it in content } }
}
