package fit.spotted.app.utils

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android implementation of the ImageConverter interface.
 */
class AndroidImageConverter : ImageConverter {
    override fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap? {
        return try {
            // Convert ByteArray to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            // Convert Bitmap to ImageBitmap
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    override fun ByteArrayImage(
        bytes: ByteArray,
        modifier: Modifier,
        contentDescription: String?
    ) {
        val imageBitmap = byteArrayToImageBitmap(bytes)
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }
}

/**
 * Actual implementation of getImageConverter for Android.
 */
actual fun getImageConverter(): ImageConverter = AndroidImageConverter()