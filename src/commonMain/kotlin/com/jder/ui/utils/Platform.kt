package com.jder.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.UriHandler
import com.jder.strings.FileStrings
import com.jder.ui.components.Toasts
import kotlin.coroutines.cancellation.CancellationException
import org.jetbrains.skia.Image

expect val isMacOS: Boolean

expect val usesSystemMenuBar: Boolean

expect val systemLanguage: String

expect fun copyToClipboard(text: String)

expect fun printError(message: String)

expect suspend fun readResource(path: String): ByteArray

fun shortcutLabel(key: String, hasShift: Boolean = false): String =
    if (isMacOS) "⌘${if (hasShift) "⇧" else ""}$key"
    else "Ctrl+${if (hasShift) "Shift+" else ""}$key"

fun decodeImage(bytes: ByteArray): ImageBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()

suspend fun loadImageResource(path: String, onFailure: (Exception) -> Unit = {}): ImageBitmap? =
    try {
        decodeImage(readResource(path))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onFailure(e)
        null
    }

fun fileName(path: String): String = path.substringAfterLast('/').substringAfterLast('\\')

fun UriHandler.openSafely(url: String) {
    try {
        openUri(url)
    } catch (e: Exception) {
        Toasts.error(FileStrings.cannotOpen(url), e)
    }
}
