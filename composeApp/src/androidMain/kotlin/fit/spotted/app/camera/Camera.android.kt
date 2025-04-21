package fit.spotted.app.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Android implementation of the Camera interface using CameraX.
 */
class AndroidCamera : Camera {
    private var imageCapture: ImageCapture? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var onPhotoCapturedCallback: ((ByteArray) -> Unit)? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentCameraFacing = CameraFacing.BACK
    private var lifecycleOwnerRef: androidx.lifecycle.LifecycleOwner? = null
    private var previewUseCase: Preview? = null

    @Composable
    override fun CameraPreview(
        modifier: Modifier,
        onPhotoCaptured: (ByteArray) -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Store the lifecycle owner for later use when switching cameras
        lifecycleOwnerRef = lifecycleOwner

        // Store the callback
        onPhotoCapturedCallback = onPhotoCaptured

        // Permission state
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // Permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

        // Request permission if not granted
        LaunchedEffect(Unit) {
            if (!hasCameraPermission) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        Box(modifier = modifier) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            // Set the scale type to FIT_CENTER to ensure the preview
                            // stays within the bounds of the view
                            scaleType = PreviewView.ScaleType.FIT_CENTER
                            // This ensures the preview is centered and scaled to fit
                            // without extending beyond the view boundaries
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        previewUseCase = Preview.Builder().build()
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        previewUseCase?.surfaceProvider = previewView.surfaceProvider

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            cameraProvider = cameraProviderFuture.get()
                            bindCamera()
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = { previewView ->
                        // This will be called when cameraFacingState changes
                        // Rebind camera with the new facing direction
                        bindCamera()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show message when permission is not granted
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required to use this feature")
                }
            }
        }
    }

    private fun bindCamera() {
        val cameraProvider = cameraProvider ?: return
        val lifecycleOwner = lifecycleOwnerRef ?: return
        val preview = previewUseCase ?: return
        val imageCapture = imageCapture ?: return

        try {
            // Unbind any existing use cases
            cameraProvider.unbindAll()

            // Select camera based on current facing direction
            val cameraSelector = when (currentCameraFacing) {
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            }

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("AndroidCamera", "Use case binding failed", e)
        }
    }

    override fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        // Get the image buffer
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)

                        // Get the rotation value from the ImageProxy
                        val rotation = image.imageInfo.rotationDegrees

                        // Process the image using ImageProxy for rotation and mirroring
                        val processedBytes = processImageBytes(bytes, rotation, currentCameraFacing)

                        onPhotoCapturedCallback?.invoke(processedBytes)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("AndroidCamera", "Photo capture failed", exception)
                }
            }
        )
    }

    /**
     * Process image bytes to apply rotation and mirroring based on camera facing direction.
     * 
     * @param bytes The original image bytes
     * @param rotation The rotation value from ImageProxy
     * @param cameraFacing The camera facing direction
     * @return The processed image bytes
     */
    private fun processImageBytes(bytes: ByteArray, rotation: Int, cameraFacing: CameraFacing): ByteArray {
        try {
            // Convert ByteArray to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            // Create a matrix for transformation
            val matrix = Matrix()

            // Apply rotation
            matrix.postRotate(rotation.toFloat())

            // For front camera, mirror the image horizontally
            if (cameraFacing == CameraFacing.FRONT) {
                matrix.postScale(-1f, 1f)
            }

            // Create a new bitmap with the applied transformations
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 
                0, 
                0, 
                bitmap.width, 
                bitmap.height, 
                matrix, 
                true
            )

            // Convert back to ByteArray
            val outputStream = java.io.ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("AndroidCamera", "Failed to process image", e)
            return bytes // Return original bytes if processing fails
        }
    }

    override fun release() {
        imageCapture = null
        onPhotoCapturedCallback = null
        cameraProvider = null
        lifecycleOwnerRef = null
        previewUseCase = null
    }

    override fun switchCamera(): CameraFacing {
        currentCameraFacing = when (currentCameraFacing) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }

        // Rebind camera with new facing direction
        bindCamera()

        return currentCameraFacing
    }

    override fun getCurrentCameraFacing(): CameraFacing {
        return currentCameraFacing
    }
}

/**
 * Actual implementation of getCamera for Android.
 */
actual fun getCamera(): Camera = AndroidCamera()
