package com.jder.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import com.jder.strings.FileStrings
import com.jder.ui.components.Toasts
import kotlin.coroutines.cancellation.CancellationException
import org.jetbrains.skia.Image

expect val isMacOS: Boolean

expect val usesSystemMenuBar: Boolean

expect val showsToolbarMenus: Boolean

expect val systemLanguage: String

expect fun copyToClipboard(text: String)

expect suspend fun readResource(path: String): ByteArray

expect fun fontFromBytes(name: String, bytes: ByteArray, weight: FontWeight): Font

fun shortcutLabel(key: String, hasShift: Boolean = false): String =
    if (isMacOS) "⌘${if (hasShift) "⇧" else ""}$key"
    else "Ctrl+${if (hasShift) "Shift+" else ""}$key"

fun decodeImage(bytes: ByteArray): ImageBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()

suspend fun loadImageResource(path: String): ImageBitmap? =
    try {
        decodeImage(readResource(path))
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
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
