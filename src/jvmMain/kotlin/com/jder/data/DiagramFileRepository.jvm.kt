package com.jder.data

import com.jder.domain.model.DiagramType
import com.jder.strings.AppStrings
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun <T> DiagramFileRepository<T>.saveDiagram(diagram: T, file: File): Result<Unit> =
    try {
        file.writeBytesAtomically(encodeDiagram(diagram).toByteArray())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

fun <T> DiagramFileRepository<T>.loadDiagram(file: File): Result<T> =
    try {
        Result.success(decodeDiagram(file.readText()))
    } catch (e: Exception) {
        Result.failure(e)
    }

fun detectDiagramType(file: File): DiagramType? = detectDiagramType(file.readText())

internal fun File.writeBytesAtomically(bytes: ByteArray) {
    val target = toPath()
    if (Files.exists(target) && !Files.isWritable(target)) {
        throw AccessDeniedException(this, reason = AppStrings.READ_ONLY_FILE)
    }
    val temporary = File.createTempFile(".$name.", ".tmp", absoluteFile.parentFile).toPath()
    try {
        Files.write(temporary, bytes)
        try {
            Files.move(
                temporary,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temporary)
    }
}
