package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.post.PostDetailView
import fit.spotted.app.utils.DateTimeUtils
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen that displays the feed of photos from friends in a full-screen TikTok-like style.
 */
class FeedScreen : Screen {
    // Callback for navigating to a friend's profile
    var onNavigateToFriendProfile: ((String) -> Unit)? = null
    
    @Composable
    override fun Content() {
        var isLoading by remember { mutableStateOf(true) }
        var refreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val apiClient by remember { mutableStateOf(ApiProvider.getApiClient()) }
        var posts by remember { mutableStateOf(emptyList<PostDetailedData>()) }

        val coroutineScope = rememberCoroutineScope()

        // Function to load feed data
        fun loadFeed() {
            coroutineScope.launch {
                try {
                    val request = apiClient.getFeed()
                    if (request.result == "ok" && request.response != null) {
                        posts = request.response
                        errorMessage = null
                    } else {
                        errorMessage = request.message ?: "Failed to load feed"
                    }
                } catch (e: Exception) {
                    errorMessage = when {
                        e.message?.contains("401") == true -> "Session expired. Please log in again."
                        else -> e.message ?: "An error occurred"
                    }
                } finally {
                    isLoading = false
                    refreshing = false
                }
            }
        }

        // Initial load
        LaunchedEffect(Unit) {
            loadFeed()
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // Show error with retry button
        errorMessage?.let {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        isLoading = true
                        loadFeed() 
                    }) {
                        Text("Try Again")
                    }
                }
            }
            return
        }

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No posts to display",
                    color = MaterialTheme.colors.onBackground
                )
            }
            return
        }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { posts.size }
        )

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val post = posts[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                PostDetailView(
                    beforeImageUrl = post.photo1,
                    afterImageUrl = post.photo2 ?: post.photo1,
                    workoutDuration = formatDuration(post.timer),
                    postedAt = formatTimestamp(post.createdAt),
                    activityType = ActivityType.valueOf(post.emoji ?: "RUNNING"),
                    userName = post.username,
                    likes = post.likes,
                    comments = post.comments,
                    isLikedByMe = post.isLikedByMe,
                    postId = post.id,
                    apiClient = apiClient,
                    onAddComment = { commentText: String ->
                        coroutineScope.launch {
                            try {
                                val response = apiClient.addComment(post.id, commentText)
                                if (response.result == "ok") {
                                    // Refresh the post to show the new comment
                                    val updatedPost = apiClient.getPost(post.id)
                                    if (updatedPost.result == "ok" && updatedPost.response != null) {
                                        // Update the post in the list
                                        posts = posts.map {
                                            if (it.id == post.id) updatedPost.response else it
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Handle specific errors
                                when {
                                    e.message?.contains("401") == true -> {
                                        // Auth error is already handled globally
                                    }
                                    else -> {
                                        // Could show a toast or some other UI feedback here
                                    }
                                }
                            }
                        }
                    },
                    onLikeStateChanged = { postId: Int, isLiked: Boolean ->
                        // Refresh the post data when like state changes
                        coroutineScope.launch {
                            try {
                                // Give the API a moment to process the like/unlike
                                delay(300)
                                val updatedPost = apiClient.getPost(postId)
                                if (updatedPost.result == "ok" && updatedPost.response != null) {
                                    // Update the post in the list
                                    posts = posts.map {
                                        if (it.id == postId) updatedPost.response else it
                                    }
                                }
                            } catch (e: Exception) {
                                // Handle specific errors
                                when {
                                    e.message?.contains("401") == true -> {
                                        // Auth error is already handled globally
                                    }
                                    else -> {
                                        // Could show a toast or some other UI feedback here
                                    }
                                }
                            }
                        }
                    },
                    onProfileClick = {
                        // Navigate to the friend's profile
                        onNavigateToFriendProfile?.invoke(post.username)
                    }
                )
            }
        }
    }

    /**
     * Formats a timestamp into a human-readable string in Instagram style.
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted string in Instagram style (e.g., "just now", "5m ago", "2h ago", "3d ago", "2w ago", or "Jan 15")
     */
    private fun formatTimestamp(timestamp: Long): String {
        return DateTimeUtils.formatInstagramStyle(timestamp)
    }

    /**
     * Formats a duration in seconds into a human-readable string.
     *
     * @param durationSeconds The duration in seconds
     * @return Formatted string (e.g., "00:05:30" for 5 minutes and 30 seconds)
     */
    private fun formatDuration(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return if (hours > 0) {
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${
                seconds.toString().padStart(2, '0')
            }"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }
    }
}
