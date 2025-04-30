package fit.spotted.app.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Web implementation of the Camera interface that reports camera as unavailable.
 */
class WasmJsCamera : Camera {
    private var onPhotoCapturedCallback: ((CapturedImage) -> Unit)? = null
    private var currentCameraFacing = CameraFacing.BACK

    @Composable
    override fun Preview(modifier: Modifier, onPhotoCaptured: (CapturedImage) -> Unit) {
        // Store the callback
        onPhotoCapturedCallback = onPhotoCaptured

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera is not available in web version",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Please use the Android app for full camera functionality",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }

    override fun takePhoto() {
        // Do nothing - camera is unavailable
    }

    override fun release() {
        // Do nothing - no resources to release
        onPhotoCapturedCallback = null
    }

    override fun switchCamera(): CameraFacing {
        // Toggle camera facing direction even though it doesn't do anything in web
        currentCameraFacing = when (currentCameraFacing) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }
        return currentCameraFacing
    }

    override fun getCurrentCameraFacing(): CameraFacing {
        return currentCameraFacing
    }
}

/**
 * Actual implementation of getCamera for wasmJs.
 */
actual fun getCamera(): Camera = WasmJsCamera()
