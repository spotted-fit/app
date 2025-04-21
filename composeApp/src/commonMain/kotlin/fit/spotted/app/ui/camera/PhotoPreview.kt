package fit.spotted.app.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.utils.ImageConverter

/**
 * A composable that displays before and after workout photos in a card deck/carousel style.
 */
@Composable
fun PhotoPreview(
    beforeWorkoutPhoto: ByteArray,
    afterWorkoutPhoto: ByteArray,
    imageConverter: ImageConverter,
    showPostAnimation: Boolean,
    currentPhotoIndex: Int = 0,
    onNextPhoto: () -> Unit = {},
    onPreviousPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !showPostAnimation,
        enter = fadeIn(),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Card deck/carousel style layout
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(3/4f),
                contentAlignment = Alignment.Center
            ) {
                // Stack the photos with the current one on top
                // The "back" card (slightly smaller and offset)
                if (currentPhotoIndex == 0) {
                    // Show "After" card in the back when "Before" is selected
                    PhotoCard(
                        photo = afterWorkoutPhoto,
                        label = "After",
                        imageConverter = imageConverter,
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .offset(x = 16.dp, y = 8.dp)
                    )
                } else {
                    // Show "Before" card in the back when "After" is selected
                    PhotoCard(
                        photo = beforeWorkoutPhoto,
                        label = "Before",
                        imageConverter = imageConverter,
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .offset(x = (-16).dp, y = 8.dp)
                    )
                }

                // The front card (current selection)
                val currentPhoto = if (currentPhotoIndex == 0) beforeWorkoutPhoto else afterWorkoutPhoto
                val currentLabel = if (currentPhotoIndex == 0) "Before" else "After"

                PhotoCard(
                    photo = currentPhoto,
                    label = currentLabel,
                    imageConverter = imageConverter,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when {
                                    // Swipe right - go to previous photo
                                    dragAmount > 0 -> {
                                        if (currentPhotoIndex > 0) onPreviousPhoto()
                                    }
                                    // Swipe left - go to next photo
                                    dragAmount < 0 -> {
                                        if (currentPhotoIndex < 1) onNextPhoto()
                                    }
                                }
                            }
                        }
                        .clickable {
                            // Keep click functionality as fallback
                            if (currentPhotoIndex == 0) onNextPhoto() else onPreviousPhoto()
                        }
                )

                // Navigation indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Dots indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (currentPhotoIndex == 0) Color.White else Color.White.copy(alpha = 0.5f))
                            .clickable { onPreviousPhoto() }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (currentPhotoIndex == 1) Color.White else Color.White.copy(alpha = 0.5f))
                            .clickable { onNextPhoto() }
                    )
                }
            }
        }
    }
}

/**
 * A composable that displays a single photo card.
 */
@Composable
private fun PhotoCard(
    photo: ByteArray,
    label: String,
    imageConverter: ImageConverter,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
    ) {
        // Custom wrapper for ByteArrayImage to ensure consistent sizing
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            imageConverter.ByteArrayImage(
                bytes = photo,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "$label workout photo"
            )
        }

        // Label
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * A composable that displays a timer in the top-right corner.
 */
@Composable
fun TimerDisplay(
    timerText: String,
    showPostAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        visible = !showPostAnimation,
        enter = fadeIn(),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = timerText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
