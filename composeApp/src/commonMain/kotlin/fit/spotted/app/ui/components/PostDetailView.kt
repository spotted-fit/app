package fit.spotted.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import fit.spotted.app.api.ApiClient
import fit.spotted.app.api.models.CommentData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.camera.TimerDisplay
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import fit.spotted.app.ui.theme.LocalReducedMotion
import fit.spotted.app.ui.theme.LocalWindowSize
import fit.spotted.app.ui.theme.WindowSizeClass
import kotlinx.coroutines.launch
import org.kodein.emoji.compose.WithPlatformEmoji

/**
 * A reusable component that displays a post or photo detail view.
 * This component is used in both the feed and profile screens.
 */
@Composable
fun PostDetailView(
    // Image URLs
    beforeImageUrl: String,
    afterImageUrl: String,

    // Common parameters
    workoutDuration: String,
    postedAt: String,
    activityType: ActivityType,
    userName: String,

    // Optional parameters with default values
    showBeforeAfterToggle: Boolean = true,
    initialShowAfterImage: Boolean = false,

    // Likes and comments
    likes: Int = 0,
    comments: List<CommentData> = emptyList(),
    isLikedByMe: Boolean = false,

    // Action buttons (for backward compatibility)
    actionButtons: @Composable ColumnScope.() -> Unit = {},

    // Optional close action
    onClose: (() -> Unit)? = null,

    onActivityTypeClick: (() -> Unit)? = null,

    // Callback for adding a comment
    onAddComment: ((text: String) -> Unit)? = null,

    // Callback for deleting a post
    onDeletePost: (() -> Unit)? = null,

    // Post ID for comment functionality
    postId: Int? = null,

    // API client for like functionality
    apiClient: ApiClient? = null,

    // Callback for when a post is liked/unliked
    onLikeStateChanged: ((postId: Int, isLiked: Boolean) -> Unit)? = null
) {
    // State for tracking which image to show (before or after)
    var showAfterImage by remember { mutableStateOf(initialShowAfterImage) }

    // State for like button, comments, and delete confirmation
    var isLiked by remember { mutableStateOf(isLikedByMe) }
    var showComments by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Animation for smooth transitions between before/after images
    val imageTransition = animateFloatAsState(
        targetValue = if (showAfterImage) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    // Get adaptive spacing and window size
    val adaptiveSpacing = LocalAdaptiveSpacing.current
    val windowSize = LocalWindowSize.current
    val isReducedMotion = LocalReducedMotion.current

    // Adjust animation duration based on reduced motion preference
    if (isReducedMotion) 200 else 500

    // Adjust dimensions for different screen sizes
    val buttonSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 56.dp
        WindowSizeClass.MEDIUM -> 64.dp
        WindowSizeClass.EXPANDED -> 72.dp
    }

    val iconSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 28.dp
        WindowSizeClass.MEDIUM -> 32.dp
        WindowSizeClass.EXPANDED -> 36.dp
    }

    val titleTextSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 16.sp
        WindowSizeClass.MEDIUM -> 18.sp
        WindowSizeClass.EXPANDED -> 20.sp
    }

    val captionTextSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 14.sp
        WindowSizeClass.MEDIUM -> 16.sp
        WindowSizeClass.EXPANDED -> 18.sp
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main content area with photo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Display the photo
            val currentUrl = if (imageTransition.value < 0.5f) beforeImageUrl else afterImageUrl

            // For tablet layouts, adjust content scale to fit better
            val contentScaleMode = if (windowSize.widthSizeClass == WindowSizeClass.EXPANDED) {
                ContentScale.Fit
            } else {
                ContentScale.Crop
            }

            AsyncImage(
                model = currentUrl,
                contentDescription = "Post Image",
                contentScale = contentScaleMode,
                modifier = Modifier.fillMaxSize()
            )

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
                        if (showAfterImage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        else Color.DarkGray.copy(alpha = 0.8f)
                    )
                    .padding(horizontal = adaptiveSpacing.medium, vertical = adaptiveSpacing.small)
            ) {
                Text(
                    text = if (showAfterImage) "AFTER" else "BEFORE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = captionTextSize
                )
            }
        }

        // Action buttons column
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = adaptiveSpacing.huge * 3)
                .padding(end = adaptiveSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(adaptiveSpacing.medium)
        ) {
            // Before/After toggle
            if (showBeforeAfterToggle) {
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showAfterImage = !showAfterImage },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showAfterImage) "B" else "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleTextSize
                    )
                }
            }

            // Like button with animation
            val coroutineScope = rememberCoroutineScope()
            var wasLikeClicked by remember { mutableStateOf(false) }
            val likeScale by animateFloatAsState(
                targetValue = if (wasLikeClicked) 1.4f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                finishedListener = { if (wasLikeClicked) wasLikeClicked = false }
            )
            val likeRotation by animateFloatAsState(
                targetValue = if (wasLikeClicked) 20f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .graphicsLayer {
                        scaleX = likeScale
                        scaleY = likeScale
                        rotationZ = likeRotation
                    }
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable {
                        isLiked = !isLiked
                        wasLikeClicked = true

                        postId?.let { id ->
                            onLikeStateChanged?.invoke(id, isLiked)

                            apiClient?.let { client ->
                                coroutineScope.launch {
                                    try {
                                        if (isLiked) {
                                            client.likePost(id)
                                        } else {
                                            client.unlikePost(id)
                                        }
                                    } catch (_: Exception) {
                                        isLiked = !isLiked
                                        onLikeStateChanged?.invoke(id, isLiked)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(iconSize)
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
                val displayCount = if (isLiked && !isLikedByMe) likes + 1
                else if (!isLiked && isLikedByMe) likes - 1
                else likes

                Text(
                    text = "$displayCount",
                    color = if (isLiked) Color.Red else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment button
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showComments = !showComments },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
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
                    text = "${comments.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Delete button
            onDeletePost?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showDeleteConfirmation = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑️",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // Close button
            if (onClose != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            // Additional action buttons
            actionButtons()
        }

        // Position the timer aligned with the comments button
        val commentsButtonIndex = if (showBeforeAfterToggle) 2 else 1
        val offsetToCommentsButton = buttonSize * commentsButtonIndex +
                (adaptiveSpacing.medium.value * (commentsButtonIndex)).dp

        TimerDisplay(
            timerText = workoutDuration,
            showPostAnimation = false,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(
                    y = adaptiveSpacing.huge * 3 + offsetToCommentsButton
                )
                .padding(start = 4.dp)
        )

        // User info overlay at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(adaptiveSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar
                val avatarSize = when (windowSize.widthSizeClass) {
                    WindowSizeClass.COMPACT -> 40.dp
                    WindowSizeClass.MEDIUM -> 48.dp
                    WindowSizeClass.EXPANDED -> 56.dp
                }

                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(avatarSize / 1.6f),
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(adaptiveSpacing.medium))

                // User info
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleTextSize,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = adaptiveSpacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(onClick = { onActivityTypeClick?.invoke() })
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WithPlatformEmoji(
                                activityType.emoji
                            ) { emojiString, inlineContent ->
                                Text(
                                    text = emojiString,
                                    inlineContent = inlineContent,
                                    fontSize = when (windowSize.widthSizeClass) {
                                        WindowSizeClass.COMPACT -> 24.sp
                                        else -> 28.sp
                                    },
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(adaptiveSpacing.small))

                        // Dot separator
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(adaptiveSpacing.small))
                        Text(
                            text = postedAt,
                            fontSize = captionTextSize,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Comments overlay
        if (showComments) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp)
            ) {
                Column {
                    // Header with title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showComments = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Comments list
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            // User avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.username.first().toString(),
                                    color = MaterialTheme.colors.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Comment content
                            Column {
                                Text(
                                    text = comment.username,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.text,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Add comment input field
                    if (onAddComment != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        var commentText by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text field for comment
                            TextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Add a comment...", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.White.copy(alpha = 0.1f),
                                    cursorColor = Color.White,
                                    textColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Submit button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.primary)
                                    .clickable {
                                        if (commentText.isNotBlank()) {
                                            onAddComment(commentText)
                                            commentText = ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Post") },
                text = { Text("Are you sure you want to delete this post?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            onDeletePost?.invoke()
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    }
}

/**
 * An overload of PostDetailView that accepts ImageBitmap images instead of URLs.
 * This is used for previewing images before posting.
 */
@Composable
fun PostDetailView(
    // ImageBitmap images
    beforeWorkoutPhoto: ImageBitmap?,
    afterWorkoutPhoto: ImageBitmap?,

    // Common parameters
    workoutDuration: String,
    postedAt: String,
    activityType: ActivityType,
    userName: String,

    // Optional parameters
    showBeforeAfterToggle: Boolean = true,
    initialShowAfterImage: Boolean = false,
    likes: Int = 0,
    comments: List<CommentData> = emptyList(),
    isLikedByMe: Boolean = false,
    actionButtons: @Composable ColumnScope.() -> Unit = {},
    onClose: (() -> Unit)? = null,
    onActivityTypeClick: (() -> Unit)? = null,
    onAddComment: ((text: String) -> Unit)? = null,
    onDeletePost: (() -> Unit)? = null,
    postId: Int? = null,
    apiClient: ApiClient? = null,
    onLikeStateChanged: ((postId: Int, isLiked: Boolean) -> Unit)? = null
) {
    // State management
    var showAfterImage by remember { mutableStateOf(initialShowAfterImage) }
    var isLiked by remember { mutableStateOf(isLikedByMe) }
    var showComments by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Animations
    animateFloatAsState(
        targetValue = if (showAfterImage) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    // Adaptive sizing
    val adaptiveSpacing = LocalAdaptiveSpacing.current
    val windowSize = LocalWindowSize.current
    val isReducedMotion = LocalReducedMotion.current
    if (isReducedMotion) 200 else 500

    val buttonSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 56.dp
        WindowSizeClass.MEDIUM -> 64.dp
        WindowSizeClass.EXPANDED -> 72.dp
    }

    val iconSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 28.dp
        WindowSizeClass.MEDIUM -> 32.dp
        WindowSizeClass.EXPANDED -> 36.dp
    }

    val titleTextSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 16.sp
        WindowSizeClass.MEDIUM -> 18.sp
        WindowSizeClass.EXPANDED -> 20.sp
    }

    val captionTextSize = when (windowSize.widthSizeClass) {
        WindowSizeClass.COMPACT -> 14.sp
        WindowSizeClass.MEDIUM -> 16.sp
        WindowSizeClass.EXPANDED -> 18.sp
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main image display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val currentPhoto = if (showAfterImage) afterWorkoutPhoto else beforeWorkoutPhoto

            currentPhoto?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = if (showAfterImage) "After workout photo" else "Before workout photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Before/After indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = adaptiveSpacing.statusBarPadding,
                        end = adaptiveSpacing.medium
                    )
                    .clip(CircleShape)
                    .background(
                        if (showAfterImage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        else Color.DarkGray.copy(alpha = 0.8f)
                    )
                    .padding(horizontal = adaptiveSpacing.medium, vertical = adaptiveSpacing.small)
            ) {
                Text(
                    text = if (showAfterImage) "AFTER" else "BEFORE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = captionTextSize
                )
            }
        }

        // Action buttons column
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = adaptiveSpacing.huge * 3)
                .padding(end = adaptiveSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(adaptiveSpacing.medium)
        ) {
            // Before/After toggle
            if (showBeforeAfterToggle) {
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showAfterImage = !showAfterImage },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showAfterImage) "B" else "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleTextSize
                    )
                }
            }

            // Like button with animation
            var wasLikeClickedBitmap by remember { mutableStateOf(false) }
            val likeScaleBitmap by animateFloatAsState(
                targetValue = if (wasLikeClickedBitmap) 1.4f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                finishedListener = { if (wasLikeClickedBitmap) wasLikeClickedBitmap = false }
            )
            val likeRotationBitmap by animateFloatAsState(
                targetValue = if (wasLikeClickedBitmap) 20f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .graphicsLayer {
                        scaleX = likeScaleBitmap
                        scaleY = likeScaleBitmap
                        rotationZ = likeRotationBitmap
                    }
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable {
                        isLiked = !isLiked
                        wasLikeClickedBitmap = true

                        postId?.let { id ->
                            onLikeStateChanged?.invoke(id, isLiked)

                            apiClient?.let { client ->
                                coroutineScope.launch {
                                    try {
                                        if (isLiked) {
                                            client.likePost(id)
                                        } else {
                                            client.unlikePost(id)
                                        }
                                    } catch (_: Exception) {
                                        isLiked = !isLiked
                                        onLikeStateChanged?.invoke(id, isLiked)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(iconSize)
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
                val displayCount = if (isLiked && !isLikedByMe) likes + 1
                else if (!isLiked && isLikedByMe) likes - 1
                else likes

                Text(
                    text = "$displayCount",
                    color = if (isLiked) Color.Red else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment button
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showComments = !showComments },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
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
                    text = "${comments.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Delete button
            onDeletePost?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showDeleteConfirmation = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑️",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // Close button
            if (onClose != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            // Additional action buttons
            actionButtons()
        }

        // Position the timer aligned with the comments button
        val commentsButtonIndex = if (showBeforeAfterToggle) 2 else 1
        val offsetToCommentsButton = buttonSize * commentsButtonIndex +
                (adaptiveSpacing.medium.value * (commentsButtonIndex)).dp

        TimerDisplay(
            timerText = workoutDuration,
            showPostAnimation = false,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(
                    y = adaptiveSpacing.huge * 3 + offsetToCommentsButton
                )
                .padding(start = 4.dp)
        )

        // User info overlay at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(adaptiveSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar
                val avatarSize = when (windowSize.widthSizeClass) {
                    WindowSizeClass.COMPACT -> 40.dp
                    WindowSizeClass.MEDIUM -> 48.dp
                    WindowSizeClass.EXPANDED -> 56.dp
                }

                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(avatarSize / 1.6f),
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(adaptiveSpacing.medium))

                // User info
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleTextSize,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = adaptiveSpacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(onClick = { onActivityTypeClick?.invoke() })
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WithPlatformEmoji(
                                activityType.emoji
                            ) { emojiString, inlineContent ->
                                Text(
                                    text = emojiString,
                                    inlineContent = inlineContent,
                                    fontSize = when (windowSize.widthSizeClass) {
                                        WindowSizeClass.COMPACT -> 24.sp
                                        else -> 28.sp
                                    },
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(adaptiveSpacing.small))

                        // Dot separator
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(adaptiveSpacing.small))
                        Text(
                            text = postedAt,
                            fontSize = captionTextSize,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Comments overlay
        if (showComments) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp)
            ) {
                Column {
                    // Header with title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showComments = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Comments list
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            // User avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.username.first().toString(),
                                    color = MaterialTheme.colors.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Comment content
                            Column {
                                Text(
                                    text = comment.username,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.text,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Add comment input field
                    if (onAddComment != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        var commentText by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text field for comment
                            TextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Add a comment...", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.White.copy(alpha = 0.1f),
                                    cursorColor = Color.White,
                                    textColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Submit button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.primary)
                                    .clickable {
                                        if (commentText.isNotBlank()) {
                                            onAddComment(commentText)
                                            commentText = ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Post") },
                text = { Text("Are you sure you want to delete this post?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            onDeletePost?.invoke()
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    }
}
