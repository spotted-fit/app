package fit.spotted.app.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fit.spotted.app.camera.Camera
import fit.spotted.app.utils.ImageConverter

/**
 * A composable that displays either the camera preview or a captured photo.
 */
@Composable
fun CameraPreview(
    camera: Camera?,
    imageConverter: ImageConverter,
    photoData: ByteArray?,
    isVisible: Boolean,
    photoTaken: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (photoTaken && photoData != null) {
            // Show captured photo preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Display the actual captured photo using the platform-specific image converter
                imageConverter.ByteArrayImage(
                    bytes = photoData,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Captured photo"
                )
            }
        } else if (camera != null && isVisible && !photoTaken) {
            // Camera preview
            camera.CameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp)),
                onPhotoCaptured = onPhotoCaptured
            )
        } else {
            // Placeholder when camera is not available or screen is not visible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera initializing...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}