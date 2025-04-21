package fit.spotted.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable component that displays a post or photo detail view.
 * This component is used in both the feed and profile screens.
 */
@Composable
fun PostDetailView(
    // Common parameters
    beforeImageUrl: String,
    afterImageUrl: String,
    workoutDuration: String,
    activityType: String,
    userName: String,
    
    // Optional parameters with default values
    showBeforeAfterToggle: Boolean = false,
    initialShowAfterImage: Boolean = false,
    
    // Action buttons
    actionButtons: @Composable ColumnScope.() -> Unit = {},
    
    // Optional close action
    onClose: (() -> Unit)? = null
) {
    // State for tracking which image to show (before or after)
    var showAfterImage by remember { mutableStateOf(initialShowAfterImage) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Before/After images with carousel-like display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // We would implement a proper image carousel here
            // For now, just show placeholders for both images
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showAfterImage) "After: $afterImageUrl" else "Before: $beforeImageUrl",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Duration: $workoutDuration",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay with user info at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Row {
                        Text(
                            text = activityType,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $workoutDuration",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Action buttons on the right side
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom action buttons provided by the caller
            actionButtons()
            
            // Before/After toggle button if needed
            if (showBeforeAfterToggle) {
                Button(
                    onClick = { showAfterImage = !showAfterImage }
                ) {
                    Text(
                        text = if (showAfterImage) "Show Before" else "Show After",
                        color = Color.White
                    )
                }
            }
            
            // Close button if needed
            if (onClose != null) {
                Button(
                    onClick = onClose
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * A helper function to create an action button with an icon and a count.
 */
@Composable
fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    count: Int,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "$count",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}