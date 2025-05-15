package fit.spotted.app.ui.components.post.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiClient
import fit.spotted.app.ui.components.post.state.PostDetailState
import fit.spotted.app.ui.components.post.util.AdaptiveSizes
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.launch

/**
 * Component that displays the action buttons column (like, comment, delete, etc.).
 */
@Composable
fun PostActionButtons(
    state: PostDetailState,
    adaptiveSizes: AdaptiveSizes,
    showBeforeAfterToggle: Boolean = true,
    likes: Int = 0,
    commentsCount: Int = 0,
    isLikedByMe: Boolean = false,
    postId: Int? = null,
    apiClient: ApiClient? = null,
    onLikeStateChanged: ((postId: Int, isLiked: Boolean) -> Unit)? = null,
    actionButtons: @Composable ColumnScope.() -> Unit = {},
    showLikesAndComments: Boolean = true
) {
    val adaptiveSpacing = LocalAdaptiveSpacing.current
    val coroutineScope = rememberCoroutineScope()

    // Like button animation
    val likeScale by animateFloatAsState(
        targetValue = if (state.wasLikeClicked) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        finishedListener = { if (state.wasLikeClicked) state.resetLikeAnimation() }
    )

    val likeRotation by animateFloatAsState(
        targetValue = if (state.wasLikeClicked) 20f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Column(
        modifier = Modifier
            .padding(end = adaptiveSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(adaptiveSpacing.medium)
    ) {
        // Before/After toggle button
        if (showBeforeAfterToggle) {
            Box(
                modifier = Modifier
                    .size(adaptiveSizes.buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { state.toggleAfterImage() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FLIP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Only show likes and comments if showLikesAndComments is true
        if (showLikesAndComments) {
            // Like button with animation
            Box(
                modifier = Modifier
                    .size(adaptiveSizes.buttonSize)
                    .graphicsLayer {
                        scaleX = likeScale
                        scaleY = likeScale
                        rotationZ = likeRotation
                    }
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable {
                        state.toggleLike()

                        postId?.let { id ->
                            onLikeStateChanged?.invoke(id, state.isLiked)

                            apiClient?.let { client ->
                                coroutineScope.launch {
                                    try {
                                        if (state.isLiked) {
                                            client.likePost(id)
                                        } else {
                                            client.unlikePost(id)
                                        }
                                    } catch (_: Exception) {
                                        state.updateIsLiked(!state.isLiked)
                                        onLikeStateChanged?.invoke(id, state.isLiked)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (state.isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(adaptiveSizes.iconSize)
                )
            }

            // Like count indicator
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                val displayCount = if (state.isLiked && !isLikedByMe) likes + 1
                else if (!state.isLiked && isLikedByMe) likes - 1
                else likes

                Text(
                    text = "$displayCount",
                    color = if (state.isLiked) Color.Red else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment button
            Box(
                modifier = Modifier
                    .size(adaptiveSizes.buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { state.toggleComments() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(adaptiveSizes.iconSize)
                )
            }

            // Comment count indicator
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$commentsCount",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Additional action buttons
        actionButtons()
    }
}
