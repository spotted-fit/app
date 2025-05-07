package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil3.compose.AsyncImage
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.api.models.ProfilePost
import fit.spotted.app.api.models.ProfileResponse
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.utils.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen that displays a user's profile, including their avatar, stats, and posts.
 * 
 * @property username Optional username of the user to display. If null, displays the current user's profile.
 */
class ProfileScreen(private val username: String? = null) : Screen {

    private val apiClient = ApiProvider.getApiClient()

    // Enum to track the current view mode
    private enum class ViewMode {
        GRID,
        TIKTOK
    }

    private suspend fun getLoggedInUser(): String {
        val response = apiClient.getMe()
        if(response.result == "ok" && response.response != null) {
            return response.response.username
        } else {
            throw IllegalStateException("Failed to get logged in user")
        }
    }

    @Composable
    override fun Content() {
        // State for profile data
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var profileData by remember { mutableStateOf<ProfileResponse?>(null) }

        // State for view mode
        var viewMode by remember { mutableStateOf(ViewMode.GRID) }

        // State for detailed post data (for TikTok view)
        var detailedPosts by remember { mutableStateOf<List<PostDetailedData>>(emptyList()) }
        var isLoadingDetailedPosts by remember { mutableStateOf(false) }

        // State to track the selected post index in TikTok view
        var selectedPostIndex by remember { mutableStateOf(0) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch profile data when the screen is first displayed
        LaunchedEffect(username) {
            coroutineScope.launch {
                try {
                    val response = apiClient.getUserProfile(
                        username ?: getLoggedInUser()
                    )

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

        // Function to load detailed post data for TikTok view
        fun loadDetailedPosts(selectedPostId: Int? = null) {
            if (profileData?.posts.isNullOrEmpty()) return

            isLoadingDetailedPosts = true
            coroutineScope.launch {
                val posts = mutableListOf<PostDetailedData>()

                profileData?.posts?.forEach { post ->
                    try {
                        val request = apiClient.getPost(post.id)
                        if (request.result == "ok" && request.response != null) {
                            posts.add(request.response)

                            // If this is the selected post, update the index
                            if (selectedPostId != null && post.id == selectedPostId) {
                                selectedPostIndex = posts.size - 1
                            }
                        }
                    } catch (_: Exception) {
                        // Skip failed posts
                    }
                }

                detailedPosts = posts
                isLoadingDetailedPosts = false

                // Switch to TikTok view after loading posts
                if (posts.isNotEmpty()) {
                    viewMode = ViewMode.TIKTOK
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
            when (viewMode) {
                ViewMode.GRID -> {
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
                                        // Load detailed posts and switch to TikTok view
                                        loadDetailedPosts(post.id)
                                    }
                                )
                            }
                        }
                    }

                }

                ViewMode.TIKTOK -> {
                    // TikTok-like full-screen post view
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Back button to return to grid view
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { viewMode = ViewMode.GRID }
                                .align(Alignment.TopStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back to Grid",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isLoadingDetailedPosts) {
                            // Show loading indicator while fetching detailed posts
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (detailedPosts.isEmpty()) {
                            // Show message if no posts are available
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No posts to display",
                                    color = MaterialTheme.colors.onBackground
                                )
                            }
                        } else {
                            // Vertical pager for TikTok-like scrolling
                            val pagerState = rememberPagerState(
                                initialPage = selectedPostIndex,
                                pageCount = { detailedPosts.size }
                            )

                            VerticalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val post = detailedPosts[page]
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
                                        onClose = { viewMode = ViewMode.GRID }, // Add close button to exit TikTok view
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
                                                            detailedPosts = detailedPosts.map { 
                                                                if (it.id == post.id) updatedPost.response else it 
                                                            }
                                                        }
                                                    }
                                                } catch (_: Exception) {
                                                    // Handle error
                                                }
                                            }
                                        },
                                        onDeletePost = {
                                            coroutineScope.launch {
                                                try {
                                                    val response = apiClient.deletePost(post.id)
                                                    if (response.result == "ok") {
                                                        // Remove the post from the list
                                                        detailedPosts = detailedPosts.filter { it.id != post.id }

                                                        // If there are no more posts, go back to grid view
                                                        if (detailedPosts.isEmpty()) {
                                                            viewMode = ViewMode.GRID
                                                        }

                                                        // Also update the profile data to remove the post
                                                        profileData = profileData?.copy(
                                                            posts = profileData?.posts?.filter { it.id != post.id } ?: emptyList()
                                                        )
                                                    }
                                                } catch (_: Exception) {
                                                    // Handle error
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
                                                        detailedPosts = detailedPosts.map { 
                                                            if (it.id == postId) updatedPost.response else it 
                                                        }
                                                    }
                                                } catch (_: Exception) {
                                                    // Handle error
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays the user's profile header with avatar and stats.
     *
     * @param profile The user profile data to display
     */
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
                    text = profile.username,
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

    /**
     * Displays a single post thumbnail in the profile grid.
     *
     * @param post The post data to display
     * @param onClick Callback for when the thumbnail is clicked
     */
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
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }
    }

}
