package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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

/**
 * Screen that allows users to take photos of their fitness activities.
 */
class CameraScreen : Screen {

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

        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Take a Photo of Your Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Camera preview placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoTaken) {
                        Text("Photo captured! (Mock)")
                    } else {
                        Text("Camera Preview (Mock)")
                    }
                }

                // Camera button
                Button(
                    onClick = { photoTaken = !photoTaken },
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Take Photo"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (photoTaken) "Retake Photo" else "Take Photo")
                }

                // Upload button
                Button(
                    onClick = { /* Handle upload action */ },
                    enabled = photoTaken,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Upload"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo")
                }
            }

            // Activity emoji in bottom corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary)
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedActivity,
                    fontSize = 24.sp
                )
            }

            // Emoji picker popup
            if (showEmojiPicker) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    offset = IntOffset(-16, -64),
                    onDismissRequest = { showEmojiPicker = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Emoji grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.height(120.dp)
                            ) {
                                items(activityTypes) { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (emoji == selectedActivity)
                                                    MaterialTheme.colors.primary.copy(alpha = 0.3f)
                                                else
                                                    Color.Transparent
                                            )
                                            .clickable {
                                                selectedActivity = emoji
                                                showEmojiPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 24.sp
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
