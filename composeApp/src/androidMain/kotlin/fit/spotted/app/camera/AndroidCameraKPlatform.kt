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
import java.io.ByteArrayOutputStream

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
        }
    }

    /**
     * Compresses an image byte array to reduce its size.
     * 
     * @param imageByteArray The original image byte array
     * @param quality The compression quality (0-100)
     * @return The compressed image byte array
     */
    @OptIn(ExperimentalResourceApi::class)
    private fun compressImage(imageByteArray: ByteArray, quality: Int = 80): ByteArray {
        // Decode the byte array to a bitmap
        val bitmap = imageByteArray.decodeToImageBitmap().asAndroidBitmap()

        // Create a new byte array output stream
        val outputStream = ByteArrayOutputStream()

        // Compress the bitmap to JPEG format with the specified quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Return the compressed byte array
        return outputStream.toByteArray()
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun captureImage(): Result<CapturedImage> {
        val controller = cameraController ?: return Result.failure(
            IllegalStateException("Camera controller is not initialized")
        )

        return when (val result = controller.takePicture()) {
            is ImageCaptureResult.Success -> {
                // Compress the image before returning
                val compressedByteArray = compressImage(result.byteArray)
                var bitmap = compressedByteArray.decodeToImageBitmap()
                var finalByteArray = compressedByteArray

                if (currentCameraLens == CameraLens.FRONT) {
                    bitmap = flipImageHorizontally(bitmap)
                    // Convert the flipped bitmap back to a byte array
                    val outputStream = ByteArrayOutputStream()
                    bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    finalByteArray = outputStream.toByteArray()
                }

                Result.success(CapturedImage(bitmap, finalByteArray))
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
