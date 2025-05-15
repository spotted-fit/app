package fit.spotted.app.ui.components.post.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fit.spotted.app.ui.components.post.state.PostDetailState
import fit.spotted.app.ui.components.post.util.AdaptiveSizes
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import fit.spotted.app.ui.theme.LocalReducedMotion
import fit.spotted.app.ui.theme.WindowSizeClass
import kotlinx.coroutines.delay

/**
 * Component that displays the before/after images with a toggle indicator.
 */
@Composable
fun PostImageDisplay(
    state: PostDetailState,
    beforeContent: @Composable () -> Unit,
    afterContent: @Composable () -> Unit,
    adaptiveSizes: AdaptiveSizes,
) {
    val adaptiveSpacing = LocalAdaptiveSpacing.current
    val isReducedMotion = LocalReducedMotion.current

    // Animation for smooth transitions between before/after images
    val imageTransition by animateFloatAsState(
        targetValue = if (state.showAfterImage) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isReducedMotion) 200 else 500, 
            easing = FastOutSlowInEasing
        )
    )
    
    // State for double-tap like animation
    var showLikeAnimation by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // Hide like animation after delay
    LaunchedEffect(showLikeAnimation) {
        if (showLikeAnimation) {
            delay(1000)
            showLikeAnimation = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { 
                        if (!state.isLiked) {
                            state.toggleLike()
                            showLikeAnimation = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Before image with flip animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // When transitioning to "after", rotate the "before" image from 0 to -90 degrees
                    // Only visible from 0 to 0.5 of the transition
                    rotationY = -180f * imageTransition
                    // Gradually hide as we rotate past 90 degrees
                    alpha = if (imageTransition < 0.5f) 1f else 0f
                    cameraDistance = 12f * density
                }
        ) {
            if (imageTransition < 0.5f) {
                beforeContent()
            }
        }

        // After image with flip animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // When transitioning to "after", rotate the "after" image from 90 to 0 degrees
                    // Only visible from 0.5 to 1 of the transition
                    rotationY = 180f - (180f * imageTransition)
                    // Gradually hide as we rotate past 90 degrees
                    alpha = if (imageTransition >= 0.5f) 1f else 0f
                    cameraDistance = 12f * density
                }
        ) {
            if (imageTransition >= 0.5f) {
                afterContent()
            }
        }
        
        // Like animation overlay
        if (showLikeAnimation) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like",
                tint = Color.Red,
                modifier = Modifier
                    .size(100.dp)
                    .scale(
                        animateFloatAsState(
                            targetValue = if (showLikeAnimation) 1.2f else 0f,
                            animationSpec = tween(300)
                        ).value
                    )
            )
        }

        // Show before/after indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = adaptiveSpacing.statusBarPadding,
                    end = adaptiveSpacing.medium
                )
                .clip(CircleShape)
                .background(
                    if (state.showAfterImage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                    else Color.DarkGray.copy(alpha = 0.8f)
                )
                .padding(horizontal = adaptiveSpacing.medium, vertical = adaptiveSpacing.small)
        ) {
            Text(
                text = if (state.showAfterImage) "AFTER" else "BEFORE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = adaptiveSizes.captionTextSize
            )
        }
    }
}

/**
 * Helper function to create AsyncImage content for URL-based images.
 */
@Composable
fun ImageUrlContent(
    imageUrl: String,
    windowSizeClass: WindowSizeClass
) {
    // For tablet layouts, adjust content scale to fit better
    val contentScaleMode = if (windowSizeClass == WindowSizeClass.EXPANDED) {
        ContentScale.Fit
    } else {
        ContentScale.Crop
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = "Post Image",
        contentScale = contentScaleMode,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Helper function to create Image content for ImageBitmap-based images.
 */
@Composable
fun ImageBitmapContent(
    imageBitmap: ImageBitmap?,
    windowSizeClass: WindowSizeClass
) {
    // For tablet layouts, adjust content scale to fit better
    val contentScaleMode = if (windowSizeClass == WindowSizeClass.EXPANDED) {
        ContentScale.Fit
    } else {
        ContentScale.Crop
    }

    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Post Image",
            contentScale = contentScaleMode,
            modifier = Modifier.fillMaxSize()
        )
    }
}
