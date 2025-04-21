package fit.spotted.app.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign

/**
 * Web implementation of the ImageConverter interface.
 * 
 * Note: Full image conversion is not implemented for web in this version.
 * This is a placeholder implementation that shows a message instead.
 */
class WasmJsImageConverter : ImageConverter {
    override fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap? {
        // Web implementation would typically use browser APIs to convert ByteArray to an image
        // For now, we return null as this functionality is not implemented
        return null
    }

    @Composable
    override fun ByteArrayImage(
        bytes: ByteArray,
        modifier: Modifier,
        contentDescription: String?
    ) {
        // Since we can't convert the ByteArray to an image in this implementation,
        // we'll display a placeholder with information about the image
        Box(
            modifier = modifier.background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Photo captured (${bytes.size / 1024} KB)",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Actual implementation of getImageConverter for wasmJs.
 */
actual fun getImageConverter(): ImageConverter = WasmJsImageConverter()
