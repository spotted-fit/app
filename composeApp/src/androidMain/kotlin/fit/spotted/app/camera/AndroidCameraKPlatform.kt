package fit.spotted.app.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.enums.TorchMode
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Android implementation of CameraKPlatform.
 */
class AndroidCameraKPlatform : CameraKPlatform {
    private var cameraController: CameraController? = null
    private var currentCameraLens: CameraLens = CameraLens.BACK

    /**
     * Flips an ImageBitmap horizontally.
     * 
     * @param bitmap The ImageBitmap to flip
     * @return A new ImageBitmap that is flipped horizontally
     */
    private fun flipImageHorizontally(bitmap: ImageBitmap): ImageBitmap {
        val androidBitmap = bitmap.asAndroidBitmap()
        val matrix = Matrix().apply {
            preScale(-1f, 1f)
        }
        val flippedBitmap = Bitmap.createBitmap(
            androidBitmap,
            0,
            0,
            androidBitmap.width,
            androidBitmap.height,
            matrix,
            true
        )
        return flippedBitmap.asImageBitmap()
    }

    @Composable
    override fun CreateCameraPreview(
        modifier: Modifier,
        cameraFacing: CameraFacing,
        onControllerReady: () -> Unit
    ) {
        val cameraControllerState = remember { mutableStateOf<CameraController?>(null) }

        // Set the current camera lens based on the cameraFacing parameter
        currentCameraLens = when (cameraFacing) {
            CameraFacing.BACK -> CameraLens.BACK
            CameraFacing.FRONT -> CameraLens.FRONT
        }

        Box(modifier = modifier) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraConfiguration = {
                    setCameraLens(currentCameraLens)
                    setFlashMode(FlashMode.OFF)
                    setImageFormat(ImageFormat.PNG)
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
        // Update the current camera lens
        currentCameraLens = when (currentCameraLens) {
            CameraLens.BACK -> CameraLens.FRONT
            CameraLens.FRONT -> CameraLens.BACK
            else -> currentCameraLens
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun captureImage(): Result<CapturedImage> {
        val controller = cameraController ?: return Result.failure(
            IllegalStateException("Camera controller is not initialized")
        )

        return when (val result = controller.takePicture()) {
            is ImageCaptureResult.Success -> {
                var bitmap = result.byteArray.decodeToImageBitmap()

                // If the current camera is the front camera, flip the image horizontally
                if (currentCameraLens == CameraLens.FRONT) {
                    bitmap = flipImageHorizontally(bitmap)
                }

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
