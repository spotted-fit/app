package fit.spotted.app.camera

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Android implementation of the Camera interface using CameraX.
 */
class AndroidCamera : Camera {
    private var imageCapture: ImageCapture? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var onPhotoCapturedCallback: ((ByteArray) -> Unit)? = null

    @Composable
    override fun CameraPreview(
        modifier: Modifier,
        onPhotoCaptured: (ByteArray) -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

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
                        val preview = Preview.Builder().build()

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            try {
                                // Unbind any existing use cases
                                cameraProvider.unbindAll()

                                // Bind use cases to camera
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("AndroidCamera", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
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

    override fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    onPhotoCapturedCallback?.invoke(bytes)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("AndroidCamera", "Photo capture failed", exception)
                }
            }
        )
    }

    override fun release() {
        imageCapture = null
        onPhotoCapturedCallback = null
    }
}

/**
 * Actual implementation of getCamera for Android.
 */
actual fun getCamera(): Camera = AndroidCamera()
