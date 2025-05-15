package fit.spotted.app.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiClient
import fit.spotted.app.api.models.CommentData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.post.components.*
import fit.spotted.app.ui.components.post.state.PostDetailState
import fit.spotted.app.ui.components.post.util.rememberAdaptiveSizes
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import fit.spotted.app.ui.theme.LocalWindowSize
import org.kodein.emoji.compose.WithPlatformEmoji

/**
 * Implementation of the PostDetailView that is shared between both overloads.
 */
@Composable
internal fun PostDetailViewImpl(
    beforeContent: @Composable () -> Unit,
    afterContent: @Composable () -> Unit,

    workoutDuration: String,
    postedAt: String,
    activityType: ActivityType,
    userName: String,

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
    
    onProfileClick: (() -> Unit)? = null,

    isFromCamera: Boolean = false
) {
    val state = remember(initialShowAfterImage, isLikedByMe) {
        PostDetailState(initialShowAfterImage, isLikedByMe)
    }

    val adaptiveSpacing = LocalAdaptiveSpacing.current
    val windowSize = LocalWindowSize.current

    val adaptiveSizes = rememberAdaptiveSizes(windowSize.widthSizeClass)

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PostImageDisplay(
            state = state,
            beforeContent = beforeContent,
            afterContent = afterContent,
            adaptiveSizes = adaptiveSizes,
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = adaptiveSpacing.huge * 2)
        ) {
            PostActionButtons(
                state = state,
                adaptiveSizes = adaptiveSizes,
                showBeforeAfterToggle = showBeforeAfterToggle,
                likes = likes,
                commentsCount = comments.size,
                isLikedByMe = isLikedByMe,
                postId = postId,
                apiClient = apiClient,
                onLikeStateChanged = onLikeStateChanged,
                actionButtons = actionButtons,
                showLikesAndComments = !isFromCamera
            )
        }

        if (!isFromCamera) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = adaptiveSpacing.huge * 2)
            ) {
                onDeletePost?.let {
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(adaptiveSizes.buttonSize)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { state.showDeleteConfirmation() },
                        contentAlignment = Alignment.Center
                    ) {
                        WithPlatformEmoji(
                            text = "🗑️",
                        ) { emojiString, inlineContent ->
                            Text(
                                text = emojiString,
                                inlineContent = inlineContent,
                                fontSize = adaptiveSizes.titleTextSize,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
        ) {
            PostUserInfo(
                userName = userName,
                postedAt = postedAt,
                activityType = activityType,
                adaptiveSizes = adaptiveSizes,
                windowSizeClass = windowSize.widthSizeClass,
                workoutDuration = workoutDuration,
                state = state,
                onActivityTypeClick = onActivityTypeClick,
                onProfileClick = onProfileClick
            )
        }

        if (state.showComments) {
            CommentsOverlay(
                comments = comments,
                onAddComment = onAddComment,
                onClose = { state.toggleComments() }
            )
        }

        if (state.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onConfirm = { onDeletePost?.invoke() },
                onDismiss = { state.hideDeleteConfirmation() }
            )
        }
    }
}
