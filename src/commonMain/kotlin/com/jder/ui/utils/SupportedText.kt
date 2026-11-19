package com.jder.ui.utils

private val replacements =
    mapOf(
        '‘' to "'",
        '’' to "'",
        '“' to "\"",
        '”' to "\"",
        '–' to "-",
        '—' to "-",
        '…' to "...",
        ' ' to " ",
        '\t' to " ",
    )

fun isSupportedInput(char: Char): Boolean =
    char == '\n' || char in ' '..'~' || (char in 'À'..'ÿ' && char != '×' && char != '÷')

private fun String.normalizeInput(): String = buildString {
    for (char in this@normalizeInput) {
        val replacement = replacements[char]
        when {
            replacement != null -> append(replacement)
            isSupportedInput(char) -> append(char)
        }
    }
}

fun String.acceptInput(previous: String): String {
    var prefix = commonPrefixWith(previous).length
    if (prefix > 0 && previous[prefix - 1].isHighSurrogate()) prefix--
    var suffix =
        commonSuffixWith(previous).length.coerceAtMost(minOf(length, previous.length) - prefix)
    if (suffix > 0 && this[length - suffix].isLowSurrogate()) suffix--
    return take(prefix) + substring(prefix, length - suffix).normalizeInput() + takeLast(suffix)
}
