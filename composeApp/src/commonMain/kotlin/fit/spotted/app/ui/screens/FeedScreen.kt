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
import fit.spotted.app.api.models.PostData
import fit.spotted.app.ui.components.PostDetailView
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

    // Mock data for the feed with emoji-only activity types (used as fallback)
    private val mockPosts = listOf(
        Post(
            id = "1",
            userName = "John Doe",
            activityType = Emoji.Running,
            beforeImageUrl = "https://example.com/running_before.jpg",
            afterImageUrl = "https://example.com/running_after.jpg",
            workoutDuration = "25:30",
            likes = 15,
            comments = listOf(
                Comment("Alice", "Great job!"),
                Comment("Bob", "Keep it up!")
            )
        ),
        Post(
            id = "2",
            userName = "Jane Smith",
            activityType = Emoji.Swimming,
            beforeImageUrl = "https://example.com/yoga_before.jpg",
            afterImageUrl = "https://example.com/yoga_after.jpg",
            workoutDuration = "45:00",
            likes = 23,
            comments = listOf(
                Comment("Charlie", "Impressive pose!"),
                Comment("David", "Looking good!")
            )
        ),
        Post(
            id = "3",
            userName = "Mike Johnson",
            activityType = Emoji.Bicycle,
            beforeImageUrl = "https://example.com/cycling_before.jpg",
            afterImageUrl = "https://example.com/cycling_after.jpg",
            workoutDuration = "01:15:45",
            likes = 8,
            comments = listOf(
                Comment("Eve", "Nice ride!"),
                Comment("Frank", "How many miles?")
            )
        ),
        Post(
            id = "4",
            userName = "Sarah Williams",
            activityType = Emoji.Swimming,
            beforeImageUrl = "https://example.com/swimming_before.jpg",
            afterImageUrl = "https://example.com/swimming_after.jpg",
            workoutDuration = "35:20",
            likes = 19,
            comments = listOf(
                Comment("George", "Nice form!"),
                Comment("Hannah", "Water looks great!")
            )
        ),
        Post(
            id = "5",
            userName = "David Brown",
            activityType = Emoji.Skier,
            beforeImageUrl = "https://example.com/hiking_before.jpg",
            afterImageUrl = "https://example.com/hiking_after.jpg",
            workoutDuration = "02:30:15",
            likes = 31,
            comments = listOf(
                Comment("Irene", "Beautiful view!"),
                Comment("Jack", "Where is this trail?")
            )
        )
    )

    @Composable
    override fun Content() {
        // State for posts, loading, and error handling
        var posts by remember { mutableStateOf<List<PostData>?>(null) }
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

        // Use VerticalPager for TikTok-like swiping
        val pagerState = rememberPagerState(pageCount = { mockPosts.size })

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            FullScreenPost(mockPosts[page])
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
