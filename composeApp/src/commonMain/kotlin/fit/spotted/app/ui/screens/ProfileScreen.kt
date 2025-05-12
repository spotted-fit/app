package fit.spotted.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.api.models.ProfilePost
import fit.spotted.app.api.models.ProfileResponse
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.ui.components.ProfileSkeletonLoading
import fit.spotted.app.ui.components.PullToRefreshLayout
import fit.spotted.app.ui.theme.LocalSpacing
import fit.spotted.app.ui.theme.ThemePreferences
import fit.spotted.app.utils.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen that displays a user's profile, including their avatar, stats, and posts.
 * 
 * @property username Optional username of the user to display. If null, displays the current user's profile.
 * @property allowPostDeletion Whether to allow post deletion. Defaults to true for own profile, 
 *                            can be overridden by subclasses for friend profiles.
 */
open class ProfileScreen(
    private val username: String? = null,
    protected open val allowPostDeletion: Boolean = true
) : Screen {

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
        // Get standardized spacing values
        val spacing = LocalSpacing.current
        
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

        // Function to load profile data
        fun loadProfileData() {
            isLoading = true
            errorMessage = null
            
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

        // Initial data load
        LaunchedEffect(username) {
            loadProfileData()
        }

        // Function to load detailed post data for TikTok view
        fun loadDetailedPosts(selectedPostId: Int? = null) {
            if (profileData?.posts.isNullOrEmpty()) return

            isLoadingDetailedPosts = true
            coroutineScope.launch {
                val posts = mutableListOf<PostDetailedData>()

                // Reverse the posts so newest appear first
                val reversedPosts = profileData?.posts?.reversed() ?: emptyList()

                reversedPosts.forEach { post ->
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
                ProfileSkeletonLoading()
            }
            return
        }

        // Show error message with retry button
        errorMessage?.let {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(spacing.medium)
                        .semantics {
                            contentDescription = "Error loading profile: $it"
                        }
                ) {
                    Text(
                        text = "Error",
                        color = MaterialTheme.colors.error,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(horizontal = spacing.large)
                    )
                    
                    Spacer(modifier = Modifier.height(spacing.medium))
                    
                    // Retry button
                    val haptic = LocalHapticFeedback.current
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(spacing.extraSmall))
                            .clip(RoundedCornerShape(spacing.extraSmall))
                            .background(MaterialTheme.colors.primary)
                            .clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                loadProfileData() 
                            }
                            .padding(horizontal = spacing.medium, vertical = spacing.small)
                            .semantics {
                                contentDescription = "Retry loading profile"
                            }
                    ) {
                        Text(
                            text = "Retry",
                            color = MaterialTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            return
        }

        // Main content with view mode transitions
        AnimatedVisibility(
            visible = profileData != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            profileData?.let { profile ->
                when (viewMode) {
                    ViewMode.GRID -> {
                        // Add pull-to-refresh functionality
                        PullToRefreshLayout(
                            isRefreshing = isLoading,
                            onRefresh = { loadProfileData() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Profile header
                                ProfileHeader(profile)

                                // Photos grid
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(profile.posts.reversed()) { post ->
                                        // Create a staggered animation for each grid item
                                        val staggerDelay = remember { (post.id % 9) * 50L }
                                        var isVisible by remember { mutableStateOf(false) }
                                        
                                        LaunchedEffect(post.id) {
                                            delay(staggerDelay)
                                            isVisible = true
                                        }
                                        
                                        AnimatedVisibility(
                                            visible = isVisible,
                                            enter = fadeIn(tween(500)) + 
                                                    expandIn(
                                                        animationSpec = tween(500),
                                                        expandFrom = Alignment.Center
                                                    )
                                        ) {
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
                        }
                    }

                    ViewMode.TIKTOK -> {
                        // Get standardized spacing values
                        val spacing = LocalSpacing.current
                        
                        // Haptic feedback for interactions
                        val haptic = LocalHapticFeedback.current
                        
                        // TikTok-like full-screen post view
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Back button to return to grid view
                            Box(
                                modifier = Modifier
                                    .padding(top = spacing.statusBarPadding, start = spacing.medium)
                                    .size(spacing.huge) // Increased size for better tap target
                                    .shadow(4.dp, CircleShape) // Add shadow for better visibility
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .clickable { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewMode = ViewMode.GRID 
                                    }
                                    .align(Alignment.TopStart)
                                    // Add semantic description for accessibility
                                    .semantics {
                                        contentDescription = "Back to photo grid"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = null, // null because we set it on the parent
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp) // Slightly larger icon
                                )
                            }

                            if (isLoadingDetailedPosts) {
                                // Show loading indicator while fetching detailed posts
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colors.primary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            } else if (detailedPosts.isEmpty()) {
                                // Show message if no posts are available
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No posts to display",
                                        color = MaterialTheme.colors.onBackground,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
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
                                            onDeletePost = if (allowPostDeletion) {
                                                {
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
                                                }
                                            } else null, // Set to null when not allowed to delete
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
    }

    /**
     * Displays the user's profile header with avatar and stats.
     *
     * @param profile The user profile data to display
     */
    @Composable
    private fun ProfileHeader(profile: ProfileResponse) {
        // Get standardized spacing values
        val spacing = LocalSpacing.current
        val isSystemDark = isSystemInDarkTheme()
        val haptic = LocalHapticFeedback.current
        
        // Add extra padding at the top if this is a friend's profile (not the current user's profile)
        val topPadding = if (username != null) spacing.friendProfileTopPadding else spacing.medium
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.medium, 
                    end = spacing.medium, 
                    top = topPadding, 
                    bottom = spacing.medium
                )
                // Add semantic description for accessibility
                .semantics {
                    contentDescription = "Profile information for ${profile.username}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar with improved visuals
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface)
                        // Add semantic description for accessibility
                        .semantics {
                            contentDescription = "Profile picture for ${profile.username}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.avatar != null) {
                        SubcomposeAsyncImage(
                            model = profile.avatar,
                            contentDescription = "Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(30.dp),
                                    strokeWidth = 2.dp
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier.size(45.dp),
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Avatar",
                            modifier = Modifier.size(45.dp),
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Profile info with improved layout
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = profile.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colors.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Profile stats with nicer display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Posts count
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${profile.posts.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Posts",
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        // Friends count
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${profile.friendsCount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Friends",
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Theme toggle button (only on own profile)
                if (username == null) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(spacing.extraSmall))
                            .clip(RoundedCornerShape(spacing.extraSmall))
                            .background(MaterialTheme.colors.primary)
                            .clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ThemePreferences.toggleTheme(isSystemDark)
                            }
                            .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                            .semantics {
                                contentDescription = "Toggle dark/light theme"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSystemDark) "Light Mode" else "Dark Mode",
                            color = MaterialTheme.colors.onPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider with better visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            )
        }
    }

    /**
     * Displays a single post thumbnail in the profile grid.
     * Implements lazy loading and proper caching for better performance.
     *
     * @param post The post data to display
     * @param onClick Callback for when the thumbnail is clicked
     */
    @Composable
    private fun PhotoGridItem(post: ProfilePost, onClick: () -> Unit) {
        val spacing = LocalSpacing.current
        val haptic = LocalHapticFeedback.current
        
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .shadow(1.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
                .semantics {
                    contentDescription = "Photo from ${formatTimestamp(post.createdAt)}"
                }
        ) {
            // Use SubcomposeAsyncImage for better loading/error states
            SubcomposeAsyncImage(
                model = post.photo1,
                contentDescription = null, // Already set on parent
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.surface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colors.primary
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.error.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.error
                        )
                    }
                }
            )
            
            // Show a date/time indicator in the corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spacing.extraSmall)
                    .clip(RoundedCornerShape(spacing.extraSmall))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatTimestamp(post.createdAt),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
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
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }
    }
}
