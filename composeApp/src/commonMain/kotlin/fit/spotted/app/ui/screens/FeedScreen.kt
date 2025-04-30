package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.utils.DateTimeUtils
import kotlinx.coroutines.launch

/**
 * Screen that displays the feed of photos from friends in a full-screen TikTok-like style.
 */
class FeedScreen : Screen {
    @Composable
    override fun Content() {
        // State for posts, loading, and error handling
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val apiClient by remember { mutableStateOf(ApiProvider.getApiClient()) }
        var posts by remember { mutableStateOf(emptyList<PostDetailedData>()) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch posts when the screen is first displayed
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val request = apiClient.getFeed()
                    if (request.result == "ok" && request.response != null) {
                        posts = request.response
                    }
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = e.message ?: "An error occurred"
                    isLoading = false
                }
            }
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

        // Show error message
        errorMessage?.let {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        // Show empty state if no posts
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

        // TikTok-like vertical pager for posts
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
                    workoutDuration = formatTimestamp(post.createdAt),
                    activityType = ActivityType.valueOf(post.emoji ?: "RUNNING"),
                    userName = post.username,
                    likes = post.likes,
                    comments = post.comments,
                    postId = post.id,
                    onAddComment = { commentText ->
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
                                // Handle error
                            }
                        }
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
}
