package fit.spotted.app.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fit.spotted.app.camera.Camera

/**
 * Camera controls for taking photos and switching cameras.
 */
@Composable
fun CameraControls(
    camera: Camera?,
    photoTaken: Boolean,
    photoData: ByteArray?,
    isAfterWorkoutMode: Boolean,
    onRetake: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!photoTaken && camera != null) {
        // Camera controls when no photo is taken
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera swap button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.7f), shape = CircleShape)
                    .clickable { camera.switchCamera() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Switch Camera",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            // Custom camera button with white border
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { camera.takePhoto() },
                contentAlignment = Alignment.Center
            ) {
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(width = 2.dp, color = Color.Black, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Empty box to create a simple camera button
                }
            }

            // Placeholder for layout balance
            Box(
                modifier = Modifier
                    .size(56.dp)
            ) {
                // Empty box for layout balance
            }
        }
    } else if (photoTaken) {
        // Controls when photo is taken
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Retake button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.7f), shape = CircleShape)
                    .clickable(onClick = onRetake),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Retake Photo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Accept button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (photoData != null) MaterialTheme.colors.primary else Color.Gray)
                    .border(
                        width = 1.dp, 
                        color = Color.White.copy(alpha = 0.7f), 
                        shape = CircleShape
                    )
                    .clickable(enabled = photoData != null, onClick = onAccept),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (isAfterWorkoutMode) "Finish" else "Next",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

