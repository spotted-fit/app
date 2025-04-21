package fit.spotted.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Common interface for camera functionality across platforms.
 */
interface Camera {
    /**
     * Provides a composable that displays the camera preview.
     *
     * @param modifier Modifier to be applied to the camera preview
     * @param onPhotoCaptured Callback that is invoked when a photo is captured, with the photo data as a ByteArray
     */
    @Composable
    fun CameraPreview(
        modifier: Modifier,
        onPhotoCaptured: (ByteArray) -> Unit
    )
    
    /**
     * Takes a photo using the camera.
     */
    fun takePhoto()
    
    /**
     * Releases camera resources.
     */
    fun release()
}

/**
 * Expect function to get the platform-specific camera implementation.
 */
expect fun getCamera(): Camera