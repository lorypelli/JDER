package com.jder.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jder.ui.utils.fontFromBytes
import com.jder.ui.utils.readResource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private val fonts = mapOf("Roboto-Regular" to FontWeight.Normal, "Roboto-Bold" to FontWeight.Bold)

object AppFont {
    var family: FontFamily = FontFamily.Default
        private set

    suspend fun load() {
        family =
            FontFamily(
                coroutineScope {
                    fonts
                        .map { (name, weight) ->
                            async { fontFromBytes(name, readResource("fonts/$name.woff2"), weight) }
                        }
                        .awaitAll()
                },
            )
    }
}
