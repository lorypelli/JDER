package com.jder.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.jder.strings.AppStrings
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

fun encodePng(imageBitmap: ImageBitmap): ByteArray {
    val image = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
    val data =
        try {
            image.encodeToData(EncodedImageFormat.PNG)
        } finally {
            image.close()
        } ?: throw Exception(AppStrings.IMAGE_ENCODING_FAILED)
    try {
        return data.bytes
    } finally {
        data.close()
    }
}
