package com.jder.ui.utils

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.jder.strings.AppStrings
import com.jder.ui.components.logError
import java.awt.Desktop
import java.awt.Taskbar
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.Locale
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual val isMacOS: Boolean = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

actual val usesSystemMenuBar: Boolean = isMacOS

actual val systemLanguage: String
    get() = Locale.getDefault().language

actual fun copyToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}

actual fun printError(message: String) = System.err.println(message)

actual suspend fun readResource(path: String): ByteArray =
    withContext(Dispatchers.IO) { resourceBytes(path) }

private fun resourceBytes(path: String): ByteArray = ClassLoader.getSystemResource(path).readBytes()

fun setDockIcon(resourcePath: String) {
    if (!isMacOS) return
    try {
        Taskbar.getTaskbar().iconImage = ImageIO.read(ClassLoader.getSystemResource(resourcePath))
    } catch (e: Exception) {
        logError(AppStrings.DOCK_ICON_FAILED, e)
    }
}

fun resourcePainter(resourcePath: String): Painter =
    BitmapPainter(decodeImage(resourceBytes(resourcePath)))

fun ComposeWindow.bringToFront() {
    isMinimized = false
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
        desktop.requestForeground(true)
    } else {
        isAlwaysOnTop = true
        toFront()
        isAlwaysOnTop = false
    }
    requestFocus()
}
