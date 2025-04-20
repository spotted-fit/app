package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            imageUrl = "https://example.com/running.jpg",
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
            imageUrl = "https://example.com/yoga.jpg",
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
            imageUrl = "https://example.com/cycling.jpg",
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
            imageUrl = "https://example.com/swimming.jpg",
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
            imageUrl = "https://example.com/hiking.jpg",
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
        // State for like button
        var isLiked by remember { mutableStateOf(false) }
        var showComments by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image placeholder (full screen)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image: ${post.imageUrl}",
                    color = Color.White
                )
            }

            // Overlay with user info at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // User info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = post.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = post.activityType,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Action buttons on the right side
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like button
                IconButton(
                    onClick = { isLiked = !isLiked }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "${if (isLiked) post.likes + 1 else post.likes}",
                    color = Color.White,
                    fontSize = 14.sp
                )

                // Comment button
                IconButton(
                    onClick = { showComments = !showComments }
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "${post.comments.size}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Comments overlay (shown when comment button is clicked)
            if (showComments) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Comments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        post.comments.forEach { comment ->
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${comment.userName}: ",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = comment.text,
                                    color = Color.White
                                )
                            }
                        }

                        // Close button
                        Button(
                            onClick = { showComments = false },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 16.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Data classes for mock data
    data class Post(
        val id: String,
        val userName: String,
        val activityType: String,
        val imageUrl: String,
        val likes: Int,
        val comments: List<Comment>
    )

    data class Comment(
        val userName: String,
        val text: String
    )
}
