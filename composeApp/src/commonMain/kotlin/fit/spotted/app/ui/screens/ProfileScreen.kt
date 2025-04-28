package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kodein.emoji.Emoji
import org.kodein.emoji.activities.sport.BoxingGlove
import org.kodein.emoji.compose.WithPlatformEmoji
import org.kodein.emoji.people_body.person_activity.Running
import org.kodein.emoji.people_body.person_sport.Skier
import org.kodein.emoji.people_body.person_sport.Swimming
import org.kodein.emoji.travel_places.transport_ground.Bicycle

/**
 * Screen that displays the user's profile information and activity photos in a grid layout.
 * Simplified to match Instagram style.
 */
class ProfileScreen : Screen {

    // Mock data for user profile
    private val mockUser = User(
        id = "1",
        name = "John Doe",
        bio = "Fitness enthusiast | Runner | Cyclist"
    )

    // Mock data for activity photos with only emojis
    private val mockActivityPhotos = listOf(
        ActivityPhoto(
            id = "1", 
            activityType = Emoji.Running,
            beforeImageUrl = "https://example.com/running1_before.jpg", 
            afterImageUrl = "https://example.com/running1_after.jpg", 
            workoutDuration = "25:30", 
            date = "2023-05-15",
            likes = 15,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Alice", "Great job!"),
                fit.spotted.app.ui.components.Comment("Bob", "Keep it up!")
            )
        ),
        ActivityPhoto(
            id = "3", 
            activityType = Emoji.Skier,
            beforeImageUrl = "https://example.com/swimming1_before.jpg", 
            afterImageUrl = "https://example.com/swimming1_after.jpg", 
            workoutDuration = "35:20", 
            date = "2023-05-10",
            likes = 8,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Eve", "Nice form!"),
                fit.spotted.app.ui.components.Comment("Frank", "Water looks great!")
            )
        ),
        ActivityPhoto(
            id = "4", 
            activityType = Emoji.BoxingGlove,
            beforeImageUrl = "https://example.com/yoga1_before.jpg", 
            afterImageUrl = "https://example.com/yoga1_after.jpg", 
            workoutDuration = "30:00", 
            date = "2023-05-08",
            likes = 19,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("George", "Peaceful pose!"),
                fit.spotted.app.ui.components.Comment("Hannah", "Looks relaxing!")
            )
        ),
        ActivityPhoto(
            id = "6", 
            activityType = Emoji.Bicycle,
            beforeImageUrl = "https://example.com/cycling2_before.jpg", 
            afterImageUrl = "https://example.com/cycling2_after.jpg", 
            workoutDuration = "01:15:30", 
            date = "2023-05-03",
            likes = 12,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Kate", "Long ride!"),
                fit.spotted.app.ui.components.Comment("Liam", "Beautiful scenery!")
            )
        ),
        ActivityPhoto(
            id = "7", 
            activityType = Emoji.Running,
            beforeImageUrl = "https://example.com/running3_before.jpg", 
            afterImageUrl = "https://example.com/running3_after.jpg", 
            workoutDuration = "22:15", 
            date = "2023-05-01",
            likes = 7,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Mia", "Quick run!"),
                fit.spotted.app.ui.components.Comment("Noah", "Good pace!")
            )
        ),
        ActivityPhoto(
            id = "8", 
            activityType = Emoji.Bicycle,
            beforeImageUrl = "https://example.com/cycling3_before.jpg", 
            afterImageUrl = "https://example.com/cycling3_after.jpg", 
            workoutDuration = "50:10", 
            date = "2023-04-28",
            likes = 16,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Olivia", "Great ride!"),
                fit.spotted.app.ui.components.Comment("Peter", "How was the terrain?")
            )
        ),
        ActivityPhoto(
            id = "9", 
            activityType = Emoji.Swimming,
            beforeImageUrl = "https://example.com/swimming2_before.jpg", 
            afterImageUrl = "https://example.com/swimming2_after.jpg", 
            workoutDuration = "40:05", 
            date = "2023-04-25",
            likes = 21,
            comments = listOf(
                fit.spotted.app.ui.components.Comment("Quinn", "Nice swim!"),
                fit.spotted.app.ui.components.Comment("Rachel", "Water looks refreshing!")
            )
        )
    )

    @Composable
    override fun Content() {
        // State to track the selected photo for detail view
        var selectedPhoto by remember { mutableStateOf<ActivityPhoto?>(null) }
        var currentPhotoIndex by remember { mutableStateOf(0) } // 0 for before, 1 for after
        var selectedPhotoIndex by remember { mutableStateOf(0) } // Index of the selected photo in the list

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Profile header
                ProfileHeader(mockUser)

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Photos grid
                Text(
                    text = "Photos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )

                PhotosGrid(
                    photos = mockActivityPhotos,
                    onPhotoClick = { photo ->
                        selectedPhoto = photo
                        currentPhotoIndex = 0 // Start with "before" photo
                        selectedPhotoIndex = mockActivityPhotos.indexOf(photo)
                    }
                )
            }

            // Photo detail overlay - Feed-like browsing experience
            if (selectedPhoto != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Use VerticalPager for TikTok-like swiping
                    val pagerState = rememberPagerState(
                        initialPage = selectedPhotoIndex,
                        pageCount = { mockActivityPhotos.size }
                    )

                    // Update selectedPhoto when page changes
                    LaunchedEffect(pagerState.currentPage) {
                        selectedPhotoIndex = pagerState.currentPage
                        selectedPhoto = mockActivityPhotos[pagerState.currentPage]
                        currentPhotoIndex = 0 // Reset to "before" photo when changing pages
                    }

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val photo = mockActivityPhotos[page]

                        // Use the enhanced PostDetailView component
                        fit.spotted.app.ui.components.PostDetailView(
                            beforeImageUrl = photo.beforeImageUrl,
                            afterImageUrl = photo.afterImageUrl,
                            workoutDuration = photo.workoutDuration,
                            activityType = photo.activityType,
                            userName = mockUser.name,
                            showBeforeAfterToggle = true,
                            initialShowAfterImage = page == selectedPhotoIndex && currentPhotoIndex == 1,
                            likes = photo.likes,
                            comments = photo.comments
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ProfileHeader(user: User) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Text(
                        text = user.bio,
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Photo count
            Text(
                text = "${mockActivityPhotos.size} photos",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    @Composable
    private fun PhotosGrid(
        photos: List<ActivityPhoto>,
        onPhotoClick: (ActivityPhoto) -> Unit
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(photos) { photo ->
                PhotoGridItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) }
                )
            }
        }
    }

    @Composable
    private fun PhotoGridItem(
        photo: ActivityPhoto,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, we would load the actual image here
            // For now, we'll show a placeholder with activity type and duration
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WithPlatformEmoji(
                    photo.activityType.toString(),
                ) { text, inlineContent ->
                    Text(
                        text = text,
                        inlineContent = inlineContent,
                        fontSize = 24.sp
                    )
                }
                Text(
                    text = photo.workoutDuration,
                    fontSize = 14.sp
                )

                // Visual indication of before/after photos
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colors.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colors.secondary)
                    )
                }
            }
        }
    }

    // Data classes for mock data
    data class User(
        val id: String,
        val name: String,
        val bio: String
    )

    data class ActivityPhoto(
        val id: String,
        val activityType: Emoji,
        val beforeImageUrl: String,
        val afterImageUrl: String,
        val workoutDuration: String,
        val date: String,
        val likes: Int = 0,
        val comments: List<fit.spotted.app.ui.components.Comment> = emptyList()
    )
}
