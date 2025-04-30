package fit.spotted.app.ui.camera

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fit.spotted.app.camera.Camera
import fit.spotted.app.camera.CapturedImage

/**
 * A composable that displays either the camera preview or a captured photo.
 */
@Composable
fun CameraPreview(
    camera: Camera?,
    photoData: ImageBitmap?,
    isVisible: Boolean,
    photoTaken: Boolean,
    onPhotoCaptured: (CapturedImage) -> Unit,
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
                // Display the actual captured photo directly using Image composable
                // Check if the photo was taken with the front camera and mirror it if needed
                Image(
                    bitmap = photoData,
                    contentDescription = "Captured photo",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else if (camera != null && isVisible && !photoTaken) {
            // Camera preview
            camera.Preview(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp)),
                onPhotoCaptured = { capturedImage ->
                    onPhotoCaptured(capturedImage)
                }
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
