package fit.spotted.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Interface for platform-specific camera functionality.
 * This allows the common CameraK implementation to delegate platform-specific operations.
 */
interface CameraKPlatform {
    /**
     * Creates and configures a camera preview composable.
     * 
     * @param modifier Modifier to be applied to the camera preview
     * @param cameraFacing The current camera facing direction
     * @param onControllerReady Callback when the camera controller is ready
     */
    @Composable
    fun CreateCameraPreview(
        modifier: Modifier,
        cameraFacing: CameraFacing,
        onControllerReady: () -> Unit
    )

    /**
     * Toggles between front and back camera.
     */
    fun toggleCameraLens()

    /**
     * Captures an image from the camera.
     * 
     * @return Result containing the captured image (bitmap and byteArray) or an error
     */
    suspend fun captureImage(): Result<CapturedImage>

    /**
     * Releases camera resources.
     */
    fun releaseResources()
}
