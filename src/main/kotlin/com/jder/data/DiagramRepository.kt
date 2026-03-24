package com.jder.data
import com.jder.domain.model.ERDiagram
import kotlinx.serialization.encodeToString
import java.io.File
class DiagramRepository {
    fun saveDiagram(diagram: ERDiagram, file: File): Result<Unit> = try {
        file.writeText(diagramJson.encodeToString(diagram))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    fun loadDiagram(file: File): Result<ERDiagram> = try {
        Result.success(diagramJson.decodeFromString<ERDiagram>(file.readText()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
