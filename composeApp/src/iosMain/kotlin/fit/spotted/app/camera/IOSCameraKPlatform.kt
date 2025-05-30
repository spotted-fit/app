package fit.spotted.app.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.*
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * iOS implementation of CameraKPlatform.
 */
class IOSCameraKPlatform : CameraKPlatform {
    private var cameraController: CameraController? = null

    @Composable
    override fun CreateCameraPreview(
        modifier: Modifier,
        cameraFacing: CameraFacing,
        onControllerReady: () -> Unit
    ) {
        val cameraControllerState = remember { mutableStateOf<CameraController?>(null) }

        Box(modifier = modifier) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraConfiguration = {
                    setCameraLens(
                        when (cameraFacing) {
                            CameraFacing.BACK -> CameraLens.BACK
                            CameraFacing.FRONT -> CameraLens.FRONT
                        }
                    )
                    setFlashMode(FlashMode.OFF)
                    setImageFormat(ImageFormat.JPEG)
                    setDirectory(Directory.PICTURES)
                    setTorchMode(TorchMode.OFF)
                },
                onCameraControllerReady = { controller ->
                    cameraController = controller
                    cameraControllerState.value = controller
                    onControllerReady()
                }
            )
        }
    }

    override fun toggleCameraLens() {
        cameraController?.toggleCameraLens()
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun captureImage(): Result<CapturedImage> {
        val controller = cameraController ?: return Result.failure(
            IllegalStateException("Camera controller is not initialized")
        )

        return when (val result = controller.takePicture()) {
            is ImageCaptureResult.Success -> {
                val bitmap = result.byteArray.decodeToImageBitmap()
                Result.success(CapturedImage(bitmap, result.byteArray))
            }

            is ImageCaptureResult.Error -> {
                Result.failure(result.exception)
            }
        }
    }

    override fun releaseResources() {
        cameraController = null
    }
}
