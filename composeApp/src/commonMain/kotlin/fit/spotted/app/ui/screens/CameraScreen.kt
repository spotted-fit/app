package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import fit.spotted.app.camera.getCamera
import fit.spotted.app.utils.getImageConverter

/**
 * Screen that allows users to take photos of their fitness activities.
 */
class CameraScreen(private val isVisible: Boolean = true) : Screen {

    // Mock list of activity types with only emojis
    private val activityTypes = listOf(
        "🏃",
        "🚶",
        "🚴",
        "🏊",
        "🧘"
    )

    @Composable
    override fun Content() {
        var selectedActivity by remember { mutableStateOf(activityTypes.first()) }
        var photoTaken by remember { mutableStateOf(false) }
        var showEmojiPicker by remember { mutableStateOf(false) }
        var photoData by remember { mutableStateOf<ByteArray?>(null) }

        // Only initialize the camera when the screen is visible
        val camera = remember(isVisible) { 
            if (isVisible) getCamera() else null 
        }

        // Get the image converter
        val imageConverter = remember { getImageConverter() }

        // Clean up camera resources when the screen is disposed or becomes invisible
        DisposableEffect(isVisible) {
            onDispose {
                camera?.release()
            }
        }

        // Main container that fills the entire screen
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp)) // Clip to ensure content stays within bounds
        ) {
            // Camera preview or captured photo - takes the full screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3/4f)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(30.dp)), // Clip to ensure content stays within bounds
                contentAlignment = Alignment.Center
            ) {
                if (photoTaken && photoData != null) {
                    // Show captured photo preview - fill the entire screen with the image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display the actual captured photo using the platform-specific image converter
                        imageConverter.ByteArrayImage(
                            bytes = photoData!!,
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "Captured photo"
                        )
                    }
                } else if (camera != null && isVisible) {
                    // Camera preview - full screen
                    camera.CameraPreview(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(0.dp)), // Clip to ensure content stays within bounds
                        onPhotoCaptured = { bytes ->
                            photoData = bytes
                            photoTaken = true
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

            // Bottom controls overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Bottom row with camera controls
                if (!photoTaken && camera != null && isVisible) {
                    Row(
                        modifier = Modifier
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
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                // Empty box to create a simple camera button
                                // BeReal uses a simple circle without an icon
                            }
                        }

                        // Activity emoji button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(width = 1.dp, color = Color.White.copy(alpha = 0.7f), shape = CircleShape)
                                .clickable { showEmojiPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedActivity,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Instagram-style controls when photo is taken - side by side buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Retake button - styled like Instagram
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(width = 1.dp, color = Color.White.copy(alpha = 0.7f), shape = CircleShape)
                                .clickable { 
                                    photoTaken = false
                                    photoData = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Retake Photo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Upload button - styled like Instagram
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
                                .clickable(enabled = photoData != null) { 
                                    if (photoData != null) {
                                        println("Photo uploaded! Size: ${photoData?.size ?: 0} bytes")
                                        photoTaken = false
                                        photoData = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Upload",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Camera controls have been moved to the bottom row

            // Emoji picker popup - Instagram-style
            if (showEmojiPicker) {
                Popup(
                    alignment = Alignment.BottomCenter,
                    offset = IntOffset(0, -120),
                    onDismissRequest = { showEmojiPicker = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Card(
                        modifier = Modifier
                            .width(240.dp),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color.Black.copy(alpha = 0.8f),
                        elevation = 10.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Title
                            Text(
                                "Select Activity Type",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Emoji grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.height(140.dp)
                            ) {
                                items(activityTypes) { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (emoji == selectedActivity)
                                                    MaterialTheme.colors.primary
                                                else
                                                    Color.DarkGray.copy(alpha = 0.6f)
                                            )
                                            .clickable {
                                                selectedActivity = emoji
                                                showEmojiPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
