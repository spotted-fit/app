package fit.spotted.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Enum representing camera lens facing direction.
 */
enum class CameraFacing {
    BACK,
    FRONT
}

/**
 * Common interface for camera functionality across platforms.
 */
interface Camera {
    /**
     * Provides a composable that displays the camera preview.
     *
     * @param modifier Modifier to be applied to the camera preview
     * @param onPhotoCaptured Callback that is invoked when a photo is captured
     */
    @Composable
    fun Preview(
        modifier: Modifier,
        onPhotoCaptured: (CapturedImage) -> Unit
    )

    /**
     * Takes a photo using the camera.
     */
    fun takePhoto()

    /**
     * Releases camera resources.
     */
    fun release()

    /**
     * Switches between front and back cameras.
     *
     * @return The new camera facing direction after switching
     */
    fun switchCamera(): CameraFacing

    /**
     * Gets the current camera facing direction.
     *
     * @return The current camera facing direction
     */
    fun getCurrentCameraFacing(): CameraFacing
}

/**
 * Expect function to get the platform-specific camera implementation.
 */
expect fun getCamera(): Camera
