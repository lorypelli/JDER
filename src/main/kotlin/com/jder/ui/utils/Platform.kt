package com.jder.ui.utils

val isMacOS: Boolean = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

fun shortcutLabel(key: String, shift: Boolean = false): String =
    if (isMacOS) "⌘" + (if (shift) "⇧" else "") + key
    else "Ctrl+" + (if (shift) "Shift+" else "") + key
