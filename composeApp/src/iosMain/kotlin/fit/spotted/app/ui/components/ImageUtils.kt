package fit.spotted.app.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

@OptIn(ExperimentalForeignApi::class)
actual fun createImageBitmapFromBytes(bytes: ByteArray): ImageBitmap {
    val image = Image.makeFromEncoded(bytes)
    return image.toComposeImageBitmap()
}

actual suspend fun readFileBytes(file: PlatformFile): ByteArray = withContext(Dispatchers.Default) {
    file.readBytes()
} 