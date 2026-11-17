package com.jder.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

private val compactMaxWidth = 600.dp
private val compactMaxHeight = 480.dp

@Composable fun isCompactWidth(): Boolean = windowSizeMatches { it.width < compactMaxWidth }

@Composable fun isCompactHeight(): Boolean = windowSizeMatches { it.height < compactMaxHeight }

@Composable
fun windowSizeMatches(predicate: (DpSize) -> Boolean): Boolean {
    val windowInfo = LocalWindowInfo.current
    val matches by
        remember(windowInfo) {
            derivedStateOf { windowInfo.containerDpSize.let { it.isSpecified && predicate(it) } }
        }
    return matches
}
