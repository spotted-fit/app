package fit.spotted.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Modifier

/**
 * Interface for converting ByteArray to an image that can be displayed in Compose.
 */
interface ImageConverter {
    /**
     * Converts a ByteArray to an ImageBitmap.
     *
     * @param bytes The ByteArray containing the image data
     * @return The converted ImageBitmap, or null if conversion failed
     */
    fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap?

    /**
     * A composable that displays an image from a ByteArray.
     *
     * @param bytes The ByteArray containing the image data
     * @param modifier Modifier to be applied to the image
     * @param contentDescription Content description for accessibility
     */
    @Composable
    fun ByteArrayImage(
        bytes: ByteArray,
        modifier: Modifier = Modifier,
        contentDescription: String? = null
    )
}

/**
 * Expect function to get the platform-specific image converter implementation.
 */
expect fun getImageConverter(): ImageConverter