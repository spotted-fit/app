package fit.spotted.app.ui.components.post

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import fit.spotted.app.api.ApiClient
import fit.spotted.app.api.models.CommentData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.post.components.ImageBitmapContent
import fit.spotted.app.ui.components.post.components.ImageUrlContent
import fit.spotted.app.ui.theme.LocalWindowSize

/**
 * A reusable component that displays a post or photo detail view.
 * This component is used in both the feed and profile screens.
 * 
 * This version accepts image URLs.
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
    onLikeStateChanged: ((postId: Int, isLiked: Boolean) -> Unit)? = null,

    // Flag to indicate if the view is shown from camera
    isFromCamera: Boolean = false
) {
    // Delegate to the implementation with URL-based content providers
    PostDetailViewImpl(
        beforeContent = { ImageUrlContent(beforeImageUrl, windowSizeClass = LocalWindowSize.current.widthSizeClass) },
        afterContent = { ImageUrlContent(afterImageUrl, windowSizeClass = LocalWindowSize.current.widthSizeClass) },
        workoutDuration = workoutDuration,
        postedAt = postedAt,
        activityType = activityType,
        userName = userName,
        showBeforeAfterToggle = showBeforeAfterToggle,
        initialShowAfterImage = initialShowAfterImage,
        likes = likes,
        comments = comments,
        isLikedByMe = isLikedByMe,
        actionButtons = actionButtons,
        onActivityTypeClick = onActivityTypeClick,
        onAddComment = onAddComment,
        onDeletePost = onDeletePost,
        postId = postId,
        apiClient = apiClient,
        onLikeStateChanged = onLikeStateChanged,
        isFromCamera = isFromCamera
    )
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
    onActivityTypeClick: (() -> Unit)? = null,
    onAddComment: ((text: String) -> Unit)? = null,
    onDeletePost: (() -> Unit)? = null,
    postId: Int? = null,
    apiClient: ApiClient? = null,
    onLikeStateChanged: ((postId: Int, isLiked: Boolean) -> Unit)? = null,

    // Flag to indicate if the view is shown from camera
    isFromCamera: Boolean = false
) {
    // Delegate to the implementation with ImageBitmap-based content providers
    PostDetailViewImpl(
        beforeContent = { ImageBitmapContent(beforeWorkoutPhoto, windowSizeClass = LocalWindowSize.current.widthSizeClass) },
        afterContent = { ImageBitmapContent(afterWorkoutPhoto, windowSizeClass = LocalWindowSize.current.widthSizeClass) },
        workoutDuration = workoutDuration,
        postedAt = postedAt,
        activityType = activityType,
        userName = userName,
        showBeforeAfterToggle = showBeforeAfterToggle,
        initialShowAfterImage = initialShowAfterImage,
        likes = likes,
        comments = comments,
        isLikedByMe = isLikedByMe,
        actionButtons = actionButtons,
        onActivityTypeClick = onActivityTypeClick,
        onAddComment = onAddComment,
        onDeletePost = onDeletePost,
        postId = postId,
        apiClient = apiClient,
        onLikeStateChanged = onLikeStateChanged,
        isFromCamera = isFromCamera
    )
}
