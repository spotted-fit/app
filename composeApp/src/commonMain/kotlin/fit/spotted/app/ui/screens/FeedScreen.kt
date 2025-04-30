package fit.spotted.app.ui.screens

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
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.GetPostResponse
import fit.spotted.app.ui.components.Comment
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.ui.components.toUiComment
import kotlinx.coroutines.launch
import org.kodein.emoji.Emoji
import org.kodein.emoji.people_body.person_activity.Running
import org.kodein.emoji.people_body.person_sport.Skier
import org.kodein.emoji.people_body.person_sport.Swimming
import org.kodein.emoji.travel_places.transport_ground.Bicycle

/**
 * Screen that displays the feed of photos from friends in a full-screen TikTok-like style.
 */
class FeedScreen : Screen {
    // API client
    private val apiClient = ApiProvider.getApiClient()

    // Post IDs to fetch (in a real app, these would come from a feed endpoint)
    private val postIds = listOf(1, 2, 3, 4, 5)

    @Composable
    override fun Content() {
        // State for posts, loading, and error handling
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch posts when the screen is first displayed
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    // In a real app, we would fetch posts from the API
                    // For now, we'll use mock data
                    // TODO: Implement API call to fetch posts
                    // Example: val response = apiClient.getPosts()

                    // Simulate API delay
                    kotlinx.coroutines.delay(1000)

                    // Use mock data for now
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = e.message ?: "An error occurred"
                    isLoading = false
                }
            }
        }

        // Show loading indicator
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
    }

    @Composable
    private fun FullScreenPost(post: Post) {
        Box(modifier = Modifier.fillMaxSize()) {
            PostDetailView(
                beforeImageUrl = post.beforeImageUrl,
                afterImageUrl = post.afterImageUrl,
                workoutDuration = post.workoutDuration,
                activityType = post.activityType,
                userName = post.userName,
                showBeforeAfterToggle = true,
                likes = post.likes,
                comments = post.comments.map { fit.spotted.app.ui.components.Comment(it.userName, it.text) }
            )
        }
    }

    // Data classes for mock data
    data class Post(
        val id: String,
        val userName: String,
        val activityType: Emoji,
        val beforeImageUrl: String,
        val afterImageUrl: String,
        val workoutDuration: String,
        val likes: Int,
        val comments: List<Comment>
    )

    data class Comment(
        val userName: String,
        val text: String
    )
}
