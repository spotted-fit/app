package fit.spotted.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Common implementation of Camera interface that delegates platform-specific operations
 * to a CameraKPlatform implementation.
 */
class CommonCameraK(private val platform: CameraKPlatform) : Camera {
    private var currentCameraFacing = CameraFacing.BACK
    private var onPhotoCapturedCallback: ((CapturedImage) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @Composable
    override fun Preview(
        modifier: Modifier,
        onPhotoCaptured: (CapturedImage) -> Unit
    ) {
        onPhotoCapturedCallback = onPhotoCaptured

        platform.CreateCameraPreview(
            modifier = modifier,
            cameraFacing = currentCameraFacing,
            onControllerReady = { /* Controller is ready */ }
        )
    }

    override fun takePhoto() {
        val callback = onPhotoCapturedCallback ?: return

        coroutineScope.launch {
            handleImageCapture(callback)
        }
    }

    private suspend fun handleImageCapture(
        onImageCaptured: (CapturedImage) -> Unit
    ) {
        platform.captureImage().fold(
            onSuccess = { capturedImage ->
                onImageCaptured(capturedImage)
            },
            onFailure = { exception ->
                println("Image Capture Error: ${exception.message}")
            }
        )
    }

    override fun release() {
        platform.releaseResources()
        onPhotoCapturedCallback = null
    }

    override fun switchCamera(): CameraFacing {
        currentCameraFacing = when (currentCameraFacing) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }
        platform.toggleCameraLens()
        return currentCameraFacing
    }

    override fun getCurrentCameraFacing(): CameraFacing {
        return currentCameraFacing
    }
}
