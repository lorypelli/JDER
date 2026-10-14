package com.jder.data

import com.jder.domain.model.UseCaseDiagram
import java.io.File
import kotlinx.serialization.encodeToString

class UseCaseRepository {
  fun saveDiagram(diagram: UseCaseDiagram, file: File): Result<Unit> =
      try {
        file.writeText(diagramJson.encodeToString(diagram))
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }

  fun loadDiagram(file: File): Result<UseCaseDiagram> =
      try {
        Result.success(diagramJson.decodeFromString<UseCaseDiagram>(file.readText()))
      } catch (e: Exception) {
        Result.failure(e)
      }
}
