package com.jder.data
import com.jder.domain.model.ClassDiagram
import kotlinx.serialization.encodeToString
import java.io.File
class ClassDiagramRepository {
    fun saveDiagram(diagram: ClassDiagram, file: File): Result<Unit> = try {
        file.writeText(diagramJson.encodeToString(diagram))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    fun loadDiagram(file: File): Result<ClassDiagram> = try {
        Result.success(diagramJson.decodeFromString<ClassDiagram>(file.readText()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
