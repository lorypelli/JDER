package com.jder.ui.components

import com.jder.strings.FileStrings

private const val INVALID_FILE_NAME_CHARACTERS = "/\\:*?\"<>|"

fun baseFileName(input: String, extension: String): String {
    val trimmed = input.trim()
    return if (trimmed.endsWith(extension, ignoreCase = true)) {
        trimmed.dropLast(extension.length).trim()
    } else {
        trimmed
    }
}

fun fileNameError(baseName: String): String? =
    when {
        baseName.isBlank() -> FileStrings.FILE_NAME_REQUIRED
        baseName.any { it in INVALID_FILE_NAME_CHARACTERS } -> FileStrings.FILE_NAME_INVALID
        else -> null
    }
