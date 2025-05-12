package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Screen that displays the profile of a friend.
 * It reuses the functionality of ProfileScreen but adds a back button
 * and removes the ability to delete posts.
 */
class FriendProfileScreen(
    private val username: String,
    private val onNavigateBack: () -> Unit
) : Screen {

    /**
     * A read-only version of the ProfileScreen that doesn't allow post deletion
     */
    private class ReadOnlyProfileScreen(username: String) : ProfileScreen(username, allowPostDeletion = false)

    @Composable
    override fun Content() {
        // Use the existing profile screen to display the friend's profile
        Box {
            // Display the friend's profile using ProfileScreen without deletion capability
            ReadOnlyProfileScreen(username = username).Content()
            
            // Add a back button on top, with adjusted position to avoid notification tray
            Box(
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp) // Increased top padding to avoid notification tray
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onNavigateBack() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 