package fit.spotted.app.camera

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data class to hold both the ImageBitmap and the original ByteArray of a captured image.
 */
data class CapturedImage(
    val bitmap: ImageBitmap,
    val byteArray: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CapturedImage

        if (bitmap != other.bitmap) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}