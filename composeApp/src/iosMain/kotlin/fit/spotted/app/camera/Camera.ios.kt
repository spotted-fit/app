package fit.spotted.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import fit.spotted.app.utils.toByteArray
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIView

/**
 * iOS implementation of the Camera interface using AVFoundation.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSCamera : Camera {
    private var session: AVCaptureSession? = null
    private var cameraPreviewLayer: AVCaptureVideoPreviewLayer? = null
    private var stillImageOutput: AVCaptureStillImageOutput? = null
    private var currentCameraFacing = CameraFacing.BACK
    private var onPhotoCapturedCallback: ((ByteArray) -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun CameraPreview(
        modifier: Modifier,
        onPhotoCaptured: (ByteArray) -> Unit
    ) {
        onPhotoCapturedCallback = onPhotoCaptured

        // Create and configure the capture session
        val captureSession = remember { AVCaptureSession() }
        session = captureSession

        // Configure the session with preset photo quality
        captureSession.sessionPreset = AVCaptureSessionPresetPhoto

        // Setup the camera device
        val device = remember {
            val devices = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
            val cameraPosition = if (currentCameraFacing == CameraFacing.BACK) {
                AVCaptureDevicePositionBack
            } else {
                AVCaptureDevicePositionFront
            }

            devices.firstOrNull { device ->
                (device as AVCaptureDevice).position == cameraPosition
            } as? AVCaptureDevice
        }

        // Setup the camera input
        val input = remember {
            device?.let {
                AVCaptureDeviceInput.deviceInputWithDevice(it, null)
            }
        }

        // Setup the still image output
        val output = remember {
            AVCaptureStillImageOutput().apply {
                outputSettings = mapOf(AVVideoCodecKey to AVVideoCodecJPEG)
            }
        }
        stillImageOutput = output

        // Configure the session with input and output
        DisposableEffect(captureSession, input, output) {
            if (input != null) {
                captureSession.addInput(input)
                captureSession.addOutput(output)
                captureSession.startRunning()
            }

            onDispose {
                captureSession.stopRunning()
                captureSession.inputs.forEach { captureSession.removeInput(it as AVCaptureInput) }
                captureSession.outputs.forEach { captureSession.removeOutput(it as AVCaptureOutput) }
            }
        }

        // Create the camera preview layer
        val previewLayer = remember { AVCaptureVideoPreviewLayer(session = captureSession) }
        cameraPreviewLayer = previewLayer

        // Display the camera preview using UIKitView
        UIKitView(
            modifier = modifier,
            background = Color.Black,
            factory = {
                val container = UIView()
                container.layer.addSublayer(previewLayer)
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                container
            },
            onResize = { container: UIView, rect: CValue<CGRect> ->
                CATransaction.begin()
                CATransaction.setValue(true, kCATransactionDisableActions)
                container.layer.setFrame(rect)
                previewLayer.setFrame(rect)
                CATransaction.commit()
            }
        )
    }

    override fun takePhoto() {
        val stillImageOutput = stillImageOutput ?: return

        val videoConnection = stillImageOutput.connectionWithMediaType(AVMediaTypeVideo) ?: return

        stillImageOutput.captureStillImageAsynchronouslyFromConnection(videoConnection) { buffer, error ->
            if (error != null) {
                println("Error capturing photo: ${(error).localizedDescription}")
                return@captureStillImageAsynchronouslyFromConnection
            }

            buffer?.let {
                val imageData = AVCaptureStillImageOutput.jpegStillImageNSDataRepresentation(it)
                // Convert to UIImage and back to JPEG data
                // This is a reliable approach that works with Kotlin/Native
                val bytes = imageData?.let { data ->
                    // Create UIImage from NSData
                    val image = UIImage.imageWithData(data)

                    // Convert UIImage back to JPEG data with compression
                    val jpegData = image?.let { 
                        UIImageJPEGRepresentation(it, 0.9) 
                    }

                    // Convert NSData to ByteArray using the extension function
                    jpegData?.toByteArray()
                }

                bytes?.let { photoBytes ->
                    onPhotoCapturedCallback?.invoke(photoBytes)
                }
            }
        }
    }

    override fun release() {
        session?.stopRunning()
        session = null
        cameraPreviewLayer = null
        stillImageOutput = null
        onPhotoCapturedCallback = null
    }

    override fun switchCamera(): CameraFacing {
        currentCameraFacing = when (currentCameraFacing) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }

        // Stop the current session
        session?.stopRunning()

        // Clear inputs and outputs
        session?.inputs?.forEach { session?.removeInput(it as AVCaptureInput) }
        session?.outputs?.forEach { session?.removeOutput(it as AVCaptureOutput) }

        // The session will be reconfigured the next time CameraPreview is called

        return currentCameraFacing
    }

    override fun getCurrentCameraFacing(): CameraFacing {
        return currentCameraFacing
    }
}

/**
 * Actual implementation of getCamera for iOS.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getCamera(): Camera = IOSCamera()
