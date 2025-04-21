package fit.spotted.app.ui.camera

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * A modern iOS-style emoji picker for selecting activity types.
 * Features a more polished design with animations and visual effects.
 */
@Composable
fun EmojiPicker(
    activityTypes: List<String>,
    selectedActivity: String,
    onActivitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Animation for the popup appearance
    val popupScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, -100),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        // iOS-style card with blur effect simulation
        Card(
            modifier = Modifier
                .width(300.dp)
                .graphicsLayer {
                    scaleX = popupScale
                    scaleY = popupScale
                    alpha = popupScale
                }
                .shadow(elevation = 15.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color.Black.copy(alpha = 0.85f),
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modern header with title and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Activity",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    // iOS-style close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = 0.6f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Emoji grid with iOS-style appearance
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                ) {
                    items(activityTypes) { emoji ->
                        // Animation for selection
                        val scale = animateFloatAsState(
                            targetValue = if (emoji == selectedActivity) 1.1f else 1f,
                            animationSpec = tween(durationMillis = 150)
                        )

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(56.dp)
                                .graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (emoji == selectedActivity)
                                        Color(0xFF3D5AFE).copy(alpha = 0.8f)
                                    else
                                        Color.DarkGray.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = if (emoji == selectedActivity) 2.dp else 0.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onActivitySelected(emoji)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // iOS-style emoji with shadow effect
                            Text(
                                text = emoji,
                                fontSize = 30.sp,
                                modifier = Modifier
                                    .graphicsLayer {
                                        shadowElevation = 4f
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
