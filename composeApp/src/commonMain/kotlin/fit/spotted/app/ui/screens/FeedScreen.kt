package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fit.spotted.app.ui.components.PostDetailView

/**
 * Screen that displays the feed of photos from friends in a full-screen TikTok-like style.
 */
class FeedScreen : Screen {

    // Mock data for the feed with emoji-only activity types
    private val mockPosts = listOf(
        Post(
            id = "1",
            userName = "John Doe",
            activityType = "🏃",
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
            activityType = "🧘",
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
            activityType = "🚴",
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
            activityType = "🏊",
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
            activityType = "🥾",
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
            // Use the enhanced PostDetailView component
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
        val activityType: String,
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
