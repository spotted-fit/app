package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        ActivityPhoto("1", "🏃", "https://example.com/running1_before.jpg", "https://example.com/running1_after.jpg", "25:30", "2023-05-15"),
        ActivityPhoto("2", "🚴", "https://example.com/cycling1_before.jpg", "https://example.com/cycling1_after.jpg", "45:00", "2023-05-12"),
        ActivityPhoto("3", "🏊", "https://example.com/swimming1_before.jpg", "https://example.com/swimming1_after.jpg", "35:20", "2023-05-10"),
        ActivityPhoto("4", "🧘", "https://example.com/yoga1_before.jpg", "https://example.com/yoga1_after.jpg", "30:00", "2023-05-08"),
        ActivityPhoto("5", "🏃", "https://example.com/running2_before.jpg", "https://example.com/running2_after.jpg", "28:45", "2023-05-05"),
        ActivityPhoto("6", "🚴", "https://example.com/cycling2_before.jpg", "https://example.com/cycling2_after.jpg", "01:15:30", "2023-05-03"),
        ActivityPhoto("7", "🏃", "https://example.com/running3_before.jpg", "https://example.com/running3_after.jpg", "22:15", "2023-05-01"),
        ActivityPhoto("8", "🚴", "https://example.com/cycling3_before.jpg", "https://example.com/cycling3_after.jpg", "50:10", "2023-04-28"),
        ActivityPhoto("9", "🏊", "https://example.com/swimming2_before.jpg", "https://example.com/swimming2_after.jpg", "40:05", "2023-04-25")
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

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Before/After images with carousel-like display
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                // We would implement a proper image carousel here
                                // For now, just show placeholders for both images
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (page == selectedPhotoIndex && currentPhotoIndex == 1) 
                                            "After: ${photo.afterImageUrl}" 
                                        else 
                                            "Before: ${photo.beforeImageUrl}",
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Duration: ${photo.workoutDuration}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                                            text = mockUser.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Row {
                                            Text(
                                                text = photo.activityType,
                                                fontSize = 14.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• ${photo.workoutDuration}",
                                                fontSize = 14.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
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
                                // Simple button for switching between before/after
                                Button(
                                    onClick = { 
                                        if (page == selectedPhotoIndex) {
                                            currentPhotoIndex = if (currentPhotoIndex == 0) 1
                                            else 0
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (page == selectedPhotoIndex && currentPhotoIndex == 0) "Show After" else "Show Before",
                                        color = Color.White
                                    )
                                }

                                // Close button
                                Button(
                                    onClick = { selectedPhoto = null }
                                ) {
                                    Text("Close")
                                }
                            }
                        }
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
                Text(
                    text = photo.activityType,
                    fontSize = 24.sp
                )
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
        val activityType: String,
        val beforeImageUrl: String,
        val afterImageUrl: String,
        val workoutDuration: String,
        val date: String
    )
}
