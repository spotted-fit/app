package fit.spotted.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import fit.spotted.app.api.ApiClient
import fit.spotted.app.api.models.CommentData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.camera.TimerDisplay
import kotlinx.coroutines.launch
import org.kodein.emoji.compose.WithPlatformEmoji

/**
 * Linear interpolation for Float values - our own implementation
 */
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

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
    
    // Use animation for smooth transitions between before/after images
    val imageTransition = animateFloatAsState(
        targetValue = if (showAfterImage) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )
    
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
            
            AsyncImage(
                model = currentUrl,
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Show before/after indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(
                        if (showAfterImage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        else Color.DarkGray.copy(alpha = 0.8f)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (showAfterImage) "AFTER" else "BEFORE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        // Timer display
        TimerDisplay(
            timerText = workoutDuration,
            showPostAnimation = false,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 100.dp, start = 16.dp)
        )
        
        // User info overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User info
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
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
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Small dot separator
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = postedAt,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Action buttons (right side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Before/After toggle
            if (showBeforeAfterToggle) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                        fontSize = 18.sp
                    )
                }
            }
            
            // Like button
            val coroutineScope = rememberCoroutineScope()
            ActionButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                count = if (isLiked && !isLikedByMe) likes + 1
                else if (!isLiked && isLikedByMe) likes - 1
                else likes,
                tint = if (isLiked) Color.Red else Color.White,
                backgroundColor = Color(0x22FFFFFF),
                countBackgroundColor = if (isLiked) Color(0x33FF0000) else Color(0x33FFFFFF),
                countTextColor = if (isLiked) Color.Red else Color.White,
                onClick = {
                    isLiked = !isLiked

                    // Notify parent component about like state change
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
                                    // Also notify about the revert
                                    onLikeStateChanged?.invoke(id, isLiked)
                                }
                            }
                        }
                    }
                },
                animated = true
            )

            // Comment button
            ActionButton(
                icon = Icons.Default.Email,
                contentDescription = "Comment",
                count = comments.size,
                tint = Color.White,
                backgroundColor = Color.White.copy(alpha = 0.1f),
                countBackgroundColor = Color.White.copy(alpha = 0.2f),
                countTextColor = Color.White,
                onClick = { showComments = !showComments },
                animated = true
            )
            
            // Delete button
            onDeletePost?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showDeleteConfirmation = true },
                    contentAlignment = Alignment.Center
                ) {
                    WithPlatformEmoji(
                        "🗑️"
                    ) { emojiString, inlineContent ->
                        Text(
                            text = emojiString,
                            color = Color.White,
                            inlineContent = inlineContent,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Additional action buttons
            actionButtons()

            // Close button
            if (onClose != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
 * A helper function to create a modern action button with an icon and a count.
 * Supports animation for a more dynamic feel.
 */
@Composable
fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    count: Int,
    tint: Color = Color.White,
    backgroundColor: Color = MaterialTheme.colors.surface.copy(alpha = 0.9f),
    countBackgroundColor: Color = MaterialTheme.colors.surface.copy(alpha = 0.7f),
    countTextColor: Color = MaterialTheme.colors.onSurface,
    onClick: () -> Unit,
    animated: Boolean = false
) {
    // Animation for scale effect when clicked
    var wasClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (wasClicked) 1.2f else 1f,
        animationSpec = if (animated) {
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    )
    
    // Animate count changes
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp) // Larger touch target
                .shadow(4.dp, CircleShape) // Add shadow for depth
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable { 
                    onClick()
                    // Trigger animation
                    wasClicked = true
                    // Reset after animation
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(300)
                        wasClicked = false
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(28.dp) // Slightly larger icon
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Animated count with nicer styling
        Box(
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(countBackgroundColor)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$animatedCount",
                color = countTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
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

    // Optional parameters with default values
    showLikesAndComments: Boolean = false,

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

    // Optional callback for activity type click
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
    
    // Use animation for smooth transitions between before/after images
    val imageTransition = animateFloatAsState(
        targetValue = if (showAfterImage) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )
    
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
            // Display the current image based on showAfterImage state
            val currentPhoto = if (showAfterImage) afterWorkoutPhoto else beforeWorkoutPhoto

            // Use standard Image composable to display the ImageBitmap
            currentPhoto?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = if (showAfterImage) "After workout photo" else "Before workout photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Show before/after indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(
                        if (showAfterImage) MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        else Color.DarkGray.copy(alpha = 0.8f)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (showAfterImage) "AFTER" else "BEFORE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        // Timer display
        TimerDisplay(
            timerText = workoutDuration,
            showPostAnimation = false,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 100.dp, start = 16.dp)
        )
        
        // User info overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User info
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
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
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Small dot separator
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = postedAt,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Only show action buttons if showLikesAndComments is true
        if (showLikesAndComments) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Before/After toggle
                if (showBeforeAfterToggle) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Like button
                val coroutineScope = rememberCoroutineScope()
                ActionButton(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    count = if (isLiked && !isLikedByMe) likes + 1
                    else if (!isLiked && isLikedByMe) likes - 1
                    else likes,
                    tint = if (isLiked) Color.Red else Color.White,
                    backgroundColor = Color(0x22FFFFFF),
                    countBackgroundColor = if (isLiked) Color(0x33FF0000) else Color(0x33FFFFFF),
                    countTextColor = if (isLiked) Color.Red else Color.White,
                    onClick = {
                        isLiked = !isLiked

                        // Notify parent component about like state change
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
                                        // Also notify about the revert
                                        onLikeStateChanged?.invoke(id, isLiked)
                                    }
                                }
                            }
                        }
                    },
                    animated = true
                )

                // Comment button
                ActionButton(
                    icon = Icons.Default.Email,
                    contentDescription = "Comment",
                    count = comments.size,
                    tint = Color.White,
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    countBackgroundColor = Color.White.copy(alpha = 0.2f),
                    countTextColor = Color.White,
                    onClick = { showComments = !showComments },
                    animated = true
                )
                
                // Additional action buttons
                actionButtons()
            }
        } else {
            // Just show the toggle button and action buttons, but not likes/comments
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Before/After toggle
                if (showBeforeAfterToggle) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Close button
                if (onClose != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
        }
    }
}
