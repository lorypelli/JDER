package com.jder.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jder.strings.AppStrings
import com.jder.strings.CommonStrings
import com.jder.ui.utils.loadImageResource

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val icon by produceState<ImageBitmap?>(null) { value = loadImageResource("jder_icon.png") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(modifier = Modifier.size(72.dp)) {
                icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(72.dp)) }
            }
        },
        title = { Text("JDER") },
        text = {
            Text("Java Diagrammi E/R\n\n${AppStrings.ABOUT_TEXT}", textAlign = TextAlign.Center)
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(CommonStrings.CLOSE) } },
    )
}
