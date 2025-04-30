import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.api.models.ProfilePost
import fit.spotted.app.api.models.ProfileResponse
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.ui.components.toUiComment
import fit.spotted.app.ui.screens.Screen
import kotlinx.coroutines.launch
import org.kodein.emoji.Emoji
import org.kodein.emoji.people_body.person_activity.Running

class ProfileScreen(private val userId: Int? = null) : Screen {

    private val apiClient = ApiProvider.getApiClient()

    @Composable
    override fun Content() {
        // State for profile data
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var profileData by remember { mutableStateOf<ProfileResponse?>(null) }
        var selectedPost: PostDetailedData? by remember { mutableStateOf<PostDetailedData?>(null) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch profile data when the screen is first displayed
        LaunchedEffect(userId) {
            coroutineScope.launch {
                try {
                    // Use userId if provided, otherwise use current user's profile
                    val id = userId ?: 1 // Default to user ID 1 if not specified
                    val response = apiClient.getUserProfile(id)

                    if (response.result == "ok" && response.response != null) {
                        profileData = response.response
                        isLoading = false
                    } else {
                        errorMessage = response.message ?: "Failed to load profile"
                        isLoading = false
                    }
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

        profileData?.let { profile ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Profile header
                ProfileHeader(profile)

                // Photos grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(profile.posts) { post ->
                        PhotoGridItem(
                            post = post,
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val request = apiClient.getPost(post.id)
                                        if(request.result == "ok" && request.response != null) {
                                            selectedPost = request.response
                                        } else {
                                            println("Failed to get post ${post.id}")
                                        }
                                    } catch (e: Exception) {
                                        // Handle error
                                        errorMessage = e.message ?: "Failed to load post"
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Show full post detail only when a post is selected
            selectedPost?.let { post ->
                Dialog(
                    onDismissRequest = { selectedPost = null }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        PostDetailView(
                            beforeImageUrl = post.photo1,
                            afterImageUrl = post.photo2 ?: post.photo1,
                            workoutDuration = formatTimestamp(post.createdAt),
                            activityType = Emoji.Running, // Use actual activity type
                            userName = "User ${post.userId}",
                            likes = post.likes,
                            comments = post.comments.map { it.toUiComment { id -> "User $id" } },
                            onClose = { selectedPost = null }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ProfileHeader(profile: ProfileResponse) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (profile.avatar != null) {
                    AsyncImage(
                        model = profile.avatar,
                        contentDescription = "Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile info
            Column {
                Text(
                    text = "User ${profile.id}", // Use user ID instead of username
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${profile.posts.size} posts",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${profile.friendsCount} friends",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    private fun PhotoGridItem(post: ProfilePost, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = post.photo1,
                contentDescription = "Post Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }


    private fun formatTimestamp(timestamp: Long): String {
        // Simple timestamp formatting - in a real app, you'd use a proper date formatter
        val seconds = (timestamp / 1000) % 60
        val minutes = (timestamp / (1000 * 60)) % 60
        val hours = timestamp / (1000 * 60 * 60)

        val paddedSeconds = seconds.toString().padStart(2, '0')
        val paddedMinutes = minutes.toString().padStart(2, '0')
        val paddedHours = hours.toString().padStart(2, '0')

        return if (hours > 0) {
            "$paddedHours:$paddedMinutes:$paddedSeconds"
        } else {
            "$paddedMinutes:$paddedSeconds"
        }
    }
}