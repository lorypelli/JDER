package com.jder.ui.utils

val isMacOS: Boolean = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

fun shortcutLabel(key: String, hasShift: Boolean = false): String =
    if (isMacOS) "⌘${if (hasShift) "⇧" else ""}$key"
    else "Ctrl+${if (hasShift) "Shift+" else ""}$key"
