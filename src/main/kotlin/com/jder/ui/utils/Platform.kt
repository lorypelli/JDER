package com.jder.ui.utils

import androidx.compose.ui.res.useResource
import java.io.File

val isMacOS: Boolean = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

fun shortcutLabel(key: String, hasShift: Boolean = false): String =
    if (isMacOS) "⌘${if (hasShift) "⇧" else ""}$key"
    else "Ctrl+${if (hasShift) "Shift+" else ""}$key"

fun setDockIcon(resourcePath: String) {
    if (!isMacOS) return
    try {
        val iconFile = File.createTempFile("jder_icon", ".png")
        iconFile.deleteOnExit()
        useResource(resourcePath) { input -> iconFile.outputStream().use { input.copyTo(it) } }
        System.setProperty("apple.awt.application.icon", iconFile.absolutePath)
    } catch (_: Exception) {}
}
