package com.jder.data
import com.jder.domain.model.UseCaseDiagram
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
class UseCaseRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    fun saveDiagram(diagram: UseCaseDiagram, file: File): Result<Unit> = try {
        file.writeText(json.encodeToString(diagram))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    fun loadDiagram(file: File): Result<UseCaseDiagram> = try {
        Result.success(json.decodeFromString<UseCaseDiagram>(file.readText()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
