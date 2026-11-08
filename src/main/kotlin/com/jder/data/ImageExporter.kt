package com.jder.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.jder.strings.AppStrings
import java.io.File
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

class ImageExporter {
    fun exportToPNG(imageBitmap: ImageBitmap, file: File): Result<Unit> =
        try {
            val data =
                Image.makeFromBitmap(imageBitmap.asSkiaBitmap()).use {
                    it.encodeToData(EncodedImageFormat.PNG)
                }
            data?.use {
                file.writeBytesAtomically(it.bytes)
                Result.success(Unit)
            } ?: Result.failure(Exception(AppStrings.IMAGE_ENCODING_FAILED))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
