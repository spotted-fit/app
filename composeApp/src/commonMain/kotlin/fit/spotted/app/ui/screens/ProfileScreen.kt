package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        ActivityPhoto("1", "🏃", "https://example.com/running1.jpg", "2023-05-15"),
        ActivityPhoto("2", "🚴", "https://example.com/cycling1.jpg", "2023-05-12"),
        ActivityPhoto("3", "🏊", "https://example.com/swimming1.jpg", "2023-05-10"),
        ActivityPhoto("4", "🧘", "https://example.com/yoga1.jpg", "2023-05-08"),
        ActivityPhoto("5", "🏃", "https://example.com/running2.jpg", "2023-05-05"),
        ActivityPhoto("6", "🚴", "https://example.com/cycling2.jpg", "2023-05-03"),
        ActivityPhoto("7", "🏃", "https://example.com/running3.jpg", "2023-05-01"),
        ActivityPhoto("8", "🚴", "https://example.com/cycling3.jpg", "2023-04-28"),
        ActivityPhoto("9", "🏊", "https://example.com/swimming2.jpg", "2023-04-25")
    )

    @Composable
    override fun Content() {
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

            PhotosGrid(mockActivityPhotos)
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
    private fun PhotosGrid(photos: List<ActivityPhoto>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(photos) { photo ->
                PhotoGridItem(photo)
            }
        }
    }

    @Composable
    private fun PhotoGridItem(photo: ActivityPhoto) {
        Box(
            modifier = Modifier
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, we would load the actual image here
            Text(photo.activityType)
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
        val imageUrl: String,
        val date: String
    )
}
