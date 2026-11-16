package com.jder.data

import androidx.compose.ui.graphics.ImageBitmap
import java.io.File

fun exportPng(imageBitmap: ImageBitmap, file: File): Result<Unit> =
    try {
        file.writeBytesAtomically(encodePng(imageBitmap))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
